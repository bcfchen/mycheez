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


KeyStore password: mycheez123

keytool -v -list -keystore MyCheez.jks
Enter keystore password:

Keystore type: JKS
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: mycheezkey
Creation date: Aug 13, 2015
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=Amit Hetawal, L=San Francisco, ST=California, C=01
Issuer: CN=Amit Hetawal, L=San Francisco, ST=California, C=01
Serial number: 55cd5e56
Valid from: Thu Aug 13 20:19:50 PDT 2015 until: Mon Aug 06 20:19:50 PDT 2040
Certificate fingerprints:
	 MD5:  F6:AB:34:B6:85:88:D0:79:B5:1D:32:17:46:A2:15:79
	 SHA1: 88:8A:B5:35:53:38:55:B3:4E:35:68:43:CA:27:D5:F8:36:87:2B:62
	 SHA256: B3:DE:AB:A7:6E:07:B9:C8:BC:4C:A3:F1:80:37:38:58:5E:EC:73:BC:D1:58:96:80:CE:46:7E:44:A9:12:1B:93
	 Signature algorithm name: SHA1withRSA
	 Version: 3

