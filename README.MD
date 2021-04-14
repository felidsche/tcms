# TCMS
A Traffic Control Management System using Microservices
This system controls the traffic lights in a fictious city.

## System Use Cases
Interactors of the system are (see Figure below):
- Emergency vehices
- The Major of the fictious city
- Operational staff 
- possible attackers
![gsm_cases](https://user-images.githubusercontent.com/29780475/114725942-399ba680-9d3d-11eb-8e47-937369b0b520.jpeg)

## System Components
![ssas_group6_sec_design_component](https://user-images.githubusercontent.com/29780475/114726022-491aef80-9d3d-11eb-955b-4e8ed012cb90.png)

## Start the System using Minikube
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

