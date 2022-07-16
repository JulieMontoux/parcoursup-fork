#!/bin/bash
# A lancer depuis le répertoire racine du projet avec : ./test-exe/prod/run-ci.sh

export TNS_ADMIN=test-exe/prod/ci/
export PARAMS_FILE=test-exe/prod/ci/params.xml

dockerize -wait tcp://parcoursup-bdd:8080 -timeout 3600s ./test-exe/prod/exec.sh

echo "Tests exécutés avec succès."
