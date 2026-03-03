import React from 'react';
import { View, Text } from 'react-native';
import styles from '../theme/styles';
import PrimaryButton from './PrimaryButton';

const ProviderCard = ({ provider, onBook }) => (
  <View style={styles.card}>
    <Text style={styles.subtitle}>{provider.providerId?.name || 'Provider'}</Text>
    <Text>Vehicle: {provider.vehicleType}</Text>
    <Text>Capacity: {provider.capacity} kg</Text>
    <Text>Rating: {provider.providerId?.rating || 5}</Text>
    <PrimaryButton title="Book Now" onPress={() => onBook(provider)} />
  </View>
);

export default ProviderCard;
