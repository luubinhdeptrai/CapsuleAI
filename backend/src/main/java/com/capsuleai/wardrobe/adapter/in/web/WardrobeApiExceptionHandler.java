package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.GarmentFieldViolation;
import com.capsuleai.wardrobe.application.GarmentNoLongerCurrentException;
import com.capsuleai.wardrobe.application.GarmentNotFoundException;
import com.capsuleai.wardrobe.application.GarmentRevisionConflictException;
import com.capsuleai.wardrobe.application.GarmentValidationException;
import com.capsuleai.wardrobe.application.IdempotencyKeyReusedException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates Wardrobe outcomes into RFC 9457 problem responses with a stable
 * {@code code}, using the same body shape as Identity. Scoped to Wardrobe's web
 * package; no body echoes submitted values. A failure is never reported as an
 * empty wardrobe or as a saved garment (AC-11, AC-12, ERR-NET-001).
 */
@RestControllerAdvice(basePackageClasses = WardrobeApiExceptionHandler.class)
public class WardrobeApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WardrobeApiExceptionHandler.class);

    @ExceptionHandler(GarmentValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(GarmentValidationException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Some garment values are missing, unconfirmed or not allowed. Correct or clear them and confirm again; "
                        + "nothing was saved.");
        problem.setProperty("fieldErrors", fieldErrors(exception.violations()));
        return respond(problem);
    }

    /** OPEN-S2-10: an unusable limit or cursor is a client error, never an empty or restarted page. */
    @ExceptionHandler(InvalidWardrobePageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPage(InvalidWardrobePageException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "This page of the wardrobe cannot be loaded. Load the wardrobe again from the first page.");
        problem.setProperty("fieldErrors", exception.errors().stream()
                .map(error -> {
                    Map<String, String> fieldError = new LinkedHashMap<>();
                    fieldError.put("field", error.field());
                    fieldError.put("code", error.code());
                    return fieldError;
                })
                .toList());
        return respond(problem);
    }

    @ExceptionHandler(IdempotencyKeyReusedException.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyKeyReused(IdempotencyKeyReusedException exception) {
        return respond(problem(HttpStatus.UNPROCESSABLE_CONTENT, "IDEMPOTENCY_KEY_REUSED",
                "This Idempotency-Key was already used for a different garment. Use a new key for a new garment; "
                        + "nothing was saved."));
    }

    /** The same answer for an unknown id and another account's garment (NFR-SEC-001). */
    @ExceptionHandler(GarmentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleGarmentNotFound(GarmentNotFoundException exception) {
        return respond(problem(HttpStatus.NOT_FOUND, "GARMENT_NOT_FOUND",
                "This garment is not in your wardrobe. Return to your wardrobe to choose another one."));
    }

    /** US-WAR-002 AC-09: the owner's garment is no longer current; it is not reported as deleted data loss. */
    @ExceptionHandler(GarmentNoLongerCurrentException.class)
    public ResponseEntity<ProblemDetail> handleGarmentNoLongerCurrent(GarmentNoLongerCurrentException exception) {
        return respond(problem(HttpStatus.GONE, "GARMENT_NO_LONGER_CURRENT",
                "This garment is no longer in your current wardrobe. Refresh your wardrobe to see what you own now."));
    }

    /** US-GAR-002 (S3-GAR-02/04): the edit was based on an outdated revision; nothing changed. */
    @ExceptionHandler(GarmentRevisionConflictException.class)
    public ResponseEntity<ProblemDetail> handleRevisionConflict(GarmentRevisionConflictException exception) {
        return respond(problem(HttpStatus.CONFLICT, "REVISION_CONFLICT",
                "This garment changed after you opened it, so your edit was not applied. Reload it, review the "
                        + "current values and confirm your changes again."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "The request body is missing or is not valid JSON."));
    }

    /**
     * Only an unreachable database becomes 503. Other data-access errors (for
     * example invalid SQL) are programming errors and stay 500.
     */
    @ExceptionHandler({DataAccessResourceFailureException.class, TransientDataAccessResourceException.class})
    public ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
        log.error("Wardrobe persistence operation failed", exception);
        return outcomeNotConfirmed();
    }

    /** A transaction that could not start because of a database connection problem is the same outage. */
    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ProblemDetail> handleTransactionNotStarted(CannotCreateTransactionException exception) {
        if (!(exception.getCause() instanceof SQLException)) {
            throw exception;
        }
        log.error("Wardrobe transaction could not be started", exception);
        return outcomeNotConfirmed();
    }

    private static List<Map<String, String>> fieldErrors(List<GarmentFieldViolation> violations) {
        return violations.stream()
                .map(violation -> {
                    Map<String, String> error = new LinkedHashMap<>();
                    error.put("field", violation.field());
                    error.put("code", violation.problem().name());
                    return error;
                })
                .toList();
    }

    private static ResponseEntity<ProblemDetail> outcomeNotConfirmed() {
        return respond(problem(HttpStatus.SERVICE_UNAVAILABLE, "TEMPORARILY_UNAVAILABLE",
                "The wardrobe could not be reached, so the outcome is not confirmed. Check the current state before "
                        + "retrying: an addition repeated with the same Idempotency-Key is never added twice, and an "
                        + "edit repeated with the same expectedRevision is never applied twice."));
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