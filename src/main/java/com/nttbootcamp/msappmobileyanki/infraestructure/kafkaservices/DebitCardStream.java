package com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices;

import com.nttbootcamp.msappmobileyanki.application.exception.EntityAlreadyExistsException;
import com.nttbootcamp.msappmobileyanki.domain.beans.DebitCardDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.YankiOperationDTO;
import com.nttbootcamp.msappmobileyanki.domain.model.DebitCard;
import com.nttbootcamp.msappmobileyanki.domain.model.Message;
import com.nttbootcamp.msappmobileyanki.domain.repository.DebitCardYankiRepository;
import com.nttbootcamp.msappmobileyanki.infraestructure.services.YankiAccountTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class DebitCardStream implements IDebitCardStream {

    @Autowired
    private DebitCardYankiRepository servicesdebitcard;
    @Autowired
    private StreamBridge streamBridge;
    private DebitCard db;

    @Bean
    Consumer<Message> input() {
        return message -> {
            switch (message.getReferencia3()) {
                case "DEBIT_CARD_CONSUMPTION":
                case "DEBIT_CARD_WITHDRAWAL":

                    servicesdebitcard.findByCardNumber(message.getReferencia1())
                            .flatMap(r-> {
                                r.setAmount(message.getAmount());
                                servicesdebitcard.save(r);
                                return null;
                            }).subscribe();
                    break;
                case "DEBIT_CARD_CREATE":
                    db = DebitCard.builder()
                            .cardNumber(message.getReferencia1())
                            .amount(message.getAmount()).build();
                    servicesdebitcard.save(db).subscribe();
                    break;
            }
        };
    }
    public void sendMessage(Message message)
    {
        streamBridge.send("output-out-0",message);
    }

    @Override
    public Mono<DebitCardDTO> findById(String debitCardNumber) {
        return null;
    }

    @Override
    public Mono<DebitCard> doCardWithdraw(String debitCardNumber, BigDecimal amount) {
        return servicesdebitcard.findByCardNumber(debitCardNumber)
                .flatMap(r-> {
                    r.setAmount(amount);
                    return servicesdebitcard.save(r);
                })
                .switchIfEmpty(Mono.error(new EntityAlreadyExistsException()));
    }

    @Override
    public Mono<DebitCard> doCardDeposit(String debitCardNumber, BigDecimal amount) {
        return null;
    }
}
