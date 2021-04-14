cd tcms
bash init_lights_ingress.sh
cd vertx-minikube/vertx-minikube-lights
bash deploy.sh
cd ../vertx-minikube-vehicle
bash deploy.sh
kubectl create clusterrolebinding default-service-lister --clusterrole=system:controller:service-controller --serviceaccount=default:default
cd ../vertx-minikube-tcms
bash deploy.sh
