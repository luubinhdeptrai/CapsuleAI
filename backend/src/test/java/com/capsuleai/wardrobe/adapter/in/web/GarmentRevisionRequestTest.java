package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.FieldChange;
import com.capsuleai.wardrobe.application.GarmentChanges;
import com.capsuleai.wardrobe.application.GarmentChanges.PriceInput;
import com.capsuleai.wardrobe.application.ReviseGarmentCommand;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-GAR-002 (OPEN-S3-05): the PATCH body keeps "omitted", "null" and "wrong kind" apart. This also
 * pins the Jackson 3 behaviour the design relies on: an absent JsonNode record component is null,
 * an explicit JSON null is a NullNode.
 */
class GarmentRevisionRequestTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final UUID OWNER = UUID.randomUUID();

    @Test
    void anOmittedPropertyIsKeptAndAnExplicitNullClearsIt() {
        GarmentChanges changes = changes("{\"confirmed\":true,\"expectedRevision\":\"3\",\"subtype\":null,"
                + "\"materials\":null,\"price\":null}");

        assertThat(changes.category()).isInstanceOf(FieldChange.Keep.class);
        assertThat(changes.color()).isInstanceOf(FieldChange.Keep.class);
        assertThat(changes.brand()).isInstanceOf(FieldChange.Keep.class);
        assertThat(changes.subtype()).isInstanceOf(FieldChange.Clear.class);
        assertThat(changes.materials()).isInstanceOf(FieldChange.Clear.class);
        assertThat(changes.price()).isInstanceOf(FieldChange.Clear.class);
    }

    @Test
    void aStringReplacesAndAnyOtherJsonKindIsMalformed() {
        GarmentChanges changes = changes("{\"subtype\":\"JEANS\",\"fit\":12,\"brand\":true,"
                + "\"pattern\":{\"code\":\"SOLID\"},\"color\":[\"RED\"]}");

        assertThat(changes.subtype()).isEqualTo(FieldChange.replace("JEANS"));
        assertThat(changes.fit()).isInstanceOf(FieldChange.Malformed.class);
        assertThat(changes.brand()).isInstanceOf(FieldChange.Malformed.class);
        assertThat(changes.pattern()).isInstanceOf(FieldChange.Malformed.class);
        assertThat(changes.color()).isInstanceOf(FieldChange.Malformed.class);
    }

    @Test
    void anArrayReplacesTheWholeSetAndNonStringEntriesBecomeUnsupportedPositions() {
        GarmentChanges changes = changes("{\"materials\":[\"COTTON\",7,null],\"styles\":\"CASUAL\"}");

        assertThat(changes.materials()).isEqualTo(FieldChange.replace(Arrays.asList("COTTON", null, null)));
        assertThat(changes.styles()).isInstanceOf(FieldChange.Malformed.class);
        assertThat(changes("{\"styles\":[]}").styles()).isEqualTo(FieldChange.replace(java.util.List.of()));
    }

    @Test
    void priceIsReplacedAsOneObjectAndOtherShapesAreMalformed() {
        assertThat(changes("{\"price\":{\"amount\":\"0\",\"currency\":\"VND\"}}").price())
                .isEqualTo(FieldChange.replace(new PriceInput("0", "VND")));
        assertThat(changes("{\"price\":{\"amount\":\"12\"}}").price())
                .as("an incomplete pair reaches validation, which reports INCOMPLETE_PRICE")
                .isEqualTo(FieldChange.replace(new PriceInput("12", null)));
        assertThat(changes("{\"price\":\"12 VND\"}").price()).isInstanceOf(FieldChange.Malformed.class);
        assertThat(changes("{\"price\":{\"amount\":12,\"currency\":\"VND\"}}").price())
                .isInstanceOf(FieldChange.Malformed.class);
    }

    @Test
    void confirmationAndExpectedRevisionArePassedOnForTheServiceToCheck() {
        ReviseGarmentCommand explicit = command("{\"confirmed\":false,\"expectedRevision\":\"7\"}");
        ReviseGarmentCommand empty = command("{}");

        assertThat(explicit.confirmed()).isFalse();
        assertThat(explicit.expectedRevision()).isEqualTo("7");
        assertThat(explicit.ownerAccountId()).isEqualTo(OWNER);
        assertThat(explicit.garmentId()).isEqualTo("garment-id-as-text");
        assertThat(empty.confirmed()).isNull();
        assertThat(empty.expectedRevision()).isNull();
        assertThat(empty.changes()).isEqualTo(GarmentChanges.none());
    }

    private static GarmentChanges changes(String json) {
        return command(json).changes();
    }

    private static ReviseGarmentCommand command(String json) {
        return JSON.readValue(json, GarmentRevisionRequest.class).toCommand(OWNER, "garment-id-as-text");
    }
}
