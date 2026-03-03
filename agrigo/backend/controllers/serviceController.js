const Service = require('../models/Service');

const upsertService = async (req, res, next) => {
  try {
    const { vehicleType, capacity, availability, latitude, longitude } = req.body;

    const service = await Service.findOneAndUpdate(
      { providerId: req.user._id },
      {
        providerId: req.user._id,
        vehicleType,
        capacity,
        availability,
        currentLocation: {
          type: 'Point',
          coordinates: [Number(longitude || 0), Number(latitude || 0)],
        },
      },
      { upsert: true, new: true }
    );

    return res.json({ message: 'Service profile saved', service });
  } catch (error) {
    return next(error);
  }
};

const getNearbyProviders = async (req, res, next) => {
  try {
    const { lng, lat, maxDistance = 10000, page = 1, limit = 10 } = req.query;
    const skip = (Number(page) - 1) * Number(limit);

    const baseQuery = {
      availability: true,
      currentLocation: {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [Number(lng), Number(lat)],
          },
          $maxDistance: Number(maxDistance),
        },
      },
    };

    const [services, total] = await Promise.all([
      Service.find(baseQuery)
        .populate('providerId', 'name phone rating role')
        .skip(skip)
        .limit(Number(limit)),
      Service.countDocuments(baseQuery),
    ]);

    return res.json({
      services,
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

module.exports = { upsertService, getNearbyProviders };
