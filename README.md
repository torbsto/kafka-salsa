## kafka-salsa

### Build & Run Project
The project can be built with Maven:
```bash
mvn package
```

It contains two runnables: the main application and Kafka Producer for sample data.
To launch the main application, execute the following command:
```bash
java -jar target/kafka-salsa.jar ...
````

#### Commands
You are required to specify a processor type as subcommand: 

 | Command        |  Description                        | 
 | -------------- | ----------------------------------- |
 | range-key      | RangeKey Edge Processor |
 | sampling       | Sampling Edge Processor |
 | segmented      | Edge processor with GraphJet-like engine |
 | simple         | Simple Edge Processor |

#### Parameters
Every subcommand supports the following parameters:

| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --application-id | yes     | Name of streams application | - |
| --host | yes     | Host address of the REST service | - |
| --port | no      | Port of REST service | 8070 |
| --brokers | yes  | Address and port of kafka brokers | - |
| --schema-registry-url | yes  | Address and port of schema registry | - |
| --topic | no  | Name of the input topic | edges |

The sampling processor additionally supports:

| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --buffer | no | Size of buffer for sampling edge processor | 5000 |

The segmented processor additionally supports:

| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --segments | no | Segments inside graphjet index | 10 |
| --pools | no | Pools inside graphjet segment | 16 |
| --nodesPerPool | no | Nodes per graphjet pool | 131072 |

#### Producers
The producers are located in the `de.hpi.msd.salsa.producer` package. `MockDataProducer.java` creates random data. The `CsvDataProducer.java` can ingest crawled data into the topic.

### Run Locally
For local development and testing purposes, there is a docker compose with the necessary services Zookeeper, Kafka and Confluent's Schema Registry. Simply execute `docker-compose up` in the dev directory to start the services.
To run the application, execute the following command:
```bash
java -jar target/kafka-salsa.jar --host=localhost  --brokers=localhost:29092 --schema-registry-url=http://localhost:8081
````

### REST API
The REST API consists of a recommendation service and an adjacency query service. The responses are in JSON.

#### Recommendations
Get the X recommendations for a user:
```
GET http://localhost:8070/recommendation/salsa/userId?limit=X
```

#### Adjacency State Queries

Get the degree of a node:
```
GET http://localhost:8070/state/[left|right]Node/id/degree
```
Get the neighbors of a node:
```
GET http://localhost:8070/state/[left|right]Node/id/neighborhood
```

