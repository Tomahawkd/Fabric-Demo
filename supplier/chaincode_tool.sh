#!/bin/bash

set -xe
# chaincode check install
CORE_PEER_LOCALMSPID="SupplierMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@supplier.com/msp CORE_PEER_ADDRESS=localhost:9051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer lifecycle chaincode queryinstalled

read -p "Check if the chaincode is installed? " REPLY
if [[ "$REPLY" =~ ^[Yy]$ ]]
then
  echo "Continue"
else
  echo "Exit"
  exit 0
fi

CORE_PEER_LOCALMSPID="SupplierMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@supplier.com/msp CORE_PEER_ADDRESS=localhost:9051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer lifecycle chaincode checkcommitreadiness --channelID track --name basic --version 1.0 --sequence 1 --output json

read -p "Check if the chaincode is approved? " REPLY
if [[ "$REPLY" =~ ^[Yy]$ ]]
then
  echo "Continue"
else
  echo "Exit"
  exit 0
fi
