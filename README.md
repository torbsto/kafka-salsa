# Kafka Salsa
## 1. Abstract
GraphJet is Twitter's real-time recommender system that uses a personalized SALSA algorithm for tweet recommendations on an unpartitioned bipartite user-tweet graph. Kafka Salsa is our implementation of GraphJet using the Kafka Streams platform. We evaluate how three Kafka Stream state store patterns perform in comparison to GraphJet's custom storage engine. We show that our system can achieve fast real-time recommendations by storing the graph in a standard Kafka key-value store. We also introduce a custom state store based on the architecture of GraphJet that deals with memory limitations by pruning old segments of the graph. Our implementation allows for easy integration of a GraphJet-like recommender system into an existing Kafka environment and enables the reuse of individual parts of our system, such as our four storage engines for bipartite graphs.

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
Recommender systems are an integral part of most of today's web services. Large websites such as Amazon, LinkedIn, or Youtube operate production recommender systems that handle vast amounts of user data to personalize their products. To increase the currentness and relevance of their recommendations, real-time computation of recommendations has become a crucial trend in the industry in recent years.

Social networks such as Twitter and Pinterest introduced recommender systems that use random walk algorithms to compute real-time recommendations on large social network graphs. Twitter proposed with  GraphJet the first production system to keep an entire social graph of users and tweet interactions in memory on a single machine to compute tweet recommendations. We built Kafka Salsa on the principles of GraphJet to create a fast, real-time recommender system that is easily deployed into the Kafka ecosystem. The Kafka Streams framework enables us to also extend GraphJet with a managed graph storage that recovers automatically from failure and is easily scalable in a production setting. We present four different graph stores that you can plug  into the Kafka Salsa application and evaluate their respective performance.

## 5. Contribution
We contribute the following:
* Kafka Salsa, an adaption of Twitter's real-time, graph-based recommender system GraphJet on Kafka Streams.
* Four graph storage engines that can be plugged into the recommender system that extend GraphJet with automatic failure recovery.

We publish our entire project including the recommender sytem, our dataset, crawler, evaluation suite and scripts to deploy the application on Kubernetes.

## 6. Related Work
This project is motivated by the GraphJet production recommender system at Twitter [1] that holds an entire bipartite user-tweet-interaction graph in memory on a single machine to compute real-time recommendations using a personalized SALSA random-walk algorithm [5]. Before Twitter, several companies have described large-scale, production recommender systems [6, 7, 8]. However, unlike GraphJet, these systems are not real-time as they precompute recommendations in batches before they are requested by users. 

GraphJet's predecessor, the "Who To Follow" (WTF) system [9], is the first recommender system that proposes to store an entire production-scale follower graph in memory on a single machine. WTF also uses a personalized SALSA algorithm to compute recommendations, but WTF is not used in a real-time environment as recommendations are precomputed and stored in a DBMS on a daily schedule.

Conceptually, GraphJet borrows heavily from Twitters search engine Earybird [10], whose index structure to store posting lists is very similar in how GraphJet manages adjacency lists to store the interaction graph.

Pinterest's production system Pixie [2] is the closest project to GraphJet, also storing a bipartite graph in memory on a single machine. But in contrast to a personalized SALSA, they propose a novel random walk algorithm that terminates early once the results start converging. 

The findings by WTF, Pixie and GraphJet inspired us to adopt the concepts of an undistributed, graph-based, real-time recommender system. This project can be seen as a Kafka-based adaption of GraphJet, but with various extensions to the storage layer to evaluate different storage options of the Kafka Streams platform and how they compare to the original and custom storage layer of GraphJet.

## 7. GraphJet
GraphJet can be divided into three modules: a storage engine, a recommendation engine, and a REST service. The storage engine processes incoming user-tweet interactions and creates a bipartite graph. To create recommendations, a third party calls the REST service. It forwards the request to the recommendation engine. It computes recommendations by performing SALSA with the bipartite graph created by the storage engine. In the following, we describe the storage of the bipartite graph and SALSA. 

### Storage Engine
The bipartite graph consists of two indices. One index stores all tweets for a specific user. The other stores all users for a specific tweet. An index is based on mutable and immutable index segments. At any time, there is a single mutable segment. It processes all write operations and is optimized for fast inserts by storing an adjacency list in an array. However, it does not know the number of interactions a particular tweet or user has in advance. Therefore, it can not allocate a correctly sized array. GraphJet introduces the idea of edge pools to solve this problem. An edge pool is an array of size 2<sup>i</sup>. When it is full, the index segment creates a new edge pool with size 2<sup>i+1</sup>. This is based on the observation, that user-tweet interactions roughly follow the power-law distribution. However, reading nodes from a mutable segment requires jumping between the edge pools. Hence, GraphJet optimizes mutable segments once they reach their  capacity limit. Full segments are copied into a new, immutable segment, which allocates a continuous array that can hold the nodes from all edge pools in a single array. Storing nodes in the immutable segment in large, continous spaces of memory optimizes for fast read operations, while all write operations are directed to a new, empty mutable segment.

