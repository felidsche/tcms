## Start Cluster
`minikube start`

### Start with Auditing
* make auditing scripts executable `chmod +x start_with_auditing.sh`
* run `start_with_auditing.sh` once to setup auditing
* run `kubectl logs kube-apiserver-minikube -n  kube-system | grep audit.k8s.io/v1% ` to see the auditing data

## use the centralized deploy script
* `./deploy.sh`
* this initializes the ingress and deploys all services

## Alternatively deploy manually

### Init Ingress
* make init scripts executable `chmod +x init_ingress.sh`
* run `init_ingress.sh` once to setup the secret and dns locally on your machine

### Deploy a Service
* make deployment scripts executable `chmod +x service/deploy.sh`
* run `service/deploy.sh`

## Stop Cluster
`minikube stop`

## Delete the Cluster
`minikube delete`

