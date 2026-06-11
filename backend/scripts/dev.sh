#!/usr/bin/env bash

# Wrapper around the flask backend

SCRIPT_DIR="$(dirname -- "$(realpath -- "$0")")"

export RECIPES_SECRET_KEY="Test"
export FLASK_DEBUG=1
uv run --project "$SCRIPT_DIR/../image" "$SCRIPT_DIR/../image/app.py" "$SCRIPT_DIR/../seed_data"
