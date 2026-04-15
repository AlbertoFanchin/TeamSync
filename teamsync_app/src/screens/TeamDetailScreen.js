// src/screens/TeamDetailScreen.js - Dettaglio squadra con giocatori, allenamenti e tattica

import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Switch,
} from 'react-native';
import { useApp } from '../context/AppContext';
import { trainingPlans, formations } from '../data/mockData';

export default function TeamDetailScreen({ navigation, route }) {
  const { team } = route.params;
  const [activeTab, setActiveTab] = useState('players');
  const [selectedPlan, setSelectedPlan] = useState(null);

  const sportPlans = trainingPlans[team.sport] || [];
  const sportFormations = formations[team.sport] || [];

  const tabs = [
    { id: 'players', label: 'Giocatori', icon: '👥' },
    { id: 'training', label: 'Allenamenti', icon: '📋' },
    { id: 'tactics', label: 'Tattica', icon: '♟️' },
    { id: 'matches', label: 'Partite', icon: '🏆' },
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'players':
        return <PlayersTab team={team} />;
      case 'training':
        return (
          <TrainingTab
            plans={sportPlans}
            selectedPlan={selectedPlan}
            onSelectPlan={setSelectedPlan}
          />
        );
      case 'tactics':
        return <TacticsTab formations={sportFormations} sport={team.sport} />;
      case 'matches':
        return <MatchesTab team={team} navigation={navigation} />;
      default:
        return null;
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.teamName}>{team.name}</Text>
        <Text style={styles.teamSport}>{team.sport.toUpperCase()}</Text>
        <View style={styles.scheduleInfo}>
          <Text style={styles.scheduleText}>
            📅 {team.trainingDays.join(' • ')}
          </Text>
          <Text style={styles.scheduleText}>⏰ {team.trainingTime}</Text>
        </View>
      </View>

      <View style={styles.tabs}>
        {tabs.map((tab) => (
          <TouchableOpacity
            key={tab.id}
            style={[styles.tab, activeTab === tab.id && styles.activeTab]}
            onPress={() => setActiveTab(tab.id)}
          >
            <Text style={styles.tabIcon}>{tab.icon}</Text>
            <Text
              style={[styles.tabLabel, activeTab === tab.id && styles.activeTabLabel]}
            >
              {tab.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <ScrollView style={styles.content}>{renderContent()}</ScrollView>
    </View>
  );
}

function PlayersTab({ team }) {
  // Mock players - in produzione verrebbero dal context/API
  const players = [
    { id: '1', name: 'Marco Rossi', position: 'Capitano', skill: 5, available: true },
    { id: '2', name: 'Giulia Bianchi', position: 'Vice', skill: 4, available: true },
    { id: '3', name: 'Luca Verdi', position: 'Riserva', skill: 3, available: false },
  ];

  return (
    <View style={styles.tabContent}>
      {players.map((player) => (
        <View key={player.id} style={styles.playerCard}>
          <View style={styles.playerHeader}>
            <Text style={styles.playerName}>{player.name}</Text>
            <Switch value={player.available} disabled />
          </View>
          <Text style={styles.playerPosition}>{player.position}</Text>
          <View style={styles.skillBar}>
            <View style={[styles.skillFill, { width: `${(player.skill / 5) * 100}%` }]} />
          </View>
        </View>
      ))}
      <TouchableOpacity style={styles.addPlayerButton}>
        <Text style={styles.addPlayerText}>+ Aggiungi Giocatore</Text>
      </TouchableOpacity>
    </View>
  );
}

function TrainingTab({ plans, selectedPlan, onSelectPlan }) {
  return (
    <View style={styles.tabContent}>
      {plans.map((plan) => (
        <TouchableOpacity
          key={plan.id}
          style={[
            styles.planCard,
            selectedPlan === plan.id && styles.selectedPlanCard,
          ]}
          onPress={() => onSelectPlan(plan.id)}
        >
          <Text style={styles.planName}>{plan.name}</Text>
          <Text style={styles.planDuration}>⏱️ {plan.duration} minuti</Text>
          <View style={styles.exercisesList}>
            {plan.exercises.map((exercise, index) => (
              <View key={index} style={styles.exerciseItem}>
                <Text style={styles.exerciseName}>{exercise.name}</Text>
                <Text style={styles.exerciseDetails}>
                  {exercise.duration}min × {exercise.sets} serie
                </Text>
              </View>
            ))}
          </View>
        </TouchableOpacity>
      ))}
    </View>
  );
}

function TacticsTab({ formations, sport }) {
  return (
    <View style={styles.tabContent}>
      <Text style={styles.sectionTitle}>Formazioni Disponibili</Text>
      {formations.map((formation, index) => (
        <View key={index} style={styles.formationCard}>
          <Text style={styles.formationName}>{formation.name}</Text>
          <Text style={styles.formationDescription}>{formation.description}</Text>
        </View>
      ))}
      
      <Text style={[styles.sectionTitle, { marginTop: 20 }]}>Consigli Tattici</Text>
      <View style={styles.tipsCard}>
        {sport === 'volleyball' && (
          <>
            <Text style={styles.tipItem}>• Mantenere la ricezione bassa e controllata</Text>
            <Text style={styles.tipItem}>• Comunicare sempre sul muro</Text>
            <Text style={styles.tipItem}>• Variare il tipo di battuta</Text>
          </>
        )}
        {sport === 'basketball' && (
          <>
            <Text style={styles.tipItem}>• Muovere la palla rapidamente</Text>
            <Text style={styles.tipItem}>• Chiudere il rimbalzo difensivo</Text>
            <Text style={styles.tipItem}>• Usare i blocchi efficacemente</Text>
          </>
        )}
        {sport === 'soccer' && (
          <>
            <Text style={styles.tipItem}>• Mantenere la posizione in fase difensiva</Text>
            <Text style={styles.tipItem}>• Supportare il portatore di palla</Text>
            <Text style={styles.tipItem}>• Sfruttare le fasce laterali</Text>
          </>
        )}
      </View>
    </View>
  );
}

function MatchesTab({ team, navigation }) {
  const matches = [
    {
      id: '1',
      opponent: 'Squadra Avversaria 1',
      date: '2024-05-15',
      time: '20:00',
      location: 'Casa',
      result: null,
    },
    {
      id: '2',
      opponent: 'Squadra Avversaria 2',
      date: '2024-05-08',
      time: '21:00',
      location: 'Trasferta',
      result: '3-1',
    },
  ];

  return (
    <View style={styles.tabContent}>
      {matches.map((match) => (
        <View key={match.id} style={styles.matchCard}>
          <View style={styles.matchHeader}>
            <Text style={styles.opponent}>{match.opponent}</Text>
            <Text style={[styles.location, match.location === 'Casa' ? styles.home : styles.away]}>
              {match.location}
            </Text>
          </View>
          <Text style={styles.matchDate}>📅 {match.date} ore {match.time}</Text>
          {match.result && (
            <View style={styles.resultBadge}>
              <Text style={styles.resultText}>Risultato: {match.result}</Text>
            </View>
          )}
        </View>
      ))}
      <TouchableOpacity style={styles.addMatchButton}>
        <Text style={styles.addMatchText}>+ Programma Partita</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  header: {
    backgroundColor: '#3498DB',
    padding: 20,
    paddingTop: 50,
  },
  teamName: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  teamSport: {
    fontSize: 14,
    color: '#FFFFFF',
    opacity: 0.9,
    marginTop: 4,
    textTransform: 'uppercase',
  },
  scheduleInfo: {
    flexDirection: 'row',
    marginTop: 12,
    gap: 15,
  },
  scheduleText: {
    fontSize: 14,
    color: '#FFFFFF',
  },
  tabs: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#ECF0F1',
  },
  tab: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  activeTab: {
    borderBottomColor: '#3498DB',
  },
  tabIcon: {
    fontSize: 20,
    marginBottom: 4,
  },
  tabLabel: {
    fontSize: 12,
    color: '#7F8C8D',
  },
  activeTabLabel: {
    color: '#3498DB',
    fontWeight: '600',
  },
  content: {
    flex: 1,
  },
  tabContent: {
    padding: 15,
  },
  playerCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 15,
    marginBottom: 10,
  },
  playerHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  playerName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2C3E50',
  },
  playerPosition: {
    fontSize: 13,
    color: '#7F8C8D',
    marginBottom: 8,
  },
  skillBar: {
    height: 6,
    backgroundColor: '#ECF0F1',
    borderRadius: 3,
    overflow: 'hidden',
  },
  skillFill: {
    height: '100%',
    backgroundColor: '#3498DB',
    borderRadius: 3,
  },
  addPlayerButton: {
    backgroundColor: '#3498DB',
    padding: 14,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 10,
  },
  addPlayerText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '600',
  },
  planCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 15,
    marginBottom: 10,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  selectedPlanCard: {
    borderColor: '#3498DB',
    backgroundColor: '#EBF5FB',
  },
  planName: {
    fontSize: 17,
    fontWeight: '600',
    color: '#2C3E50',
    marginBottom: 6,
  },
  planDuration: {
    fontSize: 13,
    color: '#7F8C8D',
    marginBottom: 12,
  },
  exercisesList: {
    borderTopWidth: 1,
    borderTopColor: '#ECF0F1',
    paddingTop: 10,
  },
  exerciseItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 6,
  },
  exerciseName: {
    fontSize: 13,
    color: '#34495E',
  },
  exerciseDetails: {
    fontSize: 12,
    color: '#7F8C8D',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2C3E50',
    marginBottom: 12,
  },
  formationCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 15,
    marginBottom: 10,
  },
  formationName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2C3E50',
    marginBottom: 4,
  },
  formationDescription: {
    fontSize: 13,
    color: '#7F8C8D',
  },
  tipsCard: {
    backgroundColor: '#FEF9E7',
    borderRadius: 10,
    padding: 15,
    borderLeftWidth: 4,
    borderLeftColor: '#F1C40F',
  },
  tipItem: {
    fontSize: 14,
    color: '#34495E',
    marginBottom: 8,
    lineHeight: 20,
  },
  matchCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 10,
    padding: 15,
    marginBottom: 10,
  },
  matchHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  opponent: {
    fontSize: 16,
    fontWeight: '600',
    color: '#2C3E50',
  },
  location: {
    fontSize: 12,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    fontWeight: '600',
  },
  home: {
    backgroundColor: '#D5F5E3',
    color: '#27AE60',
  },
  away: {
    backgroundColor: '#FADBD8',
    color: '#E74C3C',
  },
  matchDate: {
    fontSize: 13,
    color: '#7F8C8D',
    marginBottom: 8,
  },
  resultBadge: {
    backgroundColor: '#EBF5FB',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
    alignSelf: 'flex-start',
  },
  resultText: {
    fontSize: 13,
    color: '#3498DB',
    fontWeight: '600',
  },
  addMatchButton: {
    backgroundColor: '#3498DB',
    padding: 14,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 10,
  },
  addMatchText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '600',
  },
});
