#!/bin/bash
set -e

k3d registry create local-registry --port 5000
k3d cluster create tekton-poc-cluster -p "9001:9001@loadbalancer" -p "9000:9000@loadbalancer" -p "9080:9080@loadbalancer" --registry-config "./conf/k3d/tekton-registry.yaml"
