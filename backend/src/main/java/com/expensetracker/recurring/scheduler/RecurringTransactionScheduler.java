package com.expensetracker.recurring.scheduler;

import com.expensetracker.recurring.service.RecurringTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "app.recurring.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RecurringTransactionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionScheduler(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    /**
     * Executes daily at 02:00 AM UTC by default to generate due transactions.
     */
    @Scheduled(cron = "${app.recurring.cron:0 0 2 * * ?}")
    public void scheduleDailyRecurringProcessing() {
        log.info("Starting scheduled recurring transaction generation execution...");
        try {
            int count = recurringTransactionService.processDueRecurringTransactions(LocalDate.now());
            log.info("Scheduled recurring transaction generation completed. Total generated: {}", count);
        } catch (Exception ex) {
            log.error("Error occurred during scheduled recurring transaction generation", ex);
        }
    }
}
