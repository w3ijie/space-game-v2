# Spaceports

_This game is purely fictional._

## Overview

In the year 2100, Singapore has remain its position as the port city. Except, it accepts Spaceships now. You are the port officer. You are in charge of approving cargo spaceships and disapproving the invader spaceships from entering Singapore. Beware! There are aliens that will appear from the galaxy to interrupt the peace of Singapore. Stop these invaders and build Singapore's economy, officer!

This game challenges the player's reaction speed.

## Set up

Before you run the project, make sure to
1. File > Sync Project with Gradle Files
   - This is to download the external libraries for making RESTful requests
1. Build > Clean Project
2. Build > Rebuild Project

This project uses an [external API server](https://github.com/ang-rui-yan/spaceports-leaderboard) to asynchronously fetch the leaderboard. Hence, we need to add in the JWT secrets and token to be able to run the code.

In your local.properties, add the following without quotations:

```
server.url=
token=
secret.key=
```
For the professors, we have submitted the credentials to you through email.

## How to play

Click on either approve or disapprove the spaceships entering without having it to crash into the port. Click on the alien that appears on the screen as they will be there to block your view.

