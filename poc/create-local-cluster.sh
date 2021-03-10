#!/bin/bash
set -e

k3d registry create local-registry --port 5000
k3d cluster create tekton-poc-cluster --registry-config "./conf/k3d/tekton-registry.yaml"
