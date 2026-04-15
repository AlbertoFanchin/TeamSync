import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Dimensions,
  TouchableOpacity,
  Animated,
  Alert,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';

// --- DATI E CONFIGURAZIONE ---

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const BOARD_WIDTH = SCREEN_WIDTH - 40;
const BOARD_HEIGHT = BOARD_WIDTH * 0.65; // Aspect ratio approssimativo

// Sport Config
const SPORTS_CONFIG = {
  volley: {
    name: 'Pallavolo',
    color: '#3498db',
    playersCount: 6,
    rules: {
      minGenderCount: 2, // Esempio: minimo 2 donne/uomini
      requiresAlternation: true,
    },
  },
  basket: {
    name: 'Basket',
    color: '#e67e22',
    playersCount: 5,
    rules: {
      minGenderCount: 1,
      requiresAlternation: false,
    },
  },
  calcio: {
    name: 'Calcio',
    color: '#2ecc71',
    playersCount: 5, // 5vs5 per semplicità
    rules: {
      minGenderCount: 1,
      requiresAlternation: false,
    },
  },
};

// Posizioni Campi (Coordinate percentuali 0-100)
const FIELD_POSITIONS = {
  volley: {
    reception: [
      { x: 20, y: 80, id: 'P1' }, { x: 50, y: 80, id: 'P6' }, { x: 80, y: 80, id: 'P5' },
      { x: 20, y: 40, id: 'P2' }, { x: 50, y: 40, id: 'P3' }, { x: 80, y: 40, id: 'P4' },
    ],
    attack: [
      { x: 20, y: 60, id: 'P1' }, { x: 50, y: 20, id: 'P6' }, { x: 80, y: 60, id: 'P5' },
      { x: 20, y: 20, id: 'P2' }, { x: 50, y: 50, id: 'P3' }, { x: 80, y: 20, id: 'P4' },
    ],
  },
  basket: [
    { x: 50, y: 90, id: 'PG' },
    { x: 20, y: 60, id: 'SG' }, { x: 80, y: 60, id: 'SG' },
    { x: 35, y: 30, id: 'PF' }, { x: 65, y: 30, id: 'C' },
  ],
  calcio: [
    { x: 50, y: 90, id: 'POR' },
    { x: 20, y: 60, id: 'D' }, { x: 80, y: 60, id: 'D' },
    { x: 50, y: 40, id: 'C' },
    { x: 50, y: 15, id: 'A' },
  ],
};

// Dati Giocatori Mock (con genere)
const MOCK_PLAYERS = [
  { id: 1, name: 'Marco', gender: 'M', role: 'Schiacciatore', isUser: true },
  { id: 2, name: 'Giulia', gender: 'F', role: 'Palleggiatore', isUser: false },
  { id: 3, name: 'Luca', gender: 'M', role: 'Centrale', isUser: false },
  { id: 4, name: 'Sofia', gender: 'F', role: 'Opposto', isUser: false },
  { id: 5, name: 'Andrea', gender: 'M', role: 'Libero', isUser: false },
  { id: 6, name: 'Elena', gender: 'F', role: 'Schiacciatore', isUser: false },
  // Extra per altri sport
  { id: 7, name: 'Davide', gender: 'M', role: 'Guardia', isUser: false },
  { id: 8, name: 'Chiara', gender: 'F', role: 'Ala', isUser: false },
];

// --- COMPONENTI ---

