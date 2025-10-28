import json
import os

DATA_DIR = os.path.join(os.path.dirname(__file__), '..', 'data')

def load_faq(path=os.path.join(DATA_DIR, 'faq.json')):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read().strip()
        return json.loads(content) if content else []

def load_obscene_words(path=os.path.join(DATA_DIR, 'obscene_words.json')):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read().strip()
        return json.loads(content) if content else []
