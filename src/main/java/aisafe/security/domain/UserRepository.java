package aisafe.security.domain;

import java.util.Optional;

/**
 * Repository interface for managing User entities in the security domain.
 */
public interface UserRepository {
    long count();
    User save(User user);
    Optional<User> findByUsername(String username);
}
