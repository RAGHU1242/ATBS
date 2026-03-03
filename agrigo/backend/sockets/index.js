const jwt = require('jsonwebtoken');
const Service = require('../models/Service');
const Booking = require('../models/Booking');

const initializeSocket = (io) => {
  io.use((socket, next) => {
    try {
      const raw = socket.handshake.auth?.token || socket.handshake.headers.authorization;
      const token = raw?.startsWith('Bearer ') ? raw.split(' ')[1] : raw;

      if (!token) return next(new Error('Unauthorized socket connection'));

      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      socket.userId = decoded.id;
      return next();
    } catch (error) {
      return next(new Error('Unauthorized socket connection'));
    }
  });

  io.on('connection', (socket) => {
    socket.on('joinBookingRoom', async ({ bookingId }) => {
      if (!bookingId) return;
      const booking = await Booking.findById(bookingId).select('farmerId providerId');
      if (!booking) return;

      const canJoin = [booking.farmerId.toString(), booking.providerId.toString()].includes(socket.userId);
      if (!canJoin) return;

      socket.join(bookingId);
    });

    socket.on('sendLocation', async ({ bookingId, providerId, latitude, longitude }) => {
      if (!bookingId || !providerId) return;
      if (socket.userId !== providerId) return;

      const booking = await Booking.findById(bookingId).select('providerId trackingEnabled status');
      if (!booking || booking.providerId.toString() !== providerId || !booking.trackingEnabled) return;

      await Service.findOneAndUpdate(
        { providerId },
        {
          currentLocation: {
            type: 'Point',
            coordinates: [Number(longitude), Number(latitude)],
          },
        }
      );

      io.to(bookingId).emit('receiveLocation', {
        bookingId,
        providerId,
        latitude,
        longitude,
        status: booking.status,
        timestamp: new Date().toISOString(),
      });
    });
  });
};

module.exports = initializeSocket;
