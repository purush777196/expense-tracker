package com.expensetracker.integration;

import com.expensetracker.category.entity.Category;
import com.expensetracker.category.repository.CategoryRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ContextLoadAndFlywayIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Application context should load and Flyway migrations V1, V2 must be applied successfully")
    void contextLoadsAndFlywayMigrationsApplied() {
        MigrationInfo[] appliedMigrations = flyway.info().applied();

        assertThat(appliedMigrations).isNotEmpty();
        assertThat(appliedMigrations.length).isGreaterThanOrEqualTo(2);
        assertThat(appliedMigrations[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(appliedMigrations[1].getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    @DisplayName("System default categories from V2 migration must exist in the database")
    void systemDefaultCategoriesSeeded() {
        List<Category> systemCategories = categoryRepository.findByIsSystemDefaultTrue();

        assertThat(systemCategories).isNotEmpty();
        assertThat(systemCategories.size()).isGreaterThanOrEqualTo(13);

        boolean hasGroceries = systemCategories.stream().anyMatch(c -> "Groceries".equals(c.getName()));
        boolean hasSalary = systemCategories.stream().anyMatch(c -> "Salary".equals(c.getName()));

        assertThat(hasGroceries).isTrue();
        assertThat(hasSalary).isTrue();
    }
}
