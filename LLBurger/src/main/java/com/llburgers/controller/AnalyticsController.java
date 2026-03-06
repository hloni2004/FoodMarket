package com.llburgers.controller;

import com.llburgers.dto.*;
import com.llburgers.service.IAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin
public class AnalyticsController {

    private final IAnalyticsService analyticsService;

    public AnalyticsController(IAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ─── Dashboard KPIs ───────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    // ─── Revenue ──────────────────────────────────────────────────────────────

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<RevenuePoint>> getRevenueByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getRevenueByDay(from, to));
    }

    @GetMapping("/revenue/total")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getTotalRevenue(from, to));
    }

    // ─── Peak Hours ───────────────────────────────────────────────────────────

    @GetMapping("/peak-hours")
    public ResponseEntity<List<PeakHourPoint>> getPeakHours(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(analyticsService.getPeakHours(from, to));
        }
        return ResponseEntity.ok(analyticsService.getPeakHours());
    }

    // ─── Popular Items ────────────────────────────────────────────────────────

    @GetMapping("/popular/products")
    public ResponseEntity<List<PopularItemResult>> getPopularProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getPopularProducts(limit));
    }

    @GetMapping("/popular/extras")
    public ResponseEntity<List<PopularItemResult>> getPopularExtras(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getPopularExtras(limit));
    }

    @GetMapping("/popular/sides")
    public ResponseEntity<List<PopularItemResult>> getPopularSides(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getPopularSides(limit));
    }

    // ─── Customer Insights ────────────────────────────────────────────────────

    @GetMapping("/top-customers")
    public ResponseEntity<List<CustomerInsight>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopCustomers(limit));
    }
}
