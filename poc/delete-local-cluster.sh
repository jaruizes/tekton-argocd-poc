k3d cluster delete tekton-poc-cluster
docker stop k3d-local-registry || true && docker rm k3d-local-registry || true

