package com.syshero.accountservice.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.syshero.accountservice.data.Account;

public interface AccountRepository extends ReactiveCrudRepository<Account,String> {
}

