/*
 *  Copyright (c) 2021 Microsoft Corporation
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

import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Prohibition;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;

import java.util.Map;

import static org.eclipse.edc.connector.contract.spi.validation.ContractValidationService.NEGOTIATION_SCOPE;

@Extension(value = PossiblePolicyExtension.EXTENSION_NAME)
public class PossiblePolicyExtension implements ServiceExtension {

    public static final String EXTENSION_NAME = "POSSIBLE-POLICY-EXTENSION";

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
        return EXTENSION_NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        ruleBindingRegistry.bind("use", NEGOTIATION_SCOPE);
        ruleBindingRegistry.bind("USE", NEGOTIATION_SCOPE);
        ruleBindingRegistry.bind(ODRL_SCHEMA + "use", NEGOTIATION_SCOPE);

        for (Map.Entry<String, String> entry : CONSTRAINT_KEY_MAP.entrySet()) {
            ruleBindingRegistry.bind(entry.getKey(), NEGOTIATION_SCOPE);
            policyEngine.registerFunction(NEGOTIATION_SCOPE, Permission.class, entry.getKey(),
                new ClientClaimConstraintFunction<>(monitor, entry.getValue(), VERBOSE));
            policyEngine.registerFunction(NEGOTIATION_SCOPE, Prohibition.class, entry.getKey(),
                new ClientClaimConstraintFunction<>(monitor, entry.getValue(), VERBOSE));
        }
    }
}
