package com.nttbootcamp.msappmobileyanki.infraestructure.services;


import com.nttbootcamp.msappmobileyanki.application.exception.EntityNotExistsException;
import com.nttbootcamp.msappmobileyanki.application.exception.ResourceNotCreatedException;
import com.nttbootcamp.msappmobileyanki.domain.beans.YankiOperationDTO;
import com.nttbootcamp.msappmobileyanki.domain.enums.TransactionType;
import com.nttbootcamp.msappmobileyanki.domain.model.Message;
import com.nttbootcamp.msappmobileyanki.domain.model.Transaction;
import com.nttbootcamp.msappmobileyanki.domain.model.YankiAccount;
import com.nttbootcamp.msappmobileyanki.domain.repository.TransactionRepository;
import com.nttbootcamp.msappmobileyanki.infraestructure.interfaces.IYankiAccountService;
import com.nttbootcamp.msappmobileyanki.infraestructure.interfaces.IYankiAccountTransactionService;
import com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices.DebitCardStream;
import com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices.IDebitCardStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class YankiAccountTransactionService implements IYankiAccountTransactionService {

    //Services and Repositories
    @Autowired
    TransactionRepository tRepository;
    @Autowired
    YankiAccountService accountService;
    @Autowired
    DebitCardStream debitCardStream;
    //Crud
    @Override
    public Flux<Transaction> findAll() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Mono<Transaction> delete(String id) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Mono<Transaction> findById(String id) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Mono<ResponseEntity<Transaction>> update(String id, Transaction request) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Flux<Transaction> saveAll(List<Transaction> a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Transaction> doPayment(YankiOperationDTO operationDTO) {
        Mono<YankiAccount> fromAccount = accountService.findByCellphoneNumber(operationDTO.getFromCellphoneAccount())
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Origin cellphone doesn't exists")));

        Mono<YankiAccount> toAccount = accountService.findByCellphoneNumber(operationDTO.getFromCellphoneAccount())
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Destiny cellphone doesn't exists")));


        return Mono.zip(fromAccount,toAccount)
                .filter(a-> !(operationDTO.getFromCellphoneAccount().equals(operationDTO.getToCellphoneAccount())))
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Cellphone cannot be the same")))

                .flatMap(a-> accountService.updateBalanceReceive(a.getT1().getCellphoneNumber(), a.getT1().getLinkedDebitCard(), operationDTO.getAmount())

                        .thenReturn(a))
                .flatMap(a-> accountService.updateBalanceSend(a.getT2().getCellphoneNumber(), a.getT2().getLinkedDebitCard() ,operationDTO.getAmount())
                        .thenReturn(a))
                .then(Mono.just(operationDTO))
                .flatMap(r-> savedoPayment.apply(r))
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Transaction error")));
    }

    @Override
    public Mono<Transaction> doReceive(YankiOperationDTO operationDTO) {
        return null;
    }
    //Business Logic
    //Reactivo




    //Funcional

    private final Function<YankiOperationDTO, Mono<Transaction>> savedoPayment = dto -> {


        Transaction t = Transaction.builder()
                .amount(dto.getAmount())
                .fromCellphoneAccount(dto.getFromCellphoneAccount())
                .transactiontype(TransactionType.PAYMENT)
                .toCellphoneAccount(dto.getToCellphoneAccount())
                .createDate(LocalDate.now()).build();

        return tRepository.save(t);
    };


    /*

    /// Logica Vieja
    @Override
    public Mono<Transaction> doYankiPayment(YankiOperationDTO dto) {
        Mono<YankiAccount> fromAccount = accountService.findById(dto.getFromCellphoneAccount())
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Origin account doesn't exists")));
        Mono<YankiAccount> toAccount = accountService.findById(dto.getToCellphoneAccount())
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Destiny account doesn't exists")));

        return  Mono.zip(fromAccount,toAccount)
                .filter(a-> !(dto.getFromCellphoneAccount().equals(dto.getToCellphoneAccount())))
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Account cannot be the same")))
                .flatMap(a->saveTransactionPayment.apply(a,dto))
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Transaction error")));
    }
    public Mono<Transaction> doYankiOutgoutPayment(YankiOperationDTO dto) {
        Mono<YankiAccount> fromAccount = accountService.findById(dto.getFromCellphoneAccount())
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Origin account doesn't exists")));
        Mono<YankiAccount> toAccount = accountService.findById(dto.getToCellphoneAccount())
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Destiny account doesn't exists")));

        return  Mono.zip(fromAccount,toAccount)
                .filter(a-> !(dto.getFromCellphoneAccount().equals(dto.getToCellphoneAccount())))
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Account cannot be the same")))
                .flatMap(a->saveTransactionOutGoutPayment.apply(a,dto))
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Transaction error")));
    }
    //Functions
    private final BiFunction<Tuple2<YankiAccount,YankiAccount>,YankiOperationDTO, Mono<Transaction>> saveTransactionPayment
            = (tuple2,dto) -> {

           Transaction t = Transaction.builder()
                .debit(dto.getAmount())
                .credit(dto.getAmount())
                .fromCellphoneAccount(tuple2.getT1().getCellphoneNumber())
                .toCellphoneAccount(tuple2.getT2().getCellphoneNumber())
                .transactiontype(TransactionType.PAYMENT)
                .createDate(LocalDate.now())
                .createDateTime(LocalDateTime.now())
                .build();

           return Mono.just(tuple2)
                    .flatMap(t2-> Mono.just(t2.getT1().getLinkedDebitCard())
                            .filter(a->a!=null)
                            .flatMap(s->debitCardStream.doCardWithdraw(dto.getFromCellphoneAccount(),dto.getAmount()))
                            .switchIfEmpty(accountService.updateBalanceSend(dto.getFromCellphoneAccount(),
                                    dto.getAmount()))
                            .thenReturn(t2)
                    )
                    .flatMap(t2-> Mono.just(t2.getT2().getLinkedDebitCard())
                            .filter(a->a!=null)
                            .flatMap(s->debitCardStream.doCardDeposit(dto.getToCellphoneAccount(),dto.getAmount()))
                            .switchIfEmpty(accountService.updateBalanceReceive(dto.getToCellphoneAccount(),
                                                                            dto.getAmount()))
                            .thenReturn(t2)
                    ).then(tRepository.save(t));
    };
    private final BiFunction<Tuple2<YankiAccount,YankiAccount>,YankiOperationDTO, Mono<Transaction>> saveTransactionOutGoutPayment
            = (tuple2,dto) -> {

        Transaction t = Transaction.builder()
                .debit(dto.getAmount())
                .credit(dto.getAmount())
                .fromCellphoneAccount(tuple2.getT1().getCellphoneNumber())
                .toCellphoneAccount(tuple2.getT2().getCellphoneNumber())
                .transactiontype(TransactionType.PAYMENT)
                .createDate(LocalDate.now())
                .createDateTime(LocalDateTime.now())
                .build();

        return Mono.just(tuple2)
                .flatMap(t2-> Mono.just(t2.getT1().getLinkedDebitCard())
                        .filter(a->a!=null)
                        .flatMap(s->debitCardStream.doCardWithdraw(dto.getFromCellphoneAccount(),dto.getAmount()))
                        .then(accountService.updateBalanceReceive(dto.getFromCellphoneAccount(),
                                dto.getAmount()))
                        .thenReturn(t2)
                )
                .flatMap(t2-> Mono.just(t2.getT2().getLinkedDebitCard())
                        .filter(a->a!=null)
                        .flatMap(s->debitCardStream.doCardDeposit(dto.getToCellphoneAccount(),dto.getAmount()))
                        .then(accountService.updateBalanceSend(dto.getToCellphoneAccount(),
                                dto.getAmount()))
                        .thenReturn(t2)
                ).then(tRepository.save(t));
    };

     */
}






