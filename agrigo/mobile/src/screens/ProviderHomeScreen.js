import React, { useEffect, useRef, useState } from 'react';
import { Alert, ScrollView, Text, View } from 'react-native';
import api from '../services/api';
import PrimaryButton from '../components/PrimaryButton';
import styles from '../theme/styles';
import { useAuth } from '../context/AuthContext';
import { socket } from '../services/socket';
import { requestLocationPermission, startProviderTracking, stopProviderTracking } from '../utils/locationTracking';

const ProviderHomeScreen = ({ navigation }) => {
  const [bookings, setBookings] = useState([]);
  const trackingRefs = useRef({});
  const { user } = useAuth();

  const fetchBookings = async () => {
    try {
      const { data } = await api.get('/bookings?page=1&limit=20');
      setBookings(data.bookings);
    } catch (error) {
      Alert.alert('Unable to fetch bookings');
    }
  };

  useEffect(() => {
    fetchBookings();
    socket.connect();
    return () => {
      Object.values(trackingRefs.current).forEach(stopProviderTracking);
      socket.disconnect();
    };
  }, []);

  const updateStatus = async (id, status) => {
    await api.patch(`/bookings/${id}/status`, { status });
    fetchBookings();
  };

  const beginTracking = async bookingId => {
    const granted = await requestLocationPermission();
    if (!granted) return Alert.alert('Location permission is required for tracking');

    if (!trackingRefs.current[bookingId]) {
      socket.emit('joinBookingRoom', { bookingId });
      trackingRefs.current[bookingId] = startProviderTracking({ bookingId, providerId: user.id });
      Alert.alert('Tracking Started', 'Live GPS updates are being sent every 5 seconds.');
    }
  };

  const endTracking = bookingId => {
    stopProviderTracking(trackingRefs.current[bookingId]);
    delete trackingRefs.current[bookingId];
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Assigned Bookings</Text>
      {bookings.map(booking => (
        <View key={booking._id} style={styles.card}>
          <Text>Service: {booking.serviceType}</Text>
          <Text>Crop: {booking.cropType}</Text>
          <Text>Status: {booking.status}</Text>
          <PrimaryButton title="Accept" onPress={() => updateStatus(booking._id, 'accepted')} />
          <PrimaryButton title="Start Job" onPress={() => updateStatus(booking._id, 'ongoing')} />
          <PrimaryButton title="Complete" onPress={() => updateStatus(booking._id, 'completed')} />
          <PrimaryButton title="Start GPS Tracking" onPress={() => beginTracking(booking._id)} />
          <PrimaryButton title="Stop GPS Tracking" onPress={() => endTracking(booking._id)} />
          <PrimaryButton title="Track on map" onPress={() => navigation.navigate('Map', { booking })} />
        </View>
      ))}
    </ScrollView>
  );
};

export default ProviderHomeScreen;
