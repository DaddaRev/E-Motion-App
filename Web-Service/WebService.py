'''
HOW TO RUN THIS WEBSERVICE

Windows:
$env:FLASK_APP = "WebService"                                     #export a variable with the app’s name
flask --app WebService run
flask --app WebService run --host=0.0.0.0 --port=5000

Unix
flask - -app example run



'''

from dotenv import load_dotenv
from flask import Flask, jsonify, render_template, request, redirect, url_for, flash
from pymongo import MongoClient
import logging
import os

import requests

# Load environment variables from .env file
load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv("SECRET_KEY") 

# MongoDB Database connection
client = MongoClient(os.getenv("MONGO_URI")) # Database connection string
db = client['E-MotionApp']  # Replace with your MongoDB database name
users_collection = db['Users']

#logging.basicConfig(level=logging.DEBUG)  # Use for debugging purposes

@app.route('/weather', methods=['GET'])
def get_weather():
    city = request.args.get('city')

    try:
        # Getting the coordinates for a specified City
        url = f'http://api.openweathermap.org/data/2.5/forecast?q={city}&appid=3a97fda7fd3c8cc3c0b9c5e9bccb23dd&units=metric'
        response = requests.get(url)
        response.raise_for_status()
        data = response.json()  # Dictionary containing the JSON response

        # Get the forecast for the next 3 hours and 24 hours later
        forecast_3_hours = data['list'][0]  # The first forecast is for the next 3 hours
        forecast_24_hours = data['list'][8]  # The 9th forecast (index 8) is 24 hours later

        # Check weather conditions
        def is_bad_weather(forecast):
            weather_description = forecast['weather'][0]['description']
            logging.debug(f"Checking weather conditions: {weather_description}")
            return 'rain' in weather_description.lower() or 'storm' in weather_description.lower() or 'snow'in weather_description.lower() or 'thunderstorm' in weather_description.lower() 

        weather_warnings = []

        if is_bad_weather(forecast_3_hours):
            timestamp_3_hours = forecast_3_hours['dt_txt']
            weather_description_3_hours = forecast_3_hours['weather'][0]['description']
            temperature_3_hours = forecast_3_hours['main']['temp']
            weather_warnings.append(f" {timestamp_3_hours}: {weather_description_3_hours}. Temperature: {temperature_3_hours}°C")
            message1 = f"Bad weather warning! Expected {weather_description_3_hours} in the next 3 hours. \nTemperature: {temperature_3_hours}°C"
        else:
            message1 = f"Weather in {city} looks good for the next 3 hours!"

        if is_bad_weather(forecast_24_hours):
            timestamp_24_hours = forecast_24_hours['dt_txt']
            weather_description_24_hours = forecast_24_hours['weather'][0]['description']
            temperature_24_hours = forecast_24_hours['main']['temp']
            weather_warnings.append(f"{timestamp_24_hours}: {weather_description_24_hours}. \nTemperature: {temperature_24_hours}°C")
            message2 = f"Bad weather warning! Expected {weather_description_24_hours} in the next 24 hours. Temperature: {temperature_24_hours}°C"
        else:
            message2 = f"Weather in {city} looks good for the next 24 hours! "

        return jsonify({"message1": message1, "message2": message2}), 200

    except requests.exceptions.RequestException as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# Register route
@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data["username"]
    email = data["email"]
    password = data["password"]

    # Check if the username already exists
    if users_collection.find_one({'username': username}):
        return jsonify({"message": "Username already exists. Choose a different one."}), 400
    else:
        users_collection.insert_one({'username': username, 'email': email, 'password': password})

    # Registration successful
    return jsonify({"message": "Registration successful. You can now log in."}), 201

# Login route
@app.route("/login", methods=["POST"])
def login():
    data = request.get_json()
    username = data["username"]
    password = data["password"]

    #logging.debug(f"Login request received - Username: {username}, Password: {password}")  # Stampa i valori delle variabili

    user = users_collection.find_one({"username": username, "password": password})
    if user:
            return jsonify({"message": "Login successful."}), 201
    else:
        return jsonify({"message": "Invalid username or password"}), 401
    
# Test database connection --> Remove this route in production
@app.route('/test_db_connection')
def test_db_connection():
    try:
        # Check if we can list the database names
        db_names = client.list_database_names()
        return jsonify({"message": "MongoDB connected successfully!", "databases": db_names}), 200
    except Exception as e:
        return jsonify({"message": f"Error connecting to MongoDB:"}), 500


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=False)