package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterAccountServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-05T08:00:00Z");
    private static final String PASSWORD = "correct horse battery staple";

    private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    private final RecordingPasswordHasher hasher = new RecordingPasswordHasher();
    private final RegisterAccountService service =
            new RegisterAccountService(accounts, hasher, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void createsOneAccountForTheNormalizedEmail() {
        RegisteredAccount result = service.register(command("  Person@Example.COM ", PASSWORD, PASSWORD, null));

        assertThat(result.email()).isEqualTo("person@example.com");
        assertThat(accounts.all()).singleElement().satisfies(account -> {
            assertThat(account.id()).isEqualTo(result.accountId());
            assertThat(account.email().value()).isEqualTo("person@example.com");
            assertThat(account.createdAt()).isEqualTo(NOW);
            assertThat(account.passwordHash().value()).isNotEqualTo(PASSWORD);
        });
    }

    @Test
    void registersWithoutAnyOptionalInformation() {
        RegisteredAccount result = service.register(command("minimal@example.com", PASSWORD, PASSWORD, null));

        assertThat(result.displayName()).isNull();
        assertThat(accounts.all()).singleElement()
                .satisfies(account -> assertThat(account.displayName()).isEmpty());
    }

    @Test
    void keepsTheSuppliedDisplayNameExactly() {
        String name = "Nguy\u1EC5n Th\u1ECB \u00C1nh";

        RegisteredAccount result = service.register(command("anh@example.com", PASSWORD, PASSWORD, name));

        assertThat(result.displayName()).isEqualTo(name);
    }

    @Test
    void reportsEveryMissingRequiredFieldAndStoresNothing() {
        assertThatThrownBy(() -> service.register(command(null, null, null, null)))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new FieldViolation("email", ValueProblem.REQUIRED),
                                new FieldViolation("password", ValueProblem.REQUIRED),
                                new FieldViolation("confirmPassword", ValueProblem.REQUIRED)));

        assertThat(accounts.all()).isEmpty();
        assertThat(hasher.hashedValues()).isEmpty();
    }

    @Test
    void rejectsAMismatchedConfirmation() {
        assertThatThrownBy(() -> service.register(command("person@example.com", PASSWORD, PASSWORD + "!", null)))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations())
                                .containsExactly(new FieldViolation("confirmPassword", ValueProblem.MISMATCH)));

        assertThat(accounts.all()).isEmpty();
    }

    @Test
    void rejectsAPolicyInvalidPasswordBeforeHashing() {
        String elevenCharacters = "p".repeat(11);

        assertThatThrownBy(() -> service.register(
                command("short@example.com", elevenCharacters, elevenCharacters, null)))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations())
                                .containsExactly(new FieldViolation("password", ValueProblem.TOO_SHORT)));

        assertThat(hasher.hashedValues()).isEmpty();
    }

    @Test
    void hashesThePasswordExactlyAsEntered() {
        // "  Mật khẩu có khoảng trắng  " — leading/trailing spaces and Vietnamese letters
        String password = "  M\u1EADt kh\u1EA9u c\u00F3 kho\u1EA3ng tr\u1EAFng  ";

        service.register(command("exact@example.com", password, password, null));

        assertThat(hasher.hashedValues()).containsExactly(password);
    }

    @Test
    void duplicateNormalizedEmailCreatesNoSecondAccountAndKeepsTheFirst() {
        service.register(command("person@example.com", PASSWORD, PASSWORD, "First"));
        Account original = accounts.all().get(0);

        assertThatThrownBy(() -> service.register(
                command(" PERSON@Example.com ", "another password 2026", "another password 2026", "Second")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        assertThat(accounts.all()).containsExactly(original);
    }

    @Test
    void correctedResubmissionCanCompleteRegistration() {
        assertThatThrownBy(() -> service.register(command("fix@example.com", PASSWORD, "different", null)))
                .isInstanceOf(RequestValidationException.class);

        RegisteredAccount result = service.register(command("fix@example.com", PASSWORD, PASSWORD, null));

        assertThat(accounts.all()).singleElement()
                .extracting(Account::id)
                .isEqualTo(result.accountId());
    }

    private static RegisterAccountCommand command(String email, String password, String confirm, String name) {
        return new RegisterAccountCommand(email, password, confirm, name);
    }
}