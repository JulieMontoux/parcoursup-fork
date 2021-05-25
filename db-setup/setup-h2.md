# Déploiement local de bases H2

La mise en oeuvre du code de Parcoursup nécessite un accès à des bases de données pouvant être déployées localement
sous la forme de bases **H2** ou **Oracle**.

Le **SGBD utilisé par défaut est H2** et aucune manipulation particulière n'est requise pour la génération
des bases, qui sont automatiquement créées ou mises à jour à l'exécution du code affairant.

Celles-ci sont alors enregistrées sur le chemin `./h2/bacasable.mv.db` et `./h2/tests.mv.db` et on se référera
au contenu du fichier pom.xml pour en récupérer les identifiants de connexion, pour un accès direct via la
**console web H2** par exemple.