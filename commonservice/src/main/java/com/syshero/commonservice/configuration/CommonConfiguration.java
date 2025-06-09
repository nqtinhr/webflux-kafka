package com.syshero.commonservice.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CommonConfiguration {

    @Autowired
    private ReactiveKafkaAppProperties reactiveKafkaAppProperties;

    @Bean
    KafkaSender<String, String> kafkaSender() {
        Map<String, Object> props = new HashMap<>();

        // Địa chỉ Kafka broker
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, reactiveKafkaAppProperties.bootstrapServers);
        // Đảm bảo tất cả các replica nhận message (đảm bảo độ tin cậy cao)
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // Cấu hình serializer cho key và value
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Tạo đối tượng SenderOptions từ properties
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);

        // Trả về KafkaSender sử dụng trong ứng dụng
        return KafkaSender.create(senderOptions);
    }

    @Bean
    ReceiverOptions<String, String> receiverOptions() {
        Map<String, Object> propsReceiver = new HashMap<>();

        // Địa chỉ Kafka broker
        propsReceiver.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, reactiveKafkaAppProperties.bootstrapServers);
        // ID của consumer group (để Kafka theo dõi offset của consumer)
        propsReceiver.put(ConsumerConfig.GROUP_ID_CONFIG, reactiveKafkaAppProperties.consumerGroupId);
        // Cấu hình deserializer cho key và value
        propsReceiver.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsReceiver.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Trả về ReceiverOptions để dùng với KafkaReceiver
        return ReceiverOptions.create(propsReceiver);
    }
}
