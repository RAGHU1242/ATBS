import React, { useEffect, useState } from 'react';
import { Alert, ScrollView, Text, View } from 'react-native';
import api from '../services/api';
import styles from '../theme/styles';
import ProviderCard from '../components/ProviderCard';
import { getCurrentLocation, requestLocationPermission } from '../utils/locationTracking';
import PrimaryButton from '../components/PrimaryButton';

const FarmerHomeScreen = ({ navigation }) => {
  const [providers, setProviders] = useState([]);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(false);

  const loadNearby = async (nextPage = 1) => {
    try {
      const granted = await requestLocationPermission();
      if (!granted) throw new Error('Location permission denied');
      const { latitude, longitude } = await getCurrentLocation();

      const { data } = await api.get(
        `/services/nearby?lng=${longitude}&lat=${latitude}&maxDistance=20000&page=${nextPage}&limit=10`
      );

      setProviders(nextPage === 1 ? data.services : [...providers, ...data.services]);
      setPage(nextPage);
      setHasMore(nextPage < data.pagination.totalPages);
    } catch (error) {
      Alert.alert('Unable to fetch providers', error.message);
    }
  };

  useEffect(() => {
    loadNearby(1);
  }, []);

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Nearby Providers</Text>
      {providers.map(provider => (
        <ProviderCard
          key={provider._id}
          provider={provider}
          onBook={selected => navigation.navigate('Booking', { provider: selected })}
        />
      ))}
      {hasMore && <PrimaryButton title="Load More" onPress={() => loadNearby(page + 1)} />}
      <View style={styles.card}>
        <Text style={styles.subtitle} onPress={() => navigation.navigate('Map')}>Open Live Map</Text>
      </View>
    </ScrollView>
  );
};

export default FarmerHomeScreen;
