package com.syshero.accountservice.service;

import com.syshero.accountservice.model.AccountDTO;
import com.syshero.accountservice.repository.AccountRepository;
import com.syshero.commonservice.common.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    public Mono<AccountDTO> createNewAccount(AccountDTO accountDTO) {
        // log.info("Creating new account for email: {}", accountDTO.getEmail());
        return Mono.just(accountDTO)
                .map(AccountDTO::dtoToEntity)
                .flatMap(account -> accountRepository.save(account))
                .map(AccountDTO::entityToModel)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    public Mono<AccountDTO> checkBalance(String id) {
        log.info("Checking balance for account ID: {}", id);
        return findById(id);
    }

    public Mono<AccountDTO> findById(String id) {
        return accountRepository.findById(id)
                .map(AccountDTO::entityToModel)
                .switchIfEmpty(Mono.error(new CommonException("A01", "Account not found", HttpStatus.NOT_FOUND)));
    }

    public Mono<Boolean> bookAmount(double amount, String accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new CommonException("A01", "Account not found", HttpStatus.NOT_FOUND)))
                .flatMap(account -> {
                    // Kiểm tra số dư tài khoản có đủ để đặt trước không
                    if (account.getBalance() < amount + account.getReserved()) {
                        return Mono.just(false);
                    }
                    account.setReserved(account.getReserved() + amount);
                    return accountRepository.save(account);
                })
                .flatMap(account -> Mono.just(true));
    }

    public Mono<AccountDTO> subtract(double amount, String accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new CommonException("A01", "Account not found", HttpStatus.NOT_FOUND)))
                .flatMap(account -> {
                    account.setReserved(account.getReserved() - amount);
                    account.setBalance(account.getBalance() - amount);
                    return accountRepository.save(account);
                }).map(AccountDTO::entityToModel);
    }

    public Mono<AccountDTO> rollbackReserved(double amount, String accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new CommonException("A01", "Account not found", HttpStatus.NOT_FOUND)))
                .flatMap(account -> {
                    account.setReserved(account.getReserved() - amount);
                    return accountRepository.save(account);
                }).map(AccountDTO::entityToModel);
    }
}
