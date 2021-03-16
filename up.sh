# export PATH="${PWD}/bin:$PATH"

set -xe
# ca and node
cd orderer
./ca.sh
cd ../
cd platform
./ca.sh
cd ../
cd seller
./ca.sh
cd ../
cd supplier
./ca.sh
cd ../

# channel
cd orderer
./channel.sh
cd ../
sleep 2
cd platform
./channel.sh
cd ../
cd seller
./channel.sh
cd ../
cd supplier
./channel.sh
cd ../

# build chaincode
BASE_PATH=${PWD}
cd chaincode
./gradlew installDist
cd ./build/install/basic
CORE_PEER_LOCALMSPID="PlatformMSP" CORE_PEER_TLS_ROOTCERT_FILE=${BASE_PATH}/platform/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${BASE_PATH}/platform/users/Admin@platform.com/msp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${BASE_PATH}/configtx  peer lifecycle chaincode package basic.tar.gz --path ${PWD} --lang java --label basic_1.0
cp ./basic.tar.gz ${BASE_PATH}
cd ${BASE_PATH}

# install chaincode
cd platform
./chaincode.sh
cd ../
cd seller
./chaincode.sh
cd ../
cd supplier
./chaincode.sh
cd ../

# commit chaincode
cd platform
./chaincode_commit.sh
cd ../

