from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

class TFIDFMatcher:
    def __init__(self, df):
        self.df = df
        self.vectorizer = TfidfVectorizer(stop_words="english", ngram_range=(1,2))
        self.matrix = self.vectorizer.fit_transform(df["processed"])

    def find_match(self, processed_question, top_k=1):
        user_vec = self.vectorizer.transform([processed_question])
        scores = cosine_similarity(user_vec, self.matrix)[0]

        if top_k == 1:
            index = np.argmax(scores)
            return self.df.iloc[index], scores[index]
        else:
            top_idx = scores.argsort()[-top_k:][::-1]
            return self.df.iloc[top_idx], scores[top_idx]
