package com.expensetracker.recurring.service;

import com.expensetracker.recurring.entity.RecurringFrequency;

import java.time.LocalDate;

public final class RecurringDateUtils {

    private RecurringDateUtils() {
    }

    public static LocalDate calculateNextOccurrence(LocalDate current, RecurringFrequency frequency, int interval, int preferredDayOfMonth) {
        int safeInterval = interval > 0 ? interval : 1;
        int safeDay = preferredDayOfMonth > 0 ? preferredDayOfMonth : current.getDayOfMonth();

        switch (frequency) {
            case DAILY:
                return current.plusDays(safeInterval);
            case WEEKLY:
                return current.plusWeeks(safeInterval);
            case MONTHLY: {
                LocalDate targetMonth = current.plusMonths(safeInterval);
                int maxDay = targetMonth.lengthOfMonth();
                return targetMonth.withDayOfMonth(Math.min(safeDay, maxDay));
            }
            case YEARLY: {
                LocalDate targetYear = current.plusYears(safeInterval);
                int maxDay = targetYear.lengthOfMonth();
                return targetYear.withDayOfMonth(Math.min(safeDay, maxDay));
            }
            default:
                return current.plusMonths(1);
        }
    }
}
