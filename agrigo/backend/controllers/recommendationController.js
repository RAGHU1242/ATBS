const recommendationEngine = require('../utils/recommendationEngine');

const getRecommendation = async (req, res, next) => {
  try {
    const { cropType, load } = req.body;
    const recommendation = recommendationEngine({ cropType, load: Number(load) });

    return res.json({ recommendation });
  } catch (error) {
    return next(error);
  }
};

module.exports = { getRecommendation };
