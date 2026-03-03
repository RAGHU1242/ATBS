const recommendationEngine = ({ cropType, load }) => {
  const normalizedCrop = (cropType || '').toLowerCase();

  if (normalizedCrop === 'paddy' && load > 2000) {
    return { vehicleType: 'heavy-harvester', reason: 'High paddy load requires heavy harvesting support.' };
  }

  if (['wheat', 'maize', 'corn'].includes(normalizedCrop) && load > 1500) {
    return { vehicleType: 'medium-harvester', reason: 'Medium-to-large grain load detected.' };
  }

  if (normalizedCrop === 'sugarcane' && load > 3000) {
    return { vehicleType: 'tractor-with-trailer', reason: 'Sugarcane bulk transport needs high-capacity tractor.' };
  }

  if (load <= 700) {
    return { vehicleType: 'mini-tractor', reason: 'Light load can be served efficiently by mini tractor.' };
  }

  return { vehicleType: 'standard-tractor', reason: 'Balanced recommendation for general agricultural work.' };
};

module.exports = recommendationEngine;
