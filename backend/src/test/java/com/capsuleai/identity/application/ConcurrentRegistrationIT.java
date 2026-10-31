package com.capsuleai.identity.application;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentRegistrationIT extends IdentityIntegrationTestSupport {

    @Autowired
    private RegisterAccountService registerAccountService;

    @Test
    void equivalentEmailsRegisteredConcurrentlyCreateAtMostOneAccount() throws Exception {
        List<String> equivalentEmails = List.of(
                "race@example.com",
                "RACE@example.com",
                " race@example.com",
                "Race@Example.com ",
                "rAcE@eXaMpLe.CoM",
                "race@EXAMPLE.com");

        CountDownLatch ready = new CountDownLatch(equivalentEmails.size());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(equivalentEmails.size());
        List<String> outcomes = new ArrayList<>();
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (String email : equivalentEmails) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        registerAccountService.register(new RegisterAccountCommand(email, PASSWORD, PASSWORD, null));
                        return "CREATED";
                    } catch (EmailAlreadyRegisteredException duplicate) {
                        return "DUPLICATE";
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<String> future : futures) {
                outcomes.add(future.get(60, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(Collections.frequency(outcomes, "CREATED")).isOne();
        assertThat(Collections.frequency(outcomes, "DUPLICATE")).isEqualTo(equivalentEmails.size() - 1);
        assertThat(jdbc.sql("SELECT count(*) FROM identity_account WHERE email = 'race@example.com'")
                .query(Long.class).single()).isOne();
    }
}