## kafka-salsa

### Build & Run Project
The project can be built with Maven. It contains two runnable parts: the main application and Kafka Producer for sample data. They require a Kafka topic called "edges". 
The path of the streams application's main class is `de.hpi.msd.salsa.EdgeToAdjacencyApp.java`. It launches a Kafka Stream application as well as a REST API at localhost:8070. 
The producers are located in the `de.hpi.msd.salsa.producer` package. `MockDataProducer.java` creates random data. The `CsvDataProducer.java` can ingest crawled data into the topic.


### Run Local Kafka Cluster
For local development and testing purposes, there is a docker compose with the necessary services Zookeeper, Kafka and Confluent's Schema Registry. Simply use `docker-compose up` to start the services.

### REST API
The REST API consists of a recommendation service and an adjacency query service. The responses are in JSON.

#### Recommendations
Get the X recommendations for a user:
`GET http://localhost:8070/recommendation/salsa/userId?limit=X`

#### Adjacency State Queries

Get the degree of a node:
`GET http://localhost:8070/state/[left|right]Node/id/degree`
Get the neighbors of a node:
`GET http://localhost:8070/state/[left|right]Node/id/neighborhood`

