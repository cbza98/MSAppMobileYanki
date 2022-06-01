package com.nttbootcamp.msappmobileyanki.infraestructure.interfaces;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateDebitCardDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.DebitCardBalanceDTO;
import com.nttbootcamp.msappmobileyanki.domain.model.DebitCard;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IDebitCardService {
    Flux<DebitCard> findAll();

    Mono<DebitCard> createDebitCard(DebitCard debitCardDTO);

    Mono<DebitCard> delete(String id);

    Mono<DebitCard> findById(String id);
    Mono<DebitCardBalanceDTO> getDebitCardBalance(String debitCardNumber);
    Mono<Boolean> existsByCardNumber(String CardNumber);
}
