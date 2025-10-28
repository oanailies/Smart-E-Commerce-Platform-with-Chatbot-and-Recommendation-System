import requests
from collections import defaultdict, Counter
from datetime import datetime
import random

# ==============================
# CONFIG: URL-urile microserviciilor
# ==============================
ORDER_SERVICE_URL = "http://order-service:8083"
PRODUCT_SERVICE_URL = "http://product-service:8082"


# ==============================
# FETCH ORDERS
# ==============================
def get_orders(token: str | None = None):
    try:
        headers = {"Authorization": token} if token else {}
        r = requests.get(f"{ORDER_SERVICE_URL}/orders", headers=headers)
        if r.status_code == 200:
            return r.json()
    except Exception as e:
        print(f"Failed to fetch orders: {e}")
    return []


# ==============================
# RECOMMENDATIONS BASED ON CO-PURCHASE
# ==============================
def generate_recommendations(product_ids, orders, top_k=10):
    co_purchase_map = defaultdict(Counter)
    for order in orders:
        products = [item["productId"] for item in order.get("orderProducts", [])]
        if len(products) < 2:
            continue
        unique_products = set(products)
        for pid in unique_products:
            for co_pid in unique_products:
                if pid != co_pid:
                    co_purchase_map[pid][co_pid] += 1

    recommendation_scores = Counter()
    for pid in product_ids:
        recommendation_scores.update(co_purchase_map.get(pid, {}))

    # Remove already purchased products
    for pid in product_ids:
        recommendation_scores.pop(pid, None)

    return [pid for pid, _ in recommendation_scores.most_common(top_k)]


# ==============================
# TOP PURCHASED PRODUCTS
# ==============================
def get_top_purchased_products(orders, top_k=10):
    product_counter = Counter()
    for order in orders:
        for item in order.get("orderProducts", []):
            product_id = item["productId"]
            quantity = item.get("quantity", 1)
            product_counter[product_id] += quantity
    return [pid for pid, _ in product_counter.most_common(top_k)]


# ==============================
# LATEST PURCHASED PRODUCTS
# ==============================
def get_latest_purchased_products(orders, top_k=10):
    seen = set()
    latest = []
    sorted_orders = sorted(
        orders,
        key=lambda o: datetime.strptime(o["orderDate"], "%d-%m-%Y %H:%M"),
        reverse=True
    )
    for order in sorted_orders:
        for item in order.get("orderProducts", []):
            pid = item["productId"]
            if pid not in seen:
                seen.add(pid)
                latest.append(pid)
            if len(latest) >= top_k:
                return latest
    return latest


# ==============================
# BRAND RECOMMENDATIONS
# ==============================
def get_brand_recommendations(product_id: int, token: str | None = None, top_k: int = 10):
    try:
        headers = {"Authorization": token} if token else {}

        # Fetch product details
        r = requests.get(f"{PRODUCT_SERVICE_URL}/products/{product_id}", headers=headers)
        if r.status_code != 200:
            return []
        product = r.json()

        brand = (product.get("brand") or {}).get("name")
        if not brand:
            return []

        # Fetch all products to find others with same brand
        r2 = requests.get(f"{PRODUCT_SERVICE_URL}/products", headers=headers)
        if r2.status_code != 200:
            return []

        products = r2.json()
        filtered = [
            p for p in products
            if p.get("id") != product_id and (p.get("brand") or {}).get("name") == brand
        ]

        # Remove duplicate names
        seen_names = set()
        unique_filtered = []
        for p in filtered:
            name = p.get("name")
            if name not in seen_names:
                seen_names.add(name)
                unique_filtered.append(p)

        random.shuffle(unique_filtered)
        return [p.get("id") for p in unique_filtered[:top_k]]

    except Exception as e:
        print(f"Failed to fetch brand recommendations for {product_id}: {e}")
        return []
