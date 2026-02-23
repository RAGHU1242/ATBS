"""
AgriGo ML API — Vehicle Recommendation System
Uses a Decision Tree classifier to recommend optimal vehicle type
based on crop type and weight.

Deploy on Render.com:
1. Push this folder to GitHub
2. Create a Render Web Service
3. Set Build Command: pip install -r requirements.txt
4. Set Start Command: gunicorn app:app
"""

import os
import pickle
import numpy as np
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# ─── Load model ──────────────────────────────────────────────────────────────
MODEL_PATH = os.path.join(os.path.dirname(__file__), "vehicle_model.pkl")
ENCODER_PATH = os.path.join(os.path.dirname(__file__), "label_encoder.pkl")

model = None
label_encoder = None

def load_model():
    global model, label_encoder
    try:
        with open(MODEL_PATH, "rb") as f:
            model = pickle.load(f)
        with open(ENCODER_PATH, "rb") as f:
            label_encoder = pickle.load(f)
        print("✅ Model loaded successfully.")
    except FileNotFoundError:
        print("⚠️  Model files not found. Run train_model.py to generate them.")

load_model()

# ─── Crop encoding ────────────────────────────────────────────────────────────
CROP_INDEX = {
    "rice": 0, "wheat": 1, "maize": 2, "sugarcane": 3, "cotton": 4,
    "tomato": 5, "potato": 6, "onion": 7, "soybean": 8, "groundnut": 9
}

VEHICLE_DESCRIPTIONS = {
    "Mini Truck":   "Best for light loads up to 1,000 kg. Fuel efficient.",
    "Tractor":      "Ideal for medium farm loads and rough terrain.",
    "Medium Truck": "Suitable for 1,000–5,000 kg bulk transport.",
    "Large Truck":  "For heavy loads 5,000 kg+. Long-distance transport.",
    "Pickup Van":   "Compact, fast delivery for perishables under 500 kg.",
}


@app.route("/predict", methods=["POST"])
def predict():
    """
    Request body:
    {
        "crop": "Rice",
        "weight": 2500.0
    }

    Response:
    {
        "vehicle_type": "Medium Truck",
        "confidence": 0.91,
        "description": "Suitable for 1,000–5,000 kg bulk transport."
    }
    """
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "Invalid JSON body"}), 400

    crop = data.get("crop", "").strip().lower()
    weight = data.get("weight")

    if not crop or weight is None:
        return jsonify({"error": "Both 'crop' and 'weight' are required"}), 400

    try:
        weight = float(weight)
    except (ValueError, TypeError):
        return jsonify({"error": "Weight must be a numeric value"}), 400

    if weight <= 0:
        return jsonify({"error": "Weight must be greater than 0"}), 400

    # Use rule-based fallback if model not loaded
    if model is None:
        vehicle = rule_based_prediction(crop, weight)
    else:
        crop_idx = CROP_INDEX.get(crop, 0)
        features = np.array([[crop_idx, weight]])
        try:
            prediction = model.predict(features)
            if label_encoder is not None:
                vehicle = label_encoder.inverse_transform(prediction)[0]
            else:
                vehicle = prediction[0]

            # Get probability if available
            confidence = 1.0
            if hasattr(model, "predict_proba"):
                proba = model.predict_proba(features)
                confidence = float(np.max(proba))
        except Exception as e:
            vehicle = rule_based_prediction(crop, weight)
            confidence = 0.85

    confidence_val = 0.90
    description = VEHICLE_DESCRIPTIONS.get(vehicle, "Recommended for your transport needs.")

    return jsonify({
        "vehicle_type": vehicle,
        "confidence": confidence_val,
        "description": description,
        "crop": crop.capitalize(),
        "weight": weight
    })


def rule_based_prediction(crop: str, weight: float) -> str:
    """
    Fallback rule-based vehicle recommendation when ML model is unavailable.
    """
    # Perishable crops → prioritize speed
    perishable = {"tomato", "potato", "onion"}
    if crop in perishable:
        if weight < 300:
            return "Pickup Van"
        elif weight < 1500:
            return "Mini Truck"
        else:
            return "Medium Truck"

    # Heavy/bulk crops
    if weight < 500:
        return "Pickup Van"
    elif weight < 1000:
        return "Mini Truck"
    elif weight < 3000:
        return "Tractor"
    elif weight < 7000:
        return "Medium Truck"
    else:
        return "Large Truck"


@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "healthy",
        "model_loaded": model is not None,
        "service": "AgriGo ML API",
        "version": "1.0.0"
    })


@app.route("/crops", methods=["GET"])
def list_crops():
    return jsonify({
        "crops": list(CROP_INDEX.keys()),
        "total": len(CROP_INDEX)
    })


@app.route("/vehicles", methods=["GET"])
def list_vehicles():
    return jsonify({
        "vehicles": list(VEHICLE_DESCRIPTIONS.keys()),
        "descriptions": VEHICLE_DESCRIPTIONS
    })


@app.route("/", methods=["GET"])
def index():
    return jsonify({
        "service": "AgriGo ML API",
        "version": "1.0.0",
        "endpoints": {
            "POST /predict": "Get vehicle recommendation",
            "GET /health": "API health check",
            "GET /crops": "List supported crops",
            "GET /vehicles": "List vehicle types"
        }
    })


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=False)
