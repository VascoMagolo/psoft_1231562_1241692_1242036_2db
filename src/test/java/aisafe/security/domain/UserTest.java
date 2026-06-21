package aisafe.security.domain;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void ensureUserConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new User("", "hash", Role.BACKOFFICE_OPERATOR));
        assertThrows(IllegalArgumentException.class, () -> new User("username", "", Role.BACKOFFICE_OPERATOR));
        assertThrows(IllegalArgumentException.class, () -> new User("username", "hash", null));
    }

    @Test
    void ensureAssignRole() {
        User user = new User("username", "hash", Role.BACKOFFICE_OPERATOR);
        assertEquals(Role.BACKOFFICE_OPERATOR, user.getRole());
        user.assignRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void ensureReconstitute() {
        UUID id = UUID.randomUUID();
        User user = User.reconstitute(id, "username", "hash", Role.ADMIN);
        assertEquals(id, user.getUserID());
        assertEquals("username", user.getUsername());
        assertEquals("hash", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void ensureGetAuthorities() {
        User user = new User("username", "hash", Role.ADMIN);
        Collection<?> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().toString());
    }

    @Test
    void ensureUserDetailsMethods() {
        User user = new User("username", "hash", Role.BACKOFFICE_OPERATOR);
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void ensureEqualsAndHashCode() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        User u1 = User.reconstitute(id1, "user", "hash", Role.BACKOFFICE_OPERATOR);
        User u2 = User.reconstitute(id1, "differentUser", "differentHash", Role.ADMIN);
        User u3 = User.reconstitute(id2, "user", "hash", Role.BACKOFFICE_OPERATOR);

        assertEquals(u1, u1);
        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
        assertNotEquals(u1, null);
        assertNotEquals(u1, "string");
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotEquals(u1.hashCode(), u3.hashCode());
    }

    @Test
    void ensureProtectedConstructor() throws Exception {
        Constructor<User> constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        User u = constructor.newInstance();
        assertNotNull(u);
    }
}
