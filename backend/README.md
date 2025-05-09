# Backend implementation

This repository contains a simple backend for the application which can be self-hosted on your own server.

## Running the latest image version

```bash
# From docker (emulates release mode)
python3 ./scripts/dev.py start

# From flask directly (for debugging)
./scripts/dev.sh
```

## Building the docker image locally

```bash
python3 ./scripts/dev.py build
```

## Open TODOs

* Add installation instructions
* Add proper logging
* Find a better database than yaml files
* Add a check/tests for backwards compatibility
* Provide swagger
* Search for a better library for REST APIs
