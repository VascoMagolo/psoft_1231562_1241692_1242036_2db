package aisafe.security.infrastructure.persistence.jpa;

import aisafe.security.domain.User;

public class UserMapper {
    public static User toDomain(UserJpaEntity entity) {
        User user = new User(entity.getUsername(), entity.getPasswordHash(), entity.getRole());
        user.setUserID(entity.getUserID());
        return user;
    }

    public static UserJpaEntity toJpa(User user) {
        return new UserJpaEntity(user.getUserID(), user.getUsername(), user.getPassword(), user.getRole());
    }
}
