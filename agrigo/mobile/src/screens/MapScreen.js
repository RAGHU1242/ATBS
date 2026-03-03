import React, { useEffect, useMemo, useState } from 'react';
import { Alert, Text, View } from 'react-native';
import MapView, { Marker } from 'react-native-maps';
import api from '../services/api';
import { socket } from '../services/socket';
import styles from '../theme/styles';
import { getCurrentLocation, requestLocationPermission } from '../utils/locationTracking';

const MapScreen = ({ route }) => {
  const [providers, setProviders] = useState([]);
  const [liveLocation, setLiveLocation] = useState(null);
  const [currentLocation, setCurrentLocation] = useState({ latitude: 12.9716, longitude: 77.5946 });
  const booking = route.params?.booking;

  useEffect(() => {
    const fetchProviders = async () => {
      try {
        const granted = await requestLocationPermission();
        if (!granted) throw new Error('Location permission denied');
        const location = await getCurrentLocation();
        setCurrentLocation(location);

        const { data } = await api.get(
          `/services/nearby?lng=${location.longitude}&lat=${location.latitude}&maxDistance=25000&page=1&limit=50`
        );
        setProviders(data.services);
      } catch (error) {
        Alert.alert('Failed to load provider map data', error.message);
      }
    };

    fetchProviders();
  }, []);

  useEffect(() => {
    socket.connect();

    if (booking?._id) {
      socket.emit('joinBookingRoom', { bookingId: booking._id });
    }

    socket.on('receiveLocation', payload => {
      setLiveLocation({ latitude: payload.latitude, longitude: payload.longitude });
    });

    return () => {
      socket.off('receiveLocation');
      socket.disconnect();
    };
  }, [booking?._id]);

  const region = useMemo(
    () => ({
      latitude: currentLocation.latitude,
      longitude: currentLocation.longitude,
      latitudeDelta: 0.08,
      longitudeDelta: 0.08,
    }),
    [currentLocation]
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Live Service Map</Text>
      <MapView style={{ flex: 1, borderRadius: 12 }} region={region}>
        <Marker coordinate={currentLocation} pinColor="green" title="You" />
        {providers.map(service => (
          <Marker
            key={service._id}
            coordinate={{
              latitude: service.currentLocation.coordinates[1],
              longitude: service.currentLocation.coordinates[0],
            }}
            title={service.providerId?.name || 'Provider'}
            description={`${service.vehicleType} • ${service.capacity}kg`}
          />
        ))}
        {liveLocation && <Marker coordinate={liveLocation} pinColor="blue" title="Live Vehicle" />}
      </MapView>
    </View>
  );
};

export default MapScreen;
