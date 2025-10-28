from collections import Counter
from fastapi import FastAPI, Query, Request, HTTPException
from typing import List
import requests

from ml_model import load_model, recommend_for_product, load_client_model, recommend_for_client
from recommender import (
    get_orders,
    get_top_purchased_products,
    generate_recommendations,
    get_latest_purchased_products,
    get_brand_recommendations,
)

app = FastAPI()

# ===========================
# LOAD MODELS AT STARTUP
# ===========================
load_model()
load_client_model()

# ===========================
# SERVICE URLS (for Docker)
# ===========================
PRODUCT_SERVICE_URL = "http://product-service:8082"


# ===========================
# HELPER FUNCTIONS
# ===========================
def fetch_product(product_id: int, token: str | None = None):
    """Fetch a single product by ID from the product service."""
    try:
        headers = {"Authorization": token} if token else {}
        r = requests.get(f"{PRODUCT_SERVICE_URL}/products/{product_id}", headers=headers, timeout=5)
        if r.status_code == 200:
            return r.json()
    except Exception as e:
        print(f"Failed to fetch product {product_id}: {e}")
    return None


def hydrate_unique(candidate_ids: List[int], token: str | None, limit: int = 5):
    """Fetch unique product objects for a list of candidate IDs."""
    products = []
    seen = set()
    for pid in candidate_ids:
        p = fetch_product(pid, token)
        if not p:
            continue
        key = (p.get("name"), (p.get("brand") or {}).get("id"))
        if key in seen:
            continue
        seen.add(key)
        products.append(p)
        if len(products) == limit:
            break
    return products


# ===========================
# ROUTES
# ===========================
@app.get("/api/recommendations/product")
def get_product_recommendations(request: Request, product_ids: List[int] = Query(None)):
    token = request.headers.get("Authorization")
    if not product_ids:
        raise HTTPException(status_code=400, detail="You must provide product_ids")

    candidate_ids = []
    for pid in product_ids:
        candidate_ids.extend(recommend_for_product(pid, k=10))

    products = hydrate_unique(candidate_ids, token, limit=10)
    return {"input": product_ids, "recommended_products": products}


@app.get("/api/recommendations/client")
def get_client_recommendations(request: Request, user_id: int = Query(...)):
    token = request.headers.get("Authorization")
    candidate_ids = recommend_for_client(user_id, k=10)
    products = hydrate_unique(candidate_ids, token, limit=10)
    return {"user_id": user_id, "recommended_products": products}


@app.get("/api/recommendations/co-purchase")
def get_co_purchase_recommendations(request: Request, product_ids: List[int] = Query(None)):
    token = request.headers.get("Authorization")
    if not product_ids:
        raise HTTPException(status_code=400, detail="You must provide product_ids")

    orders = get_orders(token)
    candidate_ids = generate_recommendations(product_ids, orders, top_k=10)
    products = hydrate_unique(candidate_ids, token, limit=5)
    return {"input": product_ids, "recommended_products": products}


@app.get("/api/recommendations/top-purchased")
def get_top_purchased(request: Request):
    token = request.headers.get("Authorization")
    orders = get_orders(token)
    candidate_ids = get_top_purchased_products(orders, top_k=10)
    products = hydrate_unique(candidate_ids, token, limit=5)
    return products


@app.get("/api/recommendations/latest-purchased")
def get_latest_purchased(request: Request):
    token = request.headers.get("Authorization")
    orders = get_orders(token)
    candidate_ids = get_latest_purchased_products(orders, top_k=10)
    products = hydrate_unique(candidate_ids, token, limit=5)
    return products


@app.get("/api/recommendations/brand")
def get_brand_recommendations_endpoint(request: Request, product_id: int = Query(...)):
    token = request.headers.get("Authorization")
    candidate_ids = get_brand_recommendations(product_id, token, top_k=10)
    products = hydrate_unique(candidate_ids, token, limit=5)
    return {"product_id": product_id, "recommended_products": products}


@app.get("/api/recommendations/bestsellers")
def get_bestsellers(request: Request, limit: int = Query(20)):
    token = request.headers.get("Authorization")
    orders = get_orders(token)
    product_counter = Counter()

    # Count total quantities sold
    for order in orders:
        for item in order.get("orderProducts", []):
            product_id = item.get("productId")
            quantity = item.get("quantity", 1)
            if product_id:
                product_counter[product_id] += quantity

    # Fetch product catalog
    try:
        headers = {"Authorization": token} if token else {}
        r = requests.get(f"{PRODUCT_SERVICE_URL}/products", headers=headers, timeout=5)
        if r.status_code != 200:
            raise HTTPException(status_code=500, detail="Failed to fetch product catalog")
        products = r.json()
    except Exception as e:
        print(f"Failed to fetch catalog: {e}")
        raise HTTPException(status_code=500, detail="Failed to fetch product catalog")

    # Sort by total quantity sold
    sorted_products = sorted(
        products,
        key=lambda p: product_counter.get(p["id"], 0),
        reverse=True,
    )
    return {"bestsellers": sorted_products[:limit]}
