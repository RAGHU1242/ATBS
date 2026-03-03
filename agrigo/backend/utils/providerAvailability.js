const Booking = require('../models/Booking');
const Service = require('../models/Service');

const ACTIVE_STATUSES = ['accepted', 'ongoing'];

const updateProviderAvailability = async (providerId) => {
  const activeCount = await Booking.countDocuments({ providerId, status: { $in: ACTIVE_STATUSES } });
  const available = activeCount === 0;

  await Service.findOneAndUpdate(
    { providerId },
    { availability: available },
    { new: true }
  );

  return available;
};

module.exports = { updateProviderAvailability, ACTIVE_STATUSES };
