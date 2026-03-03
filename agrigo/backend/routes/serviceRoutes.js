const express = require('express');
const { body, query } = require('express-validator');
const { upsertService, getNearbyProviders } = require('../controllers/serviceController');
const { auth, allowRoles } = require('../middleware/authMiddleware');
const { validateRequest } = require('../middleware/errorMiddleware');

const router = express.Router();

router.post(
  '/',
  auth,
  allowRoles('provider'),
  [body('vehicleType').notEmpty(), body('capacity').isNumeric()],
  validateRequest,
  upsertService
);

router.get(
  '/nearby',
  auth,
  [
    query('lng').isNumeric(),
    query('lat').isNumeric(),
    query('page').optional().isInt({ min: 1 }),
    query('limit').optional().isInt({ min: 1, max: 50 }),
  ],
  validateRequest,
  getNearbyProviders
);

module.exports = router;
