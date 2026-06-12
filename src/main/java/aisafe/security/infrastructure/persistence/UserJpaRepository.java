package aisafe.security.infrastructure.persistence;

import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("jpa")
public class UserJpaRepository implements UserRepository {

    private final SpringDataUserRepository springRepo;

    public UserJpaRepository(SpringDataUserRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public void save(User user) {
        springRepo.save(UserMapper.toJpa(user));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springRepo.findByUsername(username).map(UserMapper::toDomain);
    }
}
