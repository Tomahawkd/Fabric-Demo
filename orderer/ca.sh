set -xe

# ca initialization
mkdir -p "${PWD}/fabric-ca"
cp "${PWD}/configs/fabric-ca-server-config.yaml" "${PWD}/fabric-ca"
BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-ca.yaml up -d 2>&1
sleep 2

# orderer ca
## enroll admin
fabric-ca-client enroll -H ${PWD} -u https://admin:adminpw@localhost:6054 --caname ca-orderer --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

mkdir -p "${PWD}/msp"
cp "${PWD}/configs/msp/config.yaml" "${PWD}/msp/"

## register orderer
fabric-ca-client register -H ${PWD} --caname ca-orderer --id.name orderer --id.secret ordererpw --id.type orderer --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

## register orderer admin
fabric-ca-client register -H ${PWD} --caname ca-orderer --id.name ordererAdmin --id.secret ordererAdminpw --id.type admin --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

## orderer msp
fabric-ca-client enroll -H ${PWD} -u https://orderer:ordererpw@localhost:6054 --caname ca-orderer -M "${PWD}/orderer/msp" --csr.hosts orderer.platform.com --csr.hosts localhost --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

cp "${PWD}/msp/config.yaml" "${PWD}/orderer/msp/config.yaml"

## orderer tls certs
mkdir -p "${PWD}/orderer/tls"
fabric-ca-client enroll -H ${PWD} -u https://orderer:ordererpw@localhost:6054 --caname ca-orderer -M "${PWD}/orderer/tls" --enrollment.profile tls --csr.hosts orderer.platform.com --csr.hosts localhost --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"
cp "${PWD}/orderer/tls/tlscacerts/"* "${PWD}/orderer/tls/ca.crt"
cp "${PWD}/orderer/tls/signcerts/"* "${PWD}/orderer/tls/server.crt"
cp "${PWD}/orderer/tls/keystore/"* "${PWD}/orderer/tls/server.key"

mkdir -p "${PWD}/orderer/msp/tlscacerts"
cp "${PWD}/orderer/tls/tlscacerts/"* "${PWD}/orderer/msp/tlscacerts/tlsca.platform.com-cert.pem"
mkdir -p "${PWD}/msp/tlscacerts"
cp "${PWD}/orderer/tls/tlscacerts/"* "${PWD}/msp/tlscacerts/tlsca.platform.com-cert.pem"

## orderer admin msp
fabric-ca-client enroll -H ${PWD} -u https://ordererAdmin:ordererAdminpw@localhost:6054 --caname ca-orderer -M "${PWD}/users/Admin@platform.com/msp" --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"
cp "${PWD}/msp/config.yaml" "${PWD}/users/Admin@platform.com/msp/config.yaml"


# orderer node
mkdir -p ./system-genesis-block
BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-test-net.yaml up -d 2>&1


