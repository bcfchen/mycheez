All notification code is present at this repo : 

https://github.com/ahetawal/notifications_copy


Heroku commands :

# To push new changes..
1. git add .
2. git commit -m "Fixing dependencies"
3. git push heroku master

# To bring up worker process 
heroku ps:scale worker=1

# To restart a dyno
heroku restart

# Tail logs on Heroku
heroku logs --tail


#### Locally run the notifications server ###

1. npm install
2. node server.js
