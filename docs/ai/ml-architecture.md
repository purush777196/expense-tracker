# Machine Learning Architecture Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Architectural Strategy & Machine Learning Principles

The AI capabilities in **Expense Tracker** are architected around the following foundational principles:
- **No Fabricated Inference:** All predictions, forecasts, and confidence scores must originate from empirical, verifiable model pipelines. When confidence is low or historical data is insufficient, the system transparently indicates limitations and triggers deterministic fallbacks.
- **Strict Leakage Prevention:** In all temporal modeling tasks (forecasting, budget overrun prediction), models strictly use chronological train/validation/test splits. Random k-fold cross-validation is prohibited on time-series data to avoid lookahead bias.
- **Low Latency & High Reproducibility:** Serialized scikit-learn and XGBoost pipelines are saved via Joblib with deterministic random seeds and loaded into memory upon FastAPI worker initialization.

---

## 2. Machine Learning Modules Specification

```
                                  +------------------------------------+
                                  |     Spring Boot Backend Client     |
                                  +-----------------+------------------+
                                                    |
                         +--------------------------+--------------------------+
                         |                          |                          |
                         v                          v                          v
             [ POST /categorize ]           [ GET /forecast ]          [ POST /anomaly ]
                         |                          |                          |
                         v                          v                          v
               +-------------------+      +-------------------+      +-------------------+
               |     MODULE 1:     |      |     MODULE 2:     |      |     MODULE 4:     |
               |  TF-IDF + LogReg  |      |  XGBoost Time-    |      | Isolation Forest  |
               |   Categorizer     |      |  Series Forecaster|      |  Anomaly Scorer   |
               +---------+---------+      +---------+---------+      +---------+---------+
                         |                          |                          |
                         |                          v                          |
                         |                +-------------------+                |
                         |                |     MODULE 3:     |                |
                         |                |  Budget-Overrun   |                |
                         |                |  Prediction Engine|                |
                         |                +---------+---------+                |
                         |                          |                          |
                         +--------------------------+--------------------------+
                                                    |
                                                    v
                                          +-------------------+
                                          |     MODULE 5:     |
                                          |    Explainable    |
                                          |  Recommendations  |
                                          +-------------------+
```

---

### Module 1: Transaction Categorization
- **Objective:** Automatically assign an unstructured transaction description (e.g. "WHOLEFDS SOMA SAN FRANCISCO") to a standard financial category (e.g. "Groceries").
- **Baseline Architecture:**
  - *Text Normalization:* Lowercasing, punctuation stripping, merchant entity extraction, alphanumeric regex token cleaning.
  - *Vectorization:* Scikit-learn `TfidfVectorizer` (sublinear TF scaling, n-grams `(1, 2)`, max features 10,000, English stopword removal).
  - *Classifier:* Multi-class Regularized Logistic Regression (`LogisticRegression(C=1.0, max_iter=1000, solver='lbfgs')`).
- **Confidence Scoring & Thresholding:**
  - The model outputs class probabilities via softmax.
  - If $\max(P) \ge 0.65$: The category is automatically suggested and marked with high confidence.
  - If $0.40 \le \max(P) < 0.65$: The top 3 ranked categories are presented for single-click user confirmation (`requiresConfirmation = true`).
  - If $\max(P) < 0.40$: Classified as "Uncategorized" with no automatic assignment.
- **Future Roadmap:** Evaluation of fine-tuned lightweight transformer models (e.g. DistilRoBERTa or MiniLM) for multilingual merchant descriptions.

---

### Module 2: Expense Forecasting
- **Objective:** Predict cumulative and day-by-day future expenditure over 7, 14, and 30-day horizons.
- **Baseline Architecture:** Extreme Gradient Boosting (`XGBoost Regressor`) trained on temporal aggregate sequences.
- **Feature Engineering Pipeline:**
  - *Lag Features:* Expenses lagged at $t-1$, $t-2$, $t-7$, $t-14$, and $t-30$ days.
  - *Rolling Statistics:* 7-day, 14-day, and 30-day rolling means, standard deviations, and max spending spikes.
  - *Calendar & Cyclical Signals:* Day of week (one-hot or sine/cosine transformed), day of month, month of year, weekend flag, pay-day proximity indicator (1st and 15th of month).
- **Cold-Start Protocol:**
  - If a user has $< 30$ historical transactions or $< 14$ active recording days, the system suppresses the ML model and falls back to a deterministic 7-day weighted moving average with explicit UI indicator: *"Preliminary estimate based on moving average (insufficient history for ML forecast)"*.

---

### Module 3: Budget-Overrun Prediction
- **Objective:** Compute the probability and magnitude of exceeding monthly budget limits prior to the close of the billing cycle.
- **Formulation:**
  $$\text{Projected Spend} = \text{Actual Spend to Date} + \sum_{d = \text{today}}^{\text{month end}} \hat{y}_d$$
  $$\text{Overrun Gap} = \text{Projected Spend} - \text{Budget Limit}$$
- **Severity Tiers:**
  - *Nominal:* $\text{Projected Spend} \le 0.85 \times \text{Budget Limit}$
  - *Moderate Risk (Warning):* $0.85 < \text{Projected Spend} / \text{Budget Limit} \le 1.00$
  - *Critical Overrun Risk:* $\text{Projected Spend} > 1.00 \times \text{Budget Limit}$
- Output returns estimated breach day (e.g. "Projected to exhaust budget on Day 23").

---

### Module 4: Anomaly Detection
- **Objective:** Detect statistically abnormal spending spikes and unusual transaction behaviors.
- **Model:** `IsolationForest(n_estimators=100, contamination=0.03, random_state=42)`.
- **Feature Vector:**
  - $x_1$: Normalized transaction amount.
  - $x_2$: Category Z-score relative to user's historical 90-day distribution: $\frac{x - \mu_{\text{cat}}}{\sigma_{\text{cat}}}$.
  - $x_3$: Ratio of transaction amount to category 30-day median.
  - $x_4$: Hour of day and day of week indicators.
- **Safety & Ethical Guardrail:**
  - The system labels flagged records as **"Unusual Spending Alert"**, never "Fraudulent". Alerts include an inline dismissal button ("Mark as Expected Expense") to refine user profiling.

---

### Module 5: Financial Recommendations
- **Objective:** Provide actionable, explainable guidance to enhance savings and optimize discretionary outflows.
- **Heuristic & Statistical Engine:**
  - *Discretionary Outflow Audit:* Analyzes category ratios against the classical 50/30/20 budgeting rule (50% Needs, 30% Wants, 20% Savings/Debt). Flags if "Wants" exceed 35% of total income.
  - *Subscription Leakage Hunter:* Flags recurring transactions exhibiting steady price creep or low-frequency value.
  - *Actionable Recommendations:* Each insight generates a concrete mathematical impact (e.g., *"Reducing dining expenses by 15% would reallocate \$180/month toward your Emergency Fund"*).

---

## 3. Serving Infrastructure & Performance Limits

- **Framework:** FastAPI running on Uvicorn with ASGI worker processes.
- **Inference Latency Target:** Single categorization $< 50$ ms; batch inference (up to 50 records) $< 200$ ms.
- **Memory Footprint:** Baseline models (TF-IDF vectorizer + Logistic Regression + XGBoost models) require $< 250$ MB RAM, ensuring lightweight operation in containerized environments.
