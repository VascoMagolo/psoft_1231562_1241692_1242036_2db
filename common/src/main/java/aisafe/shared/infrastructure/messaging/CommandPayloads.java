package aisafe.shared.infrastructure.messaging;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

/**
 * Reads the payload of a command into its request type and validates it, as {@code @Valid} does for
 * an HTTP body: an invalid command is answered with 400, like the route it replaces.
 */
@Component
public class CommandPayloads {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public CommandPayloads(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public <T> T read(JsonNode payload, Class<T> type) {
        T value = objectMapper.treeToValue(payload, type);
        Set<ConstraintViolation<T>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return value;
    }
}
