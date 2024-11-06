package org.eclipse.edc.extension.possiblepolicy;

import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.spi.agent.ParticipantAgent;
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
