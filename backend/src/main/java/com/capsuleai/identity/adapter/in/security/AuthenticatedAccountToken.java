package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.api.AuthenticatedAccount;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Spring Security authentication whose principal is the published
 * {@link AuthenticatedAccount}. The bearer token itself is not retained.
 */
public final class AuthenticatedAccountToken extends AbstractAuthenticationToken {

    private final AuthenticatedAccount account;

    public AuthenticatedAccountToken(AuthenticatedAccount account) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.account = account;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public AuthenticatedAccount getPrincipal() {
        return account;
    }

    @Override
    public String getName() {
        return account.accountId().toString();
    }
}