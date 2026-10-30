"""
Flask inference service for the Student Performance Intelligence System.

Loads the two trained scikit-learn models (see train_models.py) and exposes
two prediction endpoints consumed by the Spring Boot backend:

    POST /predict/performance  -> predicted final score + performance category
    POST /predict/risk         -> at-risk probability + risk level
    GET  /health               -> service + model status

Run:  ml/.venv/Scripts/python.exe ml/app.py
Port: 5001 (localhost only)
"""

import os

import joblib
from flask import Flask, jsonify, request

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, "models")

app = Flask(__name__)

# Dataset reference means (UCI student-mat) used for simple factor explanations.
REFERENCE_MEANS = {
    "G1": 10.9,
    "G2": 10.7,
    "studytime": 2.0,
    "failures": 0.33,
    "absences": 5.7,
}


def load_models():
    perf_path = os.path.join(MODELS_DIR, "performance_model.joblib")
    risk_path = os.path.join(MODELS_DIR, "risk_model.joblib")
    if not (os.path.exists(perf_path) and os.path.exists(risk_path)):
        raise RuntimeError(
            "Trained models not found. Run train_models.py first."
        )
    return joblib.load(perf_path), joblib.load(risk_path)


performance_model, risk_model = load_models()

FEATURES = ["G1", "G2", "studytime", "failures", "absences"]


def extract_features(payload: dict) -> dict:
    """Validate and convert the incoming JSON payload into model features."""
    cleaned = {}
    missing = [f for f in FEATURES if payload.get(f) is None]
    if missing:
        raise ValueError(f"Missing required features: {', '.join(missing)}")

    cleaned["G1"] = float(payload["G1"])
    cleaned["G2"] = float(payload["G2"])
    cleaned["studytime"] = float(payload["studytime"])
    cleaned["failures"] = float(payload["failures"])
    cleaned["absences"] = float(payload["absences"])

    if not (0 <= cleaned["G1"] <= 20) or not (0 <= cleaned["G2"] <= 20):
        raise ValueError("G1 and G2 must be on the 0-20 scale")
    if cleaned["absences"] < 0:
        raise ValueError("absences cannot be negative")
    if cleaned["failures"] < 0:
        raise ValueError("failures cannot be negative")
    return cleaned


def performance_category(score_100: float) -> str:
    if score_100 >= 80:
        return "EXCELLENT"
    if score_100 >= 60:
        return "GOOD"
    if score_100 >= 40:
        return "AVERAGE"
    return "POOR"


def risk_level(probability: float) -> str:
    if probability >= 0.7:
        return "HIGH"
    if probability >= 0.4:
        return "MEDIUM"
    return "LOW"


def major_factors(f: dict) -> list:
    """Simple, honest explanations: compare each feature to the dataset mean."""
    factors = []
    if f["G2"] < REFERENCE_MEANS["G2"] - 2:
        factors.append(f"Low recent assessment score (G2={f['G2']:.1f} vs class mean ~{REFERENCE_MEANS['G2']})")
    elif f["G2"] > REFERENCE_MEANS["G2"] + 2:
        factors.append(f"Strong recent assessment score (G2={f['G2']:.1f})")
    if f["G1"] < REFERENCE_MEANS["G1"] - 2:
        factors.append(f"Low first-period score (G1={f['G1']:.1f} vs class mean ~{REFERENCE_MEANS['G1']})")
    elif f["G1"] > REFERENCE_MEANS["G1"] + 2:
        factors.append(f"Strong first-period score (G1={f['G1']:.1f})")
    if f["absences"] > REFERENCE_MEANS["absences"] * 2:
        factors.append(f"High absences ({f['absences']:.0f} vs class mean ~{REFERENCE_MEANS['absences']:.0f})")
    if f["failures"] > 0:
        factors.append(f"{f['failures']:.0f} previous subject failure(s)")
    if f["studytime"] <= 1:
        factors.append("Very low weekly study time")
    if not factors:
        factors.append("All indicators are near class-average levels")
    return factors


@app.errorhandler(Exception)
def handle_error(exc):
    return jsonify({"error": str(exc)}), 400


@app.get("/health")
def health():
    return jsonify({
        "status": "UP",
        "models": {
            "performance": performance_model is not None,
            "risk": risk_model is not None,
        },
    })


@app.post("/predict/performance")
def predict_performance():
    payload = request.get_json(silent=True) or {}
    features = extract_features(payload)

    import pandas as pd
    X = pd.DataFrame([features])[FEATURES]
    predicted_20 = float(performance_model.predict(X)[0])
    predicted_20 = max(0.0, min(20.0, predicted_20))

    return jsonify({
        "predictedScore": round(predicted_20 / 20 * 100, 1),
        "performanceCategory": performance_category(predicted_20 / 20 * 100),
        "majorFactors": major_factors(features),
    })


@app.post("/predict/risk")
def predict_risk():
    payload = request.get_json(silent=True) or {}
    features = extract_features(payload)

    import pandas as pd
    X = pd.DataFrame([features])[FEATURES]
    prob = float(risk_model.predict_proba(X)[0][1])

    return jsonify({
        "riskProbability": round(prob, 4),
        "riskLevel": risk_level(prob),
        "majorFactors": major_factors(features),
    })


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5001, debug=False)
