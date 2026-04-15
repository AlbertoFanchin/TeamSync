// src/data/mockData.js - Dati mock per lo sviluppo iniziale

export const sports = [
  { id: 'volleyball', name: 'Pallavolo', icon: '🏐', playersPerTeam: 6 },
  { id: 'basketball', name: 'Basket', icon: '🏀', playersPerTeam: 5 },
  { id: 'soccer', name: 'Calcio', icon: '⚽', playersPerTeam: 11 },
];

export const initialPlayers = [
  {
    id: '1',
    name: 'Marco Rossi',
    email: 'marco@example.com',
    phone: '+39 333 1234567',
    position: 'Schiacciatore',
    skillLevel: 4,
    availability: true,
    sport: 'volleyball',
  },
  {
    id: '2',
    name: 'Giulia Bianchi',
    email: 'giulia@example.com',
    phone: '+39 333 2345678',
    position: 'Palleggiatore',
    skillLevel: 5,
    availability: true,
    sport: 'volleyball',
  },
  {
    id: '3',
    name: 'Luca Verdi',
    email: 'luca@example.com',
    phone: '+39 333 3456789',
    position: 'Centrale',
    skillLevel: 3,
    availability: false,
    sport: 'basketball',
  },
  {
    id: '4',
    name: 'Anna Neri',
    email: 'anna@example.com',
    phone: '+39 333 4567890',
    position: 'Ala',
    skillLevel: 4,
    availability: true,
    sport: 'basketball',
  },
  {
    id: '5',
    name: 'Paolo Gialli',
    email: 'paolo@example.com',
    phone: '+39 333 5678901',
    position: 'Attaccante',
    skillLevel: 5,
    availability: true,
    sport: 'soccer',
  },
];

export const initialTeams = [
  {
    id: '1',
    name: 'Volley Stars',
    sport: 'volleyball',
    players: ['1', '2'],
    coach: 'Marco Rossi',
    trainingDays: ['Monday', 'Wednesday'],
    trainingTime: '19:00',
  },
  {
    id: '2',
    name: 'Basket Bulls',
    sport: 'basketball',
    players: ['3', '4'],
    coach: 'Luca Verdi',
    trainingDays: ['Tuesday', 'Thursday'],
    trainingTime: '20:00',
  },
  {
    id: '3',
    name: ' Soccer Lions',
    sport: 'soccer',
    players: ['5'],
    coach: 'Paolo Gialli',
    trainingDays: ['Friday'],
    trainingTime: '18:30',
  },
];

export const initialMatches = [
  {
    id: '1',
    homeTeam: 'Volley Stars',
    awayTeam: 'Net Warriors',
    date: '2024-05-15',
    time: '20:00',
    location: 'Palazzetto Comunale',
    sport: 'volleyball',
    result: null,
    status: 'scheduled',
  },
  {
    id: '2',
    homeTeam: 'Basket Bulls',
    awayTeam: 'Hoop Dreams',
    date: '2024-05-10',
    time: '21:00',
    location: 'Bocciofila',
    sport: 'basketball',
    result: { home: 78, away: 82 },
    status: 'completed',
  },
];

export const trainingPlans = {
  volleyball: [
    {
      id: 'v1',
      name: 'Fondamentali',
      duration: 90,
      exercises: [
        { name: 'Bagher di precisione', duration: 15, sets: 3 },
        { name: 'Palleggio controllo', duration: 15, sets: 3 },
        { name: 'Servizio in salto', duration: 20, sets: 5 },
        { name: 'Attacco da posto 4', duration: 20, sets: 5 },
        { name: 'Muro su attaccanti', duration: 20, sets: 4 },
      ],
    },
    {
      id: 'v2',
      name: 'Tattica Avanzata',
      duration: 120,
      exercises: [
        { name: 'Ricezione su battuta flottante', duration: 20, sets: 4 },
        { name: 'Contrattacco primo tempo', duration: 25, sets: 5 },
        { name: 'Difesa su palla alta', duration: 25, sets: 4 },
        { name: 'Transizione attacco-difesa', duration: 30, sets: 3 },
        { name: 'Battuta in continuità', duration: 20, sets: 5 },
      ],
    },
  ],
  basketball: [
    {
      id: 'b1',
      name: 'Fondamentali',
      duration: 90,
      exercises: [
        { name: 'Palleggio statico', duration: 10, sets: 3 },
        { name: 'Terzo tempo', duration: 15, sets: 5 },
        { name: 'Tiro libero', duration: 15, sets: 10 },
        { name: 'Passaggio e taglio', duration: 20, sets: 4 },
        { name: 'Difesa 1vs1', duration: 20, sets: 5 },
        { name: 'Partita 3vs3', duration: 10, sets: 2 },
      ],
    },
    {
      id: 'b2',
      name: 'Tattica di Squadra',
      duration: 120,
      exercises: [
        { name: 'Pick and roll', duration: 25, sets: 5 },
        { name: 'Contropiede organizzato', duration: 20, sets: 4 },
        { name: 'Difesa a zona 2-3', duration: 25, sets: 3 },
        { name: 'Blocchi senza palla', duration: 25, sets: 4 },
        { name: 'Partita 5vs5', duration: 25, sets: 1 },
      ],
    },
  ],
  soccer: [
    {
      id: 's1',
      name: 'Tecnica Individuale',
      duration: 90,
      exercises: [
        { name: 'Guida della palla', duration: 15, sets: 3 },
        { name: 'Passaggio corto', duration: 15, sets: 4 },
        { name: 'Tiro in porta', duration: 20, sets: 5 },
        { name: 'Controllo orientato', duration: 15, sets: 4 },
        { name: 'Partitella 4vs4', duration: 25, sets: 2 },
      ],
    },
    {
      id: 's2',
      name: 'Tattica Collettiva',
      duration: 120,
      exercises: [
        { name: 'Possesso palla 6vs3', duration: 20, sets: 3 },
        { name: 'Transizione offensiva', duration: 25, sets: 4 },
        { name: 'Difesa a zona', duration: 25, sets: 3 },
        { name: 'Cross e finalizzazione', duration: 25, sets: 5 },
        { name: 'Partita 8vs8', duration: 25, sets: 1 },
      ],
    },
  ],
};

export const formations = {
  volleyball: [
    { name: '4-2', description: '4 attaccanti, 2 alzatori' },
    { name: '5-1', description: '5 attaccanti, 1 alzatore' },
    { name: '6-0', description: 'Tutti attaccanti' },
  ],
  basketball: [
    { name: '2-3', description: '2 guardie, 3 avanti/centri' },
    { name: '1-2-2', description: '1 play, 2 guardie, 2 lunghi' },
    { name: 'Small Ball', description: 'Formazione veloce senza centro puro' },
  ],
  soccer: [
    { name: '4-4-2', description: '4 difensori, 4 centrocampisti, 2 attaccanti' },
    { name: '4-3-3', description: '4 difensori, 3 centrocampisti, 3 attaccanti' },
    { name: '3-5-2', description: '3 difensori, 5 centrocampisti, 2 attaccanti' },
    { name: '4-2-3-1', description: '4 difensori, 2 mediani, 3 trequartisti, 1 punta' },
  ],
};
