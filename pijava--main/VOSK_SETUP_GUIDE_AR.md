# 🎤 دليل إعداد البحث الصوتي باستخدام Vosk

## ✅ ما تم إنجازه

تم إضافة جميع الملفات والكود اللازم للبحث الصوتي:

1. ✅ **VoskSpeechService.java** - خدمة التعرف على الصوت
2. ✅ **VoskVoiceSearchDialog.java** - نافذة البحث الصوتي
3. ✅ **RAGSearchService.java** - خدمة البحث الذكي بالذكاء الاصطناعي
4. ✅ **زر البحث الصوتي** في واجهة Annuaire
5. ✅ **تكامل كامل** مع AnnuaireController
6. ✅ **إضافة Vosk** إلى pom.xml

---

## 📋 الخطوات المتبقية

### الخطوة 1: تحديث المشروع (Reload Maven)

1. افتح IntelliJ IDEA
2. انقر بزر الماوس الأيمن على `pom.xml`
3. اختر **Maven → Reload Project**
4. انتظر حتى يتم تحميل مكتبة Vosk

**أو استخدم الأمر:**
```bash
cd pijava--main
mvn clean install
```

---

### الخطوة 2: تحميل نموذج Vosk (مهم جداً!)

#### الطريقة 1: التحميل اليدوي (موصى به)

1. **إنشاء مجلد models:**
```bash
cd pijava--main
mkdir models
cd models
```

2. **تحميل النموذج الفرنسي (39 MB):**
   - اذهب إلى: https://alphacephei.com/vosk/models
   - ابحث عن: **vosk-model-small-fr-0.22**
   - حمّل الملف: `vosk-model-small-fr-0.22.zip`
   - فك الضغط في مجلد `models/`

3. **البنية النهائية:**
```
pijava--main/
├── models/
│   └── vosk-model-small-fr-0.22/
│       ├── am/
│       ├── conf/
│       ├── graph/
│       └── ivector/
├── src/
└── pom.xml
```

#### الطريقة 2: باستخدام wget (Linux/Mac)

```bash
cd pijava--main/models
wget https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip
unzip vosk-model-small-fr-0.22.zip
```

#### الطريقة 3: باستخدام PowerShell (Windows)

```powershell
cd pijava--main
mkdir models
cd models
Invoke-WebRequest -Uri "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip" -OutFile "vosk-model-small-fr-0.22.zip"
Expand-Archive -Path "vosk-model-small-fr-0.22.zip" -DestinationPath "."
```

---

### الخطوة 3: التأكد من تشغيل API Flask

تأكد من أن API Flask يعمل على المنفذ 5000:

```bash
# تحقق من حالة API
curl http://localhost:5000/health

# يجب أن ترى:
# {"status": "ok", "records": 8692, "models": {...}}
```

إذا لم يكن API يعمل، قم بتشغيله:
```bash
python Flask_of_AT.py
```

---

### الخطوة 4: إعادة بناء المشروع

في IntelliJ IDEA:
1. **Build → Rebuild Project**
2. انتظر حتى ينتهي البناء

---

### الخطوة 5: تشغيل التطبيق واختبار البحث الصوتي

1. **شغّل التطبيق** (Run MainApp)
2. **افتح صفحة Annuaire**
3. **انقر على زر "🎤 Recherche Vocale"**
4. **تحدث بوضوح:**
   - "Je cherche un cardiologue à Tunis"
   - "أريد طبيب أسنان في صفاقس"
   - "I need a pediatrician in Ariana"
5. **انقر "🔍 Rechercher"**
6. **شاهد النتائج!**

---

## 🎨 كيف يعمل البحث الصوتي

