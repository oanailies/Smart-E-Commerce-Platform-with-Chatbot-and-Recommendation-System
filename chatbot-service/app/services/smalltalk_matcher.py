import os
import json
import pandas as pd
import numpy as np
from typing import Tuple, Optional
from functools import lru_cache
from sentence_transformers import SentenceTransformer

from app.services.preprocessing import preprocess_text
from app.services.corrector import autocorrector
from app.services.translator import translate_to_english

DATA_DIR = os.path.join(os.path.dirname(__file__), '..', 'data')
SMALLTALK_PATH = os.path.join(DATA_DIR, 'smalltalk.json')

_model = None
_smalltalk_df = None
_smalltalk_embeddings = None
_initialized = False

def get_shared_model() -> SentenceTransformer:
    global _model
    if _model is None:
        _model = SentenceTransformer("all-MiniLM-L6-v2")
    return _model

def init_smalltalk(shared_model: Optional[SentenceTransformer] = None):
    global _model, _smalltalk_df, _smalltalk_embeddings, _initialized
    _model = shared_model or get_shared_model()
    with open(SMALLTALK_PATH, encoding="utf-8") as f:
        data = json.load(f)
    _smalltalk_df = pd.DataFrame(data)
    _smalltalk_df["processed"] = _smalltalk_df["question"].apply(preprocess_text)
    _smalltalk_embeddings = _model.encode(
        _smalltalk_df["processed"].tolist(),
        convert_to_numpy=True,
        normalize_embeddings=True,
        show_progress_bar=False
    ).astype("float32")
    _initialized = True

@lru_cache(maxsize=4096)
def _encode_processed(text: str) -> np.ndarray:
    m = get_shared_model()
    return m.encode(
        [text],
        convert_to_numpy=True,
        normalize_embeddings=True,
        show_progress_bar=False
    ).astype("float32")[0]

def find_smalltalk_match(user_input: str, lang: str = "en", threshold: float = 0.60) -> Tuple[Optional[str], Optional[str], Optional[str], list]:
    if not _initialized:
        init_smalltalk()
    text = user_input if lang == "en" else translate_to_english(user_input)
    corrected = autocorrector.correct(text)
    processed = preprocess_text(corrected)
    user_vec = _encode_processed(processed)
    sims = (_smalltalk_embeddings @ user_vec).astype("float32")
    idx = int(np.argmax(sims))
    if float(sims[idx]) >= float(threshold):
        row = _smalltalk_df.iloc[idx]
        return row["answer"], row["question"], row.get("category"), row.get("flags", [])
    return None, None, None, []
