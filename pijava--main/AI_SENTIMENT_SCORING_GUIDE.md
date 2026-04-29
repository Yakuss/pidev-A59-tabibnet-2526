# 🤖 AI Sentiment Scoring System - Complete Guide

## 📋 Overview

This system automatically calculates an **AI-powered sentiment score** for each doctor based on patient feedbacks (ratings + comments). The score combines:
- **Rating score** (60% weight) - The numerical rating (1-5)
- **Sentiment analysis** (40% weight) - AI analysis of comment text using TextBlob + VADER

---

## 🏗️ Architecture

```
Patient submits feedback
         ↓
FeedbackService.ajouter()
         ↓
Feedback saved to database
         ↓
Get doctor ID from appointment
         ↓
Fetch all doctor's feedbacks
         ↓
Send to Flask Sentiment API
         ↓
AI analyzes comments + ratings
         ↓
Calculate average sentiment score
         ↓
Update medecins.ai_average_score
```

---

## 📦 Components

### 1. Flask Sentiment API (Python)
**File:** `app.py`  
**Port:** 5000  
**Endpoints:**
- `GET /health` - Check API status
- `POST /analyze` - Analyze single feedback
- `POST /doctor-sentiment-score` - Calculate doctor's average score

**How it works:**
```python
# Combines rating + sentiment analysis
final_score = (rating * 0.6) + (sentiment_score * 0.4)

# Sentiment from TextBlob + VADER
sentiment_score = (textblob_score + vader_score) / 2
```

### 2. SentimentAnalysisService.java
**Location:** `src/main/java/com/pidev/services/SentimentAnalysisService.java`

**Methods:**
- `isApiHealthy()` - Check if Flask API is running
- `analyzeSingleFeedback(comment, rating)` - Analyze one feedback
- `calculateDoctorSentimentScore(feedbacks)` - Calculate average for all feedbacks

### 3. FeedbackService.java (Updated)
**Location:** `src/main/java/com/pidev/services/FeedbackService.java`

**New Methods:**
- `getByMedecin(medecinId)` - Get all feedbacks for a doctor
- `calculateAndUpdateDoctorAIScore(medecinId)` - Calculate and save AI score
- `getMedecinIdFromRendezVous(rendezVousId)` - Get doctor ID from appointment

**Auto-update:** When `ajouter()` is called, it automatically recalculates the doctor's AI score in the background.

---

## 🚀 How to Use

### Setup

#### 1. Start Flask Sentiment API

```bash
# Install dependencies (first time only)
pip install flask flask-cors textblob vaderSentiment

# Download TextBlob data (first time only)
python -m textblob.download_corpora

# Start the API
python app.py
```

You should see:
```
🚀 Flask Sentiment API Started
📊 Endpoints:
   GET  /health
   POST /analyze - Analyze single feedback
   POST /analyze-batch - Analyze multiple feedbacks
   POST /doctor-sentiment-score - Calculate doctor's avg sentiment score
 * Running on http://0.0.0.0:5000
```

#### 2. Verify API is Running

```bash
curl http://localhost:5000/health
```

Expected response:
```json
{"status": "ok", "message": "API running"}
```

---

### Automatic Scoring (Recommended)

The AI score is **automatically calculated** when a new feedback is added:

```java
// In your controller or service
FeedbackService feedbackService = new FeedbackService();

Feedback feedback = new Feedback();
feedback.setRendezVousId(123);
feedback.setCommentaire("Excellent docteur, très professionnel!");
feedback.setNote(5);

// This will automatically:
// 1. Save the feedback
// 2. Get the doctor ID
// 3. Calculate AI score for all feedbacks
// 4. Update medecins.ai_average_score
feedbackService.ajouter(feedback);
```

---

### Manual Scoring

You can also manually recalculate a doctor's AI score:

