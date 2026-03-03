const express = require('express');
const { body } = require('express-validator');
const { getRecommendation } = require('../controllers/recommendationController');
const { auth } = require('../middleware/authMiddleware');
const { validateRequest } = require('../middleware/errorMiddleware');

const router = express.Router();

router.post(
  '/',
  auth,
  [body('cropType').notEmpty(), body('load').isNumeric()],
  validateRequest,
  getRecommendation
);

module.exports = router;
