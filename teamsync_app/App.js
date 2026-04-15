// App.js - Punto di ingresso principale dell'applicazione TeamSync

import React from 'react';
import { SafeAreaView, StatusBar } from 'react-native';
import { AppProvider } from './src/context/AppContext';
import AppNavigator from './src/navigation/AppNavigator';

export default function App() {
  return (
    <AppProvider>
      <SafeAreaView style={{ flex: 1 }}>
        <StatusBar barStyle="light-content" backgroundColor="#2C3E50" />
        <AppNavigator />
      </SafeAreaView>
    </AppProvider>
  );
}
