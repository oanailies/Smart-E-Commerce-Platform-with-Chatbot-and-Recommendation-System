import os
import re
import itertools
from functools import lru_cache
from typing import List, Iterable
from symspellpy import SymSpell, Verbosity

MAX_EDIT_DISTANCE_COMPOUND = 2
PREFIX_LENGTH = 7
MIN_FREQ_KEEP_ORIGINAL = 5
CONFIDENCE_MARGIN = 1.15

STOPWORDS = {
    "a","an","and","are","as","at","be","but","by","for","from","has","have","if","in","is","it",
    "of","on","or","so","the","to","with","can","i","you","your","we","our","not","this","that",
    "there","here","which","when","where","why","how","what","do"
}

DOMAIN_WHITELIST = {
    "paypal","visa","mastercard","applepay","googlepay","klarna","afterpay","invoice",
    "sameday","fanbox","easybox","pickup","pickupstore","locker","awb","express",
    "voucher","coupon","giftcard","wishlist","checkout","cart","restock","preorder","backorder",
    "orderstatus","sizeguide","giftwrap",
    "skincare","makeup","lipstick","foundation","concealer","mascara","eyeliner","palette","blush",
    "bronzer","highlighter","serum","toner","cleanser","moisturizer","spf","sunscreen","fragrance",
    "perfume","deodorant","shampoo","conditioner","shade","shades","sizes","ml",
    "login","signup","register","newsletter","unsubscribe","profile","billing","support","chat",
    "eco","ecofriendly","organic","crueltyfree","vegan","recyclable"
}

FALLBACK_CORRECTIONS = {
    "returr":"return","ordr":"order","refnd":"refund","shippng":"shipping","platt":"pay",
    "paypall":"paypal","fedx":"fedex","packege":"package","probelms":"problems",
    "sameeday":"sameday","produt":"product","daays":"days"
}

RE_URL   = re.compile(r"https?://\S+|www\.\S+", re.IGNORECASE)
RE_EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
RE_AWB   = re.compile(r"\b(?:AWB|TRACK|TRK|ORDER|CMD|ORD)[\-\s]?[A-Z0-9]{5,}\b", re.IGNORECASE)
RE_CODE  = re.compile(r"\b[A-Z0-9]{6,}\b")
RE_TIME  = re.compile(r"\b\d{1,3}\/\d{1,3}h\b", re.IGNORECASE)
RE_HASH  = re.compile(r"^[#@]\w+$")
RE_NUMBER= re.compile(r"^\d+([.,]\d+)?$")
RE_CLEAN = re.compile(r"[^A-Za-z0-9\-\s]+")
RE_LETTER= re.compile(r"[A-Za-z]")
RE_I_STANDALONE = re.compile(r"\bi\b")
RE_I_CONTRACTION = re.compile(r"\bi(?=(?:['’](?:m|ve|d|ll|re|s))\b)", re.IGNORECASE)
RE_SENT_START = re.compile(r"(^|[.!?]\s+)([a-z])")

def preserve_casing(src: str, dst: str) -> str:
    if src.isupper():
        return dst.upper()
    if src.istitle():
        return dst.capitalize()
    if src == "I":
        return "I"
    return dst

def should_protect_token(tok: str) -> bool:
    lower = tok.lower()
    if lower in DOMAIN_WHITELIST:
        return True
    return (
        RE_URL.search(tok) is not None or
        RE_EMAIL.search(tok) is not None or
        RE_AWB.search(tok) is not None or
        RE_CODE.fullmatch(tok) is not None or
        RE_TIME.fullmatch(tok) is not None or
        RE_HASH.fullmatch(tok) is not None or
        RE_NUMBER.fullmatch(tok) is not None
    )

