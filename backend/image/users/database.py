#!/usr/bin/env python3

import yaml
import jsonschema
import time
import shutil
from datetime import datetime
from pathlib import Path

USERS_SCHEMA = {
    'type': 'object',
    'properties': {
        'users': {
            'type': 'array',
            'items': {
                'type': 'object',
                'properties': {
                    'password': {'type': 'string'},
                    'name': {'type': 'string'},
                    'email': {'type': 'string'},
                },
                'additionalProperties': False,
                'required': [ 'name', 'password', 'email' ]
            },
        },
    },
    'additionalProperties': False,
}

empty = {
    'users': [
    ]
}

class User:
    def __init__(self, username, password):
        self.username = username
        self.password = password


class Database:
    def __init__(self, file, create_backup = False):
        self.file = file
        if not file.exists():
            with file.open(mode='w') as f:
                f.write(yaml.safe_dump(empty))
        elif create_backup:
            backup_directory = file.parent / "backup"
            date = datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d-%H-%M')
            backup_file = backup_directory / (file.stem + "-" + date + ".yaml")
            backup_directory.mkdir(exist_ok=True)
            shutil.copy(file, backup_file)

        with open(self.file, 'r') as f:
            self.data = yaml.safe_load(f)

        try:
            jsonschema.validate(self.data, USERS_SCHEMA)
        except:
            # TODO implement conversion scripts or use a proper DB
            print("Found database file can not be validated! Creating a backup and a new database")
            backup_directory = file.parent / "backup"
            date = datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d-%H-%M')
            backup_file = backup_directory / (file.stem + "-incompatible-" + date + ".yaml")
            backup_directory.mkdir(exist_ok=True)
            shutil.copy(file, backup_file)
            with file.open(mode='w') as f:
                f.write(yaml.safe_dump(empty))
            with open(self.file, 'r') as f:
                self.data = yaml.safe_load(f)


    def validate_login_request(self, data):
        if not data:
            return None, "Must provide user login credentials"

        email = data.get('email', None)
        password = data.get('password', None)
        if not email or not password:
            return None, "Must provide user email and password"

        users = [u for u in self.data['users'] if u['email'] == email]
        if not users or not len(users) == 1:
            return None, f"Could not find user with email {email}"

        user = users[0]
        if user['password'] == password:
            return {
                'email': email,
                'username': user['name'],
            }, None
        else:
            return None, "Bad credentials"


    # TODO protect
    def create(self, email, username, password):
        new_user = {
            'email' : email,
            'name': username,
            'password': password,
        }
        # TODO don't validate the entire array, just the new user
        jsonschema.validate(self.data, USERS_SCHEMA)

        self.data['users'].append(new_user)

        return new_user


    def get(self):
        return self.data['users']


    def get_by_username(self, username):
        users = [u for u in self.data['users'] if u['name'] == username]
        if users and len(users) == 1:
            return users[0]
        return None
