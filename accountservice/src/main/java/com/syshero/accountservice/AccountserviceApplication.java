package com.syshero.accountservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableR2dbcRepositories
@ComponentScan({ "com.syshero.accountservice", "com.syshero.commonservice" })
public class AccountserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountserviceApplication.class, args);
	}

	@PostConstruct
	public void enableContextPropagation() {
		Hooks.enableAutomaticContextPropagation();
	}
}
