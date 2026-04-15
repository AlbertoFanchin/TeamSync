// src/screens/TeamsScreen.js - Lista squadre per sport selezionato

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
} from 'react-native';
import { useApp } from '../context/AppContext';
import { initialTeams } from '../data/mockData';

export default function TeamsScreen({ navigation, route }) {
  const { state, dispatch } = useApp();
  const { sportId } = route.params;

  const teams = initialTeams.filter((team) => team.sport === sportId);

  const handleTeamSelect = (team) => {
    dispatch({ type: 'SET_CURRENT_TEAM', payload: team });
    navigation.navigate('TeamDetail', { team });
  };

  const renderTeamItem = ({ item }) => (
    <TouchableOpacity
      style={styles.teamCard}
      onPress={() => handleTeamSelect(item)}
    >
      <View style={styles.teamHeader}>
        <Text style={styles.teamName}>{item.name}</Text>
        <Text style={styles.playerCount}>{item.players.length} giocatori</Text>
      </View>
      <View style={styles.teamInfo}>
        <Text style={styles.infoText}>📅 {item.trainingDays.join(', ')}</Text>
        <Text style={styles.infoText}>⏰ {item.trainingTime}</Text>
      </View>
      <View style={styles.coachInfo}>
        <Text style={styles.coachLabel}>Allenatore:</Text>
        <Text style={styles.coachName}>{item.coach}</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Le Tue Squadre</Text>
      </View>

      {teams.length === 0 ? (
        <View style={styles.emptyState}>
          <Text style={styles.emptyIcon}>🏆</Text>
          <Text style={styles.emptyText}>
            Nessuna squadra per questo sport
          </Text>
          <Text style={styles.emptySubtext}>
            Crea la tua prima squadra!
          </Text>
        </View>
      ) : (
        <FlatList
          data={teams}
          renderItem={renderTeamItem}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContent}
        />
      )}

      <TouchableOpacity style={styles.addButton}>
        <Text style={styles.addButtonText}>+ Nuova Squadra</Text>
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
    backgroundColor: '#2C3E50',
    padding: 20,
    paddingTop: 50,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  listContent: {
    padding: 15,
  },
  teamCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 18,
    marginBottom: 12,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  teamHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  teamName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#2C3E50',
    flex: 1,
  },
  playerCount: {
    fontSize: 14,
    color: '#7F8C8D',
    backgroundColor: '#ECF0F1',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  teamInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  infoText: {
    fontSize: 14,
    color: '#34495E',
  },
  coachInfo: {
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: '#ECF0F1',
    paddingTop: 12,
  },
  coachLabel: {
    fontSize: 13,
    color: '#7F8C8D',
    marginRight: 6,
  },
  coachName: {
    fontSize: 13,
    color: '#2C3E50',
    fontWeight: '600',
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  emptyIcon: {
    fontSize: 64,
    marginBottom: 16,
  },
  emptyText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#7F8C8D',
    marginBottom: 8,
  },
  emptySubtext: {
    fontSize: 14,
    color: '#95A5A6',
    textAlign: 'center',
  },
  addButton: {
    backgroundColor: '#3498DB',
    margin: 15,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  addButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
});
