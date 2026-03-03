const mongoose = require('mongoose');

const serviceSchema = new mongoose.Schema(
  {
    providerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    vehicleType: { type: String, required: true },
    capacity: { type: Number, required: true },
    availability: { type: Boolean, default: true },
    currentLocation: {
      type: {
        type: String,
        enum: ['Point'],
        default: 'Point',
      },
      coordinates: {
        type: [Number],
        default: [0, 0],
      },
    },
  },
  { timestamps: true }
);

serviceSchema.index({ currentLocation: '2dsphere' });

module.exports = mongoose.model('Service', serviceSchema);
