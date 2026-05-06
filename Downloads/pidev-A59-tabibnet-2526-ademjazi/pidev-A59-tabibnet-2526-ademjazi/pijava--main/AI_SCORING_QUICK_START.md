# 🚀 AI Sentiment Scoring - Quick Start

## ✅ What's Been Done

1. ✅ **SentimentAnalysisService.java** - Service to call Flask sentiment API
2. ✅ **FeedbackService.java** - Updated with AI scoring methods
3. ✅ **Automatic scoring** - AI score calculated when feedback is added
4. ✅ **Complete documentation** - See `AI_SENTIMENT_SCORING_GUIDE.md`

---

## 🏃 Quick Start (3 Steps)

### 1️⃣ Start Flask Sentiment API

```bash
# Install dependencies (first time only)
pip install flask flask-cors textblob vaderSentiment
python -m textblob.download_corpora

# Start API
python app.py
```

### 2️⃣ Rebuild Java Project

In IntelliJ IDEA:
- **Build → Rebuild Project**

### 3️⃣ Test It!

```java
// Add a feedback - AI score will be calculated automatically
FeedbackService feedbackService = new FeedbackService();

Feedback feedback = new Feedback();
feedback.setRendezVousId(123);  // Your appointment ID
feedback.setCommentaire("Excellent docteur!");
feedback.setNote(5);

feedbackService.ajouter(feedback);
// ✅ AI score automatically calculated and saved!
```

---

## 🎯 How It Works

```
Patient submits feedback
         ↓
Feedback saved to database
         ↓
Get doctor ID from appointment
         ↓
Fetch ALL doctor's feedbacks
         ↓
Send to Flask API (localhost:5000)
         ↓
AI analyzes comments + ratings
         ↓
Calculate average sentiment score
         ↓
Update medecins.ai_average_score
```

---

## 📊 AI Score Formula

```
Final Score = (Rating × 60%) + (Sentiment × 40%)

Where:
- Rating: 1-5 stars
- Sentiment: AI analysis of comment text (TextBlob + VADER)
```

**Example:**
- Rating: 5/5
- Comment: "Excellent docteur, très professionnel!"
- Sentiment: 4.85/5
- **AI Score: 4.94/5** ✅

---

## 🔧 Manual Recalculation

```java
FeedbackService feedbackService = new FeedbackService();

// Recalculate for specific doctor
int doctorId = 42;
double aiScore = feedbackService.calculateAndUpdateDoctorAIScore(doctorId);

System.out.println("AI Score: " + aiScore + "/5");
```

---

## 📈 Display in UI

```java
// Get doctor's AI score from database
String query = "SELECT ai_average_score FROM medecins WHERE id = ?";
// ... execute query ...

// Display
Label scoreLabel = new Label(String.format("AI Score: %.2f/5 ⭐", aiScore));
```

---

## 🔍 Verify It's Working

### 1. Check Flask API

```bash
curl http://localhost:5000/health
# Should return: {"status": "ok", "message": "API running"}
```

### 2. Check Database

```sql
SELECT id, nom, ai_average_score FROM medecins WHERE ai_average_score > 0;
```

### 3. Check Console

When feedback is added, you should see:
```
✅ Feedback added successfully
✅ Doctor AI score recalculated: 4.35
✅ Doctor AI score updated in database
```

---

## ❌ Troubleshooting

| Problem | Solution |
|---------|----------|
| "Sentiment API is not available" | Start Flask API: `python app.py` |
| "No feedbacks found" | Normal - score will be 0.0 until feedbacks are added |
| "Column 'ai_average_score' not found" | Add column: `ALTER TABLE medecins ADD COLUMN ai_average_score DECIMAL(3,2) DEFAULT 0.00;` |

---

## 📚 Full Documentation

See **AI_SENTIMENT_SCORING_GUIDE.md** for:
- Complete architecture
- API reference
- Advanced usage
- Best practices
- Testing guide

---

**That's it! AI scoring is now automatic!** 🤖✨

Every time a patient leaves feedback, the doctor's AI score is recalculated using sentiment analysis!
