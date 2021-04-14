mvn dependency:copy-dependencies compile
docker build -t softsecgroup6/vehicle .
docker push softsecgroup6/vehicle
kubectl apply -f vehicle.yml
kubectl patch -f vehicle.yml -p "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}"
kubectl expose deployment vehicle --type=NodePort