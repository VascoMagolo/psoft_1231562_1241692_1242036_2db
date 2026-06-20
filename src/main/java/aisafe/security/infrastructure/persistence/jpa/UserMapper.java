package aisafe.security.infrastructure.persistence.jpa;

import aisafe.security.domain.User;

public class UserMapper {
    public static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(entity.getUserID(), entity.getUsername(), entity.getPasswordHash(), entity.getRole());
    }

    public static UserJpaEntity toJpa(User user) {
        return new UserJpaEntity(user.getUserID(), user.getUsername(), user.getPassword(), user.getRole());
    }
}
