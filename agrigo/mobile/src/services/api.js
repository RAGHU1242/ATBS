import axios from 'axios';
import { API_BASE_URL } from '../utils/config';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

export const setAuthToken = token => {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common.Authorization;
  }
};

export default api;
