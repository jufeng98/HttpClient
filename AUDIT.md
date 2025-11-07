# Audit výsledky projektu IJ_HttpClient

## Dátum auditu
2025-11-07

## Prehľad projektu
**Názov:** IJ_HttpClient
**Verzia:** 5.8.4
**Typ:** IntelliJ IDEA Plugin
**Jazyk:** Java, Kotlin
**Licencia:** LICENSE (súbor prítomný v repozitári)

## Účel projektu
Plugin pre IntelliJ IDEA poskytujúci funkcionalitu HTTP/WebSocket/Dubbo klienta priamo v editore kódu.

## Hlavné funkcie
- Podpora HTTP requestov (GET, POST, atď.)
- Podpora WebSocket requestov
- Podpora Dubbo requestov
- Podpora environment premenných a vstavaných metód
- Navigácia na SpringMVC Controller metódy z URL
- Zobrazenie informácií o SpringMVC Controller metódach pri hoveri
- JavaScript pre-procesory, post-procesory a globálne handlery
- Čítanie súborov ako HTTP request body
- Ukladanie HTTP response do súboru
- Náhľad obrázkov, HTML a PDF odpovedí
- Vyhľadávanie SpringMVC API v SearchEverywhere dialógu
- Mock Server funkcionalita

## Technická infraštruktúra

### Build systém
- **Gradle**: 8.x (Gradle wrapper prítomný)
- **Build súbor**: `build.gradle.kts` (Kotlin DSL)
- **Kotlin verzia**: 1.9.25
- **IntelliJ Platform Plugin**: 2.3.0

### Závislosti

#### IntelliJ Platform
- IntelliJ Community Edition 2024.3
- Bundled pluginy: `com.intellij.java`, `com.intellij.modules.json`
- External plugin: `ris58h.webcalm:0.12` (pre JavaScript syntax highlighting)

#### Knižnice
- `org.mozilla:rhino:1.7.15` - JavaScript engine
- `com.github.javafaker:javafaker:1.0.2` - Generovanie fake dát
- `com.jayway.jsonpath:json-path:2.9.0` - JSON path operácie
- `com.alibaba:dubbo:2.6.12` - Dubbo framework podpora

#### Test závislosti
- `junit:junit:4.13.1` - Unit testing framework

### Kompatibilita
- **Java verzia**: 17 (source & target)
- **IntelliJ IDEA Build Range**: 230 - 252.*
- **Kódovanie**: UTF-8

## 🔒 Hlbková bezpečnostná analýza

### 📊 Štatistický prehľad
- **Celkový počet súborov**: 245 Kotlin + 23 Java
- **Riadky kódu**: ~18,265 (Kotlin)
- **Externé volania**: 2 typy (npm, maven)
- **HTTP requesty**: Všetky kontrolované používateľom
- **Telemetria**: ❌ ŽIADNA
- **Analytics**: ❌ ŽIADNE
- **Tracking**: ❌ ŽIADNY

### ⚠️ KRITICKÉ BEZPEČNOSTNÉ ZISTENIA

#### 🔴 1. SŤAHOVANIE EXTERNÝCH JAVASCRIPT BALÍČKOV

**Súbor**: `src/main/kotlin/org/javamaster/httpclient/dashboard/support/JsTgz.kt:86-147`

**Riziko**: 🔴 **VYSOKÉ**

Plugin aktívne sťahuje JavaScript balíčky z externých URL pomocou direktívy `@require`:

```http
# Príklad z requests-with-scripts.http:
# @require https://registry.npmmirror.com/moment/-/moment-2.30.1.tgz
# @require https://registry.npmmirror.com/lodash/-/lodash-4.17.21.tgz
```

**Čo sa deje**:
1. Plugin otvára spojenie na URL pomocou `url.openStream()` (riadok 112)
2. Stiahne `.tgz` súbory a uloží ich do `lib/jsLib/` priečinka pluginu
3. Rozbalí tieto súbory pomocou `TgzExtractor.extract()`
4. Vykoná JavaScript kód z týchto balíčkov v kontexte pluginu

**Bezpečnostné implikácie**:
- ✅ URL sú špecifikované používateľom v `.http` súboroch (nie hardcoded)
- ✅ Používateľ musí manuálne vytvoriť `.http` súbor s `@require` direktívou
- ⚠️ **Žiadna verifikácia integrity** stiahnutých súborov (checksum/hash)
- ⚠️ **Žiadne obmedzenia** na konkrétne domény
- ⚠️ Stiahnutý JavaScript sa vykonáva v kontexte Rhino engine

**Odporúčania**:
1. Pridať SHA-256/SHA-512 checksum verifikáciu
2. Obmedziť povolené domény (whitelist)
3. Pridať používateľské potvrdenie pred stiahnutím
4. Implementovať cache integrity checks

