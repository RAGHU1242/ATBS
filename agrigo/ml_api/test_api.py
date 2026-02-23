"""
test_api.py — Quick test for the AgriGo ML API
Run: python test_api.py
"""

import requests
import json

BASE_URL = "http://localhost:5000"

def test_health():
    r = requests.get(f"{BASE_URL}/health")
    print("Health:", r.json())
    assert r.status_code == 200

def test_predict(crop, weight, expected=None):
    payload = {"crop": crop, "weight": weight}
    r = requests.post(f"{BASE_URL}/predict", json=payload)
    result = r.json()
    print(f"  {crop:12} | {weight:7} kg  →  {result.get('vehicle_type')}"
          f"  (confidence: {result.get('confidence', 0):.0%})")
    assert r.status_code == 200
    assert "vehicle_type" in result
    return result

if __name__ == "__main__":
    print("\n=== AgriGo ML API Tests ===\n")

    test_health()

    print("\nPrediction Tests:")
    test_predict("rice", 200)
    test_predict("rice", 800)
    test_predict("wheat", 2500)
    test_predict("sugarcane", 8000)
    test_predict("tomato", 150)
    test_predict("potato", 4000)

    print("\n✅ All tests passed!")
