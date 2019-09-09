# Kafka Salsa Setup Guide
Welcome to the setup guide for Kafka Salsa. This guide covers:
1. Installation: Building and running Kafka Salsa.
2. Specify the Application: Choosing and configuring an implementation approach.
3. Deploy: Run Kafka Salsa locally or deploy to Kubernetes.
4. Load Data: Ingest data into your Kafka cluster.
5. REST API: Query recommendations or the graph store directly over REST.

## 1. Installation
1. Clone the repository: `git clone git@github.com:torbsto/kafka-salsa.git`
2. Install [Apache Maven](https://maven.apache.org/install.html).
3. Navigate into this repository: `cd ./kafka-salsa`
4. Build the project with Maven: `mvn package`
5. (Optional for local development) Install [Docker](https://www.docker.com/products/docker-desktop) and start a local Kafka cluster: `cd ./dev/  && docker-compose up`
6. Run Kafka Salsa: `java -jar target/kafka-salsa.jar ...`
7. Note that Kafka Salsa contains four different implementation approaches. You can specify which approach to use using the command line. We will cover how to run the different approaches in the next section.

## 2. Specify the Application
Kafka Salsa implements four approaches to store and query the user-tweet-interaction graph. You must specify which approach to use by choosing a [Kafka Streams Processor](https://kafka.apache.org/10/documentation/streams/developer-guide/processor-api.html) on startup.

 | Command        |  Description                        | 
 | -------------- | ----------------------------------- |
 | range-key      | RangeKey Edge Processor |
 | sampling       | Sampling Edge Processor |
 | segmented      | Edge processor with GraphJet-like engine |
 | simple         | Simple Edge Processor |
 
 The command is simply the first parameter for to the JAR call from above:
 
 ```bash
java -jar target/kafka-salsa.jar simple ...
 ```
 
### Parameters
All approaches share a set of parameters that can be specified on start. Please note that all required parameters must be specified:

| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --application-id | yes     | Name of streams application | - |
| --host | yes     | Host address of the REST service | - |
| --port | no      | Port of REST service | 8070 |
| --brokers | yes  | Address and port of kafka brokers | - |
| --schema-registry-url | yes  | Address and port of schema registry | - |
| --topic | no  | Name of the input topic | edges |

Some approaches support additional parameters. We initialize them with sensible defaults, but can be adjusted for your use-case:

### Additional Sampling Approach Parameters

| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --buffer | no | Size of buffer for sampling edge processor | 5000 |

### Additional Segmentation Approach Parameters

| Parameter        | Required | Description                        | Default |
| ---------------- | -------- | ---------------------------------- | ------- |
| --segments | no | Segments inside graphjet index | 10 |
| --pools | no | Pools inside graphjet segment | 16 |
| --nodesPerPool | no | Nodes per graphjet pool | 131072 |

## 3. Deploy
## Local Development
We provide a docker-compose setup for local development and testing purposes. It contains the services Zookeeper, Kafka and Confluent's Schema Registry. Execute `docker-compose up` in the `./dev/` directory to start the services.
To run the Kafka Salsa with the local Docker setup running, execute the following command:

```bash
java -jar target/kafka-salsa.jar simple --host=localhost  --brokers=localhost:29092 --schema-registry-url=http://localhost:8081
````

## Kubernetes
We also provide a bash script that deploys our full Kafka Salsa setup to Microsoft Azure using Kubernetes.

```bash
sudo chmod -R +x ./kubernetes/ 
cd ./kubernetes/
./run.sh
```

## 4. Loading Data
We provide two Kafka Producers that help ingest data into your Kafka cluster (local or remote). Two Kafka Producers are located in the `de.hpi.msd.salsa.producer` package. The `MockDataProducer.java` creates random data in a fixed time interval, and the `CsvDataProducer.java` can ingest CSV data into a topic. To ingest our evaluation dataset from [twitter-dataset](https://github.com/philipphager/twitter-dataset/), use the `CsvDataProducer.java`.

## 5. REST API
The REST API consists of a recommendation service and an adjacency query service. The responses are in JSON.

### Recommendation Queries
Get the top n recommendations for a user:
```
GET http://localhost:8070/recommendation/salsa/userId?limit=n
```

Specify the length and number of random SALSA walks for the recommendation:
```
GET http://localhost:8070/recommendation/salsa/userId?limit=n&walks=10&walk_length=100
```

### Adjacency State Queries
You can query the graph store directly, which is useful for debugging.

Get the degree of a node:
```
GET http://localhost:8070/state/[left|right]Node/id/degree
```
Get the neighbors of a node:
```
GET http://localhost:8070/state/[left|right]Node/id/neighborhood
```