Since GraphJet does not shard data, the storage space is limited on a compute node. Therefore, GraphJet deletes old index segments when the server runs out of memory. Additionally, the authors argue that older interactions can deteriorate recommendation results because those interactions may not reflect current user interests. 

### SALSA
Lempel et al. introduced Stochastic Approach for Link-Structure Analysis (SALSA) [5] as a web page rank algorithm. Originally, SALSA performs a Monte Carlo simulation of two independent random walks to differentiate between so-called authorities and hubs. This is used to create distinct node sets and with that a bipartite graph. Since the distinct node sets are inherent in our use-case, SALSA simplifies to a Monte Carlo simulation of a single random walk. 

For a given user, we want to compute *k* recommendations. SALSA's random walk starts with the user's node. It uniformly samples an interaction that leads to a tweet the user formerly interacted with. SALSA counts the times a tweet is visited. From there, it again uniformly samples an interaction leading back to a user. This is repeated a specified number of times. Figure 1 shows a simple example for computing recommendations with SALSA.

#### Figure 1: Personalized SALSA random walk for tweet recommendation
<p align="center">
<img src="https://user-images.githubusercontent.com/17516685/64905753-22b64d80-d6dd-11e9-8fc1-4ebba6df904c.gif">
</p>

As SALSA uses a Monte Carlo simulation, multiple such random walks are performed. However, they can be easily parallelized. In each iteration, the algorithm performs the steps for each random walk.
The Monte Carlo simulation of random walks results in a count distribution over the visited tweets. SALSA sorts the tweets by their counts and filters tweets that the user already interacted with. Finally, it selects the *k* first elements. The tweets of the resulting list are the ranked recommendations.

GraphJet extends SALSA to improve the quality of recommendations. First, it introduces a reset probability. At each user sampling step, there is a fixed probability that the query-user is sampled. This should prevent the random walk to wander too far off its starting node. Additionally, GraphJet addresses the cold start problem. If a user has no or very few interactions, the resulting recommendation's quality degrades. Hence, GraphJet allows specifying a set of nodes as the starting point. Twitter calculates a circle of trust for each user, which can be used.

## 8. Approach
In GraphJet, each node processes all data. However, Kafka auto-scales Kafka Streams' applications with the same application id, so that each application only reads certain partitions. To prevent this behavior, we concatenate a UUID to each application id. 
 
Our Kafa Streams' application reads incoming interaction data from a Kafka topic. We leverage Kafka Streams' state stores to implement the storage engine. By default, Kafka Streams offers key-value stores backed by a Java map or RocksDB. We use two sate stores that represent the two indices of the storage engine. A bipartite graph class exposes them as such. With that, we can implement SALSA agonistic to the underlying storage. A third party calls a REST service to request recommendations for a user. 

The main question is how we can efficiently implement the read and write operations with Kafka Streams' state stores. In the following, we describe four different approaches.

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
However, it is not guaranteed that the entries for id 13 are positioned after the entries for id 12. Consequently, querying the range (12, 0) to (13, 0) may result in an error. Hence, we use two additional state stores. They store the current position for each id in the respective indices. 

We anticipate two advantages in comparison to the first approach. First, write operations should be more efficient because they do not require the updating of the adjacency list. Additionally, SALSA's read operations only require a sample of the data. With this approach, we can generate a list with positions and only read these. In the first approach, all adjacent nodes have to be read from the state store.

### Sampled
This approach is based on the range key approach. However, we sample the user-tweet interactions by performing reservoir sampling [11] as proposed by Jin [3]. Reservoir sampling is a method to sample streaming data. As shown in Figure 2, there is a buffer with a fixed size. The sampling method writes each element into the buffer until the buffer is full. Then, it computes a probability with that an incoming element is written into the buffer. The probability is calculated by dividing the size of the buffer by the number of seen elements. Reservoir sampling chooses a random index at which it replaces the old value.

#### Figure 2: Reservoir sampling with buffer size three
<p align="center">
<img src="https://user-images.githubusercontent.com/17516685/64905756-24801100-d6dd-11e9-88f6-f37689fa1bfc.jpg" alt="reservoir sampling">
</p>