```java
FeedbackService feedbackService = new FeedbackService();

// Recalculate AI score for doctor ID 42
int medecinId = 42;
double aiScore = feedbackService.calculateAndUpdateDoctorAIScore(medecinId);

if (aiScore >= 0) {
    System.out.println("AI Score: " + aiScore + "/5");
} else {
    System.out.println("Error calculating AI score");
}
```

---

### Batch Update All Doctors

To recalculate AI scores for all doctors:

```java
// Get all doctors
MedecinService medecinService = new MedecinService();
FeedbackService feedbackService = new FeedbackService();

List<Medecin> doctors = medecinService.getAll();

for (Medecin doctor : doctors) {
    double aiScore = feedbackService.calculateAndUpdateDoctorAIScore(doctor.getId());
    System.out.println("Doctor " + doctor.getNom() + ": " + aiScore + "/5");
}
```

---

## 📊 Understanding the AI Score

### Score Range: 0.0 - 5.0

| Score | Label | Meaning |
|-------|-------|---------|
| 4.0 - 5.0 | Very Positive | Excellent doctor, highly recommended |
| 3.0 - 3.9 | Positive | Good doctor, generally satisfied patients |
| 2.0 - 2.9 | Neutral | Mixed reviews |
| 1.0 - 1.9 | Negative | Poor reviews |
| 0.0 - 0.9 | Very Negative | Very poor reviews |

### How It's Calculated

```
Final Score = (Rating × 0.6) + (Sentiment × 0.4)

Where:
- Rating: The numerical rating (1-5)
- Sentiment: AI analysis of comment text (0-5)
  - TextBlob: Polarity analysis
  - VADER: Sentiment intensity
  - Combined and normalized to 0-5 scale
```

### Example

**Feedback:**
- Rating: 5/5
- Comment: "Excellent docteur, très professionnel et à l'écoute!"

**Analysis:**
```json
{
  "rating_score": 5.0,
  "textblob_score": 4.8,
  "vader_score": 4.9,
  "sentiment_score": 4.85,
  "final_score": 4.94,
  "sentiment_label": "very_positive",
  "confidence": "high"
}
```

**Result:** AI Score = 4.94/5 ✅

---

## 🔧 Troubleshooting

### ❌ Error: "Sentiment API is not available"

**Problem:** Flask API is not running

**Solution:**
```bash
# Start the Flask API
python app.py

# Verify it's running
curl http://localhost:5000/health
```

---

### ❌ Error: "No feedbacks found for doctor"

**Problem:** Doctor has no feedbacks yet

**Solution:** This is normal. The AI score will be set to 0.0. Once feedbacks are added, the score will be calculated automatically.

---

### ❌ Error: "Error getting doctor ID from appointment"

**Problem:** The `rendezvous` table doesn't have a `medecinId` column

**Solution:** Check your database schema. The query expects:
```sql
SELECT medecinId FROM rendezvous WHERE id = ?
```

If your column name is different (e.g., `medecin_id`), update the query in `FeedbackService.java`.

---

### ❌ Error: "Column 'ai_average_score' not found"

**Problem:** The column doesn't exist in the `medecins` table

**Solution:** Add the column:
```sql
ALTER TABLE medecins ADD COLUMN ai_average_score DECIMAL(3,2) DEFAULT 0.00;
```

---

## 📈 Display AI Score in UI

### In Doctor Profile

```java
// In your controller
Medecin doctor = medecinService.findById(doctorId);

// Get AI score from database
String query = "SELECT ai_average_score FROM medecins WHERE id = ?";
// ... execute query ...

// Display in UI
Label aiScoreLabel = new Label("AI Score: " + aiScore + "/5");
```

### In Doctor List

```java
// Add AI score column to TableView
TableColumn<Medecin, Double> aiScoreColumn = new TableColumn<>("AI Score");
aiScoreColumn.setCellValueFactory(new PropertyValueFactory<>("aiAverageScore"));

// Format with stars
aiScoreColumn.setCellFactory(column -> new TableCell<Medecin, Double>() {
    @Override
    protected void updateItem(Double score, boolean empty) {
        super.updateItem(score, empty);
        if (empty || score == null) {
            setText(null);
        } else {
            int stars = (int) Math.round(score);
            setText("⭐".repeat(stars) + " " + String.format("%.2f", score));
        }
    }
});
```

