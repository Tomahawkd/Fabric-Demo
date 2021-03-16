set -xe

# genesis block
mkdir -p ./channel-artifacts
configtxgen -configPath ${PWD}/../configtx -profile ApplicationGenesis -outputBlock ./channel-artifacts/track.block -channelID track

osnadmin channel join --channelID track --config-block ./channel-artifacts/track.block -o localhost:6053 --ca-file "${PWD}/orderer/msp/tlscacerts/tlsca.platform.com-cert.pem" --client-cert "${PWD}/orderer/tls/server.crt" --client-key "${PWD}/orderer/tls/server.key"
