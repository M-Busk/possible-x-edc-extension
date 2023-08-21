#!/bin/bash

set -xe

cd "$(dirname "$0")"

source .config
if [[ -z $EDC_ROLE ]]; then
    echo "EDC_ROLE is not set"
    exit 1
fi


if [ $USE_IONOS_DNS == True ]; then

    # DNS zone
    if [ -z `printenv IONOS_DNS_ZONE_ID` ]; then
        DNS_ZONE_ID=$(curl -X "POST" \
            -H "accept: application/json" \
            -H "Authorization: Bearer $DCD_IONOS_TOKEN" \
            -H "Content-Type: application/json" \
            -d "{ \
                \"properties\": { \
                    \"description\": \"POSSIBLE EDC DNS zone\", \
                    \"enabled\": true, \
                    \"zoneName\": \"$DNS_ZONE\"
                } \
            }" \
            "https://dns.de-fra.ionos.com/zones" |jq -r '.id')
        
        if [ $? != 0 ]; then
            echo "DNS zone creation failed"
            exit 1
        fi
    else
        DNS_ZONE_ID=$IONOS_DNS_ZONE_ID
    fi

    INGRESS_CONTROLLER_IP=$(kubectl --kubeconfig=$KUBECONFIG_EDC -n ingress-nginx get svc ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    if [ $? != 0 ]; then
        echo "Getting ingress controller ip failed"
        exit 1
    fi

    # DNS record
    curl -X "POST" \
        -H "accept: application/json" \
        -H "Authorization: Bearer $DCD_IONOS_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{ \
            \"properties\": { \
                \"name\": \"$DNS_A_RECORD\", \
                \"type\": \"A\", \
                \"content\": \"$INGRESS_CONTROLLER_IP\", \
                \"ttl\": 3600, \
                \"prio\": 0, \
                \"disabled\": false \
            } \
        }" \
        "https://dns.de-fra.ionos.com/zones/$DNS_ZONE_ID/records"

    if [ $? != 0 ]; then
        echo "DNS record creation failed"
        exit 1
    fi
fi