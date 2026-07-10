package com.iard.sinistre.kafka;

import com.iard.sinistre.config.KafkaConfig;
import com.iard.sinistre.event.SinistreDeclareEvent;
import com.iard.sinistre.service.DossierSinistreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer des déclarations publiées par la plateforme de souscription sur sinistres.declares.
 * En cas d'échec après retries, le message part sur sinistres.declares.dlq
 * (cf. KafkaConfig) sans interrompre la consommation des autres messages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SinistreDeclareConsumer {

    private final DossierSinistreService dossierSinistreService;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_SINISTRES_DECLARES,
            containerFactory = "sinistreDeclareListenerFactory")
    public void onSinistreDeclare(SinistreDeclareEvent event) {
        log.info("Événement SinistreDeclare reçu : {} (contrat {})",
                event.getNumeroSinistre(), event.getNumeroContrat());
        dossierSinistreService.enregistrerSinistre(event);
    }
}
