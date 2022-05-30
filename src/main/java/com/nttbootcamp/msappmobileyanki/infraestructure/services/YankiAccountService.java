package com.nttbootcamp.msappmobileyanki.infraestructure.services;


import com.nttbootcamp.msappmobileyanki.application.exception.EntityAlreadyExistsException;
import com.nttbootcamp.msappmobileyanki.application.exception.EntityNotExistsException;
import com.nttbootcamp.msappmobileyanki.application.exception.ResourceNotCreatedException;
import com.nttbootcamp.msappmobileyanki.domain.beans.AvailableAmountDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateYankiAccountDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateYankiAccountWithCardDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.DebitCardDTO;
import com.nttbootcamp.msappmobileyanki.domain.model.YankiAccount;
import com.nttbootcamp.msappmobileyanki.domain.repository.YankiAccountRepository;
import com.nttbootcamp.msappmobileyanki.infraestructure.interfaces.IYankiAccountService;
import com.nttbootcamp.msappmobileyanki.infraestructure.kafkaservices.IDebitCardStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class YankiAccountService implements IYankiAccountService {

    //Repositories and Services
    @Autowired
    private YankiAccountRepository repository;

    @Autowired
    private IDebitCardStream debitCardStream;

    // Crud
    @Override
    public Flux<YankiAccount> findAll() {
        return repository.findAll();
    }
    @Override
    public Mono<YankiAccount> delete(String Id) {
        return repository.findById(Id).flatMap(deleted -> repository.delete(deleted).then(Mono.just(deleted)))
        				 .switchIfEmpty(Mono.error(new EntityNotExistsException()));
    }
    @Override
    public Mono<YankiAccount> findById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Account doesn't exists")));
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
    /*public Mono<YankiAccount> findAllAccountsIn(Collection<String> accounts){

        return repository.findFirstByAccountNumberInAndBalanceGreaterThanEqualOrderByDebitCardLinkDateAsc
                (accounts,BigDecimal.ZERO);
    }*/

    //Business Logic

    @Override
    public Mono<AvailableAmountDTO> getAvailableAmount(String cellphoneNumber) {
      return repository.findById(cellphoneNumber)
                .switchIfEmpty(Mono.error(new EntityNotExistsException("Account doesn't exists")))
                .map(a->AvailableAmountDTO.builder()
                            .cellphoneNumber(a.getCellphoneNumber())
                            .availableAmount(a.getBalance()).build());
    }

    @Override
    public Mono<BigDecimal> updateBalanceSend(String id, BigDecimal balance) {
        return repository.findById(id).flatMap(a ->
                {   BigDecimal bigDecimal=a.getBalance().add(balance);
                    a.setBalance(bigDecimal);
                    return repository.save(a).map(b->b.getBalance());
                });
    }
    @Override
    public Mono<BigDecimal> updateBalanceReceive(String id, BigDecimal balance) {
        return repository.findById(id).filter(a->balance.compareTo(a.getBalance())<=0)
                .switchIfEmpty(Mono.error(new ResourceNotCreatedException("Withdrawal is more than actual balance")))
                .flatMap(a -> { a.setBalance(a.getBalance().subtract(balance));
                                return repository.save(a).map(b->b.getBalance());
                });

    }


    @Override
    public Mono<YankiAccount> createYankiAccount(CreateYankiAccountDTO account) {

        Mono<Boolean> count = repository.existsByCellphoneNumber(account.getCellphoneNumber());

        return  count.filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new EntityAlreadyExistsException("Account already exists")))
                .flatMap(t -> mapToAccountAndSave.apply(account));

    }
    @Override
    public Mono<YankiAccount> createYankiAccountWithCard(CreateYankiAccountWithCardDTO account) {

        Mono<Boolean> count = repository.existsByCellphoneNumber(account.getCellphoneNumber());

        Mono<DebitCardDTO> bsPartner = debitCardStream.findById(account.getDebitCardNumber());

        return  count.filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new EntityAlreadyExistsException("Account already exists")))
                .then(bsPartner)
                .filter(p->p.getCodeBusinessPartner().equals(account.getDocNum()))
                .switchIfEmpty(Mono.error(new EntityAlreadyExistsException("Document number is not valid for the card")))
                .flatMap(t -> mapToAccountAndSave1.apply(account));

    }

    private final Function<CreateYankiAccountDTO, Mono<YankiAccount>> mapToAccountAndSave = dto -> {

        YankiAccount a = YankiAccount.builder()
                .valid(true)
                .balance(new BigDecimal("0.00"))
                .cellphoneNumber(dto.getCellphoneNumber())
                .docIdemType(dto.getDocIdemType())
                .docNum(dto.getDocNum())
                .imei(dto.getImei())
                .createdDate(LocalDate.now())
                .createdDateTime(LocalDateTime.now())
                .email(dto.getEmail())
                .build();
        return repository.save(a);
    };

    private final Function<CreateYankiAccountWithCardDTO, Mono<YankiAccount>> mapToAccountAndSave1 = dto -> {

        YankiAccount a = YankiAccount.builder()
                .valid(true)
                .balance(new BigDecimal("0.00"))
                .cellphoneNumber(dto.getCellphoneNumber())
                .docIdemType(dto.getDocIdemType())
                .docNum(dto.getDocNum())
                .imei(dto.getImei())
                .createdDate(LocalDate.now())
                .createdDateTime(LocalDateTime.now())
                .email(dto.getEmail())
                .linkedDebitCard(dto.getDebitCardNumber())
                .build();
        return repository.save(a);
    };
}