```
┌─────────────────────────────────────────┐
│      🎤 Recherche Vocale                │
│      Parlez pour rechercher un médecin  │
├─────────────────────────────────────────┤
│                                         │
│   Langue: [Français ▼]                  │
│                                         │
│            ┌─────────┐                  │
│            │   🎤    │  ← انقر للبدء    │
│            └─────────┘                  │
│                                         │
│   🎤 Écoute en cours... Parlez!         │
│                                         │
│   ┌───────────────────────────────────┐ │
│   │ 📝 Transcription:                 │ │
│   │                                   │ │
│   │ Je cherche un cardiologue à Tunis│ │
│   │                                   │ │
│   └───────────────────────────────────┘ │
│                                         │
│   ┌──────────┐ ┌─────────┐ ┌─────────┐│
│   │🔍 Rechercher│ │🔄 Effacer│ │❌ Fermer││
│   └──────────┘ └─────────┘ └─────────┘│
└─────────────────────────────────────────┘
```

### سير العمل:

1. **المستخدم ينقر** على زر "🎤 Recherche Vocale"
2. **تفتح نافذة** البحث الصوتي (VoskVoiceSearchDialog)
3. **المستخدم ينقر** على أيقونة الميكروفون 🎤
4. **Vosk يبدأ الاستماع** ويعرض النص في الوقت الفعلي
5. **المستخدم يتحدث**: "Je cherche un cardiologue à Tunis"
6. **النص يظهر** في منطقة النص
7. **المستخدم ينقر** "🔍 Rechercher"
8. **RAGSearchService يرسل** الاستعلام إلى `/query` endpoint
9. **الذكاء الاصطناعي يحلل** الطلب ويبحث في قاعدة البيانات
10. **النتائج تظهر** في قائمة الأطباء مع رد الذكاء الاصطناعي

---

## 🌍 دعم اللغات المتعددة

### النماذج المتاحة:

