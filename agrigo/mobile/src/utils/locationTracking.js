import { PermissionsAndroid, Platform } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import { socket } from '../services/socket';

export const requestLocationPermission = async () => {
  if (Platform.OS !== 'android') return true;
  const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION);
  return granted === PermissionsAndroid.RESULTS.GRANTED;
};

export const getCurrentLocation = () =>
  new Promise((resolve, reject) => {
    Geolocation.getCurrentPosition(
      position => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
      },
      reject,
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 5000 }
    );
  });

export const startProviderTracking = ({ bookingId, providerId }) => {
  return Geolocation.watchPosition(
    position => {
      socket.emit('sendLocation', {
        bookingId,
        providerId,
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      });
    },
    () => {},
    { enableHighAccuracy: true, distanceFilter: 5, interval: 5000, fastestInterval: 3000 }
  );
};

export const stopProviderTracking = watchId => {
  if (watchId !== null && watchId !== undefined) {
    Geolocation.clearWatch(watchId);
  }
};
