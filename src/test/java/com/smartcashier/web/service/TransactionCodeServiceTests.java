package com.smartcashier.web.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionCodeServiceTests {

    private final TransactionCodeService transactionCodeService = new TransactionCodeService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    void nextSaleCodeStartsAtSequenceOne() {
        String code = transactionCodeService.nextSaleCode(Optional.empty());
        assertThat(code).isEqualTo("PJ-" + LocalDate.now().format(FORMATTER) + "-001");
    }

    @Test
    void nextPurchaseCodeIncrementsLatestSequence() {
        String code = transactionCodeService.nextPurchaseCode(Optional.of("PB-" + LocalDate.now().format(FORMATTER) + "-009"));
        assertThat(code).isEqualTo("PB-" + LocalDate.now().format(FORMATTER) + "-010");
    }
}
