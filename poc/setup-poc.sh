#!/bin/bash
set -e

nexus_admin_user="admin"
nexus_final_admin_password="admin123"
nexus_port="9001"
nexus_local_ip="localhost"

clear
echo "------------------------------------------------------------------------------------------"
echo ""
echo "  _____    _    _                    ___           _                         ____ ____    "
echo " |_   _|__| | _| |_ ___  _ __       ( _ )         / \   _ __ __ _  ___      / ___|  _ \   "
echo "   | |/ _ \ |/ / __/ _ \| '_ \      / _ \/\      / _ \ | '__/ _  |/ _ \    | |   | | | |  "
echo "   | |  __/   <| || (_) | | | |    | (_>  <     / ___ \| | | (_| | (_) |   | |___| |_| |  "
echo "   |_|\___|_|\_\\__\___/|_| |_|     \___/\/    /_/   \_\_|  \__, |\___/     \____|____/   "
echo "                                                            |___/ "
echo ""
echo "------------------------------------------------------------------------------------------"

initK8SResources() {
  kubectl create namespace cicd | true
  kubectl create namespace argocd | true
  kubectl apply -f https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml
  kubectl apply -f conf/k8s -n cicd
  kubectl apply -f https://github.com/tektoncd/dashboard/releases/latest/download/tekton-dashboard-release.yaml
  kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

  echo '-------------------------------------------------'
  echo 'Be patient while the pods are ready for you  '
  echo '-------------------------------------------------'

  while [[ $(kubectl get pods -l 'app in (nexus)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 10; done
  while [[ $(kubectl get pods -l 'app in (sonarqube)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 10; done
  while [[ $(kubectl get pods -l 'app in (tekton-pipelines-controller)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 10; done
  while [[ $(kubectl get pods -l 'app in (tekton-dashboard)' --all-namespaces -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for pods ready..." && sleep 10; done
}

setAnonymousAccessAllowed() {
  echo "   ---> Anonymous access allowed: start"
  curl -d "@conf/k8s/data/anonymous_data.json" -H "Content-Type: application/json" --location --request PUT "$nexus_api_base_url/security/anonymous" --user "$nexus_admin_user:$nexus_original_admin_pwd" | true
  echo "   ---> Anonymous access allowed: end"
}

updateAdminPassword() {
  echo "   ---> Admin password updated: start"
  curl --data-raw "$nexus_final_admin_password" -H "Content-Type: text/plain" --location --request PUT "$nexus_api_base_url/security/users/$nexus_admin_user/change-password" --user "$nexus_admin_user:$nexus_original_admin_pwd" | true
  echo "   ---> Admin password updated: end"
}

removeTemporalFiles() {
  nexus_pod_name=$(kubectl get pod -l app=nexus -n cicd --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')
  echo "   ---> Nexus pod name: $nexus_pod_name"
  kubectl exec "$nexus_pod_name" -n cicd -- rm -rf /nexus-data/tmp
  echo "   ---> Removed tmp files"
}

waitForNexusAPIBeReady() {
  attempt_counter=0
  max_attempts=10
  nexus_api_repositories="$nexus_api_base_url/repositories"
  printf '   ---> Waiting for Nexus API be ready (%s)' "$nexus_api_repositories"

  until [[ $(curl -I --silent -o /dev/null -w %{http_code} "$nexus_api_repositories") =~ 2[0-9][0-9]  ]] ;do
      if [ ${attempt_counter} -eq ${max_attempts} ];then
        echo "Max attempts reached"
        exit 1
      fi

      printf '.'
      attempt_counter=$(($attempt_counter+1))
      sleep 10
  done
  echo "   ---> Nexus API ready"
}

waitForNexusReady() {
  nexus_api_base_url="http://$nexus_local_ip:$nexus_port/service/rest/v1"
  nexus_pod_name=$(kubectl get pod -l app=nexus -n cicd --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')

  echo "   ---> Nexus pod name: $nexus_pod_name"
  printf '   ---> Waiting for Nexus be ready'
  while [[ ! $(kubectl exec "$nexus_pod_name" -n cicd -- cat /nexus-data/admin.password) ]] ;do
    printf '.'
    sleep 10
  done

  nexus_original_admin_pwd=$(kubectl exec "$nexus_pod_name" -n cicd -- cat /nexus-data/admin.password)
  echo "   ---> Admin pass: $nexus_original_admin_pwd"
  echo "   ---> Nexus ready"
}

setupNexus() {
  waitForNexusReady
  waitForNexusAPIBeReady
  setAnonymousAccessAllowed
  updateAdminPassword
  #removeTemporalFiles
}

installPoCResources() {
  echo ""
  echo "Deploying configmaps, tasks, pipelines and ArgoCD application"
  kubectl create cm maven-settings --from-file=conf/maven/settings.xml -n cicd
  kubectl apply -f conf/argocd
  kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/master/task/git-clone/0.2/git-clone.yaml -n cicd
  kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/master/task/maven/0.2/maven.yaml -n cicd
  kubectl apply -f https://raw.githubusercontent.com/tektoncd/catalog/master/task/buildah/0.2/buildah.yaml -n cicd
  kubectl apply -f conf/tekton/git-access -n cicd
  kubectl apply -f conf/tekton/tasks -n cicd
  kubectl apply -f conf/tekton/pipelines -n cicd
  kubectl patch secret -n argocd argocd-secret -p '{"stringData": { "admin.password": "'$(htpasswd -bnBC 10 "" admin123 | tr -d ':\n')'"}}'
}

showInfo() {
  echo ""
  echo ""
  echo "Execute 'kubectl proxy --port=8080' to expose the Tekton dashboard in the URL:"
  echo "http://localhost:8080/api/v1/namespaces/tekton-pipelines/services/tekton-dashboard:http/proxy/#/namespaces/cicd/pipelineruns"
  echo "-----"

  echo "Nexus is exposed the URL:"
  echo "http://localhost:9001"
  echo "User/Password: admin/admin123"
  echo "-----"

  echo "Sonar is exposed in the URL:"
  echo "http://localhost:9000"
  echo "User/Password: admin/admin"
  echo "-----"

  echo "Execute 'kubectl port-forward svc/argocd-server -n argocd 9080:443' to expose the Argo CD console "
  echo "http://localhost:9080"
  echo "User/Password: admin/admin123"
  echo ""
  echo ""
}

main() {
  initK8SResources
  setupNexus
  installPoCResources
  showInfo
}

main
