set -x

BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-ca.yaml down --volumes --remove-orphans
BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-test-net.yaml down --volumes --remove-orphans
sudo rm -rf ./fabric-ca ./orderer ./msp ./tls ./users fabric-ca-client-config.yaml ./system-genesis-block ./channel-artifacts
