#!/bin/bash
# A lancer depuis le répertoire racine du projet avec : ./test-exe/prod/run-local.sh

export TNS_ADMIN=test-exe/prod/local/
export PARAMS_FILE=test-exe/prod/local/params.xml

./test-exe/prod/exec.sh

echo "Tests exécutés avec succès."
