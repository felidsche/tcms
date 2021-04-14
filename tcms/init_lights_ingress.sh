minikube addons enable ingress
kubectl apply -f ingress.yml
kubectl create secret tls tcms-tcom-tls --cert=tls.cert --key=tls.key
