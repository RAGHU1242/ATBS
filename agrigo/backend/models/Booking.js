const mongoose = require('mongoose');

const bookingSchema = new mongoose.Schema(
  {
    farmerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    providerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    serviceType: { type: String, required: true },
    cropType: { type: String, required: true },
    load: { type: Number, required: true },
    dateTime: { type: Date, required: true },
    status: {
      type: String,
      enum: ['pending', 'accepted', 'ongoing', 'completed', 'rejected'],
      default: 'pending',
    },
    trackingEnabled: { type: Boolean, default: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Booking', bookingSchema);
