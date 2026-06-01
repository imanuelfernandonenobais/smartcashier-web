package com.smartcashier.web.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class TransactionCodeService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String nextSaleCode(Optional<String> latestCode) {
        return nextCode("PJ", latestCode);
    }

    public String nextPurchaseCode(Optional<String> latestCode) {
        return nextCode("PB", latestCode);
    }

    private String nextCode(String prefix, Optional<String> latestCode) {
        int nextSequence = latestCode
                .map(code -> code.substring(code.lastIndexOf('-') + 1))
                .map(Integer::parseInt)
                .map(value -> value + 1)
                .orElse(1);

        return prefix + "-" + LocalDate.now().format(DATE_FORMAT) + "-" + String.format("%03d", nextSequence);
    }
}
