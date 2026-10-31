package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.DisplayName;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.InvalidValueException;
import com.capsuleai.identity.domain.PasswordHash;
import com.capsuleai.identity.domain.PasswordPolicy;
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
 * US-AUTH-001 — registers one private account per normalized email identity.
 */
public final class RegisterAccountService {

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public RegisterAccountService(AccountRepository accounts, PasswordHasher passwordHasher, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws RequestValidationException      if any field is missing or invalid (nothing stored)
     * @throws EmailAlreadyRegisteredException if the normalized email is taken (nothing changed)
     */
    public RegisteredAccount register(RegisterAccountCommand command) {
        List<FieldViolation> violations = new ArrayList<>();

        EmailAddress email = null;
        try {
            email = EmailAddress.normalize(command.email());
        } catch (InvalidValueException rejected) {
            violations.add(new FieldViolation("email", rejected.problem()));
        }

        try {
            PasswordPolicy.validateNewPassword(command.password());
        } catch (InvalidValueException rejected) {
            violations.add(new FieldViolation("password", rejected.problem()));
        }

        if (command.confirmPassword() == null || command.confirmPassword().isEmpty()) {
            violations.add(new FieldViolation("confirmPassword", ValueProblem.REQUIRED));
        } else if (!command.confirmPassword().equals(command.password())) {
            violations.add(new FieldViolation("confirmPassword", ValueProblem.MISMATCH));
        }

        Optional<DisplayName> displayName = Optional.empty();
        try {
            displayName = DisplayName.optional(command.displayName());
        } catch (InvalidValueException rejected) {
            violations.add(new FieldViolation("displayName", rejected.problem()));
        }

        if (!violations.isEmpty()) {
            throw new RequestValidationException(violations);
        }

        // Hash before any database work: PBKDF2 is deliberately slow.
        PasswordHash passwordHash = passwordHasher.hash(command.password());
        Instant createdAt = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Account account = new Account(UUID.randomUUID(), email, passwordHash, displayName, createdAt);

        if (!accounts.addIfEmailAvailable(account)) {
            throw new EmailAlreadyRegisteredException();
        }
        return new RegisteredAccount(account.id(), email.value(), displayName.map(DisplayName::value).orElse(null));
    }
}