| اللغة | النموذج | الحجم | الرابط |
|------|---------|-------|--------|
| 🇫🇷 فرنسي | vosk-model-small-fr-0.22 | 39 MB | [تحميل](https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip) |
| 🇸🇦 عربي | vosk-model-ar-0.22-linto-1.1.0 | 66 MB | [تحميل](https://alphacephei.com/vosk/models/vosk-model-ar-0.22-linto-1.1.0.zip) |
| 🇺🇸 إنجليزي | vosk-model-small-en-us-0.15 | 40 MB | [تحميل](https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip) |

### لتغيير اللغة:

في `AnnuaireController.java`، غيّر مسار النموذج:

```java
// فرنسي (افتراضي)
String modelPath = "models/vosk-model-small-fr-0.22";

// عربي
String modelPath = "models/vosk-model-ar-0.22-linto-1.1.0";

// إنجليزي
String modelPath = "models/vosk-model-small-en-us-0.15";
```

---

## 🔧 حل المشاكل

### ❌ خطأ: "Model not found"

**السبب:** النموذج غير موجود في المسار الصحيح

**الحل:**
1. تأكد من وجود مجلد `models/`
2. تأكد من فك ضغط النموذج بشكل صحيح
3. تحقق من اسم المجلد: `vosk-model-small-fr-0.22`

```bash
# تحقق من البنية
ls -la pijava--main/models/vosk-model-small-fr-0.22/
# يجب أن ترى: am/, conf/, graph/, ivector/
```

---

### ❌ خطأ: "Microphone not supported"

**السبب:** الميكروفون غير متصل أو غير مسموح

**الحل:**
1. تأكد من توصيل الميكروفون
2. امنح التطبيق إذن الوصول للميكروفون
3. أغلق التطبيقات الأخرى التي تستخدم الميكروفون

---

### ❌ خطأ: "API non disponible"

**السبب:** API Flask غير مشغّل

**الحل:**
```bash
# شغّل API Flask
python Flask_of_AT.py

# تحقق من الحالة
curl http://localhost:5000/health
```

---

### ❌ النص الصوتي فارغ

**السبب:** الصوت غير واضح أو الميكروفون بعيد

**الحل:**
1. تحدث بصوت أعلى وأوضح
2. قرّب الميكروفون
3. قلل الضوضاء المحيطة
4. تحدث ببطء أكثر

---

## 📊 مقارنة الحلول

### ✅ Vosk (الحل المستخدم)

**المزايا:**
- ✅ يعمل بدون إنترنت (Offline)
- ✅ مجاني تماماً (لا يحتاج API key)
- ✅ سريع (تعرف في الوقت الفعلي)
- ✅ متعدد اللغات (20+ لغة)
- ✅ خفيف (39-66 MB)
- ✅ JavaFX نقي (بدون WebView)
- ✅ خصوصية (البيانات محلية)

**العيوب:**
- ❌ يحتاج تحميل النماذج
- ❌ دقة متوسطة (مقارنة بالسحابة)

---

### ❌ Web Speech API (بديل)

**المزايا:**
- ✅ دقة عالية جداً
- ✅ لا يحتاج تحميل نماذج

**العيوب:**
- ❌ يحتاج إنترنت
- ❌ يحتاج WebView
- ❌ يعتمد على المتصفح
- ❌ مشاكل الخصوصية

---

### ❌ OpenAI Whisper API (بديل)

**المزايا:**
- ✅ دقة عالية جداً
- ✅ متعدد اللغات ممتاز

**العيوب:**
- ❌ يحتاج إنترنت
- ❌ مدفوع (API key)
- ❌ تأخير الشبكة
- ❌ مشاكل الخصوصية

---

## 🎯 أمثلة على الاستعلامات الصوتية

### بالفرنسية:
```
✅ "Je cherche un cardiologue à Tunis"
✅ "Trouvez-moi un dentiste à Sfax"
✅ "J'ai besoin d'un pédiatre à Ariana"
✅ "Médecin généraliste à Sousse"
```

### بالعربية:
```
✅ "أريد طبيب قلب في تونس"
✅ "أبحث عن طبيب أسنان في صفاقس"
✅ "أحتاج طبيب أطفال في أريانة"
✅ "طبيب عام في سوسة"
```

### بالإنجليزية:
```
✅ "I need a cardiologist in Tunis"
✅ "Find me a dentist in Sfax"
✅ "Looking for a pediatrician in Ariana"
✅ "General practitioner in Sousse"
```

---

## 📚 الملفات المهمة

| الملف | الوصف |
|------|-------|
| `VoskSpeechService.java` | خدمة التعرف على الصوت الأساسية |
| `VoskVoiceSearchDialog.java` | نافذة البحث الصوتي |
| `RAGSearchService.java` | خدمة البحث بالذكاء الاصطناعي |
| `AnnuaireController.java` | التكامل مع واجهة Annuaire |
| `AnnuaireView.fxml` | واجهة Annuaire مع زر البحث الصوتي |
| `pom.xml` | إضافة مكتبة Vosk |

---

## ✅ قائمة التحقق النهائية

- [ ] تم إضافة Vosk إلى pom.xml
- [ ] تم Reload Maven Project
- [ ] تم إنشاء مجلد `models/`
- [ ] تم تحميل نموذج Vosk الفرنسي (39 MB)
- [ ] تم فك ضغط النموذج في `models/vosk-model-small-fr-0.22/`
- [ ] تم التحقق من تشغيل API Flask على المنفذ 5000
- [ ] تم Rebuild Project
- [ ] تم تشغيل التطبيق
- [ ] تم اختبار زر "🎤 Recherche Vocale"
- [ ] تم التحدث واختبار التعرف على الصوت
- [ ] تم البحث ورؤية النتائج

---

## 🎉 النتيجة النهائية

بعد إكمال جميع الخطوات، ستحصل على:

✅ **بحث صوتي متقدم** يعمل بدون إنترنت  
✅ **تعرف في الوقت الفعلي** على الكلام  
✅ **بحث ذكي بالذكاء الاصطناعي** باستخدام RAG  
✅ **واجهة جميلة** مع تأثيرات بصرية  
✅ **دعم متعدد اللغات** (FR, AR, EN)  
✅ **خصوصية كاملة** (كل شيء محلي)  

---

## 📞 المساعدة

إذا واجهت أي مشاكل:

1. **تحقق من الأخطاء** في Console
2. **راجع الخطوات** أعلاه
3. **تأكد من تحميل النموذج** بشكل صحيح
4. **تحقق من API Flask** يعمل
5. **جرب إعادة بناء المشروع**

---

**Vosk هو الحل الأمثل لتطبيقات JavaFX Desktop!** 🎤✨

**Offline • مجاني • سريع • Java نقي!**
