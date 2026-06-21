package aisafe.security.infrastructure.persistence.jpa;

import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class SpringDataUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @Test
    void ensureSaveAndRetrieveUser() {
        long initialCount = userRepository.count();

        User user = new User("persisteduser", "hash123", Role.BACKOFFICE_OPERATOR);
        userRepository.save(user);

        assertEquals(initialCount + 1, userRepository.count());

        Optional<User> found = userRepository.findByUsername("persisteduser");
        assertTrue(found.isPresent());
        assertEquals("persisteduser", found.get().getUsername());
        assertEquals(Role.BACKOFFICE_OPERATOR, found.get().getRole());

        // Test UserJpaEntity getters directly
        Optional<UserJpaEntity> jpaEntity = springDataUserRepository.findByUsername("persisteduser");
        assertTrue(jpaEntity.isPresent());
        assertNotNull(jpaEntity.get().getId());
        assertEquals(user.getUserID(), jpaEntity.get().getUserID());
        assertEquals("persisteduser", jpaEntity.get().getUsername());
        assertEquals("hash123", jpaEntity.get().getPasswordHash());
        assertEquals(Role.BACKOFFICE_OPERATOR, jpaEntity.get().getRole());

        // Cover default constructor of UserJpaEntity
        UserJpaEntity defaultEntity = new UserJpaEntity();
        assertNull(defaultEntity.getId());
    }

    @Test
    void ensureFindByUsernameReturnsEmptyWhenNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistentuser");
        assertTrue(found.isEmpty());
    }

    @Test
    void ensureConstructorUserMapper() {
        assertNotNull(new UserMapper());
    }
}
