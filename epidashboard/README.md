# Dashboard

Application web permettant de gérer divers services contenant chacun au moins 1 widgets.

## Technologie utilisé
- Java
- Html
- Css
- Jquery
- Ajax
- Docker
- Spring boot
- IntelliJ


## Installation

Conditions préalables
- docker
- Java 1.8

Fedora

```bash
sudo yum install docker-ce
```
or
```bash
sudo dnf install docker-ce
```


Ubuntu

```bash
sudo apt-get install docker-ce
```


## Docker

Run docker 

```python
cd DEV_dashboard_2019

sudo docker-compose build
```
Run the Docker container:

```python
sudo docker-compose up
```

### Compile sans docker.
Pour ceux qui ne veulent pas docker, vous pouvez run le programme comme ci-dessous:

```python
rm -rf build && ./gradlew build bootJar && java -jar build/libs/epidashboard-0.0.1-SNAPSHOT.jar > .log
```

## Voir le résultat
    http://localhost:8080

## Authentifications disponible
- Github
- Facebook
- Google
- Youtube
- Twitter
- Login/password


## Services disponible(12)
- Météo
- Cinéma
- Steam
- Reddit
- Twitter
- Deezer
- MemeCat
- Giphy
- Youtube
- Github
- IpLocalize
- Twitch

## Widgets disponible(19)
- Météo: actuelle
- Météo: prévision
- Cinéma: Film à l'affiche
- Cinéma: Recherche de film
- Steam: Nombre de joueurs connecté
- Steam: Recherche de news de jeux
- Reddit: Recherche de subreddit
- Twitter: Recherche de Tweets
- Twitter: Recherche de users
- Twitter: Possibilité de Tweeter
- Deezer: Recherche et lancement de musique
- MemeCat: Affichage de meme de cat aléatoire par thème
- Giphy: Recherche de Gif
- Youtube: Recherche de chaine youtube
- Youtube: Recherche de vidéo youtube
- Github: Recherche de repos
- IpLocalize: localisation des ip
- Twitch: Affiche N top streamers les plus recherchés
- Twitch: Affiche N top steams les plus recherchés

## Développement
Projet Epitech réalisé sur une période de 1 mois en groupe de 2 (Baptiste Tougouri et Tom Rouvier).