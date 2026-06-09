package com.smartcashier.web.model;

public enum UnitType {
    METER("meter"),
    ROLL("roll"),
    YARD("yard"),
    KG("kg");

    private final String label;

    UnitType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
