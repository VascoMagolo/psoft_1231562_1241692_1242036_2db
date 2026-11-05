package aisafe.security.infrastructure.persistence.jpa;

import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import aisafe.security.infrastructure.replication.UserReplica;
import aisafe.security.infrastructure.replication.UserReplicaHandler;
import aisafe.shared.infrastructure.replication.Replicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
public class UserJpaRepository implements UserRepository {

    /** Routing keys {@code account.created} and {@code account.updated} on {@code login.events}. */
    static final String ACCOUNT = "account";

    /** The {@code data} of the account events. */
    record AccountView(String username, String role) {
    }

    private final SpringDataUserRepository springRepo;
    private final Replicator replicator;
    private final EventPublisher events;

    public UserJpaRepository(SpringDataUserRepository springRepo, Replicator replicator, EventPublisher events) {
        this.springRepo = springRepo;
        this.replicator = replicator;
        this.events = events;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public User save(User user) {
        Optional<UserJpaEntity> existing = springRepo.findByUsername(user.getUsername());
        boolean created = existing.isEmpty();
        if (existing.isPresent() && existing.get().getUserID().equals(user.getUserID())) {
            existing.get().changeCredentials(user.getPassword(), user.getRole());
        } else {
            // The same username created on two logins at once: the newest account replaces the other
            existing.ifPresent(e -> {
                springRepo.delete(e);
                springRepo.flush();
            });
            springRepo.save(UserMapper.toJpa(user));
        }
        // Accounts: copied to every login
        replicator.catalogSaved(UserReplicaHandler.TYPE, user.getUsername(), UserReplica.from(user));
        // Who has an account and with which role; never the password hash
        events.changed(UserReplicaHandler.TYPE, ACCOUNT, created ? "created" : "updated", user.getUsername(), null, null,
                new AccountView(user.getUsername(), user.getRole().name()));
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springRepo.findByUsername(username).map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return springRepo.findAll().stream().map(UserMapper::toDomain).toList();
    }
}
