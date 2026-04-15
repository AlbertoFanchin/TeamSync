# TeamSync - App per la Gestione di Squadre Amatoriali

## Panoramica del Progetto

**TeamSync** è un'applicazione mobile cross-platform sviluppata con **React Native (Expo)** per la gestione di squadre amatoriali miste di **Pallavolo**, **Basket** e **Calcio**. L'app funge da assistente logistico, tattico e tecnico per allenatori e responsabili di squadra.

## Funzionalità Principali

### 🏐 Multi-Sport
- Supporto per tre sport: Pallavolo, Basket e Calcio
- Interfaccia personalizzata per ogni disciplina
- Regole e formazioni specifiche per sport

### 👥 Gestione Rosa Giocatori
- Anagrafica completa dei giocatori (nome, email, telefono)
- Ruolo/posizione specifica per sport
- Livello di abilità (1-5)
- Tracking disponibilità per allenamenti/partite

### 📅 Calendario Allenamenti
- Programmazione giorni e orari degli allenamenti
- Schede di allenamento predefinite per sport
- Esercizi dettagliati con durata e serie
- Selezione piano di allenamento per sessione

### ♟️ Assistente Tattico
- Formazioni disponibili per ogni sport
- Consigli tattici specifici
- Strategie offensive e difensive
- Visualizzazione schemi di gioco

### 🏆 Gestione Partite
- Calendario partite (casa/trasferta)
- Registrazione risultati
- Dettagli avversari e location
- Storico incontri

## Struttura del Progetto

```
teamsync_app/
├── App.js                          # Punto di ingresso principale
├── app.json                        # Configurazione Expo
├── package.json                    # Dipendenze e script
├── src/
│   ├── components/                 # Componenti riutilizzabili
│   ├── context/
│   │   └── AppContext.js           # Gestione stato globale (useReducer)
│   ├── data/
│   │   └── mockData.js             # Dati mock per sviluppo
│   ├── navigation/
│   │   └── AppNavigator.js         # Navigazione stack-based
│   ├── screens/
│   │   ├── HomeScreen.js           # Selezione sport
│   │   ├── TeamsScreen.js          # Lista squadre
│   │   └── TeamDetailScreen.js     # Dettaglio squadra (4 tab)
│   └── utils/                      # Utility functions
└── assets/                         # Risorse grafiche
```

## Tecnologie Utilizzate

- **Framework**: React Native con Expo SDK 54
- **Navigazione**: React Navigation v6 (Stack Navigator)
- **Gestione Stato**: React Context + useReducer
- **Linguaggio**: JavaScript (ES6+)
- **Styling**: StyleSheet di React Native

## Installazione e Avvio

### Prerequisiti
- Node.js >= 18
- npm o yarn
- Expo CLI (opzionale, incluso nel progetto)
- Expo Go app (per testing su dispositivo fisico)

### Passaggi di Installazione

```bash
# Spostarsi nella directory del progetto
cd teamsync_app

# Installare le dipendenze
npm install

# Avviare lo sviluppatore server
npm start

# In alternativa:
# npx expo start
```

### Esecuzione su Dispositivo

1. **Android/iOS (con Expo Go)**:
   - Scaricare l'app Expo Go dallo store
   - Scansionare il QR code mostrato dal terminal
   - L'app si avvierà sul dispositivo

2. **Web Browser**:
   ```bash
   npm run web
   ```

3. **Emulatore/Simulatore**:
   ```bash
   npm run android  # Android emulator
   npm run ios      # iOS simulator (macOS only)
   ```

## Schermate dell'App

### 1. Home Screen
- Selezione dello sport (Pallavolo, Basket, Calcio)
- Panoramica funzionalità
- Accesso rapido alle sezioni principali

### 2. Teams Screen
- Lista squadre per sport selezionato
- Info rapide (giorni/orari allenamento, allenatore)
- Bottone per creare nuova squadra

### 3. Team Detail Screen
Quattro tab principali:

#### Tab Giocatori 👥
- Lista giocatori della squadra
- Indicatore disponibilità (switch)
- Barra skill level
- Posizione/ruolo

#### Tab Allenamenti 📋
- Piani di allenamento predefiniti
- Dettaglio esercizi (durata, serie)
- Selezione piano attivo

#### Tab Tattica ♟️
- Formazioni disponibili per sport
- Descrizione formazioni
- Consigli tattici specifici

#### Tab Partite 🏆
- Calendario incontri
- Risultati partite giocate
- Distinzione casa/trasferta

## Dati Mock

Il file `src/data/mockData.js` contiene:
- Configurazione sport (icon, giocatori per squadra)
- Giocatori di esempio
- Squadre preconfigurate
- Partite di esempio
- Piani di allenamento per sport
- Formazioni per sport

## Estensioni Future

### Backend Integration
- API REST per persistenza dati
- Autenticazione utenti
- Sync multi-dispositivo

### Funzionalità Avanzate
- Notifiche push per convocazioni
- Chat di squadra
- Statistiche avanzate
- Esportazione dati (PDF, Excel)
- Integrazione con Google Calendar
- Geolocalizzazione campi/corsi

### UI/UX Improvements
- Animazioni e transizioni
- Dark mode
- Personalizzazione temi
- Accessibilità migliorata

## Struttura Dati

### Player
```javascript
{
  id: string,
  name: string,
  email: string,
  phone: string,
  position: string,
  skillLevel: number (1-5),
  availability: boolean,
  sport: string
}
```

### Team
```javascript
{
  id: string,
  name: string,
  sport: string,
  players: array of player IDs,
  coach: string,
  trainingDays: array of strings,
  trainingTime: string
}
```

### Match
```javascript
{
  id: string,
  homeTeam: string,
  awayTeam: string,
  date: string,
  time: string,
  location: string,
  sport: string,
  result: { home: number, away: number } | null,
  status: 'scheduled' | 'completed'
}
```

## Contributi

Questo progetto è open to contributions. Per contribuire:
1. Fork del repository
2. Creazione branch feature (`git checkout -b feature/NewFeature`)
3. Commit delle modifiche (`git commit -m 'Add new feature'`)
4. Push sul branch (`git push origin feature/NewFeature`)
5. Apertura Pull Request

## Licenza

MIT License - Vedi file LICENSE per dettagli.

## Contatti

Per informazioni o supporto:
- Email: support@teamsync.app
- GitHub: [repository](https://github.com/yourusername/teamsync)

---

**TeamSync** - Il tuo assistente personale per la gestione della squadra amatoriale! 🏆
