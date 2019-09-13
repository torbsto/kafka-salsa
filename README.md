# Kafka Salsa
## 1. Abstract

## 2. Repository Overview
This repository is part of a larger project. Here is a list of all related repositories:
* [kafka-salsa](https://github.com/torbsto/kafka-salsa): Reference implementation and project documentation.
* [kafka-salsa-evaluation](https://github.com/philipphager/kafka-salsa-evaluation): Evaluation suite for [kafka-salsa](https://github.com/torbsto/kafka-salsa).
* [twitter-cralwer](https://github.com/philipphager/twitter-crawler): Twitter API crawler for user-tweet-interaction data.
* [twitter-dataset](https://github.com/philipphager/twitter-dataset): Crawled datasets of user-tweet-interactions used in evaluation.

## 3. Build & Run
If you have [Apache Maven](https://maven.apache.org) installed, you can clone and build the project simply by runnig:

```bash
git clone git@github.com:torbsto/kafka-salsa.git
cd ./kafka-salsa/
mvn package
```

But there is more to learn about the setup then using those three commands. You can find a detailed guide on how to configure, run and deploy Kafka Salsa in our [CONTRIBUTINGS](https://github.com/torbsto/kafka-salsa/blob/master/CONTRIBUTING.md) guide.

## 4. Motivation

## 5. Contribution

## 6. Related Work
This project is motivated by the GraphJet production recommender system at Twitter[1] that holds an entire bipartite user-tweet-interaction graph in memory on a single machine to compute real-time recommendations using a personalized SALSA random-walk algorithm. Before Twitter, several companies have described large-scale, production recommender systems [6, 7, 8]. However, unlike GraphJet, these systems are not real-time as they precompute recommendations in batches before they are requested by users. 

GraphJet's predecessor, the "Who To Follow" (WTF) system [9], is the first recommender system that proposes to store an entire production-scale follower graph in memory on a single machine. WTF also uses a personalized SALSA algorithm to compute recommendations, but WTF is not used in a real-time environment as recommendations are precomputed and stored in a DBMS on a daily schedule.

Conceptually, GraphJet borrows heavily from Twitters search engine Earybird TODO[10], whose index structure to store posting lists is very similar in how GraphJet manages adjacency lists to store the interaction graph.

Pinterest's production system Pixie[2] is the closest project to GraphJet, also storing a bipartite graph in memory on a single machine. But in contrast to a personalized SALSA, they propose a novel random walk algorithm that terminates early once the results start converging. 

The findings by WTF, Pixie and GraphJet inspired us to adopt the concepts of an undistributed, graph-based, real-time recommender system. This project can be seen as a Kafka-based adaption of GraphJet, but with various extensions to the storage layer to evaluate different storage options of the Kafka Streams platform and how they compare to the original and custom graph storage of GraphJet.

## 7. GraphJet
GraphJet can be divided into three modules: a storage engine, a recommendation engine, and a REST service. The storage engine processes incoming user-tweet interactions and creates the bipartite graph. To create recommendations, a third party calls the REST service. It forwards the request to the recommendation engine. The recommendation engine computes recommendations by performing SALSA with the storage engine's bipartite graph. In the following, we describe the storage of the bipartite graph and SALSA. 

### Storage Engine
The bipartite graph consists of two indices. One index stores all tweets for a specific user. The other stores all users for a specific tweet. An index is based on mutable and immutable index segments. At any time, there is one mutable segment, which processes all write operations and is optimized for this. It stores an adjacency list in an array. However, it does not know the number of interactions a particular tweet or user has in advance. Therefore, it can not allocate a correctly sized array. GraphJet introduces the idea of edge pools to solve this problem. An edge pool is an array of size 2<sup>i</sup>. When an edge pool is full, the index segment creates a new edge pool with size 2<sup>i+1</sup>. This is based on the observation, that user-tweet interactions roughly follow the power-law distribution. However, reading nodes from a mutable segment requires jumping to each edge pool separately. Hence, GraphJet optimizes mutable segments as they become immutable. It allocates a continuous array that can hold the nodes from all edge pools.

Especially since GraphJet does not shard data, the storage space is limited on a node. Therefore, GraphJet deletes old index segments. Additionally, the authors argue that older interactions can deteriorate recommendation results because those interactions may not reflect current interests.

### SALSA
Lempel et al. introduced Stochastic Approach for Link-Structure Analysis (SALSA) [5] as a web page rank algorithm. Originally, SALSA performs a Monte Carlo simulation of two independent random walks to differentiate between so-called authorities and hubs. This is used to create distinct node sets and with that the bipartite graph. Since the distinct node sets are inherent in our use-case, SALSA simplifies to a Monte Carlo simulation of a single random walk. 

For a given user, we want to compute _k_ recommendations. SALSA's random walk starts with that user's node. It uniformly samples an interaction that leads to a tweet. SALSA counts the times a tweet is visited. From there, it again uniformly samples an interaction leading back to a user. This is repeated a specified number of times. As SALSA uses a Monte Carlo simulation, multiple such random walks are performed. However, they can be easily parallelized. In each iteration, the algorithm performs the steps for each random walk.
The Monte Carlo simulation of random walks results in a count distribution over the visited tweets. SALSA sorts the tweets by their counts and filters tweets that the user already interacted with. Finally, it selects the _k_ first elements. The tweets of the resulting list are the ranked recommendations.

GraphJet extends SALSA to improve the quality of recommendations. First, it introduces a reset probability. At each user sampling step, there is a fixed probability that the query-user is sampled. This should prevent the random walk to wander too far off. Additionally, GraphJet addresses the cold start problem. If a user has no or very few interactions, the resulting recommendation 's quality degrades. Hence, GraphJet allows specifying a set of nodes as the starting point. Twitter calculates a circle of trust for each user, which can be used.

## 8. Approach
 In GraphJet, each node processes all data. However, Kafka auto-scales Kafka Streams' applications with the same application id, so that each application only reads certain partitions. To prevent this behavior, we concatenate a UUID to each application id. 
 
Our Kafa Streams' application reads incoming interaction data from a Kafka topic. We leverage Kafka Streams' state stores to implement the storage engine. State stores are key-value stores. By default, Kafka Streams offers in-memory stores backed by a Java map or persistent stores backed by RocksDB. We use two stores. They represent the two indices of the storage engine. A bipartite graph class exposes the two stores as such. With that, we can implement SALSA agonistic to the underlying storage. A third party calls a REST service to request recommendations for a user. 

The main problem is how to efficiently implement the read and write operations with Kafka Streams' state stores. In the following, we investigate four different approaches.
### Simple
In the first approach, the state stores hold exactly one value for each tweet or user id. The value is a list of all adjacent nodes.  Therefore, a read operation is a single look-up of the key. A write operation first requires fetching the list. Then, we append the new element to the list and update the value in the store. An example of such a state store is shown in the following table.

| Key |Adjacency List  |
|--|--|
| 5 |[200, 50] |
|12 |[60, 120, 60] |
| ... | ...|

### Range Key
The range key approach is based on the possibility to query ranges in state stores. Consider the following table:

| Key |Adjacent Node  |
|--|--|
| (5, 0) | 200 |
| (5, 1) | 50 |
| (12, 0) | 60|
| (12, 1) | 120 |
| (12, 2) | 60 |

The key is a composition of the respective id and a position. The index stores each adjacent node in a single entry. Since range queries are supported, we can query the range (12, 0) to (12, 2) to get all entries for id 12. 
However, it not guaranteed that the entries for id 13 are behind the entries for id 12. Consequently, querying the range (12, 0) to (13, 0) may result in an error. Hence, we use two additional state stores. They store the current position for each id in the respective indices. 

We anticipate two advantages in comparison to the first approach. First, write operations should be more efficient because they do not require the updating of the adjacency list. Additionally, SALSA's read operations only require a sample of the data. With this approach, we can generate a list with positions and only read these. In the first approach, all adjacent nodes have to be read from the state store.

### Sampled
This approach is based on the range key approach. However, we sample the user-tweet interactions by performing reservoir sampling **TODO []** as proposed by Jin [3]. Reservoir sampling is a method to sample streaming data. As shown in figure XX, there is a buffer with a fixed size. The sampling method writes each element into the buffer until the buffer is full. Then, it computes a probability with that an incoming element is written into the buffer. The probability is calculated by dividing the size of the buffer by the number of seen elements. Reservoir sampling chooses a random index at which it replaces the old value. 

We use the state stores as described in the range key approach as the buffer. Since the maximal range is fixed, the range query does not require an additional query to get the current position. Nevertheless, we need to store the count of seen elements to calculate the insertion probability.

Jin [3] shows that a Monte Carlo simulation of random walks can yield correct results on a sample of the data. With this approach, we intend to limit the resource usage of the application.

### Segmented
Kafka Streams allows implementing your own state stores. In this approach, we reimplemented a simplified version of GraphJet's storage engine. 

## 9. Evaluation

## 10. Conclusion & Future Work



## 11. References
[1] Sharma, Jiang, Bommannavar, Larson, and Lin. *GraphJet: Real-Time Content Recommendations at Twitter.* PVLDB (2016).

[2] Eksombatchai, Jindal, J. Liu, Y. Liu, Sharma, Sugnet, Ulrich, and Leskovec. *Pixie: A System for Recommending 3+ Billion Items to 200+Million Users in Real-Time.* WWW (2018).

[3] Jin. *Simulating Random Walks on Graphs in the Streaming Model.* ITCS (2019).

[4] Webber, Moffat, and Zobel. *A similarity measure for indefinite rankings.* TOIS (2010).

[5] Lempel, Ronny, and Shlomo Moran. *SALSA: the stochastic approach for link-structure analysis.* TOIS (2001).

[6] Agarwal, Chen, He, Hua, Lebanon, Ma, Shiv- aswamy, Tseng, Yang, and Zhang. *Personalizing linkedin feed*. SIGKDD (2015).

[7] Covington, Adams, and Sargin. *Deep neural networks for youtube recommendations*. RECSYS (2016).

[8] Linden, Smith, and York. *Amazon.com recommendations: Item-to-item collaborative filtering*. IEEE Internet Comuting (2003).

[9] Gupta, Goel, Lin, Sharma, Wang, and Zadeh. *WTF: The Who to Follow service atTwitter*. WWW (2013)

[10]  Busch, Gade, Larson, Lok, Luckenbill and Lin. *Earlybird: Real-time search at Twitter*. ICDE (2012).