We use the state stores as described in the range key approach as the buffer. Since the maximal range is fixed, the range query does not require an additional query to get the current position. Nevertheless, we need to store the count of seen elements to calculate the insertion probability.

Jin [3] shows that a Monte Carlo simulation of random walks can yield correct results on a sample of the data. With this approach, we intend to limit the resource usage of the application.

### Segmented
Kafka Streams allows implementing your own state stores. In this approach, we reimplemented a simplified version of GraphJet's storage engine. Mainly, we adopt the index structures of immutable and mutable segments that are optimized for reads and writes and remove old segments once the server runs out of memory.

## 9. Evaluation
We conduct two evaluations of our four implementation approaches. First, we measure how the different engines impact the speed of the overall recommender system by comparing Round-Trip-Times of requesting recommendations through our REST API. Afterward, we measure how the different implementations affect the quality of the recommendations by comparing the rankings of the top ten recommendations.

### Setup
We conduct our evaluation on a graph dataset of 7.2M tweet interactions between 1.8M users and 1.7M tweets. We publish the [dataset](https://github.com/philipphager/twitter-dataset/tree/master/v1), [crawler](https://github.com/philipphager/twitter-crawler), and [crawling strategy](https://github.com/philipphager/twitter-crawler/edit/master/README.md).

The evaluation setup consists of a total of eight computing nodes (Azure Standard_E2s_v3: 2x 2,3 GHz Intel XEON CPUs, 16GB RAM). We use a Kafka cluster of three nodes and the fourth node as the schema registry used for Apache Avro serialization. We use the remaining four nodes to deploy separate instances of our recommender system, each with a different storage layer. All four recommender systems subscribe to the Kafka cluster, read in the entire dataset, and expose a REST API to request recommendations.

We uniformly sample 100 users from the dataset and request recommendations from each of the four systems. We conduct multiple requests per user, per system, and vary the number of random SALSA walks (100, 1,000, 10,000) and the length of the walks (100, 1,000, 10,000). 

Our evaluation setup was deployed on Microsoft Azure using Kubernetes. We publish all scripts to deploy the project in the `kubernetes/` directory of this repository, and our full [evaluation suite](https://github.com/philipphager/kafka-salsa-evaluation) in a separate repository. The evaluation suite to perform the API requests is executed on a local machine outside the datacenter.

### Request Round-Trip Time
We measure the Round-Trip Time (RTT) for each HTTP request to reach the server, compute the recommendations, and respond back to our evaluation setup. 

Figure 3 displays the results of performing 100 user requests with a fixed number of 100 random walks with varying length (100, 1000, 10,000).

#### Figure 3: Request Round-Trip Time for 100 random walks
![request-time](https://user-images.githubusercontent.com/9155371/64848980-b3742700-d612-11e9-9920-8b40c858daad.png)

The simple and the segmented approach are the two best performing implementations with a mean RTT of ≈180ms. The sampling approach has a mean of ≈220 ms, and the range-key application is the slowest with an RTT of ≈600ms. Notably, the increase of the walk length has minimal impact on the overall RTT. 

Figure 4 displays the results of performing 100 user requests with an increased number of 1,000 random walks with varying length  (100, 1000, 10,000).

#### Figure 4: Request Round-Trip Time for 1000 random walks
![request-time-1000](https://user-images.githubusercontent.com/9155371/64848977-b3742700-d612-11e9-8169-275f1a1e6ece.png)

Increasing the number of random walks has a significant impact on the overall performance of the recommender systems. The simple and segmented approaches take an average of ≈5sec to compute a recommendation, the sampling approach ≈7sec while the range-key implementation takes ≈30sec. Increasing the number of random walks has a more significant impact on the recommendation speed than increasing the length of the random walks.

Surprisingly, the simple implementation on Kafka Streams has a comparable read speed to the optimized GraphJet storage engine. The sampling approach needs to perform more read operations to fetch the number of seen nodes and perform a range query, which is slower than the single list retrieval in the simple approach. Surprisingly far off is the range-key implementation, which does not scale well compared to the other approaches. Since both the sampling and the range-key engines use a range key query to fetch nodes from the state store, it is apparent that sampling the nodes to reduce the number of total stored nodes in the state store enables performance benefits.

### Ranking Analysis
Next, we look at qualitative differences in the recommendations. Three of our engines store the entire bipartite graph, while the sampling approach keeps a maximum of 40 interactions per tweet or user. The difference in graph structures can impact the returned recommendations.

We evaluate the recommendations using two metrics: The Average Set Overlap, or the percentage of common elements between the top ten recommendations without respecting their order. And secondly, we calculate the Rank-Biased Overlap [4] between the top ten recommendations to evaluate differences in the result rankings.

One difficulty in evaluating a random-walk based recommender engine is their non-deterministic nature. Results can generally differ between random walks, meaning two engines operating on the same graph can return different recommendations, and even the same engine can return different results for two consecutive requests for the same user.

Figure 5 compares the percentage of common recommendations between the four approaches.

#### Figure 5: Percentage of common recommendations between approaches (Average Set Overlap)
![overlap](https://user-images.githubusercontent.com/9155371/64848975-b3742700-d612-11e9-902b-278a9242789f.png)

We can see that the simple, segmented, and range-key approach have 71% common recommendations. We attribute the remaining 29% to randomness in the walks since all three engines share the same data. Only 20% of the sampling recommendations appear in the results of the other engines. Meaning only two out of ten recommendations are similar. This 51% difference is a significant difference in quality.

Next, we inspect the order of the returned results by looking at the Rank-Biased Overlap of the different approaches. The RBO  compares two lists by looking at the Set Overlap at each rank. The resulting overlaps are weighted by their position in the list so that differences in the top ranks are more penalized than differences at the bottom. The resulting value is between 0 and 1.0, with 1.0 denoting two equal rankings and 0 denoting two rankings that have no elements in common.

Figure 6 displays the Rank-Biased Overlap between the four approaches.

#### Figure 6: Rank-Biased Overlap between approaches
![rank-biased](https://user-images.githubusercontent.com/9155371/64848976-b3742700-d612-11e9-83d0-8354e472952f.png)

The results are quite similar to the comparison without respecting order. Simple, segmented, and range-key approaches compare to each other at around  0.67, while the sampling approach has a RBO of 0.19.

Both evaluation metrics reveal a significant qualitative difference between the sampling approach and the other three approaches. One potential explanation is that the buffer size of 40 might not be appropriate for our dataset as it might lead to the removal of important connecting edges to certain cliques in our graph. Evaluating different buffer sizes on the dataset is left to future work. 

Concluding, our evaluation shows that the simple implementation has comparable read speeds to the segmented (GraphJet) engine. One major drawback of the simple approach is that it retains all nodes of the graph and is not pruned over time, meaning it would reach its memory capacity in a streaming production setting. Our sampling approach addresses this problem by sampling the graph, but the approach sacrifices recommendation quality and needs to be tuned to specific datasets. The range-key implementation should be advantageous for inserting new edges into the graph, but shows to be very slow for read access. The overall best performing storage layer is our custom state store implementation of the GraphJet storage engine. The runtime of our personalized SALSA algorithm scales for all approaches with the number of walks performed and not significantly with the length of each walk. Therefore, it seems beneficial to perform a few, but long, SALSA walks to compute the tweet ranking.

### Limitations
Note that we did not have time to evaluate all aspects of our implementation. Mainly, we lack an overview of memory usage and graph insert speed. Therefore, the main theoretical benefits of the sampling approach (reduced memory footprint) and the segmented approach (faster inserts) were not evaluated and we cannot report results in those domains. 

## 10. Conclusion & Future Work
We present Kafka Salsa a real-time, graph-based recommender system, an adoption of Twitter GraphJet on KafkaStreams. The implementation allows different graph storage layers to be plugged in. We publish and evaluate four different storage layers based on Kafka State Store patterns and Twitters original GraphJet architecture. Our work reveals that a straight-forward implementation of a simplified GraphJet architecture with Kafka State Stores can achieve comparable read performance to the GraphJet's custom graph storage. We publish our entire code for the recommender system, the storage layers, the dataset, our evaluation, and our deployment on Kubernetes.

Future work might include comprehensive evaluations of memory usage and graph insert speeds as well as an adoption of the reservoir sampling buffer size. Pinterest Pixie's early random walk termination could also be a great extension to increase recommendation performance. 

A future extension might also adopt GraphJet's solution for the cold start problem for users with no interactions in the graph. And even though the general idea behind Pixie, WTF and GraphJet is to keep the entire graph in memory on a single server, it might be feasible to investigate an extension of this project with a partitioned graph.

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

[10] Busch, Gade, Larson, Lok, Luckenbill and Lin. *Earlybird: Real-time search at Twitter*. ICDE (2012).

[11] Vitter. *Random sampling with a reservoir*. TOMS (1985)
