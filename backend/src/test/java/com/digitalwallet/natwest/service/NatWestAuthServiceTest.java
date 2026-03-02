package com.digitalwallet.natwest.service;

import com.digitalwallet.natwest.client.NatWestClient;
import com.digitalwallet.natwest.db.NatWestTokenEntity;
import com.digitalwallet.natwest.db.NatWestTokenRepository;
import com.digitalwallet.natwest.dto.NatWestTokenResponse;
import com.digitalwallet.natwest.mapper.NatWestMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NatWestAuthServiceTest {

    private static final String TOKEN_ID = "demoUser";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NatWestClient client;

    @Mock
    private NatWestTokenRepository repo;

    @Mock
    private NatWestMapper mapper;

    private AutoCloseable mocks;
    private NatWestAuthService service;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this); // <<< WICHTIG

        service = new NatWestAuthService(client, repo, mapper);

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @org.junit.After
    public void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    public void returnsExistingTokenWhenNotExpired() {
        NatWestTokenEntity entity = new NatWestTokenEntity(
                TOKEN_ID,
                "access_ok",
                Instant.now().plusSeconds(3600),
                "refresh_ok"
        );

        when(repo.findById(TOKEN_ID)).thenReturn(Optional.of(entity));

        String token = service.getValidUserAccessToken();

        assertEquals("access_ok", token);
        verify(client, never()).postForm(anyString(), anyMap(), eq(NatWestTokenResponse.class));
        verify(repo, never()).save(any());
    }

    @Test
    public void refreshesTokenWhenExpired() {
        NatWestTokenEntity expired = new NatWestTokenEntity(
                TOKEN_ID,
                "access_old",
                Instant.now().minusSeconds(60),
                "refresh_123"
        );

        when(repo.findById(TOKEN_ID)).thenReturn(Optional.of(expired));

        when(client.props().baseOb()).thenReturn("https://ob.example");
        when(client.props().clientId()).thenReturn("client_id");
        when(client.props().clientSecret()).thenReturn("client_secret");

        NatWestTokenResponse refreshed = new NatWestTokenResponse(
                "access_new",
                "refresh_new",
                3600L
        );

        when(client.postForm(anyString(), anyMap(), eq(NatWestTokenResponse.class)))
                .thenReturn(refreshed);

        String token = service.getValidUserAccessToken();

        assertEquals("access_new", token);
        verify(client, times(1)).postForm(anyString(), anyMap(), eq(NatWestTokenResponse.class));
        verify(repo, times(1)).save(any(NatWestTokenEntity.class));
    }

    @Test
    public void throwsWhenNotConnected() {
        when(repo.findById(TOKEN_ID)).thenReturn(Optional.empty());

        try {
            service.getValidUserAccessToken();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("not connected"));
        }

        verifyNoInteractions(client);
        verify(repo, never()).save(any());
    }

    @Test
    public void throwsWhenRefreshTokenMissing() {
        NatWestTokenEntity expiredNoRefresh = new NatWestTokenEntity(
                TOKEN_ID,
                "access_old",
                Instant.now().minusSeconds(60),
                "   "
        );

        when(repo.findById(TOKEN_ID)).thenReturn(Optional.of(expiredNoRefresh));

        try {
            service.getValidUserAccessToken();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("refresh"));
        }

        verifyNoInteractions(client);
        verify(repo, never()).save(any());
    }
}
