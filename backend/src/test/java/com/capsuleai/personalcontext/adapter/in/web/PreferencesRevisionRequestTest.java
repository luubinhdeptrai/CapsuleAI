package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ExpectedRevision;
import com.capsuleai.personalcontext.application.PreferenceChanges;
import com.capsuleai.personalcontext.application.RevisePreferencesCommand;
import com.capsuleai.personalcontext.application.ValueChange;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** US-PROF-001 (OPEN-S3-05): omitted, null and values stay distinct; pins the Jackson 3 behaviour relied on. */
class PreferencesRevisionRequestTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void anOmittedExpectedRevisionIsMissingAndNullMeansNothingDeclaredYet() {
        assertThat(command("{}").expectedRevision()).isInstanceOf(ExpectedRevision.Missing.class);
        assertThat(command("{\"expectedRevision\":null}").expectedRevision()).isInstanceOf(ExpectedRevision.Unset.class);
    }

    @Test
    void aRevisionTokenIsAStringOfDigits() {
        assertThat(command("{\"expectedRevision\":\"7\"}").expectedRevision()).isEqualTo(new ExpectedRevision.At(7));
        assertThat(command("{\"expectedRevision\":7}").expectedRevision()).isInstanceOf(ExpectedRevision.Malformed.class);
        assertThat(command("{\"expectedRevision\":\"07\"}").expectedRevision())
                .isInstanceOf(ExpectedRevision.Malformed.class);
    }

    @Test
    void stylesCanBeOmittedClearedReplacedOrMalformed() {
        assertThat(changes("{}").styles()).isInstanceOf(ValueChange.Keep.class);
        assertThat(changes("{\"styles\":null}").styles()).isInstanceOf(ValueChange.Clear.class);
        assertThat(changes("{\"styles\":[\"MINIMAL\",5]}").styles())
                .isEqualTo(ValueChange.replace(Arrays.asList("MINIMAL", null)));
        assertThat(changes("{\"styles\":\"MINIMAL\"}").styles()).isInstanceOf(ValueChange.Malformed.class);
    }

    @Test
    void eachNeedIsKeptClearedOrReplacedIndividuallyAndANullNeedObjectIsMalformed() {
        PreferenceChanges changes = changes("{\"commonNeeds\":{\"TRAVEL\":0,\"WORK_PROFESSIONAL\":null,"
                + "\"SPORT_ACTIVE\":\"HIGH\",\"SCHOOL_UNIVERSITY\":2.5}}");

        assertThat(changes.commonNeeds().get("TRAVEL")).isEqualTo(ValueChange.replace(0));
        assertThat(changes.commonNeeds().get("WORK_PROFESSIONAL")).isInstanceOf(ValueChange.Clear.class);
        assertThat(changes.commonNeeds().get("SPORT_ACTIVE")).isInstanceOf(ValueChange.Malformed.class);
        assertThat(changes.commonNeeds().get("SCHOOL_UNIVERSITY")).isInstanceOf(ValueChange.Malformed.class);
        assertThat(changes.commonNeeds()).doesNotContainKey("EVERYDAY_CASUAL");
        assertThat(changes.commonNeedsMalformed()).isFalse();
        assertThat(changes("{}").commonNeedsMalformed()).isFalse();
        assertThat(changes("{\"commonNeeds\":null}").commonNeedsMalformed()).isTrue();
        assertThat(changes("{\"commonNeeds\":[1]}").commonNeedsMalformed()).isTrue();
    }

    private static PreferenceChanges changes(String json) {
        return command(json).changes();
    }

    private static RevisePreferencesCommand command(String json) {
        return JSON.readValue(json, PreferencesRevisionRequest.class).toCommand(UUID.randomUUID());
    }
}