---

#### 🔴 2. SŤAHOVANIE DUBBO JAR SÚBOROV

**Súbor**: `src/main/kotlin/org/javamaster/httpclient/dubbo/support/DubboJars.kt:64-133`

**Riziko**: 🔴 **VYSOKÉ**

Plugin sťahuje Java JAR súbory z Aliyun Maven repository:

**Hardcoded URL**:
```kotlin
private const val REPOSITORY_URL = "https://maven.aliyun.com/nexus/content/groups/public"
```

**Sťahované JAR súbory**:
- `javassist-3.30.2-GA.jar` (bytecode manipulation)
- `curator-client-4.0.1.jar` (ZooKeeper client)
- `curator-framework-4.0.1.jar` (ZooKeeper framework)
- `netty-3.10.5.Final.jar` (networking) ⚠️ **STARÁ VERZIA**
- `zookeeper-3.5.3-beta.jar` (distributed coordination)

**Bezpečnostné implikácie**:
- ✅ URL je hardcoded (nie modifikovateľné používateľom)
- ✅ Používa známy Maven repository (Aliyun)
- ⚠️ **Žiadna verifikácia integrity** (SHA checksum)
- ⚠️ **Žiadne overenie podpisu** JAR súborov
- ⚠️ JAR súbory sa načítajú cez vlastný `DubboClassLoader`
- 🔴 **Stará verzia Netty (3.10.5)** - potenciálne CVE zraniteľnosti

**Odporúčania**:
1. Pridať Maven checksum verifikáciu (SHA1/MD5)
2. Overiť JAR podpisy (signature verification)
3. **AKTUALIZOVAŤ Netty** na novšiu verziu (bezpečnostné záplaty)
4. Pridať fallback mirror URLs

---

#### 🟡 3. VYKONÁVANIE JAVASCRIPT KÓDU

**Súbor**: `src/main/kotlin/org/javamaster/httpclient/js/JsExecutor.kt:54-58`

**Riziko**: 🟡 **STREDNÉ**

Plugin používa Mozilla Rhino engine na vykonávanie JavaScript kódu z `.http` súborov.

**JavaScript má prístup k**:
```javascript
// Prístupné v JavaScript kontexte:
System.getProperties()  // Všetky systémové vlastnosti
System.getenv()         // Všetky environment premenné
request.body            // Telo requestu
request.headers         // HTTP hlavičky
response.body           // Telo odpovede
client.global           // Globálne premenné
```

**Bezpečnostné implikácie**:
- ⚠️ JS kód má prístup k **systémovým premenným**
- ⚠️ JS kód sa vykonáva s **privilégiami pluginu**
- ✅ Sandboxované cez Mozilla Rhino (bezpečnejšie ako priame Java volania)
- ✅ Používateľ musí explicitne vytvoriť `.http` súbor s JavaScript

**Odporúčania**:
1. Obmedziť prístup k `System.getProperties()/getenv()`
2. Pridať whitelist povolených properties
3. Sandbox obmedzenia pre file system prístup
4. Security warning pre používateľov

---

#### 🟢 4. HTTP REQUESTY - LEGITÍMNE

**Súbor**: `src/main/kotlin/org/javamaster/httpclient/dashboard/HttpProcessHandler.kt:539`

**Riziko**: 🟢 **NÍZKE**

Všetky HTTP/WebSocket/Dubbo requesty sú:
- ✅ Iniciované používateľom
- ✅ URL špecifikované v `.http` súboroch
- ✅ Používa štandardné Java HttpClient API
- ✅ **Žiadne skryté/automatické pripojenia** na backend

---

### 🔍 ZÁVISLOSTI A EXTERNÉ SLUŽBY

#### Maven závislosti (build.gradle.kts)
```kotlin
- org.mozilla:rhino:1.7.15          // JavaScript engine
- com.github.javafaker:javafaker:1.0.2  // Fake data generator
- com.jayway.jsonpath:json-path:2.9.0   // JSON query
- com.alibaba:dubbo:2.6.12          // Dubbo RPC framework (⚠️ STARÁ VERZIA)
```

#### Používané externé služby

**1. Aliyun Maven Repository**
- URL: `https://maven.aliyun.com/nexus/content/groups/public`
- Účel: Sťahovanie Dubbo JAR súborov
- Automatické: ✅ Áno (pri prvom použití Dubbo funkcií)

**2. NPM Mirror Registry**
- URL: `https://registry.npmmirror.com/...`
- Účel: Sťahovanie JavaScript balíčkov
- Automatické: ❌ Nie (len ak používateľ pridá `@require` direktívu)

