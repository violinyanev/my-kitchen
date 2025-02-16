#!/usr/bin/env bash

# Wrapper around the flask backend

SCRIPT_DIR="$(dirname -- "$(realpath -- "$0")")"

RECIPES_SECRET_KEY="Test" python3 $SCRIPT_DIR/../image/app.py $SCRIPT_DIR/../seed_data
