mvn dependency:copy-dependencies compile
docker build -t softsecgroup6/tcms .
docker push softsecgroup6/tcms
kubectl apply -f tcms.yml
kubectl patch -f tcms.yml -p "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}"
kubectl expose deployment lights-tcms --type=NodePort