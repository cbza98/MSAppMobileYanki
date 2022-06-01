package com.nttbootcamp.msappmobileyanki.infraestructure.services;


import com.nttbootcamp.msappmobileyanki.application.exception.EntityNotExistsException;
import com.nttbootcamp.msappmobileyanki.application.exception.ResourceNotCreatedException;
import com.nttbootcamp.msappmobileyanki.domain.beans.AvailableAmountDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateYankiAccountDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateYankiAccountWithCardDTO;
import com.nttbootcamp.msappmobileyanki.domain.model.Message;
import com.nttbootcamp.msappmobileyanki.domain.model.YankiAccount;
import com.nttbootcamp.msappmobileyanki.domain.repository.YankiAccountRepository;
import com.nttbootcamp.msappmobileyanki.infraestructure.interfaces.IYankiAccountService;
import com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices.DebitCardStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class YankiAccountService implements IYankiAccountService {

    //Attributes
    @Autowired
    private YankiAccountRepository repository;

    @Autowired
    private DebitCardService debitCardService;

    @Autowired
    private DebitCardStream senddebit;

    // Crud
    @Override
    public Flux<YankiAccount> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<YankiAccount> delete(String Id) {
        return repository.findById(Id).flatMap(deleted -> repository.delete(deleted).then(Mono.just(deleted))).switchIfEmpty(Mono.error(new EntityNotExistsException()));
    }

    @Override
    public Mono<YankiAccount> findById(String id) {
        return repository.findById(id).switchIfEmpty(Mono.error(new EntityNotExistsException("Account doesn't exists")));
    }

    @Override
    public Flux<YankiAccount> saveAll(List<YankiAccount> a) {

        return repository.saveAll(a);
    }

    @Override
    public Mono<YankiAccount> update(YankiAccount _request) {

        return repository.findById(_request.getCellphoneNumber()).flatMap(a -> {
            a.setCellphoneNumber(_request.getCellphoneNumber());
            a.setValid(_request.getValid());
            a.setDocIdemType(_request.getDocIdemType());
            a.setDocNum(_request.getDocNum());
            a.setEmail(_request.getEmail());
            a.setValid(_request.getValid());
            a.setLinkedDebitCard(_request.getLinkedDebitCard());
            return repository.save(a);
        }).switchIfEmpty(Mono.error(new EntityNotExistsException()));
    }

    //Business Logic
    @Override
    public Mono<AvailableAmountDTO> getAvailableAmount(String cellphoneNumber) {
        return repository.findById(cellphoneNumber).switchIfEmpty(Mono.error(new EntityNotExistsException("Account doesn't exists"))).map(a -> AvailableAmountDTO.builder().cellphoneNumber(a.getCellphoneNumber()).availableAmount(a.getBalance()).build());
    }

    @Override
    public Mono<YankiAccount> findByCellphoneNumber(String cellphoneNumber) {
        return repository.findByCellphoneNumber(cellphoneNumber);
    }

    @Override
    public Mono<BigDecimal> updateBalanceSend(String id, String linkcard, BigDecimal balance) {
        Mono<BigDecimal> returnvaluesend = repository.findById(id).flatMap(a -> {
            BigDecimal bigDecimal = a.getBalance().subtract(balance);
            a.setBalance(bigDecimal);
            return repository.save(a).map(b -> b.getBalance());
        });

        if (linkcard != null) {
            Message M = Message.builder()
                    .amount(balance)
                    .referencia1(linkcard)
                    .build();
            senddebit.sendMessage(M);
        }

        return returnvaluesend;

    }

    @Override
    public Mono<BigDecimal> updateBalanceReceive(String id, String linkcard, BigDecimal balance) {


        Mono<BigDecimal> returnvalue = repository.findById(id)
                .filter(a -> balance.compareTo(a.getBalance()) <= 0)
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Withdrawal is more than actual balance")))
                .flatMap(a -> {

            a.setBalance(a.getBalance().add(balance));
            return repository.save(a).map(b -> b.getBalance());
        });

        if (linkcard != null) {
            Message M = Message.builder()
                    .amount(balance)
                    .referencia1(linkcard)
                    .build();
            senddebit.sendMessage(M);
        }


        return returnvalue;

    }

    @Override
    public Mono<YankiAccount> createYankiAccount(CreateYankiAccountDTO account) {

        Mono<Boolean> _bool = repository.existsByCellphoneNumber(account.getCellphoneNumber());

        return Mono.zip(_bool, Mono.just(account)).filter(t -> cellphoneValidation.test(t.getT1().booleanValue())).switchIfEmpty(Mono.error(new ResourceNotCreatedException("CellPhone has associate"))).flatMap(t -> mapToAccountAndSave.apply(t.getT2()));
    }

    @Override
    public Mono<YankiAccount> createYankiAccountWithCard(CreateYankiAccountWithCardDTO account) {

        Mono<Boolean> exist = repository.existsByCellphoneNumber(account.getCellphoneNumber());
        Mono<Boolean> _debitcard = debitCardService.existsByCardNumber(account.getDebitCardNumber());

        Mono<YankiAccount> createa = Mono.zip(exist, _debitcard).filter(t -> cellphoneValidation.test(t.getT1().booleanValue())).switchIfEmpty(Mono.error(new ResourceNotCreatedException("CellPhone has associate"))).filter(t -> debitcardexist.test(t.getT2().booleanValue())).switchIfEmpty(Mono.error(new ResourceNotCreatedException("Debit card not exist"))).flatMap(t -> mapToAccountAndSaveWithCard.apply(account));


        return createa;
    }

    private final Function<CreateYankiAccountDTO, Mono<YankiAccount>> mapToAccountAndSave = dto -> {

        YankiAccount a = YankiAccount.builder().valid(true).balance(new BigDecimal("0.00")).cellphoneNumber(dto.getCellphoneNumber()).docIdemType(dto.getDocIdemType()).docNum(dto.getDocNum()).imei(dto.getImei()).createdDate(LocalDate.now()).createdDateTime(LocalDateTime.now()).email(dto.getEmail()).build();
        return repository.save(a);
    };
    private final Function<CreateYankiAccountWithCardDTO, Mono<YankiAccount>> mapToAccountAndSaveWithCard = dto -> {

        YankiAccount a = YankiAccount.builder().valid(true).balance(new BigDecimal("0.00")).cellphoneNumber(dto.getCellphoneNumber()).docIdemType(dto.getDocIdemType()).docNum(dto.getDocNum()).imei(dto.getImei()).createdDate(LocalDate.now()).createdDateTime(LocalDateTime.now()).email(dto.getEmail()).linkedDebitCard(dto.getDebitCardNumber()).build();
        return repository.save(a);
    };
    private final Predicate<Boolean> cellphoneValidation = t -> t.equals(false);
    private final Predicate<Boolean> debitcardexist = t -> t.equals(true);

    private final Predicate<String> poseetarjeta = t ->{

      return (t.equals(!t.isBlank()) || t.equals(!t.isEmpty()));
    };
}
