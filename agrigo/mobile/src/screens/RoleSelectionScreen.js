import React from 'react';
import { Text, View } from 'react-native';
import styles from '../theme/styles';
import PrimaryButton from '../components/PrimaryButton';

const RoleSelectionScreen = ({ navigation }) => (
  <View style={styles.container}>
    <Text style={styles.title}>Select your role</Text>
    <PrimaryButton title="I am a Farmer" onPress={() => navigation.navigate('Register', { role: 'farmer' })} />
    <PrimaryButton title="I am a Service Provider" onPress={() => navigation.navigate('Register', { role: 'provider' })} />
  </View>
);

export default RoleSelectionScreen;
