package com.llburgers.config;

import com.llburgers.domain.Admin;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds essential startup data on every application boot.
 * Each seed operation is idempotent — it checks before inserting,
 * so running the application multiple times is always safe.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String ADMIN_EMAIL    = "hloni2@gmail.com";
    private static final String ADMIN_PASSWORD = "SuperAdmin@123";
    private static final String ADMIN_NAME     = "Hloni Super Admin";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.adminRepository = adminRepository;
        this.passwordEncoder  = passwordEncoder;
        this.jdbcTemplate     = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        fixCategoryCheckConstraint();
        seedDefaultAdmin();
    }

    // ─── DB Constraint Fix ──────────────────────────────────────────────

    /**
     * The products_category_check constraint may have been created without the
     * SIDE value (if the SIDE enum was added after initial deployment).
     * This runs on every startup and ensures the constraint allows all current categories.
     */
    private void fixCategoryCheckConstraint() {
        try {
            // Drop ANY existing check constraint on the category column, regardless of its
            // auto-generated name, then recreate it with all four current categories.
            jdbcTemplate.execute(
                "DO $$ DECLARE con_name TEXT; BEGIN " +
                "FOR con_name IN " +
                "  SELECT con.conname FROM pg_constraint con " +
                "  JOIN pg_class rel ON rel.oid = con.conrelid " +
                "  WHERE rel.relname = 'products' AND con.contype = 'c' " +
                "  AND pg_get_constraintdef(con.oid) ILIKE '%category%' " +
                "LOOP " +
                "  EXECUTE 'ALTER TABLE products DROP CONSTRAINT IF EXISTS ' || quote_ident(con_name); " +
                "END LOOP; END $$"
            );
            jdbcTemplate.execute(
                "ALTER TABLE products ADD CONSTRAINT products_category_check " +
                "CHECK (category IN ('BURGER', 'DRINK', 'SAUCE', 'SIDE'))"
            );
            log.info("[SEED] products_category_check constraint updated to include all categories.");
        } catch (Exception e) {
            log.warn("[SEED] Could not update products_category_check: {}", e.getMessage());
        }
    }

    // ─── Admin Seed ───────────────────────────────────────────────────────────

    private void seedDefaultAdmin() {
        if (adminRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("[SEED] Admin '{}' already exists — skipping.", ADMIN_EMAIL);
            return;
        }

        Admin admin = Admin.builder()
                .name(ADMIN_NAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .adminLevel(AdminLevel.SUPER_ADMIN)
                .active(true)
                .build();

        adminRepository.save(admin);
        log.info("[SEED] Default admin created — email: {}", ADMIN_EMAIL);
    }
}
