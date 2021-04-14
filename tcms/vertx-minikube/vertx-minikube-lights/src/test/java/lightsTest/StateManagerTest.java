package lightsTest;

import io.vertx.junit5.VertxExtension;
import lights.LightState;
import lights.StateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

public class StateManagerTest {

    @BeforeEach
    void resetStateManager() {
        StateManager.setState(LightState.EMERGENCY);
    }

    /**
     * StateManager should be initialized with EMERGENCY
     */
    @Test
    @DisplayName("test getState")
    void testGetState() {
        assertThat(StateManager.getState()).isEqualTo(LightState.EMERGENCY);
    }

    /**
     * StateManager allows for setting state
     */
    @Test
    @DisplayName("test setState")
    void testSetState() {
        assertThat(StateManager.getState()).isEqualTo(LightState.EMERGENCY);
        assertThat(StateManager.setState(LightState.GREEN)).isTrue();
        assertThat(StateManager.getState()).isEqualTo(LightState.GREEN);
    }

    /**
     * StateManager disallows invalid state transition
     */
    @Test
    @DisplayName("test setState")
    void testSetStateFailure() {
        assertThat(StateManager.getState()).isEqualTo(LightState.EMERGENCY);
        assertThat(StateManager.setState(LightState.GREEN)).isTrue();
        assertThat(StateManager.getState()).isEqualTo(LightState.GREEN);
        assertThat(StateManager.setState(LightState.GREEN)).isFalse();
    }
}
