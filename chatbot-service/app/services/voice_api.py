import os
from flask import Blueprint, request, jsonify
from flask_cors import cross_origin
from pydub import AudioSegment
from faster_whisper import WhisperModel
from gtts import gTTS

from app.services.responses import get_chatbot_response


AudioSegment.converter = r"C:\ffmpeg\ffmpeg-7.1.1-essentials_build\bin\ffmpeg.exe"
AudioSegment.ffprobe = r"C:\ffmpeg\ffmpeg-7.1.1-essentials_build\bin\ffprobe.exe"

voice_bp = Blueprint("voice", __name__)
model = WhisperModel("base")

STATIC_DIR = "static"
os.makedirs(STATIC_DIR, exist_ok=True)


def convert_to_wav(input_path, output_path="temp.wav"):
    audio = AudioSegment.from_file(input_path)
    audio = audio.set_channels(1)
    audio = audio.set_frame_rate(16000)
    audio.export(output_path, format="wav")
    return output_path


def transcribe_audio(wav_path):
    segments, _ = model.transcribe(wav_path)
    return " ".join([segment.text for segment in segments])


def text_to_speech(text, lang="en", output_path="static/response.mp3"):
    try:
        tts = gTTS(text=text, lang=lang)
        tts.save(output_path)
        return output_path
    except Exception as e:
        print("[gTTS Error]", str(e))
        return None



@voice_bp.route("/api/voice-chat", methods=["POST"])
@cross_origin(origin="http://localhost:5173")
def handle_voice_chat():
    audio_file = request.files.get("audio")
    lang = request.form.get("language", "en")

    if not audio_file:
        return jsonify({"error": "No audio file uploaded."}), 400

    input_path = "uploaded_audio.webm"
    audio_file.save(input_path)
    wav_path = convert_to_wav(input_path)

    try:
        user_text = transcribe_audio(wav_path)
    except Exception as e:
        print("[faster-whisper Error]", str(e))
        return jsonify({"error": "Speech-to-text failed."}), 500

    chatbot_result = get_chatbot_response(user_text, lang)

    tts_path = text_to_speech(
        chatbot_result["answer"],
        lang,
        os.path.join(STATIC_DIR, "response.mp3")
    )

    return jsonify({
        "user_input": user_text,
        "response": chatbot_result["answer"],
        "audio_response": f"/static/response.mp3" if tts_path else None,
        "matched_question": chatbot_result["matched_question"],
        "category": chatbot_result["category"],
        "flags": chatbot_result["flags"]
    })