const TacticalBoard = () => {
  const [currentSport, setCurrentSport] = useState('volley');
  const [phase, setPhase] = useState('reception'); // 'reception' | 'attack'
  const [formation, setFormation] = useState([]);
  const [validationError, setValidationError] = useState(null);

  // Inizializza formazione quando cambia lo sport
  useEffect(() => {
    const config = SPORTS_CONFIG[currentSport];
    const positions = currentSport === 'volley' 
      ? (phase === 'reception' ? FIELD_POSITIONS.volley.reception : FIELD_POSITIONS.volley.attack)
      : (Array.isArray(FIELD_POSITIONS[currentSport]) ? FIELD_POSITIONS[currentSport] : []);
    
    // Assegna giocatori alle posizioni (semplificato)
    const newFormation = positions.map((pos, index) => ({
      ...pos,
      player: MOCK_PLAYERS[index % MOCK_PLAYERS.length], // Ciclo giocatori
      xAnim: new Animated.Value(pos.x),
      yAnim: new Animated.Value(pos.y),
    }));

    setFormation(newFormation);
    validateFormation(newFormation, config.rules);
  }, [currentSport, phase]);

  // Animazione transizione fasi (Volley)
  useEffect(() => {
    if (currentSport === 'volley') {
      const targetPositions = phase === 'reception' 
        ? FIELD_POSITIONS.volley.reception 
        : FIELD_POSITIONS.volley.attack;
      
      const animations = formation.map((slot, idx) => {
        const target = targetPositions[idx];
        return Animated.parallel([
          Animated.spring(slot.xAnim, { toValue: target.x, useNativeDriver: false }),
          Animated.spring(slot.yAnim, { toValue: target.y, useNativeDriver: false }),
        ]);
      });
      Animated.stagger(100, animations).start();
      
      // Ricalcola validazione dopo animazione (semplificato: immediato)
      validateFormation(formation.map((s, i) => ({...s, ...targetPositions[i]})), SPORTS_CONFIG.volley.rules);
    }
  }, [phase]);

  const validateFormation = (currentFormation, rules) => {
    if (!currentFormation || currentFormation.length === 0) return;

    const genders = currentFormation.map(s => s.player?.gender).filter(g => g);
    const menCount = genders.filter(g => g === 'M').length;
    const womenCount = genders.filter(g => g === 'F').length;

    let error = null;

    // Controllo numero minimo
    if (menCount < rules.minGenderCount || womenCount < rules.minGenderCount) {
      error = `Formazione non valida: Minimo ${rules.minGenderCount} giocatori per genere.`;
    }

    // Controllo alternanza (solo Volley per esempio)
    if (rules.requiresAlternation && currentSport === 'volley') {
      // Logica semplificata: controlla se ci sono coppie adiacenti dello stesso sesso in ricezione
      // In un'app reale, si controllerebbero le zone specifiche (2-3-4 vs 1-6-5)
      const isAlternating = true; // Placeholder per logica complessa di zone
      if (!isAlternating && (menCount > 3 || womenCount > 3)) {
         // Esempio banale: max 3 uomini in campo
         if(menCount > 3) error = "Violazione regola mista: Max 3 uomini in campo.";
         if(womenCount > 3) error = "Violazione regola mista: Max 3 donne in campo.";
      }
    }

    setValidationError(error);
  };

  const togglePhase = () => {
    setPhase(prev => prev === 'reception' ? 'attack' : 'reception');
  };

  const renderFieldLines = () => {
    const borderColor = SPORTS_CONFIG[currentSport].color;
    
    if (currentSport === 'volley') {
      return (
        <View style={styles.fieldContainer}>
          {/* Linea centrale */}
          <View style={[styles.line, { top: '50%', width: '100%', backgroundColor: borderColor }]} />
          {/* Linea d'attacco */}
          <View style={[styles.line, { top: '35%', width: '100%', backgroundColor: borderColor, borderStyle: 'dashed' }]} />
          <View style={[styles.line, { top: '65%', width: '100%', backgroundColor: borderColor, borderStyle: 'dashed' }]} />
          {/* Rete */}
          <View style={[styles.net, { top: '48%' }]} />
        </View>
      );
    } else if (currentSport === 'basket') {
      return (
        <View style={styles.fieldContainer}>
          <View style={[styles.line, { top: '50%', width: '100%', backgroundColor: borderColor }]} />
          <View style={[styles.circle, { top: '40%', left: '40%', width: '20%', height: '20%', borderColor }]} />
          {/* Area */}
          <View style={[styles.area, { top: '0%', left: '25%', width: '50%', height: '20%', borderColor }]} />
          <View style={[styles.area, { bottom: '0%', left: '25%', width: '50%', height: '20%', borderColor }]} />
        </View>
      );
    } else {
      // Calcio
      return (
        <View style={styles.fieldContainer}>
          <View style={[styles.line, { top: '50%', width: '100%', backgroundColor: borderColor }]} />
          <View style={[styles.circle, { top: '45%', left: '45%', width: '10%', height: '10%', borderColor }]} />
          <View style={[styles.area, { top: '0%', left: '20%', width: '60%', height: '15%', borderColor }]} />
          <View style={[styles.area, { bottom: '0%', left: '20%', width: '60%', height: '15%', borderColor }]} />
        </View>
      );
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Lavagna Tattica</Text>

      {/* Selettore Sport */}
      <View style={styles.controls}>
        {Object.keys(SPORTS_CONFIG).map(sport => (
          <TouchableOpacity
            key={sport}
            style={[styles.sportBtn, currentSport === sport && { backgroundColor: SPORTS_CONFIG[sport].color }]}
            onPress={() => setCurrentSport(sport)}
          >
            <Text style={styles.sportBtnText}>{SPORTS_CONFIG[sport].name}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Validatore Misto */}
      {validationError ? (
        <View style={styles.errorBox}>
          <Ionicons name="alert-circle" size={20} color="#fff" />
          <Text style={styles.errorText}>{validationError}</Text>
        </View>
      ) : (
        <View style={styles.successBox}>
          <Ionicons name="checkmark-circle" size={20} color="#2ecc71" />
          <Text style={styles.successText}>Formazione Valida</Text>
        </View>
      )}

      {/* Campo da Gioco */}
      <View style={[styles.board, { borderColor: SPORTS_CONFIG[currentSport].color }]}>
        {renderFieldLines()}
        
        {formation.map((slot, index) => (
          <Animated.View
            key={slot.id || index}
            style={[
              styles.playerToken,
              {
                left: slot.xAnim.interpolate({
                  inputRange: [0, 100],
                  outputRange: ['0%', '100%'],
                }),
                top: slot.yAnim.interpolate({
                  inputRange: [0, 100],
                  outputRange: ['0%', '100%'],
                }),
                borderColor: slot.player.isUser ? '#FFD700' : (slot.player.gender === 'M' ? '#3498db' : '#e91e63'),
                borderWidth: slot.player.isUser ? 3 : 2,
                shadowColor: slot.player.isUser ? '#FFD700' : 'transparent',
                shadowOffset: { width: 0, height: 0 },
                shadowOpacity: slot.player.isUser ? 0.8 : 0,
                shadowRadius: 10,
                elevation: slot.player.isUser ? 10 : 5,
              },
            ]}
          >
            <Text style={styles.playerNumber}>{index + 1}</Text>
            {slot.player.isUser && (
              <View style={styles.userBadge}>
                <Text style={styles.userBadgeText}>TU</Text>
              </View>
            )}
          </Animated.View>
        ))}
      </View>

      {/* Controlli Fase (Solo Volley) */}
      {currentSport === 'volley' && (
        <View style={styles.phaseControls}>
          <Text style={styles.phaseLabel}>Fase: {phase === 'reception' ? 'Ricezione' : 'Attacco'}</Text>
          <TouchableOpacity style={styles.toggleBtn} onPress={togglePhase}>
            <Text style={styles.toggleBtnText}>Cambia Fase</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* Legenda */}
      <View style={styles.legend}>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, { borderColor: '#3498db' }]} />
          <Text style={styles.legendText}>Uomo</Text>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, { borderColor: '#e91e63' }]} />
          <Text style={styles.legendText}>Donna</Text>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, { borderColor: '#FFD700', shadowColor: '#FFD700' }]} />
          <Text style={styles.legendText}>Tu</Text>
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { padding: 20, backgroundColor: '#f5f5f5', minHeight: '100%' },
  title: { fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 20, color: '#333' },
  controls: { flexDirection: 'row', justifyContent: 'space-around', marginBottom: 15 },
  sportBtn: { paddingVertical: 8, paddingHorizontal: 12, borderRadius: 20, backgroundColor: '#ddd' },
  sportBtnText: { color: '#fff', fontWeight: 'bold' },
  
  errorBox: { backgroundColor: '#e74c3c', padding: 10, borderRadius: 8, flexDirection: 'row', alignItems: 'center', marginBottom: 15 },
  errorText: { color: '#fff', marginLeft: 8, fontWeight: '600' },
  successBox: { backgroundColor: '#fff', padding: 10, borderRadius: 8, flexDirection: 'row', alignItems: 'center', marginBottom: 15, borderWidth: 1, borderColor: '#eee' },
  successText: { color: '#333', marginLeft: 8, fontWeight: '600' },

  board: {
    width: BOARD_WIDTH,
    height: BOARD_HEIGHT,
    borderWidth: 3,
    borderRadius: 8,
    backgroundColor: 'rgba(255,255,255,0.8)',
    alignSelf: 'center',
    position: 'relative',
    overflow: 'hidden',
  },
  fieldContainer: { ...StyleSheet.absoluteFillObject },
  line: { position: 'absolute', height: 2, backgroundColor: '#333' },
  net: { position: 'absolute', height: 4, backgroundColor: '#555', width: '100%' },
  circle: { position: 'absolute', borderRadius: 999, borderWidth: 2, backgroundColor: 'transparent' },
  area: { position: 'absolute', borderWidth: 2, backgroundColor: 'transparent' },

  playerToken: {
    position: 'absolute',
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: -20, // Center on coordinate
    marginTop: -20,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 3,
    elevation: 5,
  },
  playerNumber: { fontSize: 16, fontWeight: 'bold', color: '#333' },
  userBadge: {
    position: 'absolute',
    bottom: -15,
    backgroundColor: '#FFD700',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  userBadgeText: { fontSize: 10, fontWeight: 'bold', color: '#000' },

  phaseControls: { marginTop: 20, alignItems: 'center' },
  phaseLabel: { fontSize: 18, fontWeight: '600', marginBottom: 10, color: '#555' },
  toggleBtn: { backgroundColor: '#34495e', paddingVertical: 10, paddingHorizontal: 20, borderRadius: 8 },
  toggleBtnText: { color: '#fff', fontWeight: 'bold' },

  legend: { flexDirection: 'row', justifyContent: 'center', marginTop: 20, gap: 15 },
  legendItem: { flexDirection: 'row', alignItems: 'center' },
  legendDot: { width: 12, height: 12, borderRadius: 6, borderWidth: 2, backgroundColor: '#fff', marginRight: 5 },
  legendText: { fontSize: 12, color: '#666' },
});

export default TacticalBoard;
