import React, { useState } from 'react';
import { Alert, ScrollView, Text, TextInput } from 'react-native';
import api from '../services/api';
import styles from '../theme/styles';
import PrimaryButton from '../components/PrimaryButton';

const BookingScreen = ({ route, navigation }) => {
  const provider = route.params?.provider;
  const [serviceType, setServiceType] = useState(provider?.vehicleType || 'tractor');
  const [cropType, setCropType] = useState('paddy');
  const [load, setLoad] = useState('1200');
  const [dateTime, setDateTime] = useState(new Date().toISOString());
  const [recommended, setRecommended] = useState(null);

  const getRecommendation = async () => {
    const { data } = await api.post('/recommendations', { cropType, load: Number(load) });
    setRecommended(data.recommendation);
  };

  const submitBooking = async () => {
    try {
      const { data } = await api.post('/bookings', {
        providerId: provider.providerId._id,
        serviceType,
        cropType,
        load: Number(load),
        dateTime,
      });
      Alert.alert('Booking confirmed', 'Your request has been sent to provider.');
      navigation.navigate('BookingDetails', { booking: data.booking });
    } catch (error) {
      Alert.alert('Booking failed', error.response?.data?.message || 'Try again.');
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Create Booking</Text>
      <TextInput style={styles.input} value={serviceType} onChangeText={setServiceType} placeholder="Service Type" />
      <TextInput style={styles.input} value={cropType} onChangeText={setCropType} placeholder="Crop Type" />
      <TextInput style={styles.input} value={load} onChangeText={setLoad} placeholder="Load (kg)" keyboardType="numeric" />
      <TextInput style={styles.input} value={dateTime} onChangeText={setDateTime} placeholder="ISO Date Time" />
      <PrimaryButton title="Get Recommendation" onPress={getRecommendation} />
      {recommended && <Text style={styles.subtitle}>Recommended: {recommended.vehicleType}</Text>}
      <PrimaryButton title="Confirm Booking" onPress={submitBooking} />
    </ScrollView>
  );
};

export default BookingScreen;