**3. Používateľom špecifikované URL**
- URL: Ľubovoľné (HTTP/HTTPS/WS)
- Účel: Vykonávanie HTTP requestov
- Automatické: ❌ Nie (explicitne definované v `.http` súboroch)

---

### ✅ BEZPEČNOSTNÉ POZITÍVA

- ✅ **Žiadne skryté telemetrické spojenia** - Plugin neodosiela dáta o používateľovi nikam
- ✅ **Žiadne analytics/tracking** - Žiadny kód na sledovanie používateľov
- ✅ **Open source** - Celý kód je transparentný
- ✅ **Lokálne vykonávanie** - Všetky operácie sú lokálne
- ✅ **Používateľská kontrola** - Všetky requesty sú explicitné
- ✅ **Žiadne hardcoded API keys/credentials**
- ✅ Gradle wrapper prítomný (reprodukovateľné buildy)
- ✅ Použitie moderných verzií Kotlin (1.9.25)
- ✅ Java 17 (LTS verzia)
- ✅ UTF-8 kódovanie nastavené explicitne

---

### ⚠️ BEZPEČNOSTNÉ UPOZORNENIA A ZRANITEĽNOSTI

#### Vysoká priorita (🔴)
1. **Netty 3.10.5** - Stará verzia s potenciálnymi CVE zraniteľnosťami
2. **Žiadna integrity verification** pre sťahované súbory (JAR, TGZ)
3. **Dubbo 2.6.12** - Stará verzia (posledná 2.6.x verzia, odporúča sa upgrade na 3.x)

#### Stredná priorita (🟡)
4. **JavaScript prístup k systémovým premenným** - Potenciálny leak citlivých dát
5. **Žiadne domain whitelisting** pre `@require` direktívu
6. **JUnit 4.13.1** - Zastaraná verzia (odporúča sa upgrade na JUnit 5)
7. **Rhino 1.7.15** - Mozilla Rhino je v maintenance móde (zvážiť GraalVM JavaScript)

#### Nízka priorita (🟢)
8. Prítomná anotácia `@file:Suppress("VulnerableLibrariesLocal")` v build.gradle.kts
9. Žiadne JAR signature verification

---

### 🔐 KONFIGUROVANÉ CITLIVÉ ÚDAJE

Plugin podporuje podpisovanie a publikovanie cez environment premenné:
- `CERTIFICATE_CHAIN` - certifikačná reťaz
- `PRIVATE_KEY` - súkromný kľúč
- `PRIVATE_KEY_PASSWORD` - heslo k súkromnému kľúču
- `PUBLISH_TOKEN` - publikačný token

**Odporúčanie**: ✅ Tieto údaje sú správne načítané z environment premenných (nie hardcoded). Nikdy ich necommitovať do repozitára.

## Štruktúra projektu

```
IJ_HttpClient/
├── .git/
├── .gitignore
├── LICENSE
├── README.md
├── build.gradle.kts
├── gradle/
├── gradle.properties
├── gradlew
├── gradlew.bat
├── images/           # Dokumentačné obrázky
├── settings.gradle.kts
└── src/
    ├── main/
    │   └── gen/      # Generované súbory
    └── test/
```

## Kvalita kódu

### Konfigurácia
- Duplicity stratégia v JAR: `EXCLUDE` (kvôli dvojitému kompilovaniu Kotlin súborov)
- AutoReload vypnutý pre `runIde` task

### Dokumentácia
✅ README.md prítomné (bilingválne: Čínština/Angličtina)
✅ Príklady použitia so screenshotmi
✅ Kontaktné informácie autora

## Repozitáre
- Primárny: Maven Aliyun mirror
- Sekundárny: Maven Local
- Terciárny: Maven Central
- IntelliJ Platform: Default repositories

## Testovanie
- Test framework: IntelliJ Platform Test Framework
- Unit testy: JUnit 4.13.1
- Test source set: prítomný

## 📋 Odporúčania na zlepšenie

### 🔴 Kritická priorita (Bezpečnosť)
1. **AKTUALIZOVAŤ Netty** z 3.10.5.Final na najnovšiu verziu (4.x alebo 5.x)
   - Skontrolovať CVE databázu pre verziu 3.10.5
   - Otestovať kompatibilitu s Dubbo

2. **Implementovať integrity verification** pre sťahované súbory
   - Pridať SHA-256/SHA-512 checksum pre JavaScript balíčky (`@require`)
   - Pridať Maven checksum verifikáciu (SHA1/MD5) pre Dubbo JAR súbory
   - Implementovať JAR signature verification

3. **Obmedziť JavaScript sandbox**
   - Obmedziť prístup k `System.getProperties()/getenv()`
   - Implementovať whitelist povolených system properties
   - Pridať file system access restrictions

