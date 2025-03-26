/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *  Copyright 2024-2025 Dataport. All rights reserved. Extended as part of the POSSIBLE project.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *       Dataport AöR for the POSSIBLE project - Custom reimplementation for negotiation scope
 *
 */

package org.eclipse.edc.extension.possiblepolicy;

import org.eclipse.edc.connector.controlplane.policy.contract.ContractExpiryCheckFunction;
import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.spi.monitor.Monitor;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static java.lang.String.format;

/**
 * Custom reimplementation of the {@link ContractExpiryCheckFunction} from the Connector core to allow it to be used in the negotiation scope.
 * <p>
 * Constraint function that evaluates a time-based constraint. That is a constraint that either uses the "inForceDate" operand
 * with a fixed time (ISO-8061 UTC) as:
 * <pre>
 * {
 *   "leftOperand": "edc:inForceDate",
 *   "operator": "GEQ",
 *   "rightOperand": "2024-01-01T00:00:01Z"
 * }
 * </pre>
 * Alternatively, it is possible to use a duration expression:
 * <pre>
 * {
 *   "leftOperand": "edc:inForceDate",
 *   "operator": "GEQ",
 *   "rightOperand": "contractAgreement+365d"
 * }
 * </pre>
 * following the following schema: {@code <offset> + <numeric value>s|m|h|d} where {@code offset} must be equal to {@code "contractAgreement"}
 * (not case-sensitive) and refers to the signing date of the contract in Epoch seconds. Omitting the {@code offset} is not permitted.
 * The numeric value can have negative values.
 * Thus, the following examples would be valid:
 * <ul>
 *     <li>contractAgreement+15s</li>
 *     <li>contractAgreement+7d</li>
 *     <li>contractAgreement+1h</li>
 *     <li>contractAgreement+-5m (means "5 minutes before the signing of the contract")</li>
 * </ul>
 * Please note that all {@link Operator}s except {@link Operator#IN} are supported.
 */
public class NegotiationContractExpiryCheckFunction<R extends Rule> implements AtomicConstraintFunction<R> {

    private final Monitor monitor;
    private static final String EXPRESSION_REGEX = "(contract[A,a]greement)\\+(-?[0-9]+)(s|m|h|d)";

    public NegotiationContractExpiryCheckFunction(Monitor monitor) {

        this.monitor = monitor;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, R rule, PolicyContext context) {
        if (!(rightValue instanceof String)) {
            context.reportProblem("Right-value expected to be String but was " + rightValue.getClass());
            return false;
        }

        try {
            Instant now = Instant.now(); // "now" is not in context during negotiation, use current time

            var rightValueStr = (String) rightValue;
            var bound = asInstant(rightValueStr);
            if (bound != null) {
                monitor.info(format("Validating time constraint %s %s %s", now, operator, bound));
                return checkFixedPeriod(now, operator, bound);
            }

            if (rightValueStr.matches(EXPRESSION_REGEX)) {
                monitor.info(format("Offset constraints are ignored during negotiation. %s %s %s",
                    ContractExpiryCheckFunction.CONTRACT_EXPIRY_EVALUATION_KEY, operator, rightValueStr));
                return true;
            }

            context.reportProblem(format("Unsupported right-value, expected either an ISO-8061 String or a expression matching '%s', but got '%s'",
                EXPRESSION_REGEX, rightValueStr));
        } catch (NullPointerException | DateTimeParseException ex) {
            monitor.warning(format("Exception during time policy evaluation: %s %s", ex.getClass().getCanonicalName(), ex.getMessage()));
            context.reportProblem(ex.getMessage());
        }
        return false;
    }

    private boolean checkFixedPeriod(Instant now, Operator operator, Instant bound) {
        var comparison = now.compareTo(bound);

        return switch (operator) {
            case EQ -> comparison == 0;
            case NEQ -> comparison != 0;
            case GT -> comparison > 0;
            case GEQ -> comparison >= 0;
            case LT -> comparison < 0;
            case LEQ -> comparison <= 0;
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        };
    }

    private Instant asInstant(String isoString) {
        try {
            return Instant.parse(isoString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}