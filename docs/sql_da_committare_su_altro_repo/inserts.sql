-- config specificate in analisi, possono essere anche vuote
INSERT INTO config (key, value, descrizione)
VALUES ('pspID.flusso110', null, null);
INSERT INTO config (key, value, descrizione)
VALUES ('publishedGt.flusso110', null, null);
INSERT INTO config (key, value, descrizione)
VALUES ('flowDate.flusso110', null, null);
INSERT INTO config (key, value, descrizione)
VALUES ('numGiorniPublished.flusso110', null, null);

-- questo invece serve per le chiamate a pagopa
INSERT INTO subscription_config (id, codice, url, descrizione, key_primaria, key_secondaria, data_inizio_validita, data_fine_validita,
                                        utente_inserimento, utente_modifica, data_inserimento, data_modifica)
VALUES (8, 'FDR-ORG', 'https://api.uat.platform.pagopa.it/fdr-org/service/v1/', 'per il 110', null, null, '2024-02-09 14:53:07.882872', null, 'DEVELOPER', null,
        DEFAULT, DEFAULT);

-- aggiungere anche questo all'insert precedente!
UPDATE subscription_config
SET key_primaria = 'NbfE/Pkq0cp+TvUBZ4FGw0Bm3tRdY5kClzeHa3TQ2lviEV8568XHzTGhEq23ylBp'
WHERE codice = 'FDR-ORG';
