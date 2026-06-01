package com.smartcashier.web.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component("money")
public class AppFormattingService {

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public String format(BigDecimal value) {
        return currencyFormatter.format(value == null ? BigDecimal.ZERO : value);
    }
}
