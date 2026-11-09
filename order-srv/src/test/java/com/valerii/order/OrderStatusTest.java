package com.valerii.order;

import com.valerii.order.model.enums.OrderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest
    @ValueSource(strings = {"CREATED", "created", "Created", "cReAtEd", " Created "})
    void fromString_shouldReturnCreated_regardlessOfCase(String input) {
        assertThat(OrderStatus.fromString(input)).contains(OrderStatus.CREATED);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void fromString_shouldResolveEveryStatus_byNameInAnyCase(OrderStatus status) {
        assertThat(OrderStatus.fromString(status.name())).contains(status);
        assertThat(OrderStatus.fromString(status.name().toLowerCase())).contains(status);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN", "", " ", "CREATED1", "NEW"})
    void fromString_shouldReturnEmpty_whenStatusIsInvalid(String input) {
        assertThat(OrderStatus.fromString(input)).isEmpty();
    }
}
