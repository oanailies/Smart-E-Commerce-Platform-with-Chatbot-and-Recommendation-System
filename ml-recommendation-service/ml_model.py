import sys
import pickle
import numpy as np


try:
    import numpy._core
except ModuleNotFoundError:
    import numpy.core, numpy.core.numeric
    sys.modules["numpy._core"] = numpy.core
    sys.modules["numpy._core.numeric"] = numpy.core.numeric

MODEL_PATH = "models/product_rec_model.pkl"
CLIENT_MODEL_PATH = "models/product_rec_client.pkl"

model = None
client_model = None

def load_model(path: str = MODEL_PATH):
    global model
    with open(path, "rb") as f:
        model = pickle.load(f)
    print("Product model loaded successfull")
    return True

def recommend_for_product(product_id: int, k: int = 10):
    global model
    if model is None:
        print("Product model not loaded")
        return []
    pid = int(product_id)
    pre = model.get("precomputed_neighbors_reranked", {}).get("neighbors", {})
    if pid in pre and pre[pid]:
        return [p for p, _ in pre[pid][:k]]
    tops = model.get("top_popular_fallback", [])[:k]
    return tops

def load_client_model(path: str = CLIENT_MODEL_PATH):
    global client_model
    with open(path, "rb") as f:
        client_model = pickle.load(f)
    print("Client model loaded successfull")
    return True

def recommend_for_client(user_id: int, k: int = 10):
    global client_model
    if client_model is None:
        print("Client model not loaded")
        return []
    uid = int(user_id)
    pre = client_model.get("precomputed_neighbors_reranked", {}).get("neighbors", {})
    if uid in pre and pre[uid]:
        return [p for p, _ in pre[uid][:k]]
    tops = client_model.get("top_popular_fallback", [])[:k]
    return tops
