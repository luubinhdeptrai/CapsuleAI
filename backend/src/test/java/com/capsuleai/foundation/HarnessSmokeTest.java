package com.capsuleai.foundation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HarnessSmokeTest {

    interface LabelPort {
        String load();
    }

    record LabelReader(LabelPort port) {

        String read() {
            return "CapsuleAI:" + port.load();
        }
    }

    @Test
    void readsThroughAFakePort() {

        LabelReader reader =
                new LabelReader(() -> "local");

        String result = reader.read();

        assertThat(result)
                .isEqualTo("CapsuleAI:local");
    }

    @Test
    void readsThroughAMockedPort() {

        LabelPort port = mock(LabelPort.class);

        when(port.load()).thenReturn("ci");

        LabelReader reader = new LabelReader(port);

        assertThat(reader.read())
                .isEqualTo("CapsuleAI:ci");

        verify(port).load();
    }
}