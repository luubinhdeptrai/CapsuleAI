package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.AcceptBulkImportItemCommand;
import com.capsuleai.wardrobe.application.CreateBulkImportCommand;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** US-BULK-001: request mapping without coercion, and acceptance requests that carry no photo. */
class BulkImportRequestTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final UUID OWNER = UUID.randomUUID();

    @Test
    void onlyAWholeNumberIsAnItemCount() {
        assertThat(creation("{\"itemCount\":3}")).isEqualTo(new CreateBulkImportCommand(OWNER, "key", 3, true));
        assertThat(creation("{}")).isEqualTo(new CreateBulkImportCommand(OWNER, "key", null, true));
        assertThat(creation("{\"itemCount\":null}")).isEqualTo(new CreateBulkImportCommand(OWNER, "key", null, true));
        for (String malformed : List.of("{\"itemCount\":\"3\"}", "{\"itemCount\":2.5}", "{\"itemCount\":3000000000}",
                "{\"itemCount\":[3]}")) {
            assertThat(creation(malformed)).as(malformed)
                    .isEqualTo(new CreateBulkImportCommand(OWNER, "key", null, false));
        }
    }

    @Test
    void anAcceptanceCarriesTheReviewedValuesForTheNamedItem() {
        AcceptBulkImportItemCommand command = new BulkImportItemAcceptanceRequest("2", true, "TOPS", "T_SHIRT",
                "NAVY", List.of("COTTON"), null, List.of("CASUAL"), null, null, null, null, null, "Uniqlo",
                new PriceRequest("299000", "VND")).toCommand(OWNER, "batch", "item");

        assertThat(command.batchId()).isEqualTo("batch");
        assertThat(command.itemId()).isEqualTo("item");
        assertThat(command.expectedRevision()).isEqualTo("2");
        assertThat(command.values().category()).isEqualTo("TOPS");
        assertThat(command.values().materials()).containsExactly("COTTON");
        assertThat(command.values().priceAmount()).isEqualTo("299000");
        assertThat(command.values().priceCurrency()).isEqualTo("VND");
    }

    @Test
    void aMissingPriceIsUnknownNotZero() {
        AcceptBulkImportItemCommand command = new BulkImportItemAcceptanceRequest("2", true, "TOPS", null, "NAVY",
                null, null, null, null, null, null, null, null, null, null).toCommand(OWNER, "batch", "item");

        assertThat(command.values().priceAmount()).isNull();
        assertThat(command.values().priceCurrency()).isNull();
    }

    private static CreateBulkImportCommand creation(String json) {
        JsonNode body = JSON.readTree(json);
        return new BulkImportCreationRequest(body.get("itemCount")).toCommand(OWNER, "key");
    }
}
