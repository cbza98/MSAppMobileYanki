package com.nttbootcamp.msappmobileyanki.infraestructure.interfaces;

import com.nttbootcamp.msappmobileyanki.domain.beans.YankiOperationDTO;
import com.nttbootcamp.msappmobileyanki.domain.model.Transaction;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IYankiAccountTransactionService {
	
	Flux<Transaction> findAll();
	
	Mono<Transaction> delete(String id);

	Mono<Transaction> findById(String id);
	
	Mono<ResponseEntity<Transaction>> update(String id, Transaction request);
	
	Flux<Transaction> saveAll(List<Transaction> a);

    Mono<Transaction> doYankiPayment(YankiOperationDTO dto);

}
