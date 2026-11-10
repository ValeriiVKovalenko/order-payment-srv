package com.valerii.order.enums;

import com.valerii.order.exception.InvalidOrderUpdateStatusException;
import com.valerii.order.model.enums.OrderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

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

    @ParameterizedTest
    @CsvSource({
            "CREATED, PROCESSING",
            "CREATED, CANCELLED",
            "PROCESSING, COMPLETED",
            "PROCESSING, CANCELLED"
    })
    void canTransitionTo_shouldTransitionStatusBeSuccessful(OrderStatus from, OrderStatus to) {
        assertThatCode(() -> OrderStatus.validateTransitionTo(from, to))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, COMPLETED",
            "PROCESSING, CREATED",
            "COMPLETED, CREATED",
            "COMPLETED, PROCESSING",
            "COMPLETED, CANCELLED",
            "CANCELLED, CREATED",
            "CANCELLED, PROCESSING",
            "CANCELLED, COMPLETED"
    })
    void canTransitionTo_shouldReturnException(OrderStatus from, OrderStatus to) {
        assertThatThrownBy(() -> OrderStatus.validateTransitionTo(from, to))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);
    }
}