### 🟡 Vysoká priorita
4. **Aktualizovať Dubbo** z 2.6.12 na 3.x
   - Preveriť breaking changes
   - Aktualizovať závislé komponenty (ZooKeeper, Curator)

5. **Pridať domain whitelisting** pre `@require` direktívu
   - Obmedziť na dôveryhodné NPM mirrors
   - Pridať používateľské potvrdenie pre neznáme domény

6. **Aktualizovať JUnit** z 4.13.1 na JUnit 5 (Jupiter)
   - Migrovať existujúce testy
   - Využiť moderné testing features

7. **Odstrániť suppression** `VulnerableLibrariesLocal` po riešení zraniteľností

### 🟢 Stredná priorita
8. Zvážiť upgrade Rhino na GraalVM JavaScript engine (lepší výkon a bezpečnosť)
9. Pridať CI/CD pipeline konfiguráciu (GitHub Actions, GitLab CI)
10. Implementovať automated security scanning (Dependabot, Snyk)
11. Pridať code coverage reporting
12. Pridať static code analysis (SonarQube, Detekt, SpotBugs)

### ⚪ Nízka priorita
13. Pridať CHANGELOG.md pre sledovanie zmien medzi verziami
14. Rozšíriť dokumentáciu o developer guide
15. Pridať contributing guidelines
16. Implementovať security policy (SECURITY.md)

---

## 🎯 FINÁLNE BEZPEČNOSTNÉ ZHODNOTENIE

### Je tento plugin bezpečný?

**ÁNO, S VÝHRADAMI** ✅⚠️

### Plugin NEODOSIELA:
✅ Žiadne dáta o používateľovi
✅ Žiadnu telemetriu
✅ Žiadne tracking informácie
✅ Žiadne credentials nikam na backend

### Plugin ODOSIELA (s vedomím používateľa):
⚠️ HTTP/WebSocket requesty na URL špecifikované v `.http` súboroch
⚠️ Automatické sťahovanie Dubbo JAR súborov z Aliyun Maven (pri prvom použití)
⚠️ Sťahovanie npm balíčkov (len ak používateľ pridá `@require` direktívu)

### Odporúčania pre používateľov:
✅ Plugin je **bezpečný na používanie** pre bežnú prácu
⚠️ Buďte **opatrní s `@require` direktívou** - overujte URL
⚠️ **Nezdieľajte `.http` súbory** s neznámym JavaScriptom
⚠️ Používajte len **dôveryhodné npm URL**
✅ Dubbo sťahovanie je relatívne bezpečné (Aliyun Maven je dôveryhodný)

### Bezpečnostné skóre: **7/10** ⭐⭐⭐⭐⭐⭐⭐

**Hlavné dôvody zrážky:**
- Chýbajúca integrity verification pre sťahované súbory
- Prístup JavaScript k systémovým premenným
- Stará verzia Netty s potenciálnymi CVE zraniteľnosťami
- Žiadne domain whitelisting pre externé balíčky

---

## 📝 Záver

Tento **IntelliJ HttpClient plugin je legitímny vývojársky nástroj** bez malicious funkcionality. Plugin **NEODOSIELA žiadne dáta o používateľovi** na externé servery bez jeho vedomia.

### Technické hodnotenie
✅ Projekt je funkčný IntelliJ IDEA plugin s **bohatou funkčnosťou**
✅ Build konfigurácia je korektná a používa **moderné verzie build nástrojov**
✅ Kód je **čistý, dobre štruktúrovaný** a transparentný
✅ **Žiadne malware, špionážne funkcie ani skryté telemetrie**

### Bezpečnostné hodnotenie
⚠️ Hlavné bezpečnostné riziká súvisia s:
- Nedostatočnou verifikáciou integrity sťahovaných súborov
- Prístupom JavaScript kódu k systémovým premenným
- Zastaranými verziami závislostí (Netty, Dubbo, JUnit)

✅ Všetky sieťové operácie sú buď explicitne kontrolované používateľom (HTTP requesty) alebo sú zrejmé a dobre zdokumentované (Dubbo JAR sťahovanie, npm balíčky).

### Odporúčanie
**ODPORÚČAME POUŽÍVANIE** s uvedomením si bezpečnostných aspektov. Pre produkčné prostredie odporúčame implementovať kritické bezpečnostné vylepšenia (najmä integrity verification a aktualizáciu Netty).

**Celkové hodnotenie**: ⭐⭐⭐⭐ (4/5)
**Bezpečnostné hodnotenie**: ⭐⭐⭐⭐⭐⭐⭐ (7/10)

---
*Audit vykonal: Claude AI*
*Dátum: 2025-11-07*
