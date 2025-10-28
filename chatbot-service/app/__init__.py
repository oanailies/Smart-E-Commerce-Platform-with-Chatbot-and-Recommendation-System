from flask import Flask
from flask_cors import CORS
from dotenv import load_dotenv

import os

def create_app():
    load_dotenv()

    app = Flask(__name__)
    app.secret_key = os.getenv("FLASK_SECRET_KEY", "dev-secret-key")


    from .routes import chatbot_bp
    app.register_blueprint(chatbot_bp)
    from app.services.voice_api import voice_bp
    app.register_blueprint(voice_bp)
    return app
