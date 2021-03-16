#!/bin/bash

set -xe
# chaincode
CORE_PEER_LOCALMSPID="SellerMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@seller.com/msp CORE_PEER_ADDRESS=localhost:8051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer lifecycle chaincode install ../basic.tar.gz

# chaincode check install
CORE_PEER_LOCALMSPID="SellerMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@seller.com/msp CORE_PEER_ADDRESS=localhost:8051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer lifecycle chaincode queryinstalled >&install.log
cat install.log

read -p "Check if the chaincode is installed? " REPLY
if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo "Continue"
else
  echo "Exit"
  exit 0
fi

# approve for org
PACKAGE_ID=$(sed -n "/basic_1.0/{s/^Package ID: //; s/, Label:.*$//; p;}" install.log)
CORE_PEER_LOCALMSPID="SellerMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@seller.com/msp CORE_PEER_ADDRESS=localhost:8051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer lifecycle chaincode approveformyorg -o localhost:6050 --ordererTLSHostnameOverride orderer.platform.com --tls --cafile "${PWD}/../orderer/msp/tlscacerts/tlsca.platform.com-cert.pem" --channelID track --name basic --version 1.0 --package-id ${PACKAGE_ID} --sequence 1

# check approval
CORE_PEER_LOCALMSPID="SellerMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@seller.com/msp CORE_PEER_ADDRESS=localhost:8051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer lifecycle chaincode checkcommitreadiness --channelID track --name basic --version 1.0 --sequence 1 --output json

read -p "Check if the chaincode is approved? " REPLY
if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo "Continue"
else
  echo "Exit"
  exit 0
fi
