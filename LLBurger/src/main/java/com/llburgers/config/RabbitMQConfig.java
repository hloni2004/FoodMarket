package com.llburgers.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ topology for LLBurger.
 *
 * <p><b>Exchange:</b> {@code llburger.exchange} (topic) – single exchange for all app messages.</p>
 *
 * <p><b>Queues & routing keys:</b></p>
 * <ul>
 *     <li>{@code llburger.orders.placed}         ← {@code order.placed}</li>
 *     <li>{@code llburger.orders.status-changed}  ← {@code order.status.changed}</li>
 *     <li>{@code llburger.orders.cancelled}       ← {@code order.cancelled}</li>
 *     <li>{@code llburger.business.opened}        ← {@code business.opened}</li>
 *     <li>{@code llburger.business.closed}        ← {@code business.closed}</li>
 * </ul>
 *
 * <p>Every queue has a <b>Dead-Letter Exchange</b> (DLX) so that messages that are
 * rejected or expire after {@value #MESSAGE_TTL_MS} ms are routed to
 * {@code llburger.dead-letter} for inspection.</p>
 *
 * <p>Messages are serialised / deserialised as <b>JSON</b> via Jackson.</p>
 */
@Configuration
public class RabbitMQConfig {

    // ─── Exchange ─────────────────────────────────────────────────────────────
    public static final String EXCHANGE      = "llburger.exchange";

    // ─── Dead-Letter Exchange & Queue ─────────────────────────────────────────
    public static final String DLX           = "llburger.dlx";
    public static final String DLQ           = "llburger.dead-letter";

    // ─── Queue Names ──────────────────────────────────────────────────────────
    public static final String ORDER_PLACED_QUEUE          = "llburger.orders.placed";
    public static final String ORDER_STATUS_CHANGED_QUEUE  = "llburger.orders.status-changed";
    public static final String ORDER_CANCELLED_QUEUE       = "llburger.orders.cancelled";
    public static final String BUSINESS_OPENED_QUEUE       = "llburger.business.opened";
    public static final String BUSINESS_CLOSED_QUEUE       = "llburger.business.closed";

    // ─── Routing Keys ─────────────────────────────────────────────────────────
    public static final String ORDER_PLACED_KEY         = "order.placed";
    public static final String ORDER_STATUS_CHANGED_KEY = "order.status.changed";
    public static final String ORDER_CANCELLED_KEY      = "order.cancelled";
    public static final String BUSINESS_OPENED_KEY      = "business.opened";
    public static final String BUSINESS_CLOSED_KEY      = "business.closed";

    // ─── TTL: 24 hours ────────────────────────────────────────────────────────
    private static final int MESSAGE_TTL_MS = 86_400_000;

    // ─── Exchange Declarations ────────────────────────────────────────────────

    @Bean
    public TopicExchange llburgerExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ─── Dead-Letter Queue ────────────────────────────────────────────────────

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    // ─── Queue builder helper (applies DLX + TTL to every queue) ─────────────

    private QueueBuilder withDlx(String queueName) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX);
        args.put("x-dead-letter-routing-key", DLQ);
        args.put("x-message-ttl", MESSAGE_TTL_MS);
        return QueueBuilder.durable(queueName).withArguments(args);
    }

    // ─── Order Queues ─────────────────────────────────────────────────────────

    @Bean
    public Queue orderPlacedQueue() {
        return withDlx(ORDER_PLACED_QUEUE).build();
    }

    @Bean
    public Queue orderStatusChangedQueue() {
        return withDlx(ORDER_STATUS_CHANGED_QUEUE).build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return withDlx(ORDER_CANCELLED_QUEUE).build();
    }

    // ─── Business Status Queues ────────────────────────────────────────────────

    @Bean
    public Queue businessOpenedQueue() {
        return withDlx(BUSINESS_OPENED_QUEUE).build();
    }

    @Bean
    public Queue businessClosedQueue() {
        return withDlx(BUSINESS_CLOSED_QUEUE).build();
    }

    // ─── Bindings ─────────────────────────────────────────────────────────────

    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder.bind(orderPlacedQueue())
                .to(llburgerExchange()).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding orderStatusChangedBinding() {
        return BindingBuilder.bind(orderStatusChangedQueue())
                .to(llburgerExchange()).with(ORDER_STATUS_CHANGED_KEY);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue())
                .to(llburgerExchange()).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding businessOpenedBinding() {
        return BindingBuilder.bind(businessOpenedQueue())
                .to(llburgerExchange()).with(BUSINESS_OPENED_KEY);
    }

    @Bean
    public Binding businessClosedBinding() {
        return BindingBuilder.bind(businessClosedQueue())
                .to(llburgerExchange()).with(BUSINESS_CLOSED_KEY);
    }

    // ─── JSON Message Converter ────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ─── RabbitTemplate ────────────────────────────────────────────────────────

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        // Enable publisher confirmations so we know messages reached the broker
        template.setMandatory(true);
        return template;
    }

    // ─── Listener Container Factory ───────────────────────────────────────────

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false); // send rejected messages to DLQ
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }
}
