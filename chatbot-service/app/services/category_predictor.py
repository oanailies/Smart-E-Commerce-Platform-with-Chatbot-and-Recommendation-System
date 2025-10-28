import os
import pickle
import numpy as np
from functools import lru_cache
from typing import Optional, List, Tuple

class CategoryPredictor:
    def __init__(self, df, sbert_model, threshold: float = 0.35, intent_conf: float = 0.60,
                 k_per_category: int = 3, min_items_for_kmeans: int = 6, kmeans_random_state: int = 42):
        self.sbert_model = sbert_model
        self.threshold = float(threshold)
        self.intent_conf = float(intent_conf)
        self.k_per_category = int(k_per_category)
        self.min_items_for_kmeans = int(min_items_for_kmeans)
        self.kmeans_random_state = int(kmeans_random_state)
        self.emb_dim = sbert_model.get_sentence_embedding_dimension() if hasattr(sbert_model, "get_sentence_embedding_dimension") else 384
        model_path = os.getenv("INTENT_MODEL_PATH", os.path.join(os.path.dirname(__file__), "models", "intent_classifier.pkl"))
        self.intent_model = None
        self.classes_ = None
        try:
            with open(model_path, "rb") as f:
                self.intent_model = pickle.load(f)
            try:
                self.classes_ = np.array(getattr(self.intent_model, "classes_", []))
            except Exception:
                self.classes_ = None
        except FileNotFoundError:
            print(f"[CategoryPredictor] Intent model not found at: {model_path}")
        except Exception as e:
            print(f"[CategoryPredictor] Failed to load intent classifier: {e}")
        if "category" not in df.columns or "processed" not in df.columns:
            raise ValueError("[CategoryPredictor] df must contain 'category' and 'processed' columns")
        self.proto_labels, self.proto_embs = self._build_prototypes(df)
        if self.proto_embs.ndim != 2 or len(self.proto_labels) == 0:
            raise ValueError("[CategoryPredictor] Invalid prototypes index")

    def _build_prototypes(self, df) -> Tuple[List[str], np.ndarray]:
        labels: List[str] = []
        proto_vecs: List[np.ndarray] = []
        grouped = df.assign(category=df["category"].astype(str)).groupby("category")
        try:
            from sklearn.cluster import KMeans
            have_kmeans = True
        except Exception:
            have_kmeans = False
            print("[CategoryPredictor] scikit-learn not available; using single centroid per category.")
        for cat, grp in grouped:
            texts = grp["processed"].astype(str).tolist()
            if not texts:
                continue
            embs = self.sbert_model.encode(texts, convert_to_numpy=True, normalize_embeddings=True, show_progress_bar=False).astype(np.float32)
            n = embs.shape[0]
            if (not have_kmeans) or (n < self.min_items_for_kmeans) or (self.k_per_category <= 1):
                labels.append(cat); proto_vecs.append(embs.mean(axis=0)); continue
            k_auto = max(2, min(self.k_per_category, int(np.sqrt(n/2)) or 2))
            k = min(k_auto, n)
            try:
                km = KMeans(n_clusters=k, n_init=10, random_state=self.kmeans_random_state, max_iter=300)
                km.fit(embs)
                centers = km.cluster_centers_.astype(np.float32)
                for c in centers:
                    labels.append(cat); proto_vecs.append(c)
            except Exception as e:
                print(f"[CategoryPredictor] KMeans failed for '{cat}': {e}. Using single centroid.")
                labels.append(cat); proto_vecs.append(embs.mean(axis=0))
        embs_arr = np.vstack(proto_vecs) if proto_vecs else np.zeros((0, self.emb_dim), dtype=np.float32)
        return labels, embs_arr

    @lru_cache(maxsize=2048)
    def _encode_norm(self, processed_question: str) -> np.ndarray:
        emb = self.sbert_model.encode([processed_question], convert_to_numpy=True, normalize_embeddings=True, show_progress_bar=False).astype(np.float32)
        return emb[0]

    def _proto_scores(self, processed_text: str) -> np.ndarray:
        q = self._encode_norm(processed_text)
        if self.proto_embs.size == 0:
            return np.zeros(0, dtype=np.float32)
        return self.proto_embs @ q

    def _softmax(self, x: np.ndarray, temp: float = 0.1) -> np.ndarray:
        if x.size == 0:
            return x
        x = x / max(temp, 1e-6)
        x = x - np.max(x)
        e = np.exp(x)
        return e / (e.sum() + 1e-9)

    def _predict_intent_ml(self, question: str):
        if self.intent_model is None:
            raise RuntimeError("ml_model_unavailable")
        if hasattr(self.intent_model, "predict_proba"):
            proba = self.intent_model.predict_proba([question])[0]
            classes = getattr(self.intent_model, "classes_", None)
            if classes is not None:
                self.classes_ = np.array(classes)
            idx = int(np.argmax(proba))
            label = str(self.classes_[idx]) if self.classes_ is not None and len(self.classes_) else str(self.intent_model.predict([question])[0])
            return label, proba
        label = str(self.intent_model.predict([question])[0])
        return label, None

    def predict(self, question: str, processed_question: str) -> str:
        try:
            label, proba = self._predict_intent_ml(question)
            if proba is not None:
                top_i = int(np.argmax(proba))
                if float(proba[top_i]) >= self.intent_conf:
                    return str(self.classes_[top_i]) if self.classes_ is not None else label
                return None or self._predict_proto(processed_question)
            return label
        except Exception as e:
            print(f"[CategoryPredictor] ML prediction failed: {e}")
            return self._predict_proto(processed_question)

    def predict_proba(self, question: str, processed_question: str) -> np.ndarray:
        try:
            label, proba = self._predict_intent_ml(question)
            if proba is not None:
                return proba
        except Exception as e:
            print(f"[CategoryPredictor] ML prediction failed: {e}")
        scores = self._proto_scores(processed_question)
        probs = self._softmax(scores)
        self.classes_ = np.array(self.proto_labels)
        return probs

    def _predict_proto(self, processed_question: str) -> str:
        if self.proto_embs.size == 0:
            return "OTHER"
        u = self._encode_norm(processed_question)
        sims = self.proto_embs @ u
        idx = int(np.argmax(sims))
        best = float(sims[idx])
        return self.proto_labels[idx] if best >= self.threshold else "OTHER"
