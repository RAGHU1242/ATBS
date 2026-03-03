const express = require('express');
const { body, query } = require('express-validator');
const { createBooking, getMyBookings, updateBookingStatus } = require('../controllers/bookingController');
const { auth } = require('../middleware/authMiddleware');
const { validateRequest } = require('../middleware/errorMiddleware');

const router = express.Router();

router.post(
  '/',
  auth,
  [
    body('providerId').isMongoId(),
    body('serviceType').notEmpty(),
    body('cropType').notEmpty(),
    body('load').isNumeric(),
    body('dateTime').isISO8601(),
  ],
  validateRequest,
  createBooking
);

router.get(
  '/',
  auth,
  [query('page').optional().isInt({ min: 1 }), query('limit').optional().isInt({ min: 1, max: 50 })],
  validateRequest,
  getMyBookings
);

router.patch(
  '/:id/status',
  auth,
  [body('status').isIn(['accepted', 'ongoing', 'completed', 'rejected'])],
  validateRequest,
  updateBookingStatus
);

module.exports = router;
