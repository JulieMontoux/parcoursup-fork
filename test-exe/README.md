Test destiné à s'assurer que les programmes `bacasable` et `prod` se lancent et s'exécutent correctement.

Pour lancer ces tests en local, veuillez vous placer dans le répertoire racine du projet puis effectuer :

1/ Pour le test des programmes `bacasable` :

```
./test-exe/bacasable/run.sh
```

2/ Pour le test des programmes `prod` :

(ceux-ci interagissant avec une BDD Oracle, veuillez vous assurer que le conteneur Docker correspondant a été préalablement lancé)

```
./test-exe/prod/run-local.sh
```
