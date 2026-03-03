const express = require('express');
const { body } = require('express-validator');
const { register, login, me } = require('../controllers/authController');
const { auth } = require('../middleware/authMiddleware');
const { validateRequest } = require('../middleware/errorMiddleware');

const router = express.Router();

router.post(
  '/register',
  [
    body('name').notEmpty(),
    body('phone').isLength({ min: 8 }),
    body('password').isLength({ min: 6 }),
    body('role').isIn(['farmer', 'provider']),
  ],
  validateRequest,
  register
);

router.post('/login', [body('phone').notEmpty(), body('password').notEmpty()], validateRequest, login);
router.get('/me', auth, me);

module.exports = router;
