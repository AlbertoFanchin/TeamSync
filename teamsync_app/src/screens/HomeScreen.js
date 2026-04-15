// src/screens/HomeScreen.js - Schermata principale con selezione sport

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
} from 'react-native';
import { sports } from '../data/mockData';
import { useApp } from '../context/AppContext';

export default function HomeScreen({ navigation }) {
  const { dispatch } = useApp();

  const handleSportSelect = (sportId) => {
    dispatch({ type: 'SET_SELECTED_SPORT', payload: sportId });
    navigation.navigate('Teams', { sportId });
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>TeamSync</Text>
        <Text style={styles.subtitle}>
          Gestisci la tua squadra amatoriale
        </Text>
      </View>

      <View style={styles.sportsSection}>
        <Text style={styles.sectionTitle}>Seleziona Sport</Text>
        <View style={styles.sportsGrid}>
          {sports.map((sport) => (
            <TouchableOpacity
              key={sport.id}
              style={[styles.sportCard, { backgroundColor: getSportColor(sport.id) }]}
              onPress={() => handleSportSelect(sport.id)}
            >
              <Text style={styles.sportIcon}>{sport.icon}</Text>
              <Text style={styles.sportName}>{sport.name}</Text>
              <Text style={styles.playersCount}>
                {sport.playersPerTeam} giocatori
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      <View style={styles.featuresSection}>
        <Text style={styles.sectionTitle}>Funzionalità</Text>
        <View style={styles.featureList}>
          <FeatureItem icon="👥" text="Gestione Rosa Giocatori" />
          <FeatureItem icon="📅" text="Calendario Allenamenti" />
          <FeatureItem icon="🏆" text="Partite e Risultati" />
          <FeatureItem icon="📋" text="Schede Tattiche" />
          <FeatureItem icon="📊" text="Statistiche Squadra" />
          <FeatureItem icon="✅" text="Disponibilità Giocatori" />
        </View>
      </View>
    </ScrollView>
  );
}

function FeatureItem({ icon, text }) {
  return (
    <View style={styles.featureItem}>
      <Text style={styles.featureIcon}>{icon}</Text>
      <Text style={styles.featureText}>{text}</Text>
    </View>
  );
}

function getSportColor(sportId) {
  const colors = {
    volleyball: '#FF6B6B',
    basketball: '#4ECDC4',
    soccer: '#45B7D1',
  };
  return colors[sportId] || '#95A5A6';
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  header: {
    backgroundColor: '#2C3E50',
    padding: 30,
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  subtitle: {
    fontSize: 16,
    color: '#BDC3C7',
    marginTop: 8,
  },
  sportsSection: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#2C3E50',
    marginBottom: 15,
  },
  sportsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  sportCard: {
    width: '30%',
    aspectRatio: 1,
    borderRadius: 15,
    padding: 15,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 15,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  sportIcon: {
    fontSize: 40,
    marginBottom: 8,
  },
  sportName: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#FFFFFF',
    textAlign: 'center',
  },
  playersCount: {
    fontSize: 11,
    color: '#FFFFFF',
    marginTop: 4,
    opacity: 0.9,
  },
  featuresSection: {
    padding: 20,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
  },
  featureList: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  featureItem: {
    width: '50%',
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
  },
  featureIcon: {
    fontSize: 24,
    marginRight: 10,
  },
  featureText: {
    fontSize: 14,
    color: '#34495E',
  },
});
