# Hyperledger Fabric Demo

## Introduction
This repo contains fabric network demo uses **default** 
configurations from 
[fabric-samples](https://github.com/hyperledger/fabric-samples).
So please **DO NOT** use this in industry environment.

## Requirements
You also need to download [executables](https://github.com/hyperledger/fabric/releases)
and make sure they are in your `PATH`.

Other requirements please refer https://hyperledger-fabric.readthedocs.io/en/latest/prereqs.html

## Configs
Here is the network configs and ports:

CHANNEL NAME: track  
Configtx config: ApplicationGenesis

Orderer: orderer.platform.com  
ca-orderer: 6054  
orderer.platform.com: 6050  
ADMIN: 6053  
MSPID=OrdererMSP

Platform: platform.com  
ca-platform: 7054  
peer0.platform.com: 7051  
CHAINCODE: 7052  
GOSSIP: 7051  
MSPID=PlatformMSP  

Seller: seller.com  
ca-seller: 8054  
peer0.seller.com: 8051  
CHAINCODE: 8052  
GOSSIP: 8051  
MSPID=SellerMSP

Supplier: supplier.com  
ca-supplier: 9054  
peer0.supplier.com: 9051  
CHAINCODE: 9052  
GOSSIP: 9051  
MSPID=SupplierMSP
