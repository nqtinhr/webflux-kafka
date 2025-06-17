package com.syshero.accountservice.event;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.syshero.accountservice.model.AccountDTO;
import com.syshero.accountservice.service.AccountService;
import com.syshero.commonservice.common.CommonException;
import com.syshero.commonservice.models.PaymentDTO;
import com.syshero.commonservice.models.ProfileDTO;
import com.syshero.commonservice.utils.Constant;

import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.Objects;

@Service
@Slf4j
public class EventConsumer {
    Gson gson = new Gson();

    @Autowired
    AccountService accountService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    private Tracer tracer;

    public EventConsumer(ReceiverOptions<String, String> receiverOptions) {
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PROFILE_ONBOARDING_TOPIC)))
                .receive().subscribe(this::profileOnboarding);
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PAYMENT_REQUEST_TOPIC)))
                .receive().subscribe(this::paymentRequest);
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PAYMENT_COMPLETED_TOPIC)))
                .receive().subscribe(this::paymentComplete);
    }

    public void profileOnboarding(ReceiverRecord<String, String> receiverRecord) {
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-profileOnboarding").start())) {
            log.info("Profile Onboarding event");
            ProfileDTO dto = gson.fromJson(receiverRecord.value(), ProfileDTO.class);
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setEmail(dto.getEmail());
            accountDTO.setReserved(0);
            accountDTO.setBalance(dto.getInitialBalance());
            accountDTO.setCurrency("USD");
            accountService.createNewAccount(accountDTO)
                .doOnError(e -> log.error("Error creating new account for email={}: {}", dto.getEmail(), e.getMessage(), e))
                .subscribe(res -> {
                    dto.setStatus(Constant.STATUS_PROFILE_ACTIVE);
                    eventProducer.send(Constant.PROFILE_ONBOARDED_TOPIC, gson.toJson(dto))
                        .doOnError(e -> log.error("Error sending PROFILE_ONBOARDED_TOPIC for email={}: {}", dto.getEmail(), e.getMessage(), e))
                        .subscribe();
                });
        }
    }

    public void paymentRequest(ReceiverRecord<String, String> receiverRecord) {
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-paymentRequest").start())) {
            PaymentDTO paymentDTO = gson.fromJson(receiverRecord.value(), PaymentDTO.class);
            accountService.bookAmount(paymentDTO.getAmount(), paymentDTO.getAccountId())
                .doOnError(e -> log.error("Error booking amount for accountId={}: {}", paymentDTO.getAccountId(), e.getMessage(), e))
                .subscribe(result -> {
                    if (result) {
                        paymentDTO.setStatus(Constant.STATUS_PAYMENT_PROCESSING);
                        eventProducer.send(Constant.PAYMENT_CREATED_TOPIC, gson.toJson(paymentDTO))
                            .doOnError(e -> log.error("Error sending PAYMENT_CREATED_TOPIC for accountId={}: {}", paymentDTO.getAccountId(), e.getMessage(), e))
                            .subscribe();
                    } else {
                        log.error("Balance not enough for accountId={}", paymentDTO.getAccountId());
                        throw new CommonException("A02", "Balance not enough", HttpStatus.BAD_REQUEST);
                    }
                });
        }
    }

    public void paymentComplete(ReceiverRecord<String, String> receiverRecord) {
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-paymentComplete").start())) {
            log.info("Payment Complete event");
            PaymentDTO paymentDTO = gson.fromJson(receiverRecord.value(), PaymentDTO.class);
            if (Objects.equals(paymentDTO.getStatus(), Constant.STATUS_PAYMENT_SUCCESSFUL)) {
                accountService.subtract(paymentDTO.getAmount(), paymentDTO.getAccountId())
                    .doOnError(e -> log.error("Error subtracting amount for accountId={}: {}", paymentDTO.getAccountId(), e.getMessage(), e))
                    .subscribe();
            } else {
                accountService.rollbackReserved(paymentDTO.getAmount(), paymentDTO.getAccountId())
                    .doOnError(e -> log.error("Error rolling back reserved amount for accountId={}: {}", paymentDTO.getAccountId(), e.getMessage(), e))
                    .subscribe();
            }
        }
    }
}
