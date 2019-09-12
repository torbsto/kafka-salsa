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
Our basis for this project is the GraphJet production recommender system at Twitter[1] that holds the entire bipartite user-tweet-interaction graph in memory on a single machine to compute real-time recommendations using a personalized SALSA random-walk algorithm. Before Twitter, several companies have described large-scale, production recommender systems **TODO[1, 7, 21]**. However, unlike GraphJet, these systems are not real-time as they precompute recommendations in batches before they are requested by users. 

GraphJet's predecessor, the "Who To Follow" (WTF) system **TODO []**, is the first system that proposes to store an entire production-scale follower graph in memory on a single machine. WTF also uses a personalized SALSA algorithm to compute recommendations, but WTF is not used in a real-time environment as recommendations are precomputed and stored in a DBMS on a daily schedule.

Conceptually, GraphJet borrows heavily from Twitters search engine Earybird **TODO[]**, whose index structure to store posting lists is very similar in how GraphJet manages adjacency lists.

Pinterest's production system Pixie[2] is the closest project to GraphJet, also storing a bipartite graph in memory on a single machine. But in contrast to a personalized SALSA, they propose a novel random walk algorithm that terminates early once the results start converging.

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
### Simple
### Sampled
### Range-Key
### Segmented

## 9. Evaluation

## 10. Conclusion & Future Work

## 11. References
[1] Sharma, Jiang, Bommannavar, Larson, and Lin. *GraphJet: Real-Time Content Recommendations at Twitter.* PVLDB (2016).

[2] Eksombatchai, Jindal, J. Liu, Y. Liu, Sharma, Sugnet, Ulrich, and Leskovec. *Pixie: A System for Recommending 3+ Billion Items to 200+Million Users in Real-Time.* WWW (2018).

[3] Jin. *Simulating Random Walks on Graphs in the Streaming Model.* ITCS (2019).

[4] Webber, Moffat, and Zobel. *A similarity measure for indefinite rankings.* TOIS (2010).
