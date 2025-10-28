import numpy as np
from typing import Optional, Dict, Any
from sentence_transformers import SentenceTransformer

class SemanticMatcher:
    def __init__(self, df, shared_model: Optional[SentenceTransformer] = None):
        self.df = df
        self.model = shared_model or SentenceTransformer("all-MiniLM-L6-v2")
        self.embeddings = self.model.encode(
            df["processed"].tolist(),
            convert_to_numpy=True,
            normalize_embeddings=True,
            show_progress_bar=False
        ).astype("float32")

    def find_match(self, processed_user_text: str) -> Optional[Dict[str, Any]]:
        user_vec = self.model.encode(
            [processed_user_text],
            convert_to_numpy=True,
            normalize_embeddings=True,
            show_progress_bar=False
        ).astype("float32")[0]
        sims = (self.embeddings @ user_vec).astype("float32")
        idx = int(np.argmax(sims))
        score = float(sims[idx])
        threshold = 0.7 if len(processed_user_text.split()) < 3 else 0.6
        if score < threshold:
            return None
        row = self.df.iloc[idx]
        return {
            "question": row["question"],
            "answer": row["answer"],
            "score": score,
            "category": row.get("category"),
            "flags": row.get("flags", [])
        }
