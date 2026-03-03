import { Alert, Platform } from 'react-native';
import messaging from '@react-native-firebase/messaging';

export const requestNotificationPermission = async () => {
  const authStatus = await messaging().requestPermission();
  const enabled =
    authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
    authStatus === messaging.AuthorizationStatus.PROVISIONAL;
  return enabled;
};

export const getFCMToken = async () => {
  if (Platform.OS === 'ios') {
    await messaging().registerDeviceForRemoteMessages();
  }
  return messaging().getToken();
};

export const setupForegroundNotifications = () =>
  messaging().onMessage(async remoteMessage => {
    Alert.alert(remoteMessage.notification?.title || 'AgriGo', remoteMessage.notification?.body || 'New update');
  });

export const setupBackgroundNotifications = () => {
  messaging().setBackgroundMessageHandler(async () => {});
};
