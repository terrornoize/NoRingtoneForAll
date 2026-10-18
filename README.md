# NoRingtoneForAll

App Android che fa squillare il telefono **solo per chi è in rubrica**. Le chiamate da numeri sconosciuti o nascosti arrivano in silenzio: niente suoneria, niente vibrazione.

## Funzionalità

- Le chiamate da numeri **salvati in rubrica** squillano normalmente (rispettando modalità silenziosa / Non disturbare del telefono).
- Le chiamate da numeri **non in rubrica** (compresi numeri nascosti/privati) vengono **silenziate**: nessuna suoneria, nessuna vibrazione.
- La chiamata **non viene rifiutata**: compare sullo schermo, puoi rispondere, resta nel registro chiamate e genera la normale notifica di chiamata persa.
- Vale **solo per le telefonate**: SMS, WhatsApp e tutte le altre notifiche non vengono toccati.
- Nessuna icona fissa, nessun servizio sempre acceso: zero consumo di batteria quando non squilla nulla.
- Interruttore on/off e elenco delle ultime chiamate silenziate nella schermata principale.

## Come funziona

L'app usa l'API di sistema [`CallScreeningService`](https://developer.android.com/reference/android/telecom/CallScreeningService) e deve essere impostata come **app di filtro chiamate** (ruolo `ROLE_CALL_SCREENING`, "ID chiamante e spam"). Per ogni chiamata in arrivo il sistema chiede all'app cosa fare: il numero viene cercato in rubrica con `ContactsContract.PhoneLookup` (che gestisce formati diversi, es. `+39 333 …` vs `333…`); se non c'è corrispondenza la risposta è `setSilenceCall(true)`.

L'app **non deve restare in memoria**: è il sistema (Telecom) ad avviarla ad ogni chiamata in arrivo, anche se è stata chiusa, dopo un riavvio del telefono o se non è mai stata aperta. Il ruolo e il permesso rubrica restano validi finché non li revochi.

## Requisiti

- Android 10 (API 29) o superiore.
- Permessi: `READ_CONTACTS`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

## Installazione

Scarica l'APK dalla pagina [Releases](../../releases) e installalo, oppure via ADB:

```sh
adb install -r NoRingtoneForAll.apk
```

## Configurazione

Apri l'app: la sezione **Configurazione** mostra cosa manca, con un pulsante per ogni voce.

1. **App di filtro chiamate** → *Imposta* e scegli NoRingtoneForAll.
2. **Accesso alla rubrica** → *Concedi*.
3. **Ottimizzazione batteria** → *Disattiva* (precauzione per ROM aggressive come ColorOS, MIUI, EMUI).

Fatto questo l'app si può chiudere: non serve tenerla aperta.

In alternativa via ADB (alcune ROM, es. ColorOS, bloccano `pm grant`):

```sh
adb shell cmd role add-role-holder android.app.role.CALL_SCREENING com.noringtoneforall
adb shell pm grant com.noringtoneforall android.permission.READ_CONTACTS
adb shell dumpsys deviceidle whitelist +com.noringtoneforall
```

## Compilazione

Serve JDK 17 e Android SDK 35.

```sh
./gradlew testDebugUnitTest   # unit test
./gradlew assembleDebug       # APK in app/build/outputs/apk/debug/
```

### Test end-to-end su emulatore

```sh
adb emu gsm call 3331234567   # numero in rubrica  -> squilla
adb emu gsm call 5550001111   # numero sconosciuto -> silenziato
adb logcat -s NoRingtoneForAll Telecom
```

Per la chiamata silenziata Telecom registra `Ringer: Ending early -- ... silentRingingRequested=true`. Il test è stato ripetuto anche con l'app chiusa forzatamente (`am force-stop`) e dopo un riavvio senza aprirla.

## Limitazioni note

- Senza permesso rubrica l'app **non silenzia nulla** (fail-safe: meglio uno squillo in più che una chiamata persa da un contatto).
- Una sola app per volta può avere il ruolo di filtro chiamate: impostando NoRingtoneForAll si sostituisce un'eventuale altra app di "ID chiamante e spam". Il filtro antispam integrato nel dialer predefinito continua a funzionare.
- Su alcune ROM il sistema interpella l'app solo per i numeri non in rubrica: il comportamento finale è identico.

## Privacy

L'app non ha il permesso `INTERNET`: nessun dato esce dal telefono. La rubrica viene solo consultata, mai copiata; l'elenco delle chiamate silenziate resta in locale e si può svuotare dall'app.

## Licenza

[MIT](LICENSE)

---

## English summary

**NoRingtoneForAll** lets your phone ring only for people in your contacts. Calls from unknown or hidden numbers are silenced (no ringtone, no vibration) but not rejected: they still show on screen, can be answered and appear in the call log. Only phone calls are affected. It uses Android's `CallScreeningService` (`setSilenceCall`) and needs to be set as the call screening app plus contacts permission. Nothing runs in the background: Telecom starts the app on demand for each incoming call. No internet permission. Android 10+. Build with `./gradlew assembleDebug`.
