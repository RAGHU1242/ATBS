import React, { useState } from 'react';
import { Alert, Text, TextInput, View } from 'react-native';
import { useAuth } from '../context/AuthContext';
import styles from '../theme/styles';
import PrimaryButton from '../components/PrimaryButton';

const LoginScreen = ({ navigation }) => {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();

  const onSubmit = async () => {
    try {
      await login({ phone, password });
    } catch (error) {
      Alert.alert('Login failed', error.response?.data?.message || 'Please try again.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome Back</Text>
      <TextInput style={styles.input} placeholder="Phone" value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
      <TextInput style={styles.input} placeholder="Password" value={password} onChangeText={setPassword} secureTextEntry />
      <PrimaryButton title="Login" onPress={onSubmit} />
      <PrimaryButton title="Create account" onPress={() => navigation.navigate('Register')} />
    </View>
  );
};

export default LoginScreen;
