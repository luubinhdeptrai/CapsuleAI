package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.EmailAlreadyRegisteredException;
import com.capsuleai.identity.application.FieldViolation;
import com.capsuleai.identity.application.RequestValidationException;
import com.capsuleai.identity.domain.ValueProblem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityApiExceptionHandlerTest {

    private final IdentityApiExceptionHandler handler = new IdentityApiExceptionHandler();

    @Test
    void validationProblemListsEveryFieldWithoutEchoingValues() {
        ResponseEntity<ProblemDetail> response = handler.handleValidation(new RequestValidationException(List.of(
                new FieldViolation("email", ValueProblem.REQUIRED),
                new FieldViolation("password", ValueProblem.TOO_SHORT))));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties())
                .containsEntry("code", "VALIDATION_FAILED")
                .containsEntry("fieldErrors", List.of(
                        Map.of("field", "email", "code", "REQUIRED"),
                        Map.of("field", "password", "code", "TOO_SHORT")));
    }

    @Test
    void duplicateEmailOffersSignInAndRecoveryGuidance() {
        ResponseEntity<ProblemDetail> response =
                handler.handleEmailAlreadyRegistered(new EmailAlreadyRegisteredException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties()).containsEntry("code", "EMAIL_ALREADY_REGISTERED");
        assertThat(response.getBody().getDetail()).contains("Log in").contains("Forgot Password");
    }

    @Test
    void persistenceFailureIsReportedAsAnUnconfirmedOutcomeNotSuccess() {
        ResponseEntity<ProblemDetail> response =
                handler.handleDataAccess(new DataAccessResourceFailureException("database unavailable"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties()).containsEntry("code", "TEMPORARILY_UNAVAILABLE");
    }
}