def get_default_flags(question):
    if any(word in question.lower() for word in ["u", "pls", "idk"]):
        return ["COLLOQUIAL"]
    if "?" in question:
        return ["INTERROGATIVE"]
    return ["BASIC"]
