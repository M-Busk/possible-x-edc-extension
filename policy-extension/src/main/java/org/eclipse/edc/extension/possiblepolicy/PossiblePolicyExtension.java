/*
 *  Copyright (c) 2021 Microsoft Corporation
 *  Copyright 2024-2025 Dataport. All rights reserved. Extended as part of the POSSIBLE project.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *       1&1 IONOS Cloud GmbH
 *
 */

package org.eclipse.edc.extension.possiblepolicy;

import org.eclipse.edc.connector.controlplane.policy.contract.ContractExpiryCheckFunction;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Prohibition;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import static org.eclipse.edc.connector.controlplane.contract.spi.policy.TransferProcessPolicyContext.TRANSFER_SCOPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_USE_ACTION_ATTRIBUTE;

import java.util.Map;

import static org.eclipse.edc.connector.controlplane.contract.spi.policy.ContractNegotiationPolicyContext.NEGOTIATION_SCOPE;

public class PossiblePolicyExtension implements ServiceExtension {

    private static final boolean VERBOSE = true;

    private static final Map<String, String> CONSTRAINT_KEY_MAP = Map.of(
        "connectorId", "client_id",
        "did", "did"
    );

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;
    @Inject
    private PolicyEngine policyEngine;

    @Override
    public String name() {
        return "POSSIBLE-POLICY-EXTENSION";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        // register use action for both negotiation scope
        ruleBindingRegistry.bind("use", NEGOTIATION_SCOPE);
        ruleBindingRegistry.bind("USE", NEGOTIATION_SCOPE);
        ruleBindingRegistry.bind(ODRL_USE_ACTION_ATTRIBUTE, NEGOTIATION_SCOPE);

        ruleBindingRegistry.bind("use", TRANSFER_SCOPE);
        ruleBindingRegistry.bind("USE", TRANSFER_SCOPE);
        ruleBindingRegistry.bind(ODRL_USE_ACTION_ATTRIBUTE, TRANSFER_SCOPE);

        // iterate over claim constraint key map and register functions for negotiation scope
        for (Map.Entry<String, String> entry : CONSTRAINT_KEY_MAP.entrySet()) {
            ruleBindingRegistry.bind(entry.getKey(), NEGOTIATION_SCOPE);
            policyEngine.registerFunction(NEGOTIATION_SCOPE, Permission.class, entry.getKey(),
                new ClientClaimConstraintFunction<>(monitor, entry.getValue(), VERBOSE));
            policyEngine.registerFunction(NEGOTIATION_SCOPE, Prohibition.class, entry.getKey(),
                new ClientClaimConstraintFunction<>(monitor, entry.getValue(), VERBOSE));

            ruleBindingRegistry.bind(entry.getKey(), TRANSFER_SCOPE);
            policyEngine.registerFunction(TRANSFER_SCOPE, Permission.class, entry.getKey(),
                new ClientClaimConstraintFunction<>(monitor, entry.getValue(), VERBOSE));
            policyEngine.registerFunction(TRANSFER_SCOPE, Prohibition.class, entry.getKey(),
                new ClientClaimConstraintFunction<>(monitor, entry.getValue(), VERBOSE));
        }

        // also register reimplementation of standard edc contract expiry check function for negotiation scope
        ruleBindingRegistry.bind(ContractExpiryCheckFunction.CONTRACT_EXPIRY_EVALUATION_KEY, NEGOTIATION_SCOPE);
        policyEngine.registerFunction(NEGOTIATION_SCOPE, Permission.class, ContractExpiryCheckFunction.CONTRACT_EXPIRY_EVALUATION_KEY,
            new NegotiationContractExpiryCheckFunction<>(monitor));
        policyEngine.registerFunction(NEGOTIATION_SCOPE, Prohibition.class, ContractExpiryCheckFunction.CONTRACT_EXPIRY_EVALUATION_KEY,
            new NegotiationContractExpiryCheckFunction<>(monitor));
        // standard edc contract expiry check is already registered for transfer scope in contract-core extension
    }
}
