#!/usr/bin/env bash

# Wrapper around the Ktor backend

SCRIPT_DIR="$(dirname -- "$(realpath -- "$0")")"

export RECIPES_SECRET_KEY="Test"
cd "$SCRIPT_DIR/../.."
./gradlew :backend-kt:run --args="../backend/seed_data"