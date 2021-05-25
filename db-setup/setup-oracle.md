# Déploiement local d'une base de données Oracle

La mise en oeuvre du code de Parcoursup nécessite un accès à des bases de données pouvant être déployées localement
sous la forme de bases **H2** ou **Oracle**.

Le déploiement local d'un serveur de bases de données Oracle peut être réalisé au travers d'un **conteneur Docker**
dont la configuration est disponible dans ce dépôt.

Pour ce faire, assurez-vous que vous disposez bien des outils `docker` / `docker-compose` puis :

1/ lancer la fabrication du conteneur en effectuant :

```
docker-compose build parcoursup-bdd
```

**A noter** : la fabrication du conteneur est relativement longue et peut prendre plusieurs minutes ou dizaines de minutes
selon les caractéristiques de votre serveur ou de votre station de travail (connectivité Internet, ressources disponibles...)

2/ lancer le conteneur en effectuant :

```
docker-compose up -d parcoursup-bdd
```

**Note importante** : le lancement effectif du serveur peut prendre de quelques secondes à quelques minutes. À titre indicatif,
la base de données peut être considérée comme opérationnelle dès lors que la commande :

```
docker exec -ti parcoursup-bdd lsnrctl services | grep 'xepdb1'
```

Retourne un résultat (typiquement : `Service "xepdb1" has 1 instance(s).`).

Veuillez vous assurer, avant de lancer les tests unitaires associés au projet, que le serveur est bien lancé et démarré.
