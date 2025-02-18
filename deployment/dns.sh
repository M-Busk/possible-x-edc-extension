#!/bin/bash

#  Copyright 2024-2025 Dataport. All rights reserved. Developed as part of the POSSIBLE project.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

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