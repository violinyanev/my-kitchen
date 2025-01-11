#!/usr/bin/env bash

host=https://ultraviolince.com:8019
token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InRlc3R1c2VyIn0.hgvrPuJ1j0PlnnsvYD2mHiFpDycfMgvPYd6ilI3wX78

curl -X GET $host/health


curl -X POST $host/users/login \
 -H "Accept: application/json" \
 --data '{"email":"test@user.com","password":"TestPassword"}' \
 -H "Content-Type: application/json"

curl -X GET $host/recipes \
 -H "Accept: application/json" \
 -H "Authorization: bearer $token" \
 -H "Content-Type: application/json"

echo "Creating a recipe"

curl -X POST $host/recipes \
 -H "Accept: application/json" \
 --data  '{"title":"test","body":"the bodey", "timestamp": 15, "id": 5}'  \
 -H "Authorization: bearer $token" \
 -H "Content-Type: application/json"

echo "Deleting the recipe"

curl -X DELETE $host/recipes/5 \
 -H "Accept: application/json" \
 -H "Authorization: bearer $token" \
 -H "Content-Type: application/json"
