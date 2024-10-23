package org.eclipse.edc.extension.possiblepolicy;

import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

public class ConnectorIdConstraintFunction<R extends Rule> implements AtomicConstraintFunction<R> {

    private final Monitor monitor;

    public ConnectorIdConstraintFunction(Monitor monitor) {
        this.monitor = monitor;
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

        for (Map.Entry<String, Object> e : contextData.getClaims().entrySet()) {
            monitor.info(format("Found claim %s : %s", e.getKey(), e.getValue()));
        }

        for (Map.Entry<String, String> e : contextData.getAttributes().entrySet()) {
            monitor.info(format("Found attribute %s : %s", e.getKey(), e.getValue()));
        }

        String clientIdClaim = (String) contextData.getClaims().get("client_id");

        if (clientIdClaim == null) {
            return false;
        }

        monitor.info(format("Evaluating constraint: connectorId %s %s %s", clientIdClaim, operator, rightValue));

        return switch (operator) {
            case EQ -> Objects.equals(clientIdClaim, rightValue);
            case NEQ -> !Objects.equals(clientIdClaim, rightValue);
            case IN, IS_ANY_OF -> Arrays.asList(((String) rightValue).split(",")).contains(clientIdClaim);
            case IS_NONE_OF -> !Arrays.asList(((String) rightValue).split(",")).contains(clientIdClaim);
            default -> false;
        };
    }
}
