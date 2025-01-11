#!/usr/bin/python3

import os
import sys
from flask import Flask, request, jsonify, abort
from pathlib import Path
from auth.authentication import token_required
from recipes import database as recipes_db
from recipes import blueprint as recipes_bp
from users import database as users_db
from users import blueprint as users_bp
import sys

app = Flask(__name__)

def get_api_version():
    return {
        "api_version_major": 0,
        "api_version_minor": 5,
        "api_version_patch": 2,
    }


@app.route('/health', methods=['GET'])
def health():
    return 'OK', 200


@app.route('/version', methods=['GET'])
@token_required
def version(current_user):
    return {current_user}, 200


if __name__ == '__main__':
    print(f"API version: {get_api_version()}")

    app.config['SECRET_KEY'] = os.environ['RECIPES_SECRET_KEY']

    app.config['DATA_FOLDER'] = '/tmp/data'

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

    app.run(host='0.0.0.0', port=5000)
