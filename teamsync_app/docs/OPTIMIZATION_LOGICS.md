# 🧠 Logiche di Ottimizzazione TeamSync

Questo documento descrive le logiche avanzate di backend/frontend implementate per TeamSync.

---

## 📋 Indice

1. [Aggregatore Carenze di Squadra](#1-aggregatore-carenze-di-squadra)
2. [Smart Carpooling](#2-smart-carpooling)
3. [Esecuzione Test](#3-esecuzione-test)

---

## 1. Aggregatore Carenze di Squadra

### 🎯 Obiettivo
Analizzare i profili dei membri di una squadra, identificare le **5 lacune più condivise** e suggerire al Capitano una lista di esercizi mirati.

### 🔧 Implementazione

**File:** `src/logics/TeamOptimizer.js`

```javascript
analyzeTeamWeaknesses(members, exerciseDB)
```

#### Algoritmo:
1. **Conteggio Frequenze**: Itera su tutti i membri e conta le occorrenze di ogni carenza
2. **Ordinamento**: Ordina le carenze per frequenza decrescente
3. **Selezione Top 5**: Prende le prime 5 carenze più comuni
4. **Mapping Esercizi**: Per ogni carenza, recupera dal database gli esercizi corrispondenti

#### Input:
```javascript
members = [
  { id: 1, name: "Luca", weaknesses: ['ricezione', 'muro'] },
  { id: 2, name: "Marco", weaknesses: ['ricezione', 'battuta'] },
  // ...
]

exerciseDB = {
  'ricezione': [
    { id: 1, name: "Plank laterale con pallone", duration: "3x30s" },
    { id: 2, name: "Spostamenti rapidi a terra", duration: "5x10m" }
  ],
  // ...
}
```

#### Output:
```javascript
{
  totalMembersAnalyzed: 5,
  topWeaknesses: [
    {
      weakness: "ricezione",
      affectedPlayersCount: 4,
      recommendedDrills: [/* array di esercizi */]
    },
    // ... altre 4 carenze
  ],
  generatedAt: "2026-04-15T16:00:01.416Z"
}
```

### ✅ Vantaggi
- **Personalizzato**: Ogni sport ha il proprio set di carenze
- **Data-Driven**: Le decisioni si basano su dati reali della squadra
- **Actionable**: Fornisce subito esercizi pratici da svolgere

---

## 2. Smart Carpooling (Vehicle Routing Problem)

### 🎯 Obiettivo
Calcolare l'assegnazione ottimale dei passeggeri ai guidatori minimizzando le deviazioni, con vincolo di **max 4 deviazioni per auto**.

### 🔧 Implementazione

**File:** `src/logics/TeamOptimizer.js`

```javascript
optimizeCarpooling(destination, drivers, passengers)
```

#### Algoritmi Utilizzati:

##### A. Regola del 1.3 (Costo Zero API)
Per stimare la distanza stradale reale senza chiamare API esterne (Google Maps, Mapbox):

```javascript
estimateRoadDistance(p1, p2) = euclideanDistance(p1, p2) × 1.3
```

- **Distanza Euclidea**: Calcolata con formula di Haversine
- **Fattore 1.3**: Coefficiente empirico che approssima le strade reali (+30% rispetto alla linea d'aria)

##### B. Detour Cost (Costo di Deviazione)
Per valutare quanto conviene aggiungere un passeggero al percorso:

```
DetourCost = (Dist(G→P) + Dist(P→D)) - Dist(G→D)

Dove:
- G = Posizione Guidatore
- P = Posizione Passeggero  
- D = Destinazione (campo/partita)
```

##### C. Algoritmo di Clarke-Wright (Savings Algorithm)
Adattato per il carpooling sportivo:

1. **Ordinamento Guidatori**: I guidatori più vicini alla destinazione partono prima
2. **Greedy Assignment**: Per ogni auto, aggiungi il passeggero con il minor *Detour Cost*
3. **Vincoli**: 
   - Max 4 passeggeri per auto (capacità totale: 4 incluso guidatore)
   - Max 4 deviazioni (stop intermedi)

#### Input:
```javascript
destination = { lat: 45.4642, lon: 9.1900 }

drivers = [
  { id: 'd1', name: 'Sofia', location: { lat: 45.4700, lon: 9.1800 } },
  // ...
]

passengers = [
  { id: 'p1', name: 'Mario', location: { lat: 45.4680, lon: 9.1850 } },
  // ...
]
```

#### Output:
```javascript
{
  assignments: [
    {
      driver: { /* dati guidatore */ },
      passengers: [ /* array passeggeri */ ],
      totalStops: 3,
      estimatedExtraKm: 2.73
    },
    // ...
  ],
  unassignedPassengers: [],
  stats: {
    totalCarsUsed: 2,
    totalPassengersMoved: 5
  }
}
```

### ✅ Vantaggi
- **Zero API Costs**: Nessuna chiamata a servizi di mapping esterni
- **Efficiente**: Algoritmo greedy O(n²) adatto per piccoli gruppi (< 50 persone)
- **Rispetta Vincoli**: Max 4 deviazioni garantite
- **Trasparente**: Mostra i km extra stimati per ogni auto

---

## 3. Esecuzione Test

### 🧪 Test Unitari

I test verificano tutte le logiche implementate:

```bash
cd /workspace/teamsync_app
npm run test:logic
```

### Risultati Attesi:

```
🧪 TEST 1: Aggregatore Carenze di Squadra
✅ TEST 1 PASSATO: Carenza "ricezione" identificata correttamente come priorità #1

🧪 TEST 2: Smart Carpooling (Clarke-Wright + Regola 1.3)
✅ TEST 2 PASSATO: Tutti i vincoli rispettati (max 4 deviazioni, tutti assegnati)

🧪 TEST 3: Verifica Regola 1.3 (Costo Zero API)
✅ TEST 3 PASSATO: Regola 1.3 applicata correttamente
```

---

## 📊 Performance

| Logica | Complessità | Tempo Medio (10 utenti) |
|--------|-------------|-------------------------|
| Aggregatore Carenze | O(n × m)* | < 1ms |
| Smart Carpooling | O(n²) | < 5ms |

*n = membri, m = carenze medie per membro

---

## 🔮 Possibili Miglioramenti Futuri

1. **Carpooling Avanzato**:
   - Integrazione con Google Directions API per percorsi reali
   - Supporto per multi-destinazioni (più campi sportivi)
   - Preferenze utente (fumatore/non fumatore, musica, ecc.)

2. **Carenze AI**:
   - Machine learning per predire carenze future
   - Raccomandazione esercizi basata su progressi storici

3. **Gamification**:
   - Badge per chi organizza carpooling
   - Classifica miglioramenti carenze

---

## 📄 Licenza

Codice incluso nel progetto TeamSync.
