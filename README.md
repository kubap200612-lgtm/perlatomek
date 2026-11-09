# Pearl Trail (Fabric 1.21.4)

Smuga z fioletowych cząsteczek teleportacji endermana za rzuconą perłą kresu – od rzutu do miejsca uderzenia.
Mod tylko po stronie klienta (działa też na serwerach bez moda).

## Jak zbudować przez GitHub
1. Utwórz nowe repozytorium na GitHubie i wrzuć do niego całą zawartość tego folderu (razem z folderem `.github`).
2. Wejdź w zakładkę **Actions** – build odpali się sam (albo kliknij *Build → Run workflow*).
3. Po ~3 minutach otwórz zakończony build, na dole w **Artifacts** pobierz `pearl-trail`.
4. Rozpakuj zip i wrzuć `pearl-trail-1.0.0.jar` (bez `-sources`) do `.minecraft/mods` razem z Fabric API.

## Ustawienia
Na górze pliku `src/main/java/pl/pearltrail/PearlTrailClient.java`:
- `SPACING` – odstęp punktów smugi (mniej = gęstsza)
- `DENSITY` – ile cząsteczek na punkt
- `FADE_TICKS` – jak długo smuga zostaje po uderzeniu
