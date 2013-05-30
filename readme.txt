Secure Network Programming
Semester 1, 2013
by Edward Booth (s3327858), Lauren Grimes (s3228871) and Arthur Papadakis (s3207978)

Server command:
java -Djavax.net.ssl.keyStore=ass2.ks -Djavax.net.ssl.keyStorePassword=ass2key Server [portnumber]

Client command:
java -Djavax.net.ssl.trustStore=ass2.ks Client localhost [portnumber] [username]