---

## 🎯 Best Practices

### 1. Run in Background Thread

The AI score calculation can take a few seconds (API call + database update). Always run it in a background thread to avoid blocking the UI:

```java
new Thread(() -> {
    double aiScore = feedbackService.calculateAndUpdateDoctorAIScore(medecinId);
    Platform.runLater(() -> {
        // Update UI with new score
        aiScoreLabel.setText(String.format("%.2f/5", aiScore));
    });
}).start();
```

### 2. Cache Scores

Don't recalculate on every page load. The score is stored in the database and only needs to be recalculated when:
- New feedback is added
- Feedback is modified
- Feedback is deleted

### 3. Handle API Failures Gracefully

```java
double aiScore = feedbackService.calculateAndUpdateDoctorAIScore(medecinId);
if (aiScore < 0) {
    // API failed, use fallback (average rating)
    double avgRating = calculateAverageRating(medecinId);
    updateDoctorAIScore(medecinId, avgRating);
}
```

### 4. Periodic Batch Updates

Schedule a daily batch update to recalculate all scores:

```java
// Run at midnight
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    System.out.println("🔄 Starting daily AI score update...");
    updateAllDoctorScores();
}, 0, 24, TimeUnit.HOURS);
```

---

## 📚 API Reference

### Flask API Endpoints

#### POST /analyze
Analyze single feedback

**Request:**
```json
{
  "comment": "Excellent docteur!",
  "rating": 5
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "rating_score": 5.0,
    "textblob_score": 4.8,
    "vader_score": 4.9,
    "sentiment_score": 4.85,
    "final_score": 4.94,
    "sentiment_label": "very_positive",
    "confidence": "high"
  }
}
```

#### POST /doctor-sentiment-score
Calculate doctor's average score

**Request:**
```json
{
  "feedbacks": [
    {"comment": "Excellent!", "rating": 5},
    {"comment": "Très bien", "rating": 4},
    {"comment": "Bon docteur", "rating": 4}
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "average_sentiment_score": 4.35,
    "average_rating": 4.33,
    "total_feedbacks": 3,
    "feedbacks_analysis": [...]
  }
}
```

---

## ✅ Testing

### Test Single Feedback Analysis

```java
SentimentAnalysisService service = new SentimentAnalysisService();

// Test API health
if (service.isApiHealthy()) {
    System.out.println("✅ API is healthy");
} else {
    System.out.println("❌ API is not available");
    return;
}

// Analyze single feedback
JSONObject result = service.analyzeSingleFeedback(
    "Excellent docteur, très professionnel!",
    5
);

System.out.println("Final Score: " + service.extractFinalScore(result));
System.out.println("Sentiment: " + service.extractSentimentLabel(result));
```

### Test Doctor Score Calculation

```java
FeedbackService feedbackService = new FeedbackService();

// Test with a doctor who has feedbacks
int testDoctorId = 1;
double aiScore = feedbackService.calculateAndUpdateDoctorAIScore(testDoctorId);

if (aiScore >= 0) {
    System.out.println("✅ AI Score calculated: " + aiScore + "/5");
} else {
    System.out.println("❌ Error calculating AI score");
}
```

---

## 🎉 Summary

✅ **Automatic AI scoring** when feedback is added  
✅ **Combines rating + sentiment analysis** for accurate scores  
✅ **Background processing** to avoid blocking UI  
✅ **Handles missing data** gracefully  
✅ **Easy to integrate** with existing code  
✅ **Scalable** - works with any number of feedbacks  

**The AI score provides a more accurate representation of doctor quality than simple rating averages!** 🤖✨
