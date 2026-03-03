import { io } from 'socket.io-client';
import { SOCKET_URL } from '../utils/config';

export const socket = io(SOCKET_URL, {
  transports: ['websocket'],
  autoConnect: false,
});

export const authenticateSocket = token => {
  socket.auth = { token };
};
