#!/usr/bin/python3

import os
import uuid
from flask import Blueprint, request, jsonify, abort, current_app, send_from_directory
from werkzeug.utils import secure_filename

from auth.authentication import token_required

RecipesBlueprint = Blueprint('recipes', __name__)

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_db():
    return current_app.config.get('recipes_db')

def get_images_folder():
    data_folder = current_app.config.get('DATA_FOLDER', '/tmp/data')
    images_folder = os.path.join(data_folder, 'images')
    if not os.path.exists(images_folder):
        os.makedirs(images_folder)
    return images_folder

@RecipesBlueprint.route('/recipes', methods=['GET'])
@token_required
def get_recipes(current_user):
    all = request.args.get('all', False)
    return  get_db().get(current_user, all), 200


@RecipesBlueprint.route('/recipes', methods=['POST'])
@token_required
def create_recipe(current_user):
    data = request.get_json()

    try:
        result, error = get_db().put(current_user, data)
        if result:
            return jsonify({"message": "Recipe created successfully", "recipe": result}), 201
        else:
            abort(400, error)
    except:
        abort(400, "Unknown error")


@RecipesBlueprint.route('/recipes/<int:recipe_id>', methods=['DELETE'])
@token_required
def delete_recipe(current_user, recipe_id):
    success, result = get_db().delete(current_user, recipe_id, get_images_folder())
    if success:
        return jsonify({"message": "Recipe deleted successfully", "recipe": result}), 204
    else:
        abort(400, result)


@RecipesBlueprint.route('/recipes/<int:recipe_id>/image', methods=['POST'])
@token_required
def upload_recipe_image(current_user, recipe_id):
    if 'file' not in request.files:
        abort(400, 'No file part')
    file = request.files['file']
    if file.filename == '':
        abort(400, 'No selected file')
    if file and allowed_file(file.filename):
        # Generate unique filename
        extension = file.filename.rsplit('.', 1)[1].lower()
        filename = f"{recipe_id}_{uuid.uuid4().hex}.{extension}"
        filepath = os.path.join(get_images_folder(), filename)
        file.save(filepath)
        return jsonify({"message": "Image uploaded successfully", "filename": filename}), 201
    abort(400, 'Invalid file type')


@RecipesBlueprint.route('/recipes/images/<filename>', methods=['GET'])
def get_recipe_image(filename):
    return send_from_directory(get_images_folder(), filename)
