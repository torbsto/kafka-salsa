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

## 7. Related Work
Our basis for this project is the GraphJet production recommender system at Twitter[1] that holds the entire bipartite user-tweet-interaction graph in memory on a single machine to compute real-time recommendations using a personalized SALSA random-walk algorithm. Before Twitter, several companies have described large-scale, production recommender systems [1, 7, 21]. However, unlike GraphJet, these systems are not real-time as they precompute recommendations in batches before they are requested by users. 

GraphJet's predecessor, the "Who To Follow" (WTF) system, is the first system that proposes to store an entire production-scale follower graph in memory on a single machine. WTF also uses a personalized SALSA algorithm to compute recommendations, but WTF is not used in a real-time environment as recommendations are precomputed and stored in a DBMS on a daily schedule.

Conceptually, GraphJet borrows heavily from Twitters search engine Earybird[], whose index structure to store posting lists is very similar in how GraphJet manages adjacency lists.

Pinterest's production system Pixie[2] is the closest project to GraphJet, also storing a bipartite graph in memory on a single machine. But in contrast to a personalized SALSA, they propose a novel random walk algorithm that is terminated early once the results start converging.

## 6. Introduction
### GraphJet
### SALSA

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
