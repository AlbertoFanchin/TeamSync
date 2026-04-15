/**
 * TeamOptimizer.js
 * Logiche di backend/frontend per:
 * 1. Aggregatore Carenze di Squadra
 * 2. Smart Carpooling (Algoritmo Clarke-Wright + Regola 1.3)
 */

// --- COSTANTI E CONFIGURAZIONE ---
const MAX_DETOURS_PER_DRIVER = 4;
const CAR_CAPACITY = 4; // Incluso il guidatore
const DETOUR_PENALTY_FACTOR = 1.5; // Penalizza i percorsi troppo lunghi

/**
 * 1. AGGREGATORE CARENZE DI SQUADRA
 * Analizza i profili, trova le top 5 lacune e suggerisce esercizi.
 * 
 * @param {Array} members - Lista membri con skills e carenze
 * @param {Object} exerciseDB - Database di esercizi mappati per tag
 * @returns {Object} Report con top carenze ed esercizi suggeriti
 */
export const analyzeTeamWeaknesses = (members, exerciseDB) => {
  const weaknessCounts = {};

  // 1. Conteggio frequenze carenze
  members.forEach(member => {
    if (member.weaknesses && Array.isArray(member.weaknesses)) {
      member.weaknesses.forEach(weakness => {
        const tag = weakness.toLowerCase().trim();
        weaknessCounts[tag] = (weaknessCounts[tag] || 0) + 1;
      });
    }
  });

  // 2. Ordinamento e selezione Top 5
  const sortedWeaknesses = Object.entries(weaknessCounts)
    .sort(([, countA], [, countB]) => countB - countA)
    .slice(0, 5);

  // 3. Mapping con esercizi dal database
  const suggestedDrills = sortedWeaknesses.map(([tag, count]) => {
    const drills = exerciseDB[tag] || [];
    return {
      weakness: tag,
      affectedPlayersCount: count,
      recommendedDrills: drills.slice(0, 3), // Prendi i primi 3 esercizi
    };
  });

  return {
    totalMembersAnalyzed: members.length,
    topWeaknesses: suggestedDrills,
    generatedAt: new Date().toISOString(),
  };
};

// --- LOGICA SMART CARPOOLING (Clarke-Wright Savings Algorithm) ---

/**
 * Calcola la distanza euclidea tra due coordinate GPS
 */
const getEuclideanDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // Raggio terra in km
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

/**
 * Stima distanza stradale usando la "Regola del 1.3" sulla distanza euclidea.
 * Costo Zero API.
 */
const estimateRoadDistance = (p1, p2) => {
  const euclidean = getEuclideanDistance(p1.lat, p1.lon, p2.lat, p2.lon);
  return euclidean * 1.3;
};

/**
 * Calcola il costo di deviazione (Detour Cost) per aggiungere un passeggero P
 * al percorso di un guidatore G che va alla destinazione D.
 * Formula: Costo = (Dist(G->P) + Dist(P->D)) - Dist(G->D)
 */
const calculateDetourCost = (driverLoc, passengerLoc, destinationLoc) => {
  const distDriverToDest = estimateRoadDistance(driverLoc, destinationLoc);
  const distDriverToPassenger = estimateRoadDistance(driverLoc, passengerLoc);
  const distPassengerToDest = estimateRoadDistance(passengerLoc, destinationLoc);

  const newRouteDistance = distDriverToPassenger + distPassengerToDest;
  return newRouteDistance - distDriverToDest;
};

/**
 * Algoritmo di Clarke-Wright modificato per il Carpooling.
 * Obiettivo: Minimizzare la distanza totale e rispettare il max 4 deviazioni.
 * 
 * @param {Object} destination - Coordinate {lat, lon} del campo/partita
 * @param {Array} drivers - Array di oggetti {id, name, lat, lon}
 * @param {Array} passengers - Array di oggetti {id, name, lat, lon}
 * @returns {Array} Liste di auto ottimizzate
 */
