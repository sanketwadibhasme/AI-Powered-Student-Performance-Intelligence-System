"""
Evaluation script for the V0 AI/ML models (research-paper metrics).

This script reuses the EXACT pipeline defined in train_models.py:
  - dataset:      ml/data/student-mat.csv (UCI Student Performance, Math)
  - features:     G1, G2, studytime, failures, absences
  - regressor:    RandomForestRegressor(n_estimators=200, max_depth=8,
                                        random_state=42)
                  on target G3 (final grade, 0-20), 80/20 split, seed 42
  - classifier:   RandomForestClassifier(n_estimators=200, max_depth=8,
                                         class_weight="balanced",
                                         random_state=42)
                  on target at_risk = (G3 < 10), stratified 80/20 split, seed 42

It computes the six evaluation metrics required for the paper on the
held-out test sets only:
  - Random Forest Regressor : MAE, RMSE
  - Random Forest Classifier: Accuracy, macro Precision, macro Recall, macro F1

Run:  ml/.venv/Scripts/python.exe ml/evaluate_models.py
Outputs: ml/evaluation_results.json
"""

import json
import math
import os

import pandas as pd
from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor
from sklearn.metrics import (
    accuracy_score,
    f1_score,
    mean_absolute_error,
    mean_squared_error,
    precision_score,
    recall_score,
)
from sklearn.model_selection import train_test_split

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "data", "student-mat.csv")
OUTPUT_PATH = os.path.join(BASE_DIR, "evaluation_results.json")

# Must match train_models.py exactly.
PERFORMANCE_FEATURES = ["G1", "G2", "studytime", "failures", "absences"]
RISK_FEATURES = ["G1", "G2", "studytime", "failures", "absences"]
RANDOM_STATE = 42


def main() -> None:
    df = pd.read_csv(DATA_PATH, sep=";")
    df = df.dropna(subset=PERFORMANCE_FEATURES + ["G3"])
    print(f"Dataset rows after dropna: {len(df)}")

    # ---------------- Model 1: Performance prediction (regression) ----------
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
    # sklearn >=1.4 has root_mean_squared_error; fall back to sqrt(MSE).
    try:
        from sklearn.metrics import root_mean_squared_error
        rmse = root_mean_squared_error(y_test, perf_pred)
    except ImportError:
        rmse = math.sqrt(mean_squared_error(y_test, perf_pred))

    print(f"[Performance] test n={len(y_test)}  MAE={mae:.6f}  RMSE={rmse:.6f}")

    # ---------------- Model 2: Risk prediction (classification) -------------
    y_risk = (df["G3"] < 10).astype(int)
    print(f"[Risk] full-dataset class distribution: "
          f"{y_risk.value_counts().sort_index().to_dict()}")

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
    labels = sorted(yr_test.unique())
    accuracy = accuracy_score(yr_test, risk_pred)
    precision = precision_score(yr_test, risk_pred, average="macro",
                                labels=labels, zero_division=0)
    recall = recall_score(yr_test, risk_pred, average="macro",
                          labels=labels, zero_division=0)
    f1 = f1_score(yr_test, risk_pred, average="macro",
                  labels=labels, zero_division=0)

    train_counts = yr_train.value_counts().sort_index().to_dict()
    test_counts = yr_test.value_counts().sort_index().to_dict()
    print(f"[Risk] train class counts: {train_counts}")
    print(f"[Risk] test class counts : {test_counts}")
    print(f"[Risk] accuracy={accuracy:.6f} precision_macro={precision:.6f} "
          f"recall_macro={recall:.6f} f1_macro={f1:.6f}")

    results = {
        "dataset": {
            "name": "UCI Student Performance - Mathematics (student-mat.csv)",
            "n_rows_used": int(len(df)),
            "performance_features": PERFORMANCE_FEATURES,
            "risk_features": RISK_FEATURES,
        },
        "performance_regressor": {
            "type": "RandomForestRegressor",
            "hyperparameters": {"n_estimators": 200, "max_depth": 8,
                                "random_state": 42},
            "target": "G3 (final grade, 0-20 scale)",
            "split": "80/20 train_test_split, random_state=42, not stratified",
            "test_size": int(len(y_test)),
            "metrics": {
                "mae": round(float(mae), 6),
                "rmse": round(float(rmse), 6),
            },
        },
        "risk_classifier": {
            "type": "RandomForestClassifier",
            "hyperparameters": {"n_estimators": 200, "max_depth": 8,
                                "class_weight": "balanced", "random_state": 42},
            "target": "at_risk = 1 if G3 < 10 else 0 (pass threshold per train_models.py)",
            "split": "80/20 train_test_split, random_state=42, stratified on target",
            "test_size": int(len(yr_test)),
            "train_class_counts": {str(k): int(v) for k, v in train_counts.items()},
            "test_class_counts": {str(k): int(v) for k, v in test_counts.items()},
            "metrics": {
                "accuracy": round(float(accuracy), 6),
                "precision_macro": round(float(precision), 6),
                "recall_macro": round(float(recall), 6),
                "f1_macro": round(float(f1), 6),
            },
        },
    }

    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)

    print(f"\nWrote results to {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
