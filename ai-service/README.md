# Expense Tracker - AI & Machine Learning Service

## Overview
This directory contains the dedicated Python machine learning inference service for **Expense Tracker**, providing real-time categorization, time-series expense forecasting, budget overrun predictions, and spending anomaly detection.

## Technology Stack
- **Language:** Python 3.11+
- **API Framework:** FastAPI, Uvicorn
- **Data Manipulation:** Pandas, NumPy
- **Machine Learning:** Scikit-learn, XGBoost, Joblib
- **Deep Learning (Evaluated):** PyTorch / TensorFlow
- **Validation:** Pydantic v2

## Modules
1. **Module 1:** Transaction Categorization (TF-IDF + Logistic Regression baseline)
2. **Module 2:** Expense Forecasting (XGBoost time-series regression)
3. **Module 3:** Budget-Overrun Prediction (Forecasting vs. budget burn rate)
4. **Module 4:** Anomaly Detection (Isolation Forest unsupervised outlier scoring)
5. **Module 5:** Financial Recommendations (Explainable rule-based heuristics & spending insights)

## Architecture & Design
- Detailed ML architecture: [docs/ai/ml-architecture.md](../docs/ai/ml-architecture.md)
- Dataset strategy: [docs/ai/dataset-strategy.md](../docs/ai/dataset-strategy.md)
- Model evaluation plan: [docs/ai/model-evaluation-plan.md](../docs/ai/model-evaluation-plan.md)

## Planned Setup (Phase 4)
```bash
# Create and activate virtual environment
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run the FastAPI server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
