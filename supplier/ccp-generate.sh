#!/bin/bash

function one_line_pem {
    echo "`awk 'NF {sub(/\\n/, ""); printf "%s\\\\\\\n",$0;}' $1`"
}

function json_ccp {
    local PP=$(one_line_pem $5)
    local CP=$(one_line_pem $6)
    sed -e "s/\${ORG}/$1/" \
        -e "s/\${ORGUP}/$2/" \
        -e "s/\${P0PORT}/$3/" \
        -e "s/\${CAPORT}/$4/" \
        -e "s#\${PEERPEM}#$PP#" \
        -e "s#\${CAPEM}#$CP#" \
        configs/ccp-template.json
}

function yaml_ccp {
    local PP=$(one_line_pem $5)
    local CP=$(one_line_pem $6)
    sed -e "s/\${ORG}/$1/" \
        -e "s/\${ORGUP}/$2/" \
        -e "s/\${P0PORT}/$3/" \
        -e "s/\${CAPORT}/$4/" \
        -e "s#\${PEERPEM}#$PP#" \
        -e "s#\${CAPEM}#$CP#" \
        configs/ccp-template.yaml | sed -e $'s/\\\\n/\\\n          /g'
}

ORG=supplier
ORGUP=Supplier
P0PORT=7051
CAPORT=7054
PEERPEM=tlsca/tlsca.peer0.${ORG}.com-cert.pem
CAPEM=ca/ca.${ORG}.com-cert.pem

echo "$(json_ccp $ORG $ORGUP $P0PORT $CAPORT $PEERPEM $CAPEM)" > connection-${ORG}.json
echo "$(yaml_ccp $ORG $ORGUP $P0PORT $CAPORT $PEERPEM $CAPEM)" > connection-${ORG}.yaml
