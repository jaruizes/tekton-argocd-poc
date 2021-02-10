k3d registry create local-registry --port 5000
k3d cluster create tekton-poc-cluster --registry-config "./k3d/tekton-registry.yaml"