def normalize_space(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()

def fix_pronoun_I(s: str) -> str:
    s = s.replace("’", "'")
    s = RE_I_CONTRACTION.sub("I", s)
    s = RE_I_STANDALONE.sub("I", s)
    return s

def capitalize_sentences(s: str) -> str:
    return RE_SENT_START.sub(lambda m: m.group(1) + m.group(2).upper(), s)

class Autocorrector:
    def __init__(self, dict_filename: str = "frequency_dictionary.txt"):
        self.sym_spell = SymSpell(max_dictionary_edit_distance=2, prefix_length=PREFIX_LENGTH)
        dict_path = os.path.join(os.path.dirname(__file__), "..", "data", dict_filename)
        if os.path.exists(dict_path):
            loaded = self.sym_spell.load_dictionary(dict_path, term_index=0, count_index=1)
            if not loaded:
                print(f"[Autocorrector] Failed to load dictionary from {dict_path}")
        else:
            print(f"[Autocorrector] Dictionary file not found at: {dict_path}")
        self._freq = {}
        for attr in ("word_frequency", "words", "_words"):
            obj = getattr(self.sym_spell, attr, None)
            if obj is not None:
                try:
                    self._freq = dict(obj)
                    break
                except Exception:
                    pass

    def _get_freq(self, term: str) -> int:
        if term in self._freq:
            return self._freq[term]
        try:
            exact = self.sym_spell.lookup(term, Verbosity.TOP, max_edit_distance=0)
            c = exact[0].count if exact and exact[0].term == term else 0
            if c:
                self._freq[term] = c
            return c
        except Exception:
            return 0

    @staticmethod
    def remove_consecutive_duplicates(words: Iterable[str]) -> List[str]:
        return [key for key, _ in itertools.groupby(words)]

    @lru_cache(maxsize=100_000)
    def _correct_token_cached(self, token_lower: str) -> str:
        if token_lower in STOPWORDS:
            return token_lower
        if self._get_freq(token_lower) >= MIN_FREQ_KEEP_ORIGINAL:
            return token_lower
        max_ed = 1 if len(token_lower) <= 4 else 2
        suggestions = self.sym_spell.lookup(token_lower, Verbosity.TOP, max_edit_distance=max_ed)
        if not suggestions:
            return token_lower
        top = suggestions[0]
        if len(suggestions) > 1:
            nxt = suggestions[1]
            if top.count < nxt.count * CONFIDENCE_MARGIN:
                return token_lower
        if top.term == token_lower:
            return token_lower
        orig_freq = self._get_freq(token_lower)
        if orig_freq > 0 and top.count < max(orig_freq, 2) * CONFIDENCE_MARGIN:
            return token_lower
        return top.term

    def correct(self, text: str) -> str:
        try:
            text = normalize_space(text)
            if not RE_LETTER.search(text):
                return text
            compound_suggestions = self.sym_spell.lookup_compound(text, max_edit_distance=MAX_EDIT_DISTANCE_COMPOUND)
            compound_result = compound_suggestions[0].term if compound_suggestions else text
            out_words: List[str] = []
            for word in compound_result.split():
                raw = word
                lw = RE_CLEAN.sub(" ", word).strip().replace("  ", " ").lower()
                if not lw:
                    out_words.append(raw)
                    continue
                if lw in FALLBACK_CORRECTIONS:
                    corrected_lower = FALLBACK_CORRECTIONS[lw]
                else:
                    if should_protect_token(raw):
                        out_words.append(raw)
                        continue
                    if "-" in raw and self._get_freq(raw.lower()) >= MIN_FREQ_KEEP_ORIGINAL:
                        out_words.append(raw)
                        continue
                    if len(lw) <= 1:
                        out_words.append(raw)
                        continue
                    corrected_lower = self._correct_token_cached(lw)
                corrected = preserve_casing(raw, corrected_lower)
                if corrected_lower == "i":
                    corrected = "I"
                out_words.append(corrected)
            out_words = self.remove_consecutive_duplicates(out_words)
            out = normalize_space(" ".join(out_words))
            out = fix_pronoun_I(out)
            out = capitalize_sentences(out)
            return out
        except Exception as e:
            print(f"[Autocorrector Error]: {e}")
            return text

autocorrector = Autocorrector(dict_filename="frequency_dictionary.txt")
