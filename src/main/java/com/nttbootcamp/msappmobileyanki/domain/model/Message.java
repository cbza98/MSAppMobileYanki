package com.nttbootcamp.msappmobileyanki.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
@Builder
@Data
@ToString
public class Message {

    private String referencia1;
    private BigDecimal amount;
    private String referencia2;
    private String referencia3;
    private String referencia4;
    private String referencia5;
    private String referencia6;
}

