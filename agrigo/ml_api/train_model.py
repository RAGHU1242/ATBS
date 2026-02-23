"""
train_model.py — AgriGo Decision Tree Model Trainer

Run this script ONCE to generate vehicle_model.pkl and label_encoder.pkl.
These files are loaded by app.py at startup.

Usage:
    python train_model.py

Generates:
    vehicle_model.pkl
    label_encoder.pkl
"""

import pickle
import numpy as np
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score

# ─── Crop index mapping ───────────────────────────────────────────────────────
CROP_INDEX = {
    "rice": 0, "wheat": 1, "maize": 2, "sugarcane": 3, "cotton": 4,
    "tomato": 5, "potato": 6, "onion": 7, "soybean": 8, "groundnut": 9
}

# ─── Training dataset ─────────────────────────────────────────────────────────
# Features: [crop_index, weight_kg]
# Labels: vehicle_type

raw_data = [
    # Pickup Van (< 500 kg, perishables)
    (5, 100, "Pickup Van"),   # tomato 100kg
    (5, 200, "Pickup Van"),   # tomato 200kg
    (6, 150, "Pickup Van"),   # potato 150kg
    (7, 300, "Pickup Van"),   # onion 300kg
    (0, 200, "Pickup Van"),   # rice 200kg
    (1, 250, "Pickup Van"),   # wheat 250kg
    (8, 200, "Pickup Van"),   # soybean 200kg
    (9, 180, "Pickup Van"),   # groundnut 180kg

    # Mini Truck (500–1000 kg)
    (0, 600, "Mini Truck"),   # rice 600kg
    (1, 800, "Mini Truck"),   # wheat 800kg
    (2, 750, "Mini Truck"),   # maize 750kg
    (5, 500, "Mini Truck"),   # tomato 500kg
    (6, 700, "Mini Truck"),   # potato 700kg
    (7, 900, "Mini Truck"),   # onion 900kg
    (8, 650, "Mini Truck"),   # soybean 650kg
    (9, 550, "Mini Truck"),   # groundnut 550kg
    (4, 600, "Mini Truck"),   # cotton 600kg

    # Tractor (1000–3000 kg, farm/rural transport)
    (0, 1500, "Tractor"),     # rice 1500kg
    (1, 2000, "Tractor"),     # wheat 2000kg
    (2, 1800, "Tractor"),     # maize 1800kg
    (3, 2500, "Tractor"),     # sugarcane 2500kg
    (4, 1200, "Tractor"),     # cotton 1200kg
    (8, 1100, "Tractor"),     # soybean 1100kg
    (9, 1300, "Tractor"),     # groundnut 1300kg
    (6, 1500, "Tractor"),     # potato 1500kg

    # Medium Truck (3000–7000 kg)
    (3, 4000, "Medium Truck"),  # sugarcane 4000kg
    (0, 5000, "Medium Truck"),  # rice 5000kg
    (1, 4500, "Medium Truck"),  # wheat 4500kg
    (2, 3500, "Medium Truck"),  # maize 3500kg
    (5, 3000, "Medium Truck"),  # tomato 3000kg
    (6, 4000, "Medium Truck"),  # potato 4000kg
    (7, 3500, "Medium Truck"),  # onion 3500kg
    (4, 5000, "Medium Truck"),  # cotton 5000kg

    # Large Truck (> 7000 kg)
    (3, 10000, "Large Truck"),  # sugarcane 10000kg
    (0, 8000, "Large Truck"),   # rice 8000kg
    (1, 9000, "Large Truck"),   # wheat 9000kg
    (2, 7500, "Large Truck"),   # maize 7500kg
    (3, 15000, "Large Truck"),  # sugarcane 15000kg
    (4, 8000, "Large Truck"),   # cotton 8000kg
    (5, 7000, "Large Truck"),   # tomato 7000kg
    (6, 10000, "Large Truck"),  # potato 10000kg
]

# Augment dataset with slight variations
augmented = []
for crop, weight, label in raw_data:
    for _ in range(5):  # 5 variations per sample
        noise = np.random.uniform(-50, 50)
        aug_weight = max(10, weight + noise)
        augmented.append((crop, aug_weight, label))

all_data = raw_data + augmented

# ─── Prepare features and labels ─────────────────────────────────────────────
X = np.array([[item[0], item[1]] for item in all_data])
y_raw = [item[2] for item in all_data]

le = LabelEncoder()
y = le.fit_transform(y_raw)

# ─── Train / Test split ───────────────────────────────────────────────────────
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y)

# ─── Train Decision Tree ──────────────────────────────────────────────────────
clf = DecisionTreeClassifier(
    max_depth=8,
    min_samples_split=2,
    min_samples_leaf=1,
    criterion="gini",
    random_state=42
)
clf.fit(X_train, y_train)

# ─── Evaluation ───────────────────────────────────────────────────────────────
y_pred = clf.predict(X_test)
accuracy = accuracy_score(y_test, y_pred)

print("\n" + "="*50)
print("  AgriGo Vehicle Model Training Complete")
print("="*50)
print(f"\nTraining samples: {len(X_train)}")
print(f"Test samples:     {len(X_test)}")
print(f"Accuracy:         {accuracy:.2%}")
print("\nClassification Report:")
print(classification_report(y_test, y_pred, target_names=le.classes_))

# ─── Save model and encoder ───────────────────────────────────────────────────
with open("vehicle_model.pkl", "wb") as f:
    pickle.dump(clf, f)

with open("label_encoder.pkl", "wb") as f:
    pickle.dump(le, f)

print("✅ Saved: vehicle_model.pkl")
print("✅ Saved: label_encoder.pkl")
print("\nModel is ready. Start the API with: gunicorn app:app")

# ─── Quick sanity test ────────────────────────────────────────────────────────
print("\n--- Sanity Check ---")
test_cases = [
    ("tomato", 5, 200),
    ("rice", 0, 5000),
    ("sugarcane", 3, 12000),
    ("wheat", 1, 800),
    ("potato", 6, 400),
]
for name, idx, w in test_cases:
    pred = clf.predict([[idx, w]])
    vehicle = le.inverse_transform(pred)[0]
    print(f"  {name:12} | {w:6} kg  →  {vehicle}")
