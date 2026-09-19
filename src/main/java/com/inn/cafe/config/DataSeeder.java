package com.inn.cafe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Seeds a freshly provisioned database so the app is usable on first deploy.
 *
 * <p>Deliberately opt-in ({@code app.seed.enabled}) and idempotent: the catalogue is only
 * loaded when the category table is empty, and the admin is only created when one does not
 * already exist. Safe to leave enabled - it becomes a no-op after the first run.
 *
 * <p>Admin credentials come from the environment rather than being hardcoded, so this file
 * can live in a public repository without shipping a usable login.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    @Value("${app.seed.admin.email:}")
    private String adminEmail;

    @Value("${app.seed.admin.password:}")
    private String adminPassword;

    @Value("${app.seed.admin.name:Admin}")
    private String adminName;

    @Value("${app.seed.admin.contact:0000000000}")
    private String adminContact;

    public DataSeeder(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCatalogue();
        seedAdmin();
    }

    private void seedCatalogue() {
        Integer categories = jdbc.queryForObject("select count(*) from category", Integer.class);
        if (categories != null && categories > 0) {
            log.info("Seed: {} categories already present, skipping catalogue.", categories);
            return;
        }
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                    new ClassPathResource("categories.sql"),
                    new ClassPathResource("product.sql"));
            populator.execute(dataSource);
            log.info("Seed: catalogue loaded ({} categories, {} products).",
                    jdbc.queryForObject("select count(*) from category", Integer.class),
                    jdbc.queryForObject("select count(*) from product", Integer.class));
        } catch (Exception ex) {
            // Never block startup on seeding - an empty catalogue is recoverable, a crash loop is not.
            log.error("Seed: catalogue load failed: {}", ex.getMessage());
        }
    }

    private void seedAdmin() {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("Seed: app.seed.admin.email/password not set - no admin created. "
                    + "Set them to get a login on a fresh database.");
            return;
        }
        try {
            Integer existing = jdbc.queryForObject(
                    "select count(*) from `user` where email = ?", Integer.class, adminEmail);
            if (existing != null && existing > 0) {
                log.info("Seed: admin {} already exists, skipping.", adminEmail);
                return;
            }
            jdbc.update("insert into `user` (name, contactNumber, email, password, status, role) "
                            + "values (?, ?, ?, ?, 'true', 'admin')",
                    adminName, adminContact, adminEmail, adminPassword);
            log.info("Seed: admin user {} created.", adminEmail);
        } catch (Exception ex) {
            log.error("Seed: admin creation failed: {}", ex.getMessage());
        }
    }
}
