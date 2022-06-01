package com.nttbootcamp.msappmobileyanki.domain.repository;
import com.nttbootcamp.msappmobileyanki.domain.model.DebitCard;
import com.nttbootcamp.msappmobileyanki.domain.model.YankiAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DebitCardYankiRepository extends ReactiveMongoRepository<DebitCard,String> {
    Mono<DebitCard> findByCardNumberAndExpiringDateAndCvv(String creditCardNumber, String expDate, String cvv);
    Mono<Boolean> existsByCardNumber(String CardNumber);
    Mono<DebitCard> findByCardNumber(String CardNumber);
}
