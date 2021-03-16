#!/bin/bash
set -xe

# init
CORE_PEER_LOCALMSPID="PlatformMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@platform.com/msp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer chaincode invoke -o localhost:6050 --ordererTLSHostnameOverride orderer.platform.com --tls --cafile "${PWD}/../orderer/msp/tlscacerts/tlsca.platform.com-cert.pem" -C track -n basic --peerAddresses localhost:7051 --tlsRootCertFiles ${PWD}/../platform/peer/tls/ca.crt --peerAddresses localhost:8051 --tlsRootCertFiles ${PWD}/../seller/peer/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles ${PWD}/../supplier/peer/tls/ca.crt -c '{"function":"InitLedger","Args":[]}'