export const optimizeCarpooling = (destination, drivers, passengers) => {
  if (drivers.length === 0) return { error: "Nessun guidatore disponibile" };
  
  let availablePassengers = [...passengers];
  const carAssignments = [];

  // 1. Ordina i guidatori in base alla vicinanza alla destinazione (i più vicini partono prima)
  // Questo aiuta a creare cluster geografici naturali
  const sortedDrivers = [...drivers].sort((a, b) => {
    const distA = estimateRoadDistance(a.location, destination);
    const distB = estimateRoadDistance(b.location, destination);
    return distA - distB;
  });

  sortedDrivers.forEach(driver => {
    if (availablePassengers.length === 0) return;

    let currentRoute = [driver];
    let currentLoad = 1; // Il guidatore conta come 1
    
    // Copia locale dei passeggeri per calcolare i costi senza modificare l'array principale subito
    let candidates = [...availablePassengers];

    while (currentLoad < CAR_CAPACITY && candidates.length > 0) {
      // Trova il passeggero con il MINOR costo di deviazione rispetto all'ultimo punto aggiunto al percorso
      // Nota: Per semplicità qui calcoliamo la deviazione dall'ultima posizione aggiunta (greedy approach)
      // Un approccio più complesso ricalcolerebbe l'intero percorso TSP, ma per < 4 stop il greedy è efficiente.
      
      const lastStop = currentRoute[currentRoute.length - 1];
      
      let bestCandidate = null;
      let minCost = Infinity;

      candidates.forEach((passenger, index) => {
        const cost = calculateDetourCost(lastStop.location, passenger.location, destination);
        
        // Filtro hard: max 4 deviazioni totali (incluso il pickup iniziale se contiamo gli stop)
        // Qui interpretiamo "4 deviazioni" come 4 pickup extra oltre alla partenza.
        if (currentRoute.length - 1 >= MAX_DETOURS_PER_DRIVER) return;

        if (cost < minCost) {
          minCost = cost;
          bestCandidate = { ...passenger, index, cost };
        }
      });

      if (bestCandidate) {
        // Aggiungi al percorso
        currentRoute.push(bestCandidate);
        currentLoad++;
        // Rimuovi dai candidati locali e globali
        candidates.splice(bestCandidate.index, 1);
        availablePassengers = availablePassengers.filter(p => p.id !== bestCandidate.id);
      } else {
        break; // Nessun candidato valido o capacità raggiunta
      }
    }

    carAssignments.push({
      driver: driver,
      passengers: currentRoute.slice(1), // Rimuovi il guidatore dalla lista passeggeri
      totalStops: currentRoute.length - 1,
      estimatedExtraKm: currentRoute.reduce((acc, curr, idx, arr) => {
        if (idx === 0) return 0;
        const prev = arr[idx - 1];
        return acc + estimateRoadDistance(prev.location, curr.location);
      }, 0)
    });
  });

  return {
    assignments: carAssignments,
    unassignedPassengers: availablePassengers,
    stats: {
      totalCarsUsed: carAssignments.length,
      totalPassengersMoved: passengers.length - availablePassengers.length,
    }
  };
};

// --- DATI DI ESEMPIO PER TEST ---

export const mockExerciseDB = {
  'ricezione': [
    { id: 1, name: "Plank laterale con pallone", duration: "3x30s" },
    { id: 2, name: "Spostamenti rapidi a terra", duration: "5x10m" }
  ],
  'tiro da 3': [
    { id: 3, name: "Tiro da fermo angolo", duration: "10 tiri x 5 angoli" },
    { id: 4, name: "Ricezione e tiro rapido", duration: "Serie da 20" }
  ],
  'cross': [
    { id: 5, name: "Cross sul secondo palo", duration: "15 ripetizioni" }
  ],
  'muro': [
    { id: 6, name: "Salto a muro su plinto", duration: "4x10 salti" }
  ],
  'battuta': [
    { id: 7, name: "Battuta flottante mirata", duration: "10 battute x zona" }
  ],
  'palleggio': [
    { id: 8, name: "Palleggio contro muro singolo", duration: "3x1 minuto" }
  ],
  'difesa': [
    { id: 9, name: "Tuffi laterali su palloni bassi", duration: "Serie da 15" }
  ],
  'attacco': [
    { id: 10, name: "Colpo secco da posto 4", duration: "10 attacchi x lato" }
  ]
};

export const mockTeamData = {
  members: [
    { id: 1, name: "Luca", weaknesses: ['ricezione', 'muro'] },
    { id: 2, name: "Marco", weaknesses: ['ricezione', 'battuta'] },
    { id: 3, name: "Giulia", weaknesses: ['ricezione', 'palleggio'] },
    { id: 4, name: "Anna", weaknesses: ['muro', 'attacco'] },
    { id: 5, name: "Paolo", weaknesses: ['ricezione', 'difesa'] },
  ]
};

export const mockCarpoolData = {
  destination: { lat: 45.4642, lon: 9.1900 }, // Milano Duomo (esempio)
  drivers: [
    { id: 'd1', name: 'Sofia (Guida)', location: { lat: 45.4700, lon: 9.1800 } },
    { id: 'd2', name: 'Luca (Guida)', location: { lat: 45.4500, lon: 9.2000 } },
  ],
  passengers: [
    { id: 'p1', name: 'Mario', location: { lat: 45.4680, lon: 9.1850 } }, // Vicino Sofia
    { id: 'p2', name: 'Elena', location: { lat: 45.4720, lon: 9.1750 } }, // Vicino Sofia
    { id: 'p3', name: 'Giovanni', location: { lat: 45.4550, lon: 9.1950 } }, // Vicino Luca
    { id: 'p4', name: 'Francesca', location: { lat: 45.4480, lon: 9.2050 } }, // Vicino Luca
    { id: 'p5', name: 'Davide', location: { lat: 45.4600, lon: 9.1900 } }, // Centrale
  ]
};
