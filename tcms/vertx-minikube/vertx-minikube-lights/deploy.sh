mvn dependency:copy-dependencies compile
docker build -t softsecgroup6/lights .
docker push softsecgroup6/lights
# in case nothing exists
kubectl apply -R -f deployments/
# add timestamp so that patch works even though nothing has changed
kubectl patch -R -f deployments/ -p "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}"
kubectl expose deployment lights-ewc1 --type=NodePort
kubectl expose deployment lights-ewc2 --type=NodePort
kubectl expose deployment lights-ewp1 --type=NodePort
kubectl expose deployment lights-ewp2 --type=NodePort
kubectl expose deployment lights-nsc1 --type=NodePort
kubectl expose deployment lights-nsc2 --type=NodePort
kubectl expose deployment lights-nsp1 --type=NodePort
kubectl expose deployment lights-nsp2 --type=NodePort
kubectl expose deployment lights-snc1 --type=NodePort
kubectl expose deployment lights-snc2 --type=NodePort
kubectl expose deployment lights-snp1 --type=NodePort
kubectl expose deployment lights-snp2 --type=NodePort
kubectl expose deployment lights-wec1 --type=NodePort
kubectl expose deployment lights-wec2 --type=NodePort
kubectl expose deployment lights-wep1 --type=NodePort
kubectl expose deployment lights-wep2 --type=NodePort
