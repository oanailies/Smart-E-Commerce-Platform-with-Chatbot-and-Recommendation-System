import os
from deep_translator import GoogleTranslator


def translate_to_english(text: str) -> str:
    try:
        return GoogleTranslator(source="auto", target="en").translate(text)
    except Exception as e:
        print(f"[Translation Error to EN] {e}")
        return text


def translate_to_language(text: str, lang: str) -> str:
    try:
        return GoogleTranslator(source="auto", target=lang).translate(text)
    except Exception as e:
        print(f"[Translation Error to {lang}] {e}")
        return text
