package com.nttbootcamp.msappmobileyanki.domain.enums;

public enum TransactionType {
    PAYMENT("YANKI PAYMENT"),
    PAYMENT_DEBIT_CARD("YANKI PAYMENT WITH DEBIT CARD");

    public final String type;

    TransactionType(String type) {
        this.type = type;
    }
}
