import React from 'react';
import { TouchableOpacity, Text } from 'react-native';
import styles from '../theme/styles';

const PrimaryButton = ({ title, onPress }) => (
  <TouchableOpacity style={styles.button} onPress={onPress} activeOpacity={0.85}>
    <Text style={styles.buttonText}>{title}</Text>
  </TouchableOpacity>
);

export default PrimaryButton;
