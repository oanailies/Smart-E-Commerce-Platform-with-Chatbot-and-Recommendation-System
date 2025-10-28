from app.services.corrector import autocorrector
from app.services.translator import translate_to_english, translate_to_language
from app.services.preprocessing import preprocess_text
from app.services.faq_data import load_faq, load_obscene_words
from app.services.smalltalk_matcher import find_smalltalk_match
from app.services.semantic_matcher import SemanticMatcher
from app.services.category_predictor import CategoryPredictor
from app.services.flag_predictor import get_default_flags
import pandas as pd
from langdetect import detect, DetectorFactory

DetectorFactory.seed = 0

CAT_THRESH = 0.55
MATCH_THRESH = 0.60

faq = load_faq()
obscene_words = set(load_obscene_words())
faq_df = pd.DataFrame(faq)
faq_df["processed"] = faq_df["question"].apply(preprocess_text)

semantic_matcher = SemanticMatcher(faq_df)
category_predictor = CategoryPredictor(faq_df, semantic_matcher.model)

def _topk_categories(category_predictor, text, processed, k=2):
    try:
        probs = category_predictor.predict_proba(text, processed)
        classes = category_predictor.classes_
        pairs = sorted(zip(classes, probs), key=lambda x: x[1], reverse=True)
        return pairs[:k]
    except Exception:
        c = category_predictor.predict(text, processed)
        return [(c, 1.0)]

def get_chatbot_response(user_input, lang=None):
    if not lang:
        try:
            lang = detect(user_input)
        except:
            lang = "en"

    translated = translate_to_english(user_input)
    corrected = autocorrector.correct(translated)
    processed = preprocess_text(corrected)

    if set(processed.split()) & obscene_words:
        return {
            "answer": translate_to_language("Let’s keep things friendly 😊", lang),
            "matched_question": user_input,
            "category": "OFFENSIVE",
            "flags": ["inappropriate"]
        }

    smalltalk_answer, smalltalk_q, smalltalk_cat, smalltalk_flags = find_smalltalk_match(user_input, lang)
    if smalltalk_answer:
        return {
            "answer": translate_to_language(smalltalk_answer, lang),
            "matched_question": smalltalk_q,
            "category": smalltalk_cat,
            "flags": smalltalk_flags or []
        }

    topk = _topk_categories(category_predictor, user_input, processed, k=2)
    main_cat, main_prob = topk[0]

    def search_in_categories(cats):
        sub_df = faq_df[faq_df["category"].isin(cats)]
        if sub_df.empty:
            return None
        local_matcher = SemanticMatcher(sub_df)
        return local_matcher.find_match(processed)

    if main_prob >= CAT_THRESH:
        best_match = search_in_categories([main_cat])
    else:
        best_match = search_in_categories([c for c, _ in topk])

    if not best_match or best_match.get("score", 0) < MATCH_THRESH:
        best_match = semantic_matcher.find_match(processed)

    if not best_match or best_match.get("score", 0) < MATCH_THRESH:
        return {
            "answer": translate_to_language("I'm not sure about that. Could you rephrase?", lang),
            "matched_question": user_input,
            "category": "UNKNOWN",
            "flags": ["no_match"]
        }

    final_category = best_match.get("category") or main_cat
    flags = best_match.get("flags") or get_default_flags(user_input)

    return {
        "answer": translate_to_language(best_match["answer"], lang),
        "matched_question": best_match["question"],
        "category": final_category,
        "flags": flags
    }
