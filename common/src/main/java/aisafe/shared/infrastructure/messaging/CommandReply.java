package aisafe.shared.infrastructure.messaging;

import tools.jackson.databind.JsonNode;

/**
 * The answer to a command, as JSON: the same status an HTTP route would give (200, 404, 409,
 * 400...), the result when it worked, and the reason when it did not.
 */
public record CommandReply(int status, JsonNode body, String error) {

    public boolean ok() {
        return status >= 200 && status < 300;
    }
}
