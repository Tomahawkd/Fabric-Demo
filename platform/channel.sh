set -xe

CORE_PEER_LOCALMSPID="PlatformMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@platform.com/msp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer channel join -b ../orderer/channel-artifacts/track.block

# fetch config
CORE_PEER_LOCALMSPID="PlatformMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@platform.com/msp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer channel fetch config config_block.pb -o localhost:6050 --ordererTLSHostnameOverride orderer.platform.com -c track --tls --cafile "${PWD}/../orderer/msp/tlscacerts/tlsca.platform.com-cert.pem"

configtxlator proto_decode --input config_block.pb --type common.Block | jq '.data.data[0].payload.data.config' >"config.json"

# update content
jq '.channel_group.groups.Application.groups.PlatformMSP.values += {"AnchorPeers":{"mod_policy": "Admins","value":{"anchor_peers": [{"host": "peer0.platform.com","port": 7051}]},"version": "0"}}' config.json > modified_config.json

configtxlator proto_encode --input "config.json" --type common.Config >original_config.pb
configtxlator proto_encode --input "modified_config.json" --type common.Config >modified_config.pb
configtxlator compute_update --channel_id "track" --original original_config.pb --updated modified_config.pb >config_update.pb
configtxlator proto_decode --input config_update.pb --type common.ConfigUpdate >config_update.json
echo '{"payload":{"header":{"channel_header":{"channel_id":"'track'", "type":2}},"data":{"config_update":'$(cat config_update.json)'}}}' | jq . > config_update_in_envelope.json
configtxlator proto_encode --input config_update_in_envelope.json --type common.Envelope >"anchors.tx"

# update to node
CORE_PEER_LOCALMSPID="PlatformMSP" CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/peer/tls/ca.crt CORE_PEER_MSPCONFIGPATH=${PWD}/users/Admin@platform.com/msp CORE_PEER_ADDRESS=localhost:7051 CORE_PEER_TLS_ENABLED=true FABRIC_CFG_PATH=${PWD}/../configtx peer channel update -o localhost:6050 --ordererTLSHostnameOverride orderer.platform.com -c track -f anchors.tx --tls --cafile "${PWD}/../orderer/msp/tlscacerts/tlsca.platform.com-cert.pem"
