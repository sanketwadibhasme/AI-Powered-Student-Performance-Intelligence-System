"""
Trains the two V0 AI/ML models for the Student Performance Intelligence System.

Dataset: UCI Student Performance (Cortez & Silva, 2008) - Mathematics course.
         https://archive.ics.uci.edu/dataset/320/student+performance

Model 1 - Student Performance Prediction (regression):
    Predicts the final grade (G3, 0-20 scale) from prior performance:
    G1 (first-period grade), G2 (second-period grade), absences.
    Algorithm: RandomForestRegressor.

Model 2 - Academic Risk Prediction (binary classification):
    Predicts the probability that a student ends with a failing final
    grade (G3 < 10 on the 0-20 scale).
    Algorithm: RandomForestClassifier (class_weight balanced).

Both models are saved together with their preprocessing metadata so the
Flask inference service can reproduce the exact same feature pipeline.

Run:  ml/.venv/Scripts/python.exe ml/train_models.py
"""

import json
import os

import joblib
import pandas as pd
from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    mean_absolute_error,
    r2_score,
)
from sklearn.model_selection import train_test_split

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "data", "student-mat.csv")
MODELS_DIR = os.path.join(BASE_DIR, "models")
os.makedirs(MODELS_DIR, exist_ok=True)

# Feature columns actually used by the models. Kept deliberately small,
# stable and explainable - all of them map naturally to college data:
#   studytime -> weekly study hours (1-4)
#   failures  -> number of past subject failures
#   absences  -> number of school absences
#   G1, G2    -> internal assessment / previous exam scores
PERFORMANCE_FEATURES = ["G1", "G2", "studytime", "failures", "absences"]

RISK_FEATURES = ["G1", "G2", "studytime", "failures", "absences"]

RANDOM_STATE = 42


def main() -> None:
    df = pd.read_csv(DATA_PATH, sep=";")
    print(f"Loaded dataset: {df.shape[0]} rows, {df.shape[1]} columns")

    # ---------------- Data hygiene ----------------
    df = df.dropna(subset=PERFORMANCE_FEATURES + ["G3"])

    # ---------------- Model 1: Performance prediction ----------------
    X_perf = df[PERFORMANCE_FEATURES]
    y_perf = df["G3"]

    X_train, X_test, y_train, y_test = train_test_split(
        X_perf, y_perf, test_size=0.2, random_state=RANDOM_STATE
    )

    perf_model = RandomForestRegressor(
        n_estimators=200,
        max_depth=8,
        random_state=RANDOM_STATE,
    )
    perf_model.fit(X_train, y_train)

    perf_pred = perf_model.predict(X_test)
    mae = mean_absolute_error(y_test, perf_pred)
    r2 = r2_score(y_test, perf_pred)
    print(f"[Performance] MAE={mae:.3f}  R2={r2:.3f}")

    # ---------------- Model 2: Risk prediction ----------------
    # Target: student at risk if final grade is a fail (G3 < 10).
    y_risk = (df["G3"] < 10).astype(int)
    print(f"[Risk] class distribution: {y_risk.value_counts().to_dict()}")

    Xr_train, Xr_test, yr_train, yr_test = train_test_split(
        df[RISK_FEATURES],
        y_risk,
        test_size=0.2,
        random_state=RANDOM_STATE,
        stratify=y_risk,
    )

    risk_model = RandomForestClassifier(
        n_estimators=200,
        max_depth=8,
        class_weight="balanced",
        random_state=RANDOM_STATE,
    )
    risk_model.fit(Xr_train, yr_train)

    risk_pred = risk_model.predict(Xr_test)
    acc = accuracy_score(yr_test, risk_pred)
    print(f"[Risk] accuracy={acc:.3f}")
    print(classification_report(yr_test, risk_pred, digits=3))

    # ---------------- Persist artifacts ----------------
    joblib.dump(perf_model, os.path.join(MODELS_DIR, "performance_model.joblib"))
    joblib.dump(risk_model, os.path.join(MODELS_DIR, "risk_model.joblib"))

    metadata = {
        "dataset": "UCI Student Performance (student-mat.csv)",
        "performance_model": {
            "type": "RandomForestRegressor",
            "target": "G3 (final grade, 0-20)",
            "features": PERFORMANCE_FEATURES,
            "mae": round(float(mae), 4),
            "r2": round(float(r2), 4),
        },
        "risk_model": {
            "type": "RandomForestClassifier",
            "target": "at_risk = G3 < 10",
            "features": RISK_FEATURES,
            "accuracy": round(float(acc), 4),
        },
    }
    with open(os.path.join(MODELS_DIR, "metadata.json"), "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)

    print(f"Saved models + metadata to {MODELS_DIR}")


if __name__ == "__main__":
    main()
