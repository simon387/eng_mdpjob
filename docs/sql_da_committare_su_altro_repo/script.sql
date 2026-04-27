-- Sequence necessaria al nuovo batch INMDJB110 per generare gli ID di flusso_riversamento
-- Da eseguire se non esiste già (es. DB locale/test dove la tabella è stata creata manualmente)
CREATE SEQUENCE IF NOT EXISTS flusso_riversamento_id_seq;

ALTER TABLE flusso_riversamento
    ADD COLUMN jsonflusso text;

COMMENT
ON COLUMN flusso_riversamento.jsonflusso IS 'Flusso di rendicontazione in formato JSON, acquisito dal nuovo batch INMDJB110';


--
--
--
--

CREATE TABLE tracciatura_acquisizione_flusso
(
    id                       integer      not null constraint tracciatura_acquisizione_flusso_pk primary key,
    identificativo_flusso    varchar(100) not null,
    revision                 integer      not null,
    organization_id          varchar(35)  not null,
    psp_id                   varchar(35),
    data_inizio_elaborazione timestamp    not null,
    data_fine_elaborazione   timestamp,
    stato                    varchar(20)  not null,
    note                     varchar(500)
);

COMMENT
ON TABLE tracciatura_acquisizione_flusso IS 'Tracciatura acquisizione flussi di rendicontazione dal nuovo batch INMDJB110';
COMMENT
ON COLUMN tracciatura_acquisizione_flusso.stato IS 'DA_ELABORARE, ELABORATO, SCARTATO';
COMMENT
ON COLUMN tracciatura_acquisizione_flusso.revision IS 'Versione/revisione del flusso pagoPA';

CREATE SEQUENCE tracciatura_acquisizione_flusso_id_seq;

ALTER TABLE tracciatura_acquisizione_flusso
    OWNER TO mdpnew;

GRANT DELETE, INSERT, REFERENCES, SELECT, TRIGGER, TRUNCATE, UPDATE ON tracciatura_acquisizione_flusso TO public;
GRANT DELETE, INSERT, SELECT, TRIGGER, UPDATE ON tracciatura_acquisizione_flusso TO mdpnew_rw;

--
--
--
--

CREATE TABLE tracciatura_singola_acquisizione
(
    id            integer   not null constraint tracciatura_singola_acquisizione_pk primary key,
    id_flusso     integer   not null constraint fk_tracciatura_singola_acquisizione_flusso
            references tracciatura_acquisizione_flusso
            on update restrict on delete restrict,
    num_pagamenti integer,
    data_inizio   timestamp not null,
    data_fine     timestamp,
    esito         varchar(2),
    response      text,
    descrizione   varchar(500)
);

COMMENT
ON TABLE tracciatura_singola_acquisizione IS 'Tracciatura acquisizione pagamenti per singolo flusso - batch INMDJB110';
COMMENT
ON COLUMN tracciatura_singola_acquisizione.esito IS 'OK o KO';
COMMENT
ON COLUMN tracciatura_singola_acquisizione.response IS 'Response JSON completa (JSON_TESTATA+JSON_PAYMENTS) in caso di scarto';
COMMENT
ON COLUMN tracciatura_singola_acquisizione.num_pagamenti IS 'Somma dei count pagamenti dichiarati da pagoPA';

CREATE SEQUENCE tracciatura_singola_acquisizione_id_seq;

ALTER TABLE tracciatura_singola_acquisizione
    OWNER TO mdpnew;

GRANT DELETE, INSERT, REFERENCES, SELECT, TRIGGER, TRUNCATE, UPDATE ON tracciatura_singola_acquisizione TO public;
GRANT DELETE, INSERT, SELECT, TRIGGER, UPDATE ON tracciatura_singola_acquisizione TO mdpnew_rw;

-- campo in più necessario
ALTER TABLE flusso_singolo_pagamento
    ADD COLUMN cod_versamento varchar(4);

COMMENT ON COLUMN flusso_singolo_pagamento.cod_versamento
    IS 'Codice versamento recuperato da iuv_ottici al momento dell''acquisizione del flusso';

