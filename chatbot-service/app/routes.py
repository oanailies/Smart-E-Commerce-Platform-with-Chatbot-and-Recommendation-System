from flask import Blueprint, request, jsonify
from app.services.responses import get_chatbot_response



chatbot_bp = Blueprint("chatbot", __name__)

@chatbot_bp.route("/api/chat", methods=["POST"])
def chat():
    user_message = request.json.get("message", "")
    lang = request.json.get("language", "en")

    result = get_chatbot_response(user_message, lang)

    return jsonify({
        "response": result["answer"],
        "matched_question": result["matched_question"],
        "category": result["category"],
        "flags": result["flags"]
    })


