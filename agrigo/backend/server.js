require('dotenv').config();
const http = require('http');
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const { Server } = require('socket.io');
const connectDB = require('./config/db');
const { initFirebase } = require('./config/firebase');
const authRoutes = require('./routes/authRoutes');
const serviceRoutes = require('./routes/serviceRoutes');
const bookingRoutes = require('./routes/bookingRoutes');
const recommendationRoutes = require('./routes/recommendationRoutes');
const initializeSocket = require('./sockets');
const { notFound, errorHandler } = require('./middleware/errorMiddleware');
const { globalLimiter, authLimiter } = require('./middleware/rateLimiter');

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: process.env.CORS_ORIGIN?.split(',') || '*',
    methods: ['GET', 'POST', 'PATCH'],
  },
});

connectDB();
initFirebase();
initializeSocket(io);

app.use(cors({ origin: process.env.CORS_ORIGIN?.split(',') || '*' }));
app.use(helmet());
app.use(morgan('dev'));
app.use(express.json({ limit: '1mb' }));
app.use(globalLimiter);

app.get('/health', (req, res) => res.json({ status: 'ok', service: 'AgriGo API' }));
app.use('/api/auth', authLimiter, authRoutes);
app.use('/api/services', serviceRoutes);
app.use('/api/bookings', bookingRoutes);
app.use('/api/recommendations', recommendationRoutes);

app.use(notFound);
app.use(errorHandler);

const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
  console.log(`🚀 AgriGo backend running on port ${PORT}`);
});
