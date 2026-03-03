const Booking = require('../models/Booking');
const User = require('../models/User');
const { sendPushNotification } = require('../utils/notifications');
const { canTransition } = require('../utils/bookingStateMachine');
const { updateProviderAvailability } = require('../utils/providerAvailability');

const createBooking = async (req, res, next) => {
  try {
    const { providerId, serviceType, cropType, load, dateTime } = req.body;

    const booking = await Booking.create({
      farmerId: req.user._id,
      providerId,
      serviceType,
      cropType,
      load,
      dateTime,
      trackingEnabled: true,
    });

    const provider = await User.findById(providerId);
    await sendPushNotification({
      token: provider?.fcmToken,
      title: 'New Booking Request',
      body: `You have a new ${serviceType} booking request`,
      data: { bookingId: booking._id.toString(), type: 'booking_created' },
    });

    return res.status(201).json({ message: 'Booking created', booking });
  } catch (error) {
    return next(error);
  }
};

const getMyBookings = async (req, res, next) => {
  try {
    const { page = 1, limit = 10, status } = req.query;
    const skip = (Number(page) - 1) * Number(limit);

    const query = req.user.role === 'farmer' ? { farmerId: req.user._id } : { providerId: req.user._id };
    if (status) query.status = status;

    const [bookings, total] = await Promise.all([
      Booking.find(query)
        .populate('farmerId', 'name phone')
        .populate('providerId', 'name phone')
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(Number(limit)),
      Booking.countDocuments(query),
    ]);

    return res.json({
      bookings,
      pagination: {
        page: Number(page),
        limit: Number(limit),
        total,
        totalPages: Math.ceil(total / Number(limit)),
      },
    });
  } catch (error) {
    return next(error);
  }
};

const updateBookingStatus = async (req, res, next) => {
  try {
    const { status } = req.body;
    const booking = await Booking.findById(req.params.id);

    if (!booking) return res.status(404).json({ message: 'Booking not found' });

    if (req.user.role !== 'provider' || booking.providerId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ message: 'Only assigned provider can update status' });
    }

    if (!canTransition(booking.status, status)) {
      return res.status(400).json({ message: `Invalid status transition from ${booking.status} to ${status}` });
    }

    booking.status = status;
    booking.trackingEnabled = ['accepted', 'ongoing'].includes(status);
    await booking.save();

    const farmer = await User.findById(booking.farmerId);
    const providerAvailable = await updateProviderAvailability(booking.providerId);

    const statusNotifications = {
      accepted: 'Provider accepted your booking',
      ongoing: 'Provider has started the job and is en route',
      completed: 'Your job has been completed',
      rejected: 'Provider rejected your booking',
    };

    await sendPushNotification({
      token: farmer?.fcmToken,
      title: 'Booking Update',
      body: statusNotifications[status],
      data: { bookingId: booking._id.toString(), type: `booking_${status}` },
    });

    return res.json({ message: 'Booking status updated', booking, providerAvailable });
  } catch (error) {
    return next(error);
  }
};

module.exports = { createBooking, getMyBookings, updateBookingStatus };
