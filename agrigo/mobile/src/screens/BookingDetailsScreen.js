import React from 'react';
import { Text, View } from 'react-native';
import styles from '../theme/styles';

const BookingDetailsScreen = ({ route }) => {
  const booking = route.params?.booking;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Booking Details</Text>
      <View style={styles.card}>
        <Text>ID: {booking?._id}</Text>
        <Text>Service: {booking?.serviceType}</Text>
        <Text>Crop: {booking?.cropType}</Text>
        <Text>Load: {booking?.load} kg</Text>
        <Text>Status: {booking?.status}</Text>
      </View>
    </View>
  );
};

export default BookingDetailsScreen;
