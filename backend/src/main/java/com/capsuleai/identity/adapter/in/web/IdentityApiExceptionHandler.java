package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.EmailAlreadyRegisteredException;
import com.capsuleai.identity.application.FieldViolation;
import com.capsuleai.identity.application.RequestValidationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates Identity outcomes into RFC 9457 problem responses.
 *
 * <p>Every body carries a stable machine-readable {@code code}; the mobile
 * client maps codes to Vietnamese guidance. No body echoes submitted values.
 */
@RestControllerAdvice(basePackageClasses = IdentityApiExceptionHandler.class)
public class IdentityApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(IdentityApiExceptionHandler.class);

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(RequestValidationException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Some submitted values are missing or invalid. Correct them and submit again.");
        problem.setProperty("fieldErrors", fieldErrors(exception.violations()));
        return respond(problem);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return respond(problem(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                "An account already exists for this email. Log in, or use Forgot Password to recover access."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "The request body is missing or is not valid JSON."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
        log.error("Identity persistence operation failed", exception);
        return respond(problem(HttpStatus.SERVICE_UNAVAILABLE, "TEMPORARILY_UNAVAILABLE",
                "The request could not be completed and its outcome is not confirmed. Check the result or retry."));
    }

    private static List<Map<String, String>> fieldErrors(List<FieldViolation> violations) {
        return violations.stream()
                .map(violation -> {
                    Map<String, String> error = new LinkedHashMap<>();
                    error.put("field", violation.field());
                    error.put("code", violation.problem().name());
                    return error;
                })
                .toList();
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        return problem;
    }

    private static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}