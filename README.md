# mdpjob

## general info

+ deve produrre un jar (per ora produce mdpjob-1.0.0.jar)
+ log in formato json!
+ stessa struttura per il deploy di mdpetl, jar dentro tar e struttura non esplosa dei .class
+ va sul nuovo DB!

## Elenco job presenti:

1. INMDJB110
2. INMDJB520

## link utili

+ flussi da pagaopa: https://api.uat.platform.pagopa.it/fdr-org/service/v1/organizations/80087670016/fdrs?size=5&page=5

## maven

+ ~~Produce target/mdpjob-1.0.0.jar eseguibile con: `java -jar target/mdpjob-1.0.0.jar arg0 arg1 arg2`~~
+ I tre transformer sono tutti necessari: il primo imposta il manifest con la main class, il secondo e il terzo servono per CXF che altrimenti a runtime non trova i suoi servizi interni e lancia eccezioni all'avvio del client SOAP.
+ aggiunti i profile, ricordarsi di usare .local su pc locale

### update

Con `mvn package` otterrai in target/:

target/
├── mdpjob-1.0.0.jar
└── mdpjob-1.0.0.tar ← tar con dentro "mdpjob.jar" MA nella struttura log/config/lib

### nuovo update

ora si produce una struttura che immagino sarà eseguita così

`cd mdpjob-int-01/mdpjob`
`java -Dlogback.configurationFile=config/logback.xml -jar lib/mdpjob.jar`

## TODOS

+ ~~Inserire il record FDR-ORG in subscription_config con url e key cifrata per ogni ambiente~~
+ ~~Inserire i record vuoti in config per i parametri pspID.flusso110, publishedGt.flusso110, flowDate.flusso110, numGiorniPublished.flusso110~~ -> se non presenti va uguale
+ ~~Eseguire le DDL delle due nuove tabelle e la ALTER TABLE per jsonflusso~~
+ ~~Aggiungere il jar mdpnew-internal al pom del nuovo progetto (per Serviziorissrvspc e compagni)~~
+ Configurare il mail.properties nel nuovo progetto (stessa struttura del vecchio)
+ ~~nuova struttura tar come mdpetl~~
+ ~~better logs, in formato json~~

## bugfixing

+ `Test-NetConnection -ComputerName 10.138.167.218 -Port 5432` --> fallisce? per forza, non c'è il tunnel!

Confermato: la porta 5432 non è raggiungibile direttamente dalla tua macchina. IntelliJ si connette tramite SSH tunnel.

Per far funzionare il codice Java devi fare la stessa cosa: aprire un tunnel SSH manualmente prima di lanciare il batch.

Da terminale (con la VPN attiva):

`ssh -L 5433:10.138.167.218:5432 <tuo_utente>@<jump_host>`

Poi nel connection.properties cambia la URL per puntare al tunnel locale:

`url=jdbc:postgresql://localhost:5433/TEST`

Il <jump_host> lo trovi nelle impostazioni SSH della data source di IntelliJ: Data Source → SSH/SSL tab → Host.

caso di Simone:

`ssh -L 5433:10.138.167.218:5432 73863@cmpto2-gw02.site02.nivolapiemonte.it`
