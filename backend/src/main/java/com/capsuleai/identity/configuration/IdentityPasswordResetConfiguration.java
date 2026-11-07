package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.out.email.AsyncPasswordResetInstructionSender;
import com.capsuleai.identity.adapter.out.email.SmtpPasswordResetInstructionSender;
import com.capsuleai.identity.adapter.out.persistence.JdbcPasswordResetAttemptLimiter;
import com.capsuleai.identity.adapter.out.persistence.JdbcPasswordResetRepository;
import com.capsuleai.identity.adapter.out.security.SecureRandomPasswordResetTokenGenerator;
import com.capsuleai.identity.adapter.out.timing.MinimumResponseTimePacing;
import com.capsuleai.identity.application.CheckPasswordResetService;
import com.capsuleai.identity.application.CompletePasswordResetService;
import com.capsuleai.identity.application.PasswordResetAttemptGuard;
import com.capsuleai.identity.application.PurgeEndedPasswordResetsService;
import com.capsuleai.identity.application.RequestPasswordResetService;
import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.application.ports.PasswordResetAttemptLimiter;
import com.capsuleai.identity.application.ports.PasswordResetInstructionSender;
import com.capsuleai.identity.application.ports.PasswordResetRepository;
import com.capsuleai.identity.application.ports.PasswordResetTokenGenerator;
import com.capsuleai.identity.application.ports.RecoveryResponsePacing;
import com.capsuleai.identity.application.ports.SessionRepository;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mail.javamail.JavaMailSender;

/** Wires US-AUTH-005 password recovery: persistence, secret generation, email delivery and use cases. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({PasswordResetProperties.class, PasswordResetProtectionProperties.class})
public class IdentityPasswordResetConfiguration {

    /** Deliveries waiting beyond this are dropped with a warning; the user can request again. */
    static final int DELIVERY_QUEUE_CAPACITY = 100;

    @Bean
    PasswordResetRepository passwordResetRepository(JdbcClient jdbcClient) {
        return new JdbcPasswordResetRepository(jdbcClient);
    }

    @Bean
    PasswordResetAttemptLimiter passwordResetAttemptLimiter(JdbcClient jdbcClient) {
        return new JdbcPasswordResetAttemptLimiter(jdbcClient);
    }

    @Bean
    PasswordResetAttemptGuard passwordResetAttemptGuard(PasswordResetAttemptLimiter limiter,
                                                        PasswordResetProtectionProperties protection,
                                                        Clock clock) {
        return new PasswordResetAttemptGuard(limiter, protection.requestPerEmailMaxAttempts(),
                protection.requestPerClientMaxAttempts(), protection.verificationPerClientMaxAttempts(),
                protection.window(), clock);
    }

    @Bean
    RecoveryResponsePacing recoveryResponsePacing(PasswordResetProtectionProperties protection) {
        return new MinimumResponseTimePacing(protection.minimumRequestResponseTime());
    }

    @Bean
    PurgeEndedPasswordResetsService purgeEndedPasswordResetsService(
            PasswordResetRepository resets,
            PasswordResetAttemptLimiter attempts,
            @Value("${capsuleai.identity.password-reset.cleanup.batch-size}") int batchSize,
            Clock clock) {
        return new PurgeEndedPasswordResetsService(resets, attempts, batchSize, clock);
    }

    @Bean
    PasswordResetTokenGenerator passwordResetTokenGenerator() {
        return new SecureRandomPasswordResetTokenGenerator();
    }

    @Bean
    AsyncPasswordResetInstructionSender passwordResetInstructionSender(JavaMailSender mailSender,
                                                                       PasswordResetProperties properties) {
        SmtpPasswordResetInstructionSender smtpDelivery = new SmtpPasswordResetInstructionSender(
                mailSender, properties.senderAddress(), properties.instructionLink());
        return AsyncPasswordResetInstructionSender.withBoundedQueue(smtpDelivery, DELIVERY_QUEUE_CAPACITY);
    }

    @Bean
    RequestPasswordResetService requestPasswordResetService(AccountRepository accounts,
                                                            PasswordResetRepository resets,
                                                            PasswordResetTokenGenerator resetTokens,
                                                            PasswordResetInstructionSender instructionSender,
                                                            IdentityTransactions transactions,
                                                            Clock clock,
                                                            PasswordResetAttemptGuard attemptGuard,
                                                            RecoveryResponsePacing pacing) {
        return new RequestPasswordResetService(accounts, resets, resetTokens, instructionSender, transactions, clock,
                attemptGuard, pacing);
    }

    @Bean
    CheckPasswordResetService checkPasswordResetService(PasswordResetRepository resets, Clock clock,
                                                        PasswordResetAttemptGuard attemptGuard) {
        return new CheckPasswordResetService(resets, clock, attemptGuard);
    }

    @Bean
    CompletePasswordResetService completePasswordResetService(AccountRepository accounts,
                                                              PasswordResetRepository resets,
                                                              SessionRepository sessions,
                                                              PasswordHasher passwordHasher,
                                                              IdentityTransactions transactions,
                                                              Clock clock,
                                                              PasswordResetAttemptGuard attemptGuard) {
        return new CompletePasswordResetService(accounts, resets, sessions, passwordHasher, transactions, clock,
                attemptGuard);
    }
}