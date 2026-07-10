package com.iard.kafka;

import com.iard.config.KafkaConfig;
import com.iard.event.SinistreDecideEvent;
import com.iard.service.SinistreSuiviService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer des décisions publiées par sinistre-treatment sur sinistres.decisions.
 * En cas d'échec après retries, le message part sur sinistres.decisions.dlq
 * (cf. KafkaConfig) sans interrompre la consommation des autres messages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SinistreDecisionConsumer {

    private final SinistreSuiviService sinistreSuiviService;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_SINISTRES_DECISIONS,
            containerFactory = "sinistreDecideListenerFactory")
    public void onSinistreDecide(SinistreDecideEvent event) {
        log.info("Événement SinistreDecide reçu : sinistre {} → statut {}",
                event.getNumeroSinistre(), event.getStatut());
        sinistreSuiviService.appliquerDecision(event);
    }
}
