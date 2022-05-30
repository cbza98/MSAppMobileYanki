package com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices;

import com.nttbootcamp.msappmobileyanki.domain.beans.DebitCardDTO;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface IDebitCardStream {
    Mono<DebitCardDTO> findById(String debitCardNumber);

    Mono<BigDecimal> doCardWithdraw(String debitCardNumber, BigDecimal amount);

    Mono<BigDecimal> doCardDeposit(String debitCardNumber, BigDecimal amount);
}
