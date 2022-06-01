package com.nttbootcamp.msappmobileyanki.infraestructure.interfaces;

import com.nttbootcamp.msappmobileyanki.domain.beans.AvailableAmountDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateYankiAccountDTO;
import com.nttbootcamp.msappmobileyanki.domain.beans.CreateYankiAccountWithCardDTO;
import com.nttbootcamp.msappmobileyanki.domain.model.YankiAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface IYankiAccountService {
	Flux<YankiAccount> findAll();



	Mono<YankiAccount> createYankiAccount(CreateYankiAccountDTO account);
	Mono<YankiAccount> createYankiAccountWithCard(CreateYankiAccountWithCardDTO account);
	Mono<YankiAccount> delete(String id);
	Mono<YankiAccount> findById(String id);
	Flux<YankiAccount> saveAll(List<YankiAccount> a);
	Mono<YankiAccount> update(YankiAccount request);
	//Mono<YankiAccount> findAllAccountsIn(Collection<String> accounts);
	Mono<BigDecimal> updateBalanceSend(String id, String linkcard, BigDecimal balance);
	Mono<BigDecimal> updateBalanceReceive(String id, String linkcard, BigDecimal balance);
	Mono<AvailableAmountDTO> getAvailableAmount(String accountNumber);
	Mono<YankiAccount> findByCellphoneNumber(String cellphoneNumber);
}
