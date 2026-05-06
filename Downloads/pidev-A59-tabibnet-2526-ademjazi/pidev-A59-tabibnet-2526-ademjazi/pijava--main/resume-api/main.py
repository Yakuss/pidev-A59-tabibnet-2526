import nltk, warnings, re, json, concurrent.futures
import google.generativeai as genai
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sumy.parsers.plaintext import PlaintextParser
from sumy.nlp.tokenizers import Tokenizer
from sumy.summarizers.lsa import LsaSummarizer
from deep_translator import GoogleTranslator

warnings.filterwarnings("ignore")

# Setup NLTK
for r in ['tokenizers/punkt', 'tokenizers/punkt_tab']:
    try:
        nltk.data.find(r)
    except LookupError:
        nltk.download(r.split('/')[-1])

# Clé API Gemini — obtenir sur https://aistudio.google.com
genai.configure(api_key="AIzaSyExample_RemplaceMoiParTaVraieCle")

app = FastAPI(title="TabibNet IA API", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def root():
    return {"status": "ok", "endpoints": ["/summarize", "/translate", "/recommend-links"]}


# ─────────────────────────────────────────────────────────────────────────────
# API 1 — RÉSUMÉ AUTOMATIQUE
# ─────────────────────────────────────────────────────────────────────────────
class SummarizeRequest(BaseModel):
    text: str
    nb_phrases: int = 3

@app.post("/summarize")
def summarize(req: SummarizeRequest):
    if not req.text.strip():
        raise HTTPException(status_code=400, detail="Le texte est vide.")
    try:
        parser = PlaintextParser.from_string(req.text, Tokenizer("french"))
        summarizer = LsaSummarizer()
        sentences = summarizer(parser.document, req.nb_phrases)
        result = " ".join(str(s) for s in sentences)

        # Fallback si Sumy ne retourne rien (texte trop court)
        if not result.strip():
            parts = re.split(r'(?<=[.!?])\s+', req.text.strip())
            result = " ".join(parts[:req.nb_phrases])

        return {"success": True, "summary": result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ─────────────────────────────────────────────────────────────────────────────
# API 2 — TRADUCTION
# ─────────────────────────────────────────────────────────────────────────────
class TranslateRequest(BaseModel):
    text: str
    target_lang: str = "ar"

@app.post("/translate")
def translate(req: TranslateRequest):
    if not req.text.strip():
        raise HTTPException(status_code=400, detail="Le texte est vide.")

    # Normaliser les noms de langue envoyés depuis Java
    lang_map = {
        "arabe": "ar", "francais": "fr", "français": "fr",
        "englais": "en", "anglais": "en", "english": "en",
        "arabic": "ar", "french": "fr"
    }
    target = lang_map.get(req.target_lang.lower(), req.target_lang.lower())

    try:
        translated = GoogleTranslator(source='auto', target=target).translate(req.text)
        return {"success": True, "translated_text": translated, "target_language": target}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur traduction : {str(e)}")


# ─────────────────────────────────────────────────────────────────────────────
# API 3 — RECOMMANDATION DE LIENS IA
# ─────────────────────────────────────────────────────────────────────────────
class RecommendRequest(BaseModel):
    content: str = ""
    text: str = ""

    def get_text(self):
        return (self.content or self.text).strip()

@app.post("/recommend-links")
def recommend(req: RecommendRequest):
    content = req.get_text()
    if not content:
        raise HTTPException(status_code=400, detail="Contenu vide.")

    prompt = (
        f"Tu es un assistant médical expert. Analyse ce texte d'article de santé :\n\n"
        f"\"\"\"\n{content[:1500]}\n\"\"\"\n\n"
        f"Identifie le sujet principal (ex: yoga, diabète, nutrition, cardiologie...).\n"
        f"Retourne EXACTEMENT 3 URLs de sources fiables (Wikipedia, WHO, PubMed, "
        f"Mayo Clinic, Vidal, Ameli) spécifiques à ce sujet. Pas de liens génériques.\n\n"
        f"Réponds UNIQUEMENT avec un tableau JSON valide, sans texte avant ou après :\n"
        f'["https://url1.com", "https://url2.com", "https://url3.com"]'
    )

    gemini_error = None

    # Rotation automatique de modèles si quota dépassé
    for model_name in ['gemini-flash-latest', 'gemini-2.0-flash-lite', 'gemini-2.5-flash-lite']:
        try:
            model = genai.GenerativeModel(model_name)
            with concurrent.futures.ThreadPoolExecutor() as ex:
                response = ex.submit(model.generate_content, prompt).result(timeout=20)
            raw = re.sub(r'```(?:json)?', '', response.text.strip()).strip()
            match = re.search(r'\[.*?\]', raw, re.DOTALL)
            if match:
                links = json.loads(match.group())
            else:
                links = re.findall(r'https?://[^\s\'">\]]+', raw)[:3]
            return {"success": True, "links": links, "model": model_name}
        except concurrent.futures.TimeoutError:
            gemini_error = "timeout"
            continue
        except Exception as e:
            gemini_error = str(e)
            if "429" not in gemini_error and "quota" not in gemini_error.lower():
                break
            continue

    # Fallback : liens générés par mots-clés si Gemini indisponible
    stopwords = {
        "dans", "avec", "pour", "cette", "sont", "plus", "tout", "bien",
        "aussi", "mais", "donc", "comme", "leur", "nous", "vous", "une", "les", "des"
    }
    words = re.findall(r'\b[a-zA-ZÀ-ÿ]{5,}\b', content.lower())
    freq = {}
    for w in words:
        if w not in stopwords:
            freq[w] = freq.get(w, 0) + 1
    top = sorted(freq, key=freq.get, reverse=True)[:3]
    q = "%20".join(top) if top else "sante+medecine"

    return {
        "success": True,
        "links": [
            f"https://fr.wikipedia.org/wiki/Special:Search?search={'_'.join(top)}",
            f"https://pubmed.ncbi.nlm.nih.gov/?term={q}",
            f"https://www.who.int/fr/search?searchQuery={q}"
        ],
        "model": "fallback-keywords"
    }
