# oereb-cts

## (todo)
- Tests (ohne Server). Tests sind inexistent jetzt.
- xslt: `java -jar /Users/stefan/apps/SaxonHE10-6J/saxon-he-10.6.jar -s:result.xml -xsl:xml2html.xsl -o:result.html`
- /url/-Response nicht als xml speichern.
- picocli?

## Beschreibung

Oereb-CTS ist eine Kommandozeilentool zur Prüfung einiger Funktionen eines ÖREB-Katastersystems.

Es werden sowohl Service- wie auch Dokumentenaspekte validiert.

- GetEGRID:
  * HTTP Status Code
  * Response Content Type
  * Schemakonformität
  * Geometrielemente gemäss Anfrage
- Extract:
  * siehe GetEGRID
  * Eingebette Bilder gemäss Anfrage
  * Sind alle Bundesthemen im ToC enthalten?

## Komponenten

Das Unterprojekt `lib` beinhaltet die eigentlichen Prüfroutinen. Das Unterprojekt `app` die CLI-Anwendung und die Persistierung eines Resultate-Pojo in einer XML-Datei.

## Benutzung

```
java -jar ...
```

Parameter:

- `config`: Die Konfigurationsdatei mit Angaben des Serverendpunktes, Koordinaten, EGRID etc.
- `out`: Verzeichnis in welches die Resultate-Datei gespeichert wird. Fehlt die Angabe, wird das tmp-Verzeichnis des Betriebssystems verwendet.

Aufbau der Config-Datei:

```
[SO]
SERVICE_ENDPOINT=https://geo.so.ch/api/oereb/
EN=2600595,1215629
IDENTDN=SO0200002457
NUMBER=168
EGRID=CH807306583219
```

Die Prüfungen basieren auf einer Service-Request-URL. Diese Requests sind einem Template  parametrisiert. Es wird versucht aus der Config-Datei alle für die jeweilige Prüfung Parameter zu ersetzen. Gelingt dies, wird der Test durchgeführt. Gelingt dies nicht, wird der Test nicht durchgeführt. Mit diesem Ansatz können jedoch für den GetEGRID-Request keine anderen Werte für `IDENTDN` und `NUMBER` als für den Extract-Request verwendet. Man müsste eine weitere Gruppe in der Config-Datei einführen (z.B. `[SO2]`).

## Konfigurieren und starten

Siehe Benutzung.

## Externe Abhängigkeiten

Es wird eine Java-Runtime Version 17 oder höher benötigt.

## Interne Struktur

- Probes
- Wrapper (warum? mühsam von Hand)
- Results
