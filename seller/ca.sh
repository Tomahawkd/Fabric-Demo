set -xe

# ca initialization
mkdir -p "${PWD}/fabric-ca"
cp "${PWD}/configs/fabric-ca-server-config.yaml" "${PWD}/fabric-ca"
BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-ca.yaml up -d 2>&1
sleep 2

# seller ca
## enroll admin
fabric-ca-client enroll -H ${PWD} -u https://admin:adminpw@localhost:8054 --caname ca-seller --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

mkdir -p "${PWD}/msp"
cp "${PWD}/configs/msp/config.yaml" "${PWD}/msp/"

## register peer0
fabric-ca-client register -H ${PWD} --caname ca-seller --id.name peer0 --id.secret peer0pw --id.type peer --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

## register user
fabric-ca-client register -H ${PWD} --caname ca-seller --id.name user1 --id.secret user1pw --id.type client --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

## register org admin
fabric-ca-client register -H ${PWD} --caname ca-seller --id.name sellAdmin --id.secret sellAdminpw --id.type admin --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

## peer0 msp
fabric-ca-client enroll -H ${PWD} -u https://peer0:peer0pw@localhost:8054 --caname ca-seller -M "${PWD}/peer/msp" --csr.hosts peer0.seller.com --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"

cp "${PWD}/msp/config.yaml" "${PWD}/peer/msp/config.yaml"

## peer0 tls certs
mkdir -p "${PWD}/peer/tls"
fabric-ca-client enroll -H ${PWD} -u https://peer0:peer0pw@localhost:8054 --caname ca-seller -M "${PWD}/peer/tls" --enrollment.profile tls --csr.hosts peer0.seller.com --csr.hosts localhost --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"
cp "${PWD}/peer/tls/tlscacerts/"* "${PWD}/peer/tls/ca.crt"
cp "${PWD}/peer/tls/signcerts/"* "${PWD}/peer/tls/server.crt"
cp "${PWD}/peer/tls/keystore/"* "${PWD}/peer/tls/server.key"

mkdir -p "${PWD}/msp/tlscacerts"
cp "${PWD}/peer/tls/tlscacerts/"* "${PWD}/msp/tlscacerts/ca.crt"
mkdir -p "${PWD}/tlsca"
cp "${PWD}/peer/tls/tlscacerts/"* "${PWD}/tlsca/tlsca.peer0.seller.com-cert.pem"
mkdir -p "${PWD}/ca"
cp "${PWD}/peer/msp/cacerts/"* "${PWD}/ca/ca.seller.com-cert.pem"

## user msp
fabric-ca-client enroll -H ${PWD} -u https://user1:user1pw@localhost:8054 --caname ca-seller -M "${PWD}/users/User1@seller.com/msp" --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"
cp "${PWD}/msp/config.yaml" "${PWD}/users/User1@seller.com/msp/config.yaml"

## org admin msp
fabric-ca-client enroll -H ${PWD} -u https://sellAdmin:sellAdminpw@localhost:8054 --caname ca-seller -M "${PWD}/users/Admin@seller.com/msp" --tls.certfiles "${PWD}/fabric-ca/tls-cert.pem"
cp "${PWD}/msp/config.yaml" "${PWD}/users/Admin@seller.com/msp/config.yaml"

./ccp-generate.sh

# node
BASE_DIR=${PWD} docker-compose -f ./docker/docker-compose-test-net.yaml up -d 2>&1


