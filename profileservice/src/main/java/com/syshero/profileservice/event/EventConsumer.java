package com.syshero.profileservice.event;

import com.google.gson.Gson;
import com.syshero.commonservice.utils.Constant;
import com.syshero.profileservice.model.ProfileDTO;
import com.syshero.profileservice.service.ProfileService;
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
    ProfileService profileService;
    
    @Autowired
    private Tracer tracer;

    public EventConsumer(ReceiverOptions<String, String> receiverOptions){
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(Constant.PROFILE_ONBOARDED_TOPIC)))
                .receive().subscribe(this::profileOnboarded);
    }
    
    public void profileOnboarded(ReceiverRecord<String,String> receiverRecord){
        // Tạo span mới cho mỗi event
        try (Tracer.SpanInScope ws = tracer.withSpan(tracer.nextSpan().name("kafka-profileOnboarded").start())) {
            log.info("Received Profile Onboarded event: key={}, value={}", receiverRecord.key(), receiverRecord.value());
            ProfileDTO dto = gson.fromJson(receiverRecord.value(), ProfileDTO.class);
            profileService.updateStatusProfile(dto)
                .doOnError(e -> log.error("Error updating profile status for key={}: {}", receiverRecord.key(), e.getMessage(), e))
                .subscribe();
        } catch (Exception e) {
            log.error("Exception in profileOnboarded: key={}, error={}", receiverRecord.key(), e.getMessage(), e);
        }
    }
}