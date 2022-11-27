# oereb-cts

## Beschreibung

Oereb-CTS ist eine Kommandozeilentool zur Prüfung einiger Funktionen eines ÖREB-Katastersystems. Das Resultat sind eine HTML- und XML-Datei und sämtlichen Antworten-XML des Services.

Es werden sowohl Service- wie auch Dokumentenaspekte validiert.

- GetEGRID:
  * HTTP Status Code
  * Response Content Type
  * Schemakonformität
  * Geometrielemente gemäss Anfrage
- Extract:
  * siehe GetEGRID
  * Eingebettete Bilder gemäss Anfrage
  * Sind alle Bundesthemen im ToC enthalten?

## Komponenten

Das Unterprojekt `lib` beinhaltet die eigentlichen Prüfroutinen. Das Unterprojekt `app` die CLI-Anwendung und die Persistierung eines Resultate-Pojo in einer XML-Datei.

## Benutzung

### JVM

```
./bin/oereb-cts --help
```

### Native Image

```
./oereb-cts --help
```

Parameter:

- `config`: Die Konfigurationsdatei mit Angaben des Serverendpunktes, Koordinaten, EGRID etc.
- `out`: Verzeichnis in welches die Resultate-Datei gespeichert wird. Fehlt die Angabe, wird das tmp-Verzeichnis des Betriebssystems verwendet.

Aufbau der Config-Datei (*.ini):

```
[SO]
SERVICE_ENDPOINT=https://geo.so.ch/api/oereb/
EN=2600595,1215629
IDENTDN=SO0200002457
NUMBER=168
EGRID=CH807306583219
```

Vollständiger Aufruf:

```
./oereb-cts --config config.ini --out C:\Users\dummy\tmp\
```


Die Prüfungen benötigen ein Service-Request-URL. Diese Requests sind ein String-Template  parametrisiert. Es wird versucht aus der Config-Datei alle für die jeweilige Prüfung Parameter zu ersetzen. Gelingt dies, wird der Test durchgeführt. Gelingt dies nicht, wird der Test nicht durchgeführt. Mit diesem Ansatz können jedoch für den GetEGRID-Request keine anderen Werte für `IDENTDN` und `NUMBER` als für den Extract-Request verwendet. Man müsste eine weitere Gruppe in der Config-Datei einführen (z.B. `[SO2]`).

### Logging

```
-Djdk.httpclient.HttpClient.log=errors,requests,headers,frames[:control:data:window:all],content,ssl,trace,channel,all
```

```
java -Djdk.httpclient.HttpClient.log=requests,headers,frames:all,content,ssl,trace,channel,all -jar app/build/libs/oereb-cts-app-0.0.LOCALBUILD-all.jar --config ../../tmp/config-all.ini --out ../../tmp/oerebcts 2>&1 | tee debug.log
```

## Konfigurieren und starten

Siehe Benutzung.

## Externe Abhängigkeiten

Es wird eine Java-Runtime Version 17 oder höher benötigt.

## Interne Struktur

- Probes
- Wrapper (warum? mühsam von Hand)
- Results

### xpath in basex

```
declare namespace geom="http://www.interlis.ch/geometry/1.0";
count(//geom:coord[./geom:c1 < 2400000 or ./geom:c1 > 2900000 or ./geom:c2 < 1070000 or ./geom:c2 > 1300000])
```


### GraalVM

Config für Native Image wegen xpath:

```
./gradlew app:shadowJar
cd app
java -agentlib:native-image-agent=config-output-dir=conf-dir -jar build/libs/oereb-cts-app-0.0.LOCALBUILD-all.jar --config config.ini
```

Fatjar wird benötigt, weil die die Distribution mittels Shellskript aufgerufen wird und nicht mittels `java -jar ...`.

## (todo)
- Tests (ohne Server). Tests sind inexistent jetzt.
- xslt: `java -jar /Users/stefan/apps/SaxonHE10-6J/saxon-he-10.6.jar -s:result.xml -xsl:xml2html.xsl -o:result.html`
- /url/-Response nicht als xml speichern.
- picocli?

