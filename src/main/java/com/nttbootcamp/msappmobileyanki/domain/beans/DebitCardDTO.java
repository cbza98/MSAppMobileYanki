package com.nttbootcamp.msappmobileyanki.domain.beans;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class DebitCardDTO {
    private String cardNumber;
    private String cardName;
    private String expiringDate;
    private String cvv;
    private Boolean valid;
    private String codeBusinessPartner;
}
