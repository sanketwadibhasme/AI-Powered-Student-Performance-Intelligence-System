# ML Feature Mapping — College Data → UCI Model Features

This document is the developer-facing contract between the college application
database and the trained scikit-learn models. It is implemented in exactly one
place: `src/main/java/com/sanket/studentperformanceintelligencesystem/ai/service/AiPredictionService.java`.

## Model provenance (be honest about this)

- **Dataset:** UCI Student Performance dataset, Mathematics course
  (`ml/data/student-mat.csv`, 395 rows, Cortez & Silva 2008).
- **Models:**
  - `performance_model.joblib` — RandomForestRegressor predicting final grade
    G3 (0-20 scale). Test metrics: MAE 1.048, R² 0.872.
  - `risk_model.joblib` — RandomForestClassifier predicting at-risk
    (G3 < 10). Test accuracy 0.911 (class_weight balanced).
- **Features (both models):** `G1, G2, studytime, failures, absences`.
- **Important limitation:** the models were **not** trained on this college's
  student population. G1→G2→G3 in the UCI dataset are sequential assessments
  *of the same subject*; this application feeds chronological assessments
  *across subjects*. Treat outputs as directional estimates.

## Feature mapping implemented in AiPredictionService

| Model feature | College data source | Transformation | When data is missing |
|---------------|--------------------|----------------|----------------------|
| `G1` | Second-most-recent assessment (marks row, ordered by exam date, then marks id) | percent ÷ 100 × 20 | 0 assessments → HTTP 422 (no prediction); 1 assessment → only mark reused for both G1 and G2 (LIMITED_DATA) |
| `G2` | Most-recent assessment (same ordering) | percent ÷ 100 × 20 | same as G1 |
| `absences` | attendance rows: `count(total) − count(PRESENT)` | raw count | 0 if no attendance records (basis is reported) |
| `studytime` | **Not tracked** in the database | — | documented dataset-mean default `2` (2-5 h/week), reported in `defaultedFields` |
| `failures` | **Not tracked** in the database | — | documented dataset-mean default `0`, reported in `defaultedFields` |

The exact numeric feature vector sent to Flask is returned to the UI in
`PredictionResponse.featuresUsed`, and the UI can display it ("Exact features
sent to the model").

## Chronology rule

G1/G2 are chosen **by exam date** (ties broken by marks record id), never by
sorting scores numerically. The earlier behaviour — "sort all marks ascending
and take the two largest" — was replaced because it picked the *highest*
scores, not the *most recent* ones.

## Data sufficiency levels

| Level | Meaning | UI behaviour |
|-------|---------|--------------|
| `SUFFICIENT_DATA` | ≥ 2 assessments (Good), or ≥ 2 with thin attendance (Fair) | normal result with "Based on" panel |
| `LIMITED_DATA` | exactly 1 assessment | normal result + "Limited data" badge + re-run advice |
| `INSUFFICIENT_DATA` | 0 assessments | HTTP 422 — the UI shows "Not enough academic data available for a reliable prediction" instead of a number |

## What-if simulation

`POST /api/ai/students/{id}/simulate` (and `/api/ai/me/insights` with feature
values in the body) applies explicit overrides **on top of** the real data.
Partial overrides are allowed except `g1`-without-`g2` (HTTP 400). Simulations
never modify academic records, and the UI shows the change as
"model prediction changed under these simulated inputs — not a causal effect".

## Error semantics (Spring ↔ Flask ↔ UI)

| Situation | Spring → UI |
|-----------|-------------|
| Flask unreachable / connection refused | HTTP 503 "AI service is unavailable…" |
| Flask 400 (bad features, scale mismatch) | HTTP 502 with Flask's own error message preserved |
| No academic history | HTTP 422 with a user-facing explanation |
| Service up, model results OK | HTTP 200 with data-sufficiency metadata |
