# -*- coding: utf-8 -*-
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import nltk
import re

try:
    from deep_translator import GoogleTranslator
    TRANSLATOR_AVAILABLE = True
except ImportError:
    TRANSLATOR_AVAILABLE = False

nltk.download("punkt",     quiet=True)
nltk.download("punkt_tab", quiet=True)
nltk.download("stopwords", quiet=True)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class TextInput(BaseModel):
    text: str
    nb_phrases: int = 3


class TranslateInput(BaseModel):
    text: str
    target_lang: str


@app.post("/summarize")
def summarize(data: TextInput):
    text = data.text.strip()

    # Decouper en phrases simples
    sentences = re.split(r"(?<=[.!?])\s+", text)
    sentences = [s.strip() for s in sentences if len(s.strip()) > 10]

    # Prendre les N premieres phrases non vides
    nb = min(data.nb_phrases, len(sentences))
    if nb == 0:
        return {"summary": text[:400]}

    result = " ".join(sentences[:nb])
    return {"summary": result}


@app.post("/translate")
def translate(data: TranslateInput):
    """
    Traduit le texte vers la langue cible en utilisant Google Translate (gratuit).
    """
    text = data.text.strip()
    target = data.target_lang.lower()
    
    if not TRANSLATOR_AVAILABLE:
        raise HTTPException(
            status_code=503,
            detail="Le module de traduction n'est pas installé. Installez-le avec: pip install deep-translator"
        )
    
    try:
        # Détection automatique de la langue source
        translator = GoogleTranslator(source='auto', target=target)
        translated_text = translator.translate(text)
        return {"translated_text": translated_text}
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Erreur lors de la traduction: {str(e)}"
        )
