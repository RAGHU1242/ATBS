import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api, { setAuthToken } from '../services/api';
import { authenticateSocket } from '../services/socket';
import { getFCMToken, requestNotificationPermission, setupForegroundNotifications } from '../services/firebaseMessaging';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = setupForegroundNotifications();
    return unsubscribe;
  }, []);

  useEffect(() => {
    const bootstrap = async () => {
      try {
        const storedToken = await AsyncStorage.getItem('token');
        const storedUser = await AsyncStorage.getItem('user');

        if (storedToken && storedUser) {
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
          setAuthToken(storedToken);
          authenticateSocket(storedToken);
        }
      } finally {
        setLoading(false);
      }
    };

    bootstrap();
  }, []);

  const getDeviceToken = async () => {
    const allowed = await requestNotificationPermission();
    if (!allowed) return null;
    return getFCMToken();
  };

  const login = async payload => {
    const fcmToken = await getDeviceToken();
    const { data } = await api.post('/auth/login', { ...payload, fcmToken });
    await AsyncStorage.multiSet([
      ['token', data.token],
      ['user', JSON.stringify(data.user)],
    ]);
    setToken(data.token);
    setUser(data.user);
    setAuthToken(data.token);
    authenticateSocket(data.token);
  };

  const register = async payload => {
    const fcmToken = await getDeviceToken();
    const { data } = await api.post('/auth/register', { ...payload, fcmToken });
    await AsyncStorage.multiSet([
      ['token', data.token],
      ['user', JSON.stringify(data.user)],
    ]);
    setToken(data.token);
    setUser(data.user);
    setAuthToken(data.token);
    authenticateSocket(data.token);
  };

  const logout = async () => {
    await AsyncStorage.multiRemove(['token', 'user']);
    setToken(null);
    setUser(null);
    setAuthToken(null);
    authenticateSocket(null);
  };

  const value = useMemo(() => ({ user, token, loading, login, register, logout }), [user, token, loading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => useContext(AuthContext);
