set -x

BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-ca.yaml down --volumes --remove-orphans
BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-test-net.yaml down --volumes --remove-orphans
sudo rm -rf ./fabric-ca ./peer ./msp ./tlsca ./ca ./users *.yaml *.pb *.json *.log anchors.tx
