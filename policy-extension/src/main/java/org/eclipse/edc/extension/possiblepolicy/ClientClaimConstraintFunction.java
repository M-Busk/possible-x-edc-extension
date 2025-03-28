/*
 *  Copyright 2024-2025 Dataport. All rights reserved. Developed as part of the POSSIBLE project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eclipse.edc.extension.possiblepolicy;

import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

public class ClientClaimConstraintFunction<R extends Rule> implements AtomicConstraintFunction<R> {

    private final Monitor monitor;
    private final String clientClaimName;
    private final boolean verbose;

    public ClientClaimConstraintFunction(Monitor monitor, String clientClaimName, boolean verbose) {
        this.monitor = monitor;
        this.clientClaimName = clientClaimName;
        this.verbose = verbose;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, R rule, PolicyContext context) {

        if (!(rightValue instanceof String)) {
            context.reportProblem("Right-value expected to be String but was " + rightValue.getClass());
            return false;
        }

        var contextData = context.getContextData(ParticipantAgent.class);
        if (contextData == null) {
            return false;
        }

        if (verbose) {
            for (Map.Entry<String, Object> e : contextData.getClaims().entrySet()) {
                monitor.info(format("Found claim %s : %s", e.getKey(), e.getValue()));
            }

            for (Map.Entry<String, String> e : contextData.getAttributes().entrySet()) {
                monitor.info(format("Found attribute %s : %s", e.getKey(), e.getValue()));
            }
        }

        String clientClaim = (String) contextData.getClaims().get(clientClaimName);

        if (clientClaim == null) {
            monitor.info(format("Required claim %s not found.", clientClaimName));
            return false;
        }

        monitor.info(format("Evaluating constraint: %s %s %s %s", clientClaimName, clientClaim, operator, rightValue));

        return switch (operator) {
            case EQ -> Objects.equals(clientClaim, rightValue);
            case NEQ -> !Objects.equals(clientClaim, rightValue);
            case IN, IS_ANY_OF -> Arrays.asList(((String) rightValue).split(",")).contains(clientClaim);
            case IS_NONE_OF -> !Arrays.asList(((String) rightValue).split(",")).contains(clientClaim);
            default -> false;
        };
    }
}
