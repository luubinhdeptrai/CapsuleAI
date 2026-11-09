package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.InvalidValueException;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.SessionPolicy;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-AUTH-002 — verifies email and exact password, then establishes a new,
 * separate login session and issues its credentials.
 */
public final class LogInService {

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final SessionRepository sessions;
    private final RefreshTokenGenerator refreshTokens;
    private final AccessTokenIssuer accessTokens;
    private final IdentityTransactions transactions;
    private final Clock clock;

    public LogInService(AccountRepository accounts,
                        PasswordHasher passwordHasher,
                        SessionRepository sessions,
                        RefreshTokenGenerator refreshTokens,
                        AccessTokenIssuer accessTokens,
                        IdentityTransactions transactions,
                        Clock clock) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.refreshTokens = Objects.requireNonNull(refreshTokens, "refreshTokens");
        this.accessTokens = Objects.requireNonNull(accessTokens, "accessTokens");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws RequestValidationException  if the email or the password is missing
     * @throws InvalidCredentialsException if the pair does not authenticate an account
     */
    public SessionTokens logIn(LogInCommand command) {
        requireCredentials(command);

        Account account = findAccount(command.email())
                .filter(candidate -> passwordHasher.matches(command.password(), candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        LoginSession session = LoginSession.establish(UUID.randomUUID(), account.id(), now);
        String refreshToken = refreshTokens.newToken();
        Instant accessTokenExpiresAt = SessionPolicy.accessTokenExpiresAt(now, session.expiresAt());
        String accessToken = accessTokens.issue(account.id(), session.id(), now, accessTokenExpiresAt);

        // Both rows commit together; tokens leave this method only after the commit.
        // The password was verified outside this transaction, so recheck it under a
        // shared account lock: a reset that replaced the credential meanwhile wins.
        transactions.inTransactionWithoutResult(() -> {
            PasswordHash currentPasswordHash = accounts.lockCredentialAgainstChange(account.id())
                    .orElseThrow(InvalidCredentialsException::new);
            if (!currentPasswordHash.equals(account.passwordHash())) {
                throw new InvalidCredentialsException();
            }
            sessions.create(session, RefreshTokenHash.of(refreshToken));
        });

        return new SessionTokens(account.id(), accessToken, accessTokenExpiresAt, refreshToken, session.expiresAt());
    }

    private static void requireCredentials(LogInCommand command) {
        List<FieldViolation> violations = new ArrayList<>();
        if (command.email() == null || command.email().isBlank()) {
            violations.add(new FieldViolation("email", ValueProblem.REQUIRED));
        }
        if (command.password() == null || command.password().isEmpty()) {
            violations.add(new FieldViolation("password", ValueProblem.REQUIRED));
        }
        if (!violations.isEmpty()) {
            throw new RequestValidationException(violations);
        }
    }

    /** A malformed email cannot identify any account, so it is treated like an unknown one. */
    private Optional<Account> findAccount(String rawEmail) {
        try {
            return accounts.findByEmail(EmailAddress.normalize(rawEmail));
        } catch (InvalidValueException malformedEmail) {
            return Optional.empty();
        }
    }
}