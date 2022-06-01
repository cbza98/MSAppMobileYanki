package com.nttbootcamp.msappmobileyanki.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
@ToString
public class DebitCard {

    @Id
    private String cardNumber;
    private String cardName;
    private String expiringDate;
    private String cvv;
    private Boolean valid;
    private BigDecimal amount;
    private LocalDateTime createdate;
    private String codeBusinessPartner;
    List<LinkedAccount> linkedAccountList;
}
