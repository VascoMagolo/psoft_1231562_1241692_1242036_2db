package aisafe.shared.infrastructure.peers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class HttpPeerQueriesTest {

    record Thing(String id, int size) {}

    private final PeerClient peerClient = mock(PeerClient.class);
    private final aisafe.shared.infrastructure.security.ServiceTokens serviceTokens = mock(aisafe.shared.infrastructure.security.ServiceTokens.class);
    private final HttpPeerQueries queries = new HttpPeerQueries(peerClient, JsonMapper.builder().build(), serviceTokens);
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("GET", "/api/things");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(peerClient.hasPeers()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static PeerClient.PeerResponse ok(String peer, String json) {
        return new PeerClient.PeerResponse(peer, HttpStatus.OK, HttpHeaders.EMPTY, json.getBytes(StandardCharsets.UTF_8), false);
    }

    @Test
    void ensureListsOfEveryPeerAreJoinedAndUnreachablePeersRecorded() {
        when(peerClient.getFromAll(eq("/internal/things"), any())).thenReturn(List.of(
                ok("http://peer-1", "[{\"id\":\"a\",\"size\":1}]"),
                ok("http://peer-2", "[{\"id\":\"b\",\"size\":2},{\"id\":\"c\",\"size\":3}]"),
                new PeerClient.PeerResponse("http://peer-3", null, HttpHeaders.EMPTY, new byte[0], true)));

        List<Thing> things = queries.fetchFromAll("/internal/things", Thing.class);

        assertEquals(List.of(new Thing("a", 1), new Thing("b", 2), new Thing("c", 3)), things);
        assertEquals(Set.of("http://peer-3"), UnreachablePeers.of(request));
    }

    @Test
    void ensureOutsideARequestTheServiceTokenIsUsed() {
        // A command taken from RabbitMQ runs outside any HTTP request
        RequestContextHolder.resetRequestAttributes();
        when(serviceTokens.current()).thenReturn("service-token");
        when(peerClient.getFirst(any(), any())).thenReturn(new PeerClient.PeerLookup(java.util.Optional.empty(), List.of()));

        queries.fetchFirst("/internal/aircrafts/CS-TKA", Thing.class);

        verify(peerClient).getFirst(eq("/internal/aircrafts/CS-TKA"),
                argThat(h -> "Bearer service-token".equals(h.getFirst(HttpHeaders.AUTHORIZATION))));
    }

    @Test
    void ensureTokenIsRelayedToPeers() {
        when(peerClient.getFromAll(any(), any())).thenReturn(List.of());

        queries.fetchFromAll("/internal/things", Thing.class);

        verify(peerClient).getFromAll(eq("/internal/things"),
                argThat(h -> "Bearer token-123".equals(h.getFirst(HttpHeaders.AUTHORIZATION))));
    }

    @Test
    void ensureFirstPeerThatHasTheResourceIsReturned() {
        when(peerClient.getFirst(eq("/internal/things/b"), any()))
                .thenReturn(new PeerClient.PeerLookup(Optional.of(ok("http://peer-2", "{\"id\":\"b\",\"size\":2}")), List.of()));

        assertEquals(Optional.of(new Thing("b", 2)), queries.fetchFirst("/internal/things/b", Thing.class));
    }

    @Test
    void ensureForwardedRequestsNeverAskPeersAgain() {
        request.addHeader(PeerHeaders.FORWARDED, "node-1");

        assertTrue(queries.fetchFromAll("/internal/things", Thing.class).isEmpty());
        assertTrue(queries.fetchFirst("/internal/things/b", Thing.class).isEmpty());
        verify(peerClient, never()).getFromAll(any(), any());
        verify(peerClient, never()).getFirst(any(), any());
    }
}
