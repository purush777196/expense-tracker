# Model Evaluation & Validation Protocol

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Principles of Verification & Anti-Fabrication

To maintain absolute scientific and engineering integrity:
- **No Fabricated Metrics:** Accuracy, error rates, and evaluation metrics must never be invented or assumed. Every metric presented in documentation or dashboards must be derived from verified execution of reproducible test scripts.
- **Strict Data Leakage Prevention:**
  - For sequential and time-series models (Forecasting, Budget Overrun), cross-validation must strictly adhere to **chronological expanding-window or rolling-window splits** (`TimeSeriesSplit`). Random shuffling is categorically forbidden.
  - For text categorization, vectorizers and vocabulary transformers must be fit strictly on training splits and evaluated on unseen held-out validation sets.

---

## 2. Evaluation Metrics by Machine Learning Module

### Module 1: Transaction Categorization
- **Primary Metric:** Macro-averaged $F_1\text{-score}$ across all canonical classes:
  $$F_1 = 2 \cdot \frac{\text{Precision} \cdot \text{Recall}}{\text{Precision} + \text{Recall}}$$
- **Secondary Metrics:**
  - Top-1 and Top-3 Categorical Accuracy.
  - Expected Calibration Error (ECE) and Brier Score to ensure probability outputs represent true empirical confidence.
  - Per-class Confusion Matrix to identify semantic confusion between adjacent categories (e.g. "Dining" vs "Groceries").

### Module 2: Expense Forecasting
- **Primary Metrics:**
  - **Mean Absolute Error (MAE):**
    $$\text{MAE} = \frac{1}{n} \sum_{i=1}^n |y_i - \hat{y}_i|$$
  - **Weighted Absolute Percentage Error (WAPE):**
    $$\text{WAPE} = \frac{\sum_{i=1}^n |y_i - \hat{y}_i|}{\sum_{i=1}^n y_i}$$
    *(Selected over MAPE to handle days with zero expenditure without mathematical division-by-zero anomalies).*
- **Secondary Metric:** Root Mean Squared Error (RMSE) to penalize large unexpected spending forecasting errors.

### Module 3: Budget-Overrun Prediction
- **Primary Metrics:**
  - **Early Warning Lead Time:** Average number of days prior to month-end that an overrun condition is accurately flagged (target: $\ge 7$ days).
  - **Binary Precision & Recall:** Precision of overrun alarms (minimizing false alarms that cause budget fatigue) and Recall (minimizing surprise overruns).

### Module 4: Anomaly Detection
- **Primary Metrics:**
  - **Precision at Top $K$ ($\text{P}@K$):** Percentage of the top $K$ highest-scoring anomalies confirmed as legitimate unusual spending by user review.
  - **Contamination Sensitivity:** Evaluation of Isolation Forest score distributions to ensure the alert rate does not exceed $2\%$ to $3\%$ of total monthly transactions.

---

## 3. Data Splitting Protocols

```
Chronological Time Series Split:
+-----------------------------------+-------------------+-------------------+
|       Train Set (70%)             |  Validation (15%) |    Test Set (15%) |
|   e.g. 2024-01-01 to 2025-06-30   |   to 2025-10-31   |   to 2026-02-28   |
+-----------------------------------+-------------------+-------------------+
Past ----------------------------------------------------------------> Future
```

- **Step 1: Train Split (70%):** Model fitting, feature parameter optimization, TF-IDF vocabulary extraction.
- **Step 2: Validation Split (15%):** Hyperparameter tuning (regularization $C$, tree depth, learning rate), threshold calibration.
- **Step 3: Test Split (15%):** Final held-out evaluation. Run strictly once per release milestone.

---

## 4. Benchmark Progression & Model Baselines

| Module | Level 0: Naive Baseline | Level 1: Production Baseline | Level 2: Advanced Candidate |
| :--- | :--- | :--- | :--- |
| **Categorization** | Most Frequent Category | **TF-IDF + Regularized Logistic Regression** | Fine-tuned DistilRoBERTa / MiniLM |
| **Forecasting** | 7-day Historical Moving Average | **XGBoost with Lag & Rolling Statistics** | Temporal Convolutional Network (TCN) |
| **Anomaly Detection** | Simple Z-Score ($z > 3.0$) | **Isolation Forest (Multi-variate)** | Autoencoder Reconstruction Error |

---

## 5. Drift Monitoring & Continuous Quality Assurance

1. **Prediction Auditing:** Each inference record logged in PostgreSQL `expense_predictions` records the model version and generated forecast.
2. **Reconciliation:** At the conclusion of each calendar month, an automated verification job compares predicted spend vs. actual settled spend to track model drift over time.
3. **Retraining Threshold:** If 30-day WAPE degrades beyond $25\%$ across active user cohorts, a model recalibration event is flagged.
