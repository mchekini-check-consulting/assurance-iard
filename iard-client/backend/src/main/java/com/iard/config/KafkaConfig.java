package com.iard.config;

import com.iard.event.SinistreDecideEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_SINISTRES_DECLARES = "sinistres.declares";
    public static final String TOPIC_SINISTRES_DECLARES_DLQ = "sinistres.declares.dlq";
    public static final String TOPIC_SINISTRES_DECISIONS = "sinistres.decisions";
    public static final String TOPIC_SINISTRES_DECISIONS_DLQ = "sinistres.decisions.dlq";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // --- Topics ---

    @Bean
    public NewTopic sinistresDeclaresTopic() {
        return TopicBuilder.name(TOPIC_SINISTRES_DECLARES).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sinistresDeclaresDlqTopic() {
        return TopicBuilder.name(TOPIC_SINISTRES_DECLARES_DLQ).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sinistresDecisionsTopic() {
        return TopicBuilder.name(TOPIC_SINISTRES_DECISIONS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sinistresDecisionsDlqTopic() {
        return TopicBuilder.name(TOPIC_SINISTRES_DECISIONS_DLQ).partitions(1).replicas(1).build();
    }

    // --- Producer (SinistreDeclare) ---

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // Échec rapide si Kafka est indisponible : la déclaration ne doit pas être bloquée,
        // le rattrapage est assuré par SinistreEventPublisher
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10000);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // --- Consumer (SinistreDecide) ---

    @Bean
    public ConsumerFactory<String, SinistreDecideEvent> sinistreDecideConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "plateforme-souscription");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SinistreDecideEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SinistreDecideEvent> sinistreDecideListenerFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, SinistreDecideEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sinistreDecideConsumerFactory());

        // Retry (3 tentatives espacées d'1s) puis publication sur la DLQ :
        // l'échec d'un message n'interrompt pas la consommation des autres
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(TOPIC_SINISTRES_DECISIONS_DLQ, record.partition() % 1));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2)));
        return factory;
    }
}
