package com.syshero.paymentservice.event;

import com.google.gson.Gson;
import com.syshero.commonservice.utils.Constant;
import com.syshero.paymentservice.model.PaymentDTO;
import com.syshero.paymentservice.service.PaymentService;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;

@Service
@Slf4j
public class EventConsumer {
    Gson gson = new Gson();

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private Tracer tracer;

    public EventConsumer(ReceiverOptions<String, String> receiverOptions) {
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PAYMENT_CREATED_TOPIC)))
                .receive().subscribe(this::paymentCreated);
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PAYMENT_COMPLETED_TOPIC)))
                .receive().subscribe(this::paymentComplete);
    }

    public void paymentCreated(ReceiverRecord<String, String> receiverRecord) {
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-paymentCreated").start())) {
            log.info("Payment created event " + receiverRecord.value());
            PaymentDTO paymentDTO = gson.fromJson(receiverRecord.value(), PaymentDTO.class);
            paymentService.updateStatusPayment(paymentDTO)
                    .doOnNext(result -> log.info("Update Status  " + result))
                    .subscribe();
            // .subscribe(result -> log.info("Update Status " + result));
        }
    }

    public void paymentComplete(ReceiverRecord<String, String> receiverRecord) {
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-paymentComplete").start())) {
            log.info("Payment complete event " + receiverRecord.value());
            PaymentDTO paymentDTO = gson.fromJson(receiverRecord.value(), PaymentDTO.class);
            paymentService.updateStatusPayment(paymentDTO)
                       .doOnNext(result -> log.info("End process payment " + result))
                    .subscribe();
                    // .subscribe(result -> log.info("End process payment " + result));
        }
    }
}