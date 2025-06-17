package com.syshero.paymentprocessingservice.event;

import com.google.gson.Gson;
import com.syshero.commonservice.models.PaymentDTO;
import com.syshero.commonservice.utils.Constant;
import io.micrometer.tracing.Tracer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.security.SecureRandom;
import java.util.Collections;

@Service
@Slf4j
public class EventConsumer {
    Gson gson = new Gson();
    SecureRandom random = new SecureRandom();

    @Autowired
    EventProducer eventProducer;

    @Autowired
    private Tracer tracer;

    public EventConsumer(ReceiverOptions<String, String> receiverOptions){
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PAYMENT_CREATED_TOPIC)))
                .receive().subscribe(this::paymentCreated);
    }

    @SneakyThrows
    public void paymentCreated(ReceiverRecord<String,String> receiverRecord){
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-paymentCreated").start())) {
            log.info("Processing payment ...");
            PaymentDTO paymentDTO = gson.fromJson(receiverRecord.value(), PaymentDTO.class);
            String[] randomStatus = {Constant.STATUS_PAYMENT_REJECTED, Constant.STATUS_PAYMENT_SUCCESSFUL};
            int index = random.nextInt(randomStatus.length);
            paymentDTO.setStatus(randomStatus[index]);
            Thread.sleep(5000);
            eventProducer.sendEvent(Constant.PAYMENT_COMPLETED_TOPIC, gson.toJson(paymentDTO)).subscribe();
        }
    }
}
