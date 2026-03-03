import React, { useEffect, useState } from 'react';
import { Alert, Text, TextInput, View } from 'react-native';
import { useAuth } from '../context/AuthContext';
import styles from '../theme/styles';
import PrimaryButton from '../components/PrimaryButton';
import { getCurrentLocation, requestLocationPermission } from '../utils/locationTracking';

const RegisterScreen = ({ route }) => {
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState(route.params?.role || 'farmer');
  const [coords, setCoords] = useState({ latitude: 0, longitude: 0 });
  const { register } = useAuth();

  useEffect(() => {
    const initLocation = async () => {
      try {
        const granted = await requestLocationPermission();
        if (!granted) return;
        const location = await getCurrentLocation();
        setCoords(location);
      } catch {
        // non-blocking
      }
    };

    initLocation();
  }, []);

  const onSubmit = async () => {
    try {
      await register({ name, phone, password, role, ...coords });
    } catch (error) {
      Alert.alert('Registration failed', error.response?.data?.message || 'Please try again.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Create AgriGo Account</Text>
      <TextInput style={styles.input} placeholder="Full Name" value={name} onChangeText={setName} />
      <TextInput style={styles.input} placeholder="Phone" value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
      <TextInput style={styles.input} placeholder="Password" value={password} onChangeText={setPassword} secureTextEntry />
      <RoleSelection role={role} onChange={setRole} />
      <PrimaryButton title="Register" onPress={onSubmit} />
    </View>
  );
};

const RoleSelection = ({ role, onChange }) => (
  <View>
    <Text style={styles.subtitle}>Select Role</Text>
    <PrimaryButton title={`Farmer ${role === 'farmer' ? '✓' : ''}`} onPress={() => onChange('farmer')} />
    <PrimaryButton title={`Service Provider ${role === 'provider' ? '✓' : ''}`} onPress={() => onChange('provider')} />
  </View>
);

export default RegisterScreen;
