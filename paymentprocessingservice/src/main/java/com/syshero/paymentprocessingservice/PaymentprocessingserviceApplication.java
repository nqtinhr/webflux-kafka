package com.syshero.paymentprocessingservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableR2dbcRepositories
@ComponentScan({ "com.syshero.paymentprocessingservice", "com.syshero.commonservice" })
public class PaymentprocessingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentprocessingserviceApplication.class, args);
	}

	@PostConstruct
	public void enableContextPropagation() {
		Hooks.enableAutomaticContextPropagation();
	}

}
