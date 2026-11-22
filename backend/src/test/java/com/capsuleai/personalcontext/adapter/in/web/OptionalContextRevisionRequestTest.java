package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ExpectedRevision;
import com.capsuleai.personalcontext.application.OptionalContextChanges;
import com.capsuleai.personalcontext.application.ReviseOptionalContextCommand;
import com.capsuleai.personalcontext.application.ValueChange;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** US-PROF-002 (S4-PROF-03): omitted, null and values stay distinct with Jackson 3. */
class OptionalContextRevisionRequestTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void omittedNullAndStringValuesAreToldApart() {
        OptionalContextChanges changes = changes("{\"expectedRevision\":\"1\",\"genderContext\":null}");

        assertThat(changes.genderContext()).isInstanceOf(ValueChange.Clear.class);
        assertThat(changes.bodyShape()).isInstanceOf(ValueChange.Keep.class);
        assertThat(changes("{\"bodyShape\":\"ROUND\"}").bodyShape()).isEqualTo(ValueChange.replace("ROUND"));
    }

    @Test
    void valuesOfAnotherJsonKindAreMalformed() {
        OptionalContextChanges changes = changes("{\"genderContext\":3,\"bodyShape\":[\"ROUND\"]}");

        assertThat(changes.genderContext()).isInstanceOf(ValueChange.Malformed.class);
        assertThat(changes.bodyShape()).isInstanceOf(ValueChange.Malformed.class);
    }

    @Test
    void theExpectedRevisionIsMissingNullOrAToken() {
        assertThat(command("{}").expectedRevision()).isInstanceOf(ExpectedRevision.Missing.class);
        assertThat(command("{\"expectedRevision\":null}").expectedRevision())
                .isInstanceOf(ExpectedRevision.Unset.class);
        assertThat(command("{\"expectedRevision\":\"3\"}").expectedRevision()).isEqualTo(new ExpectedRevision.At(3));
        assertThat(command("{\"expectedRevision\":3}").expectedRevision())
                .isInstanceOf(ExpectedRevision.Malformed.class);
    }

    /** An empty string is a submitted value (the service rejects it); booleans and objects are malformed. */
    @Test
    void anEmptyStringIsAValueWhileBooleansAndObjectsAreMalformed() {
        assertThat(changes("{\"genderContext\":\"\"}").genderContext()).isEqualTo(ValueChange.replace(""));
        OptionalContextChanges changes = changes("{\"genderContext\":true,\"bodyShape\":{\"code\":\"ROUND\"}}");

        assertThat(changes.genderContext()).isInstanceOf(ValueChange.Malformed.class);
        assertThat(changes.bodyShape()).isInstanceOf(ValueChange.Malformed.class);
        assertThat(command("{\"expectedRevision\":\"0\"}").expectedRevision()).isInstanceOf(ExpectedRevision.Malformed.class);
        assertThat(command("{\"expectedRevision\":\"abc\"}").expectedRevision())
                .isInstanceOf(ExpectedRevision.Malformed.class);
    }

    private static ReviseOptionalContextCommand command(String json) {
        return JSON.readValue(json, OptionalContextRevisionRequest.class).toCommand(UUID.randomUUID());
    }

    private static OptionalContextChanges changes(String json) {
        return command(json).changes();
    }
}
