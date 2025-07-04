package com.syshero.paymentprocessingservice.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@Slf4j
public class EventProducer {
    @Autowired
    private KafkaSender<String, String> sender;

    public Mono<String> sendEvent(String topic,String message){
        return sender.
                send(Mono.just(SenderRecord.create(new ProducerRecord<>(topic,message),message)))
                .doOnError(e -> log.error("Kafka send error: topic={}, message={}, error={}", topic, message, e.getMessage(), e))
                .then().thenReturn("OK");
    }
}
