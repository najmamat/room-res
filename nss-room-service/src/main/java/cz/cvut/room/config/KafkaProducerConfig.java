package cz.cvut.room.config;

import cz.cvut.room.util.Constants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                Constants.KAFKA_BOOTSTRAP_ADDRESS);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /*
    @Bean
    public KafkaTemplate<String, Room> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }*/
    // Listener Container to be set up in ReplyingKafkaTemplate

    @Bean
    public KafkaTemplate<String, String> replyTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }
}