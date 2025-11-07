# Návod na vybudovanie a inštaláciu IJ_HttpClient

## Systémové požiadavky

### Povinné
- **Java Development Kit (JDK)**: 17 alebo vyššia
- **Operačný systém**: Windows, macOS, alebo Linux
- **Pamäť**: Minimálne 4 GB RAM (odporúčané 8 GB)
- **Disk**: Minimálne 2 GB voľného miesta

### Odporúčané
- **IntelliJ IDEA**: 2024.3 alebo kompatibilná verzia (build 230-252.*)
- **Git**: Pre klonovanie repozitára

## Krok 1: Získanie zdrojového kódu

### Klonovanie repozitára
```bash
git clone https://github.com/jufeng98/IJ_HttpClient.git
cd IJ_HttpClient
```

### Alternatívne: Stiahnuť ako ZIP
1. Navštívte: https://github.com/jufeng98/IJ_HttpClient
2. Kliknite na "Code" → "Download ZIP"
3. Rozbaľte archív
4. Otvorte terminál v rozbalenom adresári

## Krok 2: Overenie Java verzie

```bash
java -version
```

**Očakávaný výstup:** Java 17 alebo vyššia
```
java version "17.0.x" ...
```

Ak nemáte Java 17, stiahnite si ju z:
- OpenJDK: https://adoptium.net/
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/

## Krok 3: Build projektu

Projekt používa Gradle wrapper, ktorý automaticky stiahne správnu verziu Gradle.

### Linux / macOS
```bash
./gradlew build
```

### Windows
```cmd
gradlew.bat build
```

### Proces buildu
Build proces vykoná:
1. Stiahnutie závislostí z Maven repozitárov
2. Kompilácia Java a Kotlin kódu (target: Java 17)
3. Spustenie unit testov
4. Generovanie JAR súboru s pluginom

### Výstup buildu
Po úspešnom builde nájdete plugin v:
```
build/distributions/IJ_HttpClient-5.8.4.zip
```

## Krok 4: Inštalácia pluginu

### Metóda A: Inštalácia zo ZIP súboru (Odporúčané)

1. Otvorte IntelliJ IDEA
2. Prejdite na: **File → Settings** (Windows/Linux) alebo **IntelliJ IDEA → Preferences** (macOS)
3. V ľavom menu vyberte: **Plugins**
4. Kliknite na ikonu ⚙️ (ozubené koleso) → **Install Plugin from Disk...**
5. Vyberte súbor: `build/distributions/IJ_HttpClient-5.8.4.zip`
6. Kliknite **OK**
7. Reštartujte IntelliJ IDEA

### Metóda B: Spustenie vývojovej inštancie

Pre testovanie pluginu vo vývojovom režime:

```bash
./gradlew runIde
```

Tento príkaz:
- Spustí novú inštanciu IntelliJ IDEA s nainštalovaným pluginom
- Umožňuje testovanie bez inštalácie do produkčnej IDEA
- Podporuje hot-reload (v tomto projekte vypnuté)

## Krok 5: Overenie inštalácie

Po reštarte IntelliJ IDEA:

1. Otvorte **Settings → Plugins**
2. V sekcii **Installed** vyhľadajte "HttpClient" alebo "IJ_HttpClient"
3. Plugin by mal byť aktívny ✅

### Test funkcionality
1. Vytvorte nový súbor s príponou `.http`
2. Napíšte: `gtr` alebo `ptr` a stlačte Tab (live template)
3. Mala by sa vygenerovať HTTP request šablóna
4. Kliknite na tlačidlo ▶️ (Run) v ľavej časti editora

## Dodatočné úlohy (voliteľné)

### Spustenie testov
```bash
./gradlew test
```

### Čistenie build artefaktov
```bash
./gradlew clean
```

### Generovanie distribúcie bez testovania
```bash
./gradlew build -x test
```

### Zobrazenie všetkých dostupných taskov
```bash
./gradlew tasks
```

## Podpisovanie pluginu (pre publikáciu)

Ak chcete podpísať plugin pre publikáciu do JetBrains Marketplace:

### Nastavte environment premenné:
```bash
export CERTIFICATE_CHAIN="<váš certifikát>"
export PRIVATE_KEY="<váš privátny kľúč>"
export PRIVATE_KEY_PASSWORD="<heslo k privátnej kľúču>"
```

### Spustite signing task:
```bash
./gradlew signPlugin
```

## Publikovanie pluginu (pre správcov)

### Nastavte publikačný token:
```bash
export PUBLISH_TOKEN="<váš JetBrains token>"
```

### Publikujte plugin:
```bash
./gradlew publishPlugin
```

## Riešenie problémov

### Problém: "Permission denied" pri spustení gradlew

**Riešenie (Linux/macOS):**
```bash
chmod +x gradlew
./gradlew build
```

### Problém: Build zlyháva kvôli sieťovým problémom

**Riešenie:** Projekt je nakonfigurovaný s Aliyun Maven mirror. Ak ste mimo Číny:
1. Otvorte `build.gradle.kts`
2. Zmeňte poradie repozitárov (dajte `mavenCentral()` na prvé miesto)

### Problém: Java verzia nie je 17

**Riešenie:**
```bash
# Skontrolujte JAVA_HOME
echo $JAVA_HOME  # Linux/macOS
echo %JAVA_HOME% # Windows

# Nastavte správnu verziu
export JAVA_HOME=/path/to/jdk-17  # Linux/macOS
set JAVA_HOME=C:\path\to\jdk-17   # Windows
```

### Problém: Plugin sa nenačíta po inštalácii

**Riešenie:**
1. Skontrolujte kompatibilitu IDEA verzie (musí byť 230-252.*)
2. Skontrolujte logy: **Help → Show Log in Files/Finder**
3. Hľadajte chyby súvisiace s "HttpClient" alebo "IJ_HttpClient"
4. Skúste plugin preinštalovať

### Problém: JavaScript syntax highlighting nefunguje

**Riešenie:**
1. Nainštalujte WebCalm plugin: **Settings → Plugins → Marketplace → "WebCalm"**
2. Reštartujte IntelliJ IDEA
3. JavaScript handlery by mali mať správne zvýraznenie syntaxe

## Podpora a kontakt

- **Autor:** Liang Yu Dong
- **Email:** 375709770@qq.com
- **GitHub:** https://github.com/jufeng98
- **Autor blog:** [Zhihu](https://www.zhihu.com/people/liang-yu-dong-44)

Pri reportovaní problémov uveďte:
- Verziu IntelliJ IDEA
- Verziu pluginu
- Operačný systém
- Logy chýb

## Ďalšie kroky

Po úspešnej inštalácii odporúčame:
1. Prečítať si [README.md](README.md) pre úplný zoznam funkcií
2. Pozrieť si príklady v priečinku `images/`
3. Vytvoriť testovací `.http` súbor a vyskúšať základné requesty
4. Nakonfigurovať environment premenné pre vaše projekty

---
*Verzia dokumentu: 1.0*
*Posledná aktualizácia: 2025-11-07*
