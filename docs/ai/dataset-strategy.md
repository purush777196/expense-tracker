# Dataset Strategy & Preprocessing Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Candidate Public Datasets

To train and benchmark the AI models before accumulating organic user telemetry, the project identifies three candidate public datasets:

| Dataset Identifier | Origin / Platform | Approximate Size | Focus Domain | License / Terms |
| :--- | :--- | :--- | :--- | :--- |
| **`mitulshah/transaction-categorization`** | Hugging Face Datasets | ~50,000+ records | Text description to spending category classification. | Open Access / Educational |
| **`SayantiSaha014/Daily-Transactions`** | GitHub Repository | ~10,000 records | Daily personal banking transactions with timestamps. | MIT License |
| **`My Expenses Data`** | Kaggle Datasets | ~20,000 records | Real-world expense ledger with categories and amounts. | CC0 / Public Domain |

---

## 2. Dataset Isolation & Separation Rules

1. **Independent Preprocessing Pipelines:** Datasets must never be combined without explicit schema mapping and cross-dataset validation. Each dataset must maintain a dedicated ingestion pipeline script (`ingest_mitulshah.py`, `ingest_sayantisaha.py`, etc.).
2. **User Identity Boundary:** Under no condition shall data points from disparate public datasets be assumed to originate from the same user. Time-series features (Module 2) and anomaly baselines (Module 4) must strictly group by verified, self-consistent user IDs.
3. **Data Storage & Git Exclusions:**
   - Raw datasets, intermediate CSVs, and Parquet files must reside under `ai-service/data/raw/` or `ai-service/data/processed/`.
   - All dataset paths and large artifact files (`*.csv`, `*.parquet`, `*.joblib`, `*.pt`) are strictly ignored in `.gitignore` and must never be committed to GitHub.

---

## 3. Canonical Taxonomy Alignment

Because public datasets employ divergent classification labels, each ingestion pipeline must transform raw labels into the **Expense Tracker Canonical Taxonomy**:

| Canonical Category | Mapped Source Categories (Examples) |
| :--- | :--- |
| **Housing & Rent** | `Mortgage`, `Rent`, `HOA`, `Home Maintenance` |
| **Groceries** | `Supermarket`, `Food & Grocery`, `Produce Market`, `Costco/Wholesale` |
| **Dining & Takeout** | `Restaurants`, `Fast Food`, `Coffee Shops`, `Bars` |
| **Utilities** | `Electricity`, `Water`, `Gas`, `Internet`, `Mobile Phone` |
| **Transportation** | `Gasoline`, `Public Transit`, `Uber/Lyft`, `Parking`, `Auto Repair` |
| **Healthcare** | `Pharmacy`, `Doctor`, `Dentist`, `Health Insurance` |
| **Entertainment** | `Movies`, `Streaming Services`, `Concerts`, `Gaming` |
| **Shopping & Retail** | `Clothing`, `Electronics`, `Home Goods`, `E-Commerce` |
| **Income / Salary** | `Paycheck`, `Bonus`, `Dividends`, `Freelance Payment` |
| **Other / Miscellaneous** | `Bank Fees`, `Unclassified`, `Transfer` |

---

## 4. Text Cleaning & Normalization Pipeline

Raw transaction strings from financial aggregators typically contain noise (e.g. `POS DEBIT 10/24 TRADER JOE'S #541 SAN FRANCISCO CA REF#99824`). The preprocessing pipeline executes:

1. **Regex Noise Scrubbing:**
   - Strip numeric store identifiers (`#\d+`), POS prefixes (`POS PURCHASE`, `DEBIT CARD`, `ACH`), and transaction reference codes (`REF#\d+`).
   - Remove calendar dates (`\d{1,2}/\d{1,2}/\d{2,4}`).
2. **Text Normalization:**
   - Convert all tokens to lowercase.
   - Strip non-alphanumeric symbols except ampersands and currency indicators.
   - Remove single-character isolated tokens.
3. **Token Pruning:**
   - Eliminate standard English stop words while preserving financial descriptors (e.g., preserving "fee", "bill", "pay", "tax").

---

## 5. Documented Dataset Limitations & Mitigation

| Limitation | Impact on Model | Mitigation Protocol |
| :--- | :--- | :--- |
| **Geographic Merchant Bias:** Datasets heavily feature US/India merchants. | Lower classification accuracy for European or regional entities. | Return calibrated confidence scores; expose single-click user categorization confirmation in the UI to rapidly adapt to local entities. |
| **Irregular Temporal Sampling:** Public data often exhibits bursty or missing logging dates. | Can disrupt time-series lag feature consistency in Module 2. | Infill missing daily dates with zero-spend records to preserve true daily time step intervals before computing rolling windows. |
| **Lack of Income Transparency:** Some datasets solely record expense debit events. | Limits budget overrun and savings rate testing. | Synthesize standardized monthly income schedules with clear documentation during sandbox benchmarking. |

---

## 6. Licensing, Attribution & Privacy Compliance

- Public dataset attribution will be maintained in `ai-service/data/ATTRIBUTION.md`.
- No personally identifiable information (PII) such as bank account numbers, credit card tokens, or individual home addresses shall be retained in processed training matrices.
