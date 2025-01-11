#!/usr/bin/python3

from flask import Blueprint, request, jsonify, abort, current_app

from auth.authentication import token_required

RecipesBlueprint = Blueprint('recipes', __name__)


def get_db():
    return current_app.config.get('recipes_db')

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
    success, result = get_db().delete(current_user, recipe_id)
    if success:
        return jsonify({"message": "Recipe deleted successfully", "recipe": result}), 204
    else:
        abort(400, result)
