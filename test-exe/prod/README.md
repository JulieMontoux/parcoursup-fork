Test destiné à s'assurer que les programmes `prod` se lancent correctement.

Les programmes `prod` interagissant exclusivement avec une base de données Oracle, ceux-ci ne peuvent aboutir que si le conteneur Docker BDD Oracle a été préalablement lancé.

Pour lancer les tests en local, veuillez vous placer dans le répertoire racine du projet puis effectuer :

```
./test-exe/prod/run-local.sh
```
