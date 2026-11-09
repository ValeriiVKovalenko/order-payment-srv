package com.valerii.order.enums;

import com.valerii.order.exception.InvalidOrderUpdateStatusException;
import com.valerii.order.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void canTransitionTo_shouldTransitionStatusBeSuccessful() {
        //CREATE
        OrderStatus oldCreateStatus = OrderStatus.CREATED;
        OrderStatus newCreateStatus1 = OrderStatus.PROCESSING;
        assertThat(OrderStatus.canTransitionTo(oldCreateStatus, newCreateStatus1)).isTrue();

        OrderStatus newCreateStatus2 = OrderStatus.CANCELLED;
        assertThat(OrderStatus.canTransitionTo(oldCreateStatus, newCreateStatus2)).isTrue();

        //PROCESSING
        OrderStatus oldProcessingStatus = OrderStatus.PROCESSING;
        OrderStatus newProcessingStatus1 = OrderStatus.COMPLETED;
        assertThat(OrderStatus.canTransitionTo(oldProcessingStatus, newProcessingStatus1)).isTrue();

        OrderStatus newProcessingStatus2 = OrderStatus.CANCELLED;
        assertThat(OrderStatus.canTransitionTo(oldProcessingStatus, newProcessingStatus2)).isTrue();
    }

    @Test
    void canTransitionTo_shouldReturnException() {
        //CREATE
        OrderStatus oldCreateStatus = OrderStatus.CREATED;
        OrderStatus newCreateStatus1 = OrderStatus.COMPLETED;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCreateStatus, newCreateStatus1))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        //PROCESSING
        OrderStatus oldProcessingStatus = OrderStatus.PROCESSING;
        OrderStatus newProcessingStatus1 = OrderStatus.CREATED;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldProcessingStatus, newProcessingStatus1))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        //COMPLETED
        OrderStatus oldCompletedStatus = OrderStatus.COMPLETED;
        OrderStatus newCreatedStatus1 = OrderStatus.CREATED;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCompletedStatus, newCreatedStatus1))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        OrderStatus newCreatedStatus2 = OrderStatus.PROCESSING;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCompletedStatus, newCreatedStatus2))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        OrderStatus newCreatedStatus3 = OrderStatus.CANCELLED;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCompletedStatus, newCreatedStatus3))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        //CANCELLED
        OrderStatus oldCancelledStatus = OrderStatus.CANCELLED;
        OrderStatus newCancelledStatus1 = OrderStatus.CREATED;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCancelledStatus, newCancelledStatus1))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        OrderStatus newCancelledStatus2 = OrderStatus.PROCESSING;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCompletedStatus, newCancelledStatus2))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);

        OrderStatus newCancelledStatus3 = OrderStatus.COMPLETED;
        assertThatThrownBy(() -> OrderStatus.canTransitionTo(oldCompletedStatus, newCancelledStatus3))
                .isInstanceOf(InvalidOrderUpdateStatusException.class);
    }
}
