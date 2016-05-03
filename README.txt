cd to parent directory simple-service

mvn install

cd target

To run the Server (seprate cmd windows)

java -classpath dependency/*;classes com.example.jetty.MainJettyServer

to run the client

java -classpath dependency/*;classes com.subscriber.SingleClientConnection
