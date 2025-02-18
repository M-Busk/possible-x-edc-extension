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

cd helm

EDC_ROLE="${1:-$EDC_ROLE}"
EDC_NAMESPACE="${2:-$EDC_NAMESPACE}"

# print usage if no argument is provided
if [[ -z $EDC_ROLE || -z $EDC_NAMESPACE ]]; then
    echo "Usage: ./remove.sh <EDC_ROLE> <EDC_NAMESPACE> --delete-namespace"
    echo "e.g ./remove.sh consumer possible-x-edc-consumer"
    echo "Or set the EDC_ROLE and EDC_NAMESPACE environment variables"
    echo "If --delete-namespace is passed as the 3rd param, the namespace is deleted as well"
    exit 1
fi

EDC_VAULT_NAME="${EDC_NAMESPACE}-${EDC_ROLE}-vault"
EDC_IMAGE_PULL_SECRET_NAME="${EDC_ROLE}-github-registry-auth"

rm -f "${EDC_VAULT_NAME}-keys.json"
helm --kubeconfig=$KUBECONFIG_EDC uninstall "${EDC_VAULT_NAME}" -n "${EDC_NAMESPACE}" || true
kubectl --kubeconfig=$KUBECONFIG_EDC -n "${EDC_NAMESPACE}" delete secret "${EDC_IMAGE_PULL_SECRET_NAME}" || true

# write local values file
rm -f "${EDC_NAMESPACE}-${EDC_ROLE}.yaml" temp.yaml

helm --kubeconfig=$KUBECONFIG_EDC uninstall -n "${EDC_NAMESPACE}" "possible-x-edc-${EDC_ROLE}" || true

if [ "$3" == "--delete-namespace" ]; then
  kubectl --kubeconfig=$KUBECONFIG_EDC delete namespace "${EDC_NAMESPACE}" || true
fi

cd ..
