package com.nttbootcamp.msappmobileyanki.domain.beans;

import lombok.*;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiOperationDTO {
    @NotBlank
    private String fromCellphoneAccount;
    @NotBlank
    private String toCellphoneAccount;
    @NotNull
    @Digits(integer =20, fraction=6)
    private BigDecimal amount;

}
