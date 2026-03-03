import React from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import styles from '../theme/styles';

const SplashScreen = () => (
  <View style={[styles.container, { justifyContent: 'center', alignItems: 'center' }]}>
    <Text style={styles.title}>AgriGo</Text>
    <ActivityIndicator size="large" color="#1F6E3E" />
  </View>
);

export default SplashScreen;
