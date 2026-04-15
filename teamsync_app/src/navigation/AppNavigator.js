// src/navigation/AppNavigator.js - Navigazione principale dell'app

import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

import HomeScreen from '../screens/HomeScreen';
import TeamsScreen from '../screens/TeamsScreen';
import TeamDetailScreen from '../screens/TeamDetailScreen';

const Stack = createStackNavigator();

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Home"
        screenOptions={{
          headerStyle: {
            backgroundColor: '#2C3E50',
          },
          headerTintColor: '#FFFFFF',
          headerTitleStyle: {
            fontWeight: 'bold',
          },
        }}
      >
        <Stack.Screen
          name="Home"
          component={HomeScreen}
          options={{
            title: 'TeamSync',
            headerShown: false,
          }}
        />
        <Stack.Screen
          name="Teams"
          component={TeamsScreen}
          options={({ route }) => ({
            title: 'Squadre',
          })}
        />
        <Stack.Screen
          name="TeamDetail"
          component={TeamDetailScreen}
          options={({ route }) => ({
            title: route.params?.team?.name || 'Dettaglio Squadra',
          })}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
