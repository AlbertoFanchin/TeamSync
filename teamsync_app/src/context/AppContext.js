// src/context/AppContext.js - Context per la gestione dello stato globale

import React, { createContext, useContext, useReducer } from 'react';

const AppContext = createContext();

const initialState = {
  players: [],
  teams: [],
  matches: [],
  selectedSport: null,
  currentTeam: null,
};

function appReducer(state, action) {
  switch (action.type) {
    case 'SET_PLAYERS':
      return { ...state, players: action.payload };
    case 'ADD_PLAYER':
      return { ...state, players: [...state.players, action.payload] };
    case 'UPDATE_PLAYER':
      return {
        ...state,
        players: state.players.map((p) =>
          p.id === action.payload.id ? action.payload : p
        ),
      };
    case 'DELETE_PLAYER':
      return {
        ...state,
        players: state.players.filter((p) => p.id !== action.payload),
      };
    case 'SET_TEAMS':
      return { ...state, teams: action.payload };
    case 'ADD_TEAM':
      return { ...state, teams: [...state.teams, action.payload] };
    case 'UPDATE_TEAM':
      return {
        ...state,
        teams: state.teams.map((t) =>
          t.id === action.payload.id ? action.payload : t
        ),
      };
    case 'DELETE_TEAM':
      return {
        ...state,
        teams: state.teams.filter((t) => t.id !== action.payload),
      };
    case 'SET_MATCHES':
      return { ...state, matches: action.payload };
    case 'ADD_MATCH':
      return { ...state, matches: [...state.matches, action.payload] };
    case 'UPDATE_MATCH':
      return {
        ...state,
        matches: state.matches.map((m) =>
          m.id === action.payload.id ? action.payload : m
        ),
      };
    case 'SET_SELECTED_SPORT':
      return { ...state, selectedSport: action.payload };
    case 'SET_CURRENT_TEAM':
      return { ...state, currentTeam: action.payload };
    default:
      return state;
  }
}

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState);

  const value = { state, dispatch };

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
}

export default AppContext;
