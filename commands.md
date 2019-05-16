## Kafka

Launch KSQL-CLI
`winpty docker exec -ti ksql-cli ksql http://ksql-1:8088`

Launch console producer with key value
`kafka-console-producer --broker-list kafka-1:9092 --topic TOPIC_NAME --property "parse.key=true" --property "key.separator=:"`