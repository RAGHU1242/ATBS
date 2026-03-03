import React from 'react';
import { Text, View } from 'react-native';
import { useAuth } from '../context/AuthContext';
import styles from '../theme/styles';
import PrimaryButton from '../components/PrimaryButton';

const ProfileScreen = () => {
  const { user, logout } = useAuth();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Profile</Text>
      <View style={styles.card}>
        <Text>Name: {user?.name}</Text>
        <Text>Phone: {user?.phone}</Text>
        <Text>Role: {user?.role}</Text>
      </View>
      <PrimaryButton title="Logout" onPress={logout} />
    </View>
  );
};

export default ProfileScreen;
