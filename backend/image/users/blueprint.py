#!/usr/bin/python3

import jwt
from flask import Blueprint, request, abort, current_app
from auth.authentication import token_required


UsersBlueprint = Blueprint('users', __name__)

def get_db():
    return current_app.config.get('users_db')


@UsersBlueprint.route("/users/login", methods=["POST"])
def login():
    user, error = get_db().validate_login_request(request.get_json())
    if error:
        abort(400, error)

    user["token"] = jwt.encode(
        {"username": user["username"]},
        current_app.config["SECRET_KEY"],
        algorithm="HS256"
    )
    return {
        "message": "Successfully created authentication token",
        "data": user
    }



@UsersBlueprint.route("/users", methods=["GET"])
@token_required
def get_users(current_user):
    return  get_db().get(), 200


@UsersBlueprint.route("/user", methods=["GET"])
@token_required
def get_current_user(current_user):
    return current_user, 200
