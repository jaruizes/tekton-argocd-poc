clear
echo "---------------------------------------------------------------------------------------"
echo ""
echo "  _____    _    _                    ___           _                     ____ ____    "
echo " |_   _|__| | _| |_ ___  _ __       ( _ )         / \   _ __ __ _  ___  / ___|  _ \   "
echo "   | |/ _ \ |/ / __/ _ \| '_ \      / _ \/\      / _ \ | '__/ _  |/ _ \| |   | | | |  "
echo "   | |  __/   <| || (_) | | | |    | (_>  <     / ___ \| | | (_| | (_) | |___| |_| |  "
echo "   |_|\___|_|\_\\__\___/|_| |_|     \___/\/    /_/   \_\_|  \__, |\___/ \____|____/   "
echo "                                                            |___/ "
echo ""
echo "---------------------------------------------------------------------------------------"

kubectl create namespace tekton-poc
kubectl apply -f https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml
kubectl apply -f conf/k8s -n tekton-poc
kubectl apply -f https://github.com/tektoncd/dashboard/releases/latest/download/tekton-dashboard-release.yaml
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

echo '-------------------------------------------------'
echo 'Be patient while the pods are ready for you  '
echo '-------------------------------------------------'

while [[ $(kubectl get pods -l 'app in (sonarqube)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 10; done
while [[ $(kubectl get pods -l 'app in (nexus)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 5; done
while [[ $(kubectl get pods -l 'app in (tekton-pipelines-controller)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 5; done
while [[ $(kubectl get pods -l 'app in (tekton-dashboard)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 5; done

echo ""
echo "Configuring settings.xml (MAVEN) to work with Nexus"
nexus_pod=$(kubectl get pod -l app=nexus -n tekton-poc --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')

while ! kubectl exec $nexus_pod -n tekton-poc -- cat /nexus-data/admin.password ;do
  echo "Waiting for the Nexus admin.password file to be generated" && sleep 10
done

nexus_pwd=$(kubectl exec $nexus_pod -n tekton-poc -- cat /nexus-data/admin.password)
echo $nexus_pwd

sed -i.bak "s/<password>admin123<\/password>/<password>$nexus_pwd<\/password>/" ./k3d/maven/settings.xml
kubectl create cm maven-settings --from-file=conf/k3d/maven/settings.xml -n tekton-poc

echo ""
echo "Deploying tasks, pipelines and ArgoCD application"
kubectl apply -f conf/argocd
kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/master/task/git-clone/0.2/git-clone.yaml -n tekton-poc
kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/master/task/maven/0.2/maven.yaml -n tekton-poc
kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/master/task/buildah/0.2/buildah.yaml -n tekton-poc
kubectl apply -f conf/tekton/git-access -n tekton-poc
kubectl apply -f conf/tekton/tasks -n tekton-poc
kubectl apply -f conf/tekton/pipelines -n tekton-poc

echo ""
echo ""
echo "Execute 'kubectl proxy --port=8080' to expose the Tekton dashboard in the URL:"
echo "http://localhost:8080/api/v1/namespaces/tekton-pipelines/services/tekton-dashboard:http/proxy/#/namespaces/default/pipelines"
echo "-----"

echo "Execute 'kubectl port-forward services/nexus 9001:9001 -n tekton-poc' to access to Nexus in the URL:"
echo "http://localhost:9001"
echo "admin/admin123"
echo "-----"

echo "Execute 'kubectl port-forward services/sonarqube-service 9000:9000 -n tekton-poc' to access to Nexus in the URL:"
echo "http://localhost:9000"
echo "admin/admin"
echo "-----"

echo "Execute 'kubectl port-forward svc/argocd-server -n argocd 9080:443' to access to ArgoCD in the URL:"
echo "http://localhost:9080"
echo "admin/"$(kubectl get pods -n argocd -l app.kubernetes.io/name=argocd-server -o name | cut -d'/' -f 2)
