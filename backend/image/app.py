#!/usr/bin/python3

import os
import sys
import logging
import secrets
import tempfile
from flask import Flask, request, jsonify, abort
from pathlib import Path
from auth.authentication import token_required
from recipes import database as recipes_db
from recipes import blueprint as recipes_bp
from users import database as users_db
from users import blueprint as users_bp
import sys

app = Flask(__name__)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def get_api_version():
    return {
        "api_version_major": 0,
        "api_version_minor": 5,
        "api_version_patch": 2,
    }

@app.before_request
def log_request():
    app.logger.info(f"Incoming request: {request.method} {request.url} - Body: {request.get_data()}")

@app.after_request
def log_response(response):
    app.logger.info(f"Response: {response.status} - Body: {response.get_data(as_text=True)}")
    return response


@app.route('/health', methods=['GET'])
def health():
    return 'OK', 200


@app.route('/version', methods=['GET'])
@token_required
def version(current_user):
    return {current_user}, 200


if __name__ == '__main__':
    print(f"API version: {get_api_version()}")

    if app.config['DEBUG']:
        # Generate a secure random key for development/testing
        app.config['SECRET_KEY'] = secrets.token_hex(32)
    else:
        app.config['SECRET_KEY'] = os.environ['RECIPES_SECRET_KEY']

    # Use secure temporary directory if no custom path is provided
    default_data_folder = os.path.join(tempfile.gettempdir(), 'my_kitchen_data')
    app.config['DATA_FOLDER'] = default_data_folder

    if len(sys.argv) == 2:
        app.config['DATA_FOLDER'] = sys.argv[1]

    folder = Path(app.config['DATA_FOLDER'])
    if not folder.exists():
        folder.mkdir(parents=True, exist_ok=True)

    print(f"Using data in {str(folder)}")

    app.config['recipes_db'] = recipes_db.Database(folder / 'recipes.yaml', create_backup=True)
    app.config['users_db'] = users_db.Database(folder / 'users.yaml', create_backup=True)

    app.register_blueprint(recipes_bp.RecipesBlueprint, recipes_db=recipes_db)
    app.register_blueprint(users_bp.UsersBlueprint, users_db=users_db)

    # Configure host binding - default to 127.0.0.1 for security, but allow override for containers
    host = os.environ.get('FLASK_HOST', '127.0.0.1')
    port = int(os.environ.get('FLASK_PORT', '5000'))
    
    app.run(host=host, port=port)
