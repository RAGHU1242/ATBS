import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { useAuth } from '../context/AuthContext';
import SplashScreen from '../screens/SplashScreen';
import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';
import RoleSelectionScreen from '../screens/RoleSelectionScreen';
import FarmerHomeScreen from '../screens/FarmerHomeScreen';
import ProviderHomeScreen from '../screens/ProviderHomeScreen';
import MapScreen from '../screens/MapScreen';
import BookingScreen from '../screens/BookingScreen';
import BookingDetailsScreen from '../screens/BookingDetailsScreen';
import ProfileScreen from '../screens/ProfileScreen';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const FarmerTabs = () => (
  <Tab.Navigator>
    <Tab.Screen name="Home" component={FarmerHomeScreen} />
    <Tab.Screen name="Map" component={MapScreen} />
    <Tab.Screen name="Profile" component={ProfileScreen} />
  </Tab.Navigator>
);

const ProviderTabs = () => (
  <Tab.Navigator>
    <Tab.Screen name="Home" component={ProviderHomeScreen} />
    <Tab.Screen name="Map" component={MapScreen} />
    <Tab.Screen name="Profile" component={ProfileScreen} />
  </Tab.Navigator>
);

const RootNavigator = () => {
  const { loading, user } = useAuth();

  if (loading) return <SplashScreen />;

  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      {!user ? (
        <>
          <Stack.Screen name="Login" component={LoginScreen} />
          <Stack.Screen name="Register" component={RegisterScreen} />
          <Stack.Screen name="RoleSelection" component={RoleSelectionScreen} />
        </>
      ) : (
        <>
          <Stack.Screen name="Dashboard" component={user.role === 'farmer' ? FarmerTabs : ProviderTabs} options={{ headerShown: false }} />
          <Stack.Screen name="Booking" component={BookingScreen} />
          <Stack.Screen name="BookingDetails" component={BookingDetailsScreen} />
        </>
      )}
    </Stack.Navigator>
  );
};

export default RootNavigator;
