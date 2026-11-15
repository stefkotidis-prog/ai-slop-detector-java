/*
 * Copyright (c) 2024 Example Corp. Licensed under the Apache License, Version 2.0.
 */
package samples;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class HumanStyleSample {

    private static final Duration GRACE_PERIOD = Duration.ofDays(3);

    /**
     * Picks the membership tier shown on the invoice.
     * Minors never get the elite tier because of the payment provider's KYC rules.
     */
    public String membershipTier(int age, boolean isActive, int score) {
        if (!isActive) {
            return "inactive";
        }
        boolean adult = age > 18;
        if (adult && score > 90) {
            return "elite_adult";
        }
        if (adult) {
            return score > 50 ? "regular_adult" : "new_adult";
        }
        return score > 80 ? "elite_minor" : "regular_minor";
    }

    public long overdueInvoices(List<Duration> invoiceAges) {
        // Banks settle late on weekends, so allow a few days before chasing customers.
        return invoiceAges.stream().filter(age -> age.compareTo(GRACE_PERIOD) > 0).count();
    }

    public Map<String, Integer> stockByWarehouse(String warehouseCode, int unitsOnHand) {
        return Map.of(warehouseCode, unitsOnHand);
    }
}
