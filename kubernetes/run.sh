#!/usr/bin/env bash

echo "Running script to create Kafka on Kubernetes cluster"

# Set correct values for your subscription
export CLUSTER_NAME="kafka-k8s-cluster"
export RG_NAME="kafka-k8s"
export LOCATION="westeurope"

export KAFKA_IP_NAME_0="kafka-ip-0"
export KAFKA_IP_NAME_1="kafka-ip-1"
export KAFKA_IP_NAME_2="kafka-ip-2"

export SR_IP_NAME="mysr-ip"

echo "Creating AKS Cluster."

az group create -n ${RG_NAME} -l ${LOCATION}
az aks create -n ${CLUSTER_NAME} -g ${RG_NAME} -l ${LOCATION}  --generate-ssh-keys --node-count 2 --node-vm-size Standard_E2_v3
az aks get-credentials -n ${CLUSTER_NAME} -g ${RG_NAME} --overwrite-existing


echo "Create kubernetes dashboard"
kubectl create clusterrolebinding cluster-admin-binding --clusterrole cluster-admin --user $(az account show --query="user.name")
kubectl create -f https://raw.githubusercontent.com/kubernetes/kops/master/addons/kubernetes-dashboard/v1.8.3.yaml
kubectl apply -f dashboard_access.yaml
# Start Dashboard with 
# kubectl proxy

echo -e "Setup Helm"

kubectl create -f helm_rbac_config.yaml
helm init --upgrade --service-account tiller

export CLUSTER_RG="$(az aks show -g ${RG_NAME} -n ${CLUSTER_NAME} --query nodeResourceGroup -o tsv)"

echo "Allocate load balancer ips"
az network public-ip create -g ${CLUSTER_RG} -n ${KAFKA_IP_NAME_0} --allocation-method static
az network public-ip create -g ${CLUSTER_RG} -n ${KAFKA_IP_NAME_1} --allocation-method static
az network public-ip create -g ${CLUSTER_RG} -n ${KAFKA_IP_NAME_2} --allocation-method static
az network public-ip create -g ${CLUSTER_RG} -n ${SR_IP_NAME} --allocation-method static

KAFKA_IP_0="$(az network public-ip show --resource-group ${CLUSTER_RG} --name ${KAFKA_IP_NAME_0} --query ipAddress)"
KAFKA_IP_1="$(az network public-ip show --resource-group ${CLUSTER_RG} --name ${KAFKA_IP_NAME_1} --query ipAddress)"
KAFKA_IP_2="$(az network public-ip show --resource-group ${CLUSTER_RG} --name ${KAFKA_IP_NAME_2} --query ipAddress)"
SR_IP="$(az network public-ip show --resource-group ${CLUSTER_RG} --name ${SR_IP_NAME} --query ipAddress)"

echo "Setup Kafka"
cat kafka.yaml | sed 's/\${KAFKA_IP_0}'"/$KAFKA_IP_0/g" | sed 's/\${KAFKA_IP_1}'"/$KAFKA_IP_1/g" | sed 's/\${KAFKA_IP_2}'"/$KAFKA_IP_2/g" | helm install --name kafka incubator/kafka -f -

echo "Setup Schema Registry"
## don't name the schema registry schema-registry!
cat schema-registry.yaml | sed 's/\${SR_IP}'"/$SR_IP/g" | helm install --name mysr incubator/schema-registry -f -

#echo "Run application"
#kubectl apply -f kafka-salsa.yaml

