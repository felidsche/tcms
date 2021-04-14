package lightsTest;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.*;
import io.vertx.junit5.VertxExtension;
import lights.Lights;
import org.junit.jupiter.api.extension.ExtendWith;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class LightsTest {


    /**
     * Tests that /state can be queried and that all lights are contained
     * @param vertx
     * @param testContext
     */
    @Test
    @DisplayName("State Query Test")
    void queryState(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);

        webClient.get(31969, "192.168.99.101", "/state")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body()).contains("Light_ID: NSC1");
                        assertThat(resp.body()).contains("State:");
                        assertThat(
                            resp.body().contains("RED") ||
                                resp.body().contains("GREEN") ||
                                resp.body().contains("YELLOW") ||
                                resp.body().contains("EMERGENCY")
                        ).isTrue();
                        testContext.completeNow();
                    });
                }));
    }

    /**
     * Tests that /state/green sets light
     * @param vertx
     * @param testContext
     */
    @Test
    @DisplayName("sets state to green")
    void setStateGreen(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);

        webClient.post(31969, "192.168.99.101", "/state/green")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode() == 200 || resp.statusCode() == 202).isTrue();
                        assertThat(resp.body()).contains("green");
                        testContext.completeNow();
                    });
                }));
    }

    /**
     * Tests that /state/red sets light
     * @param vertx
     * @param testContext
     */
    @Test
    @DisplayName("sets state to red")
    void setStateRed(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);

        webClient.post(31969, "192.168.99.101", "/state/red")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode() == 200 || resp.statusCode() == 202).isTrue();
                        assertThat(resp.body()).contains("red");
                        testContext.completeNow();
                    });
                }));
    }
    /**
     * Tests that /state/emergency sets light
     * @param vertx
     * @param testContext
     */
    @Test
    @DisplayName("sets state to emergency")
    void setStateEmergency(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);

        webClient.post(31969, "192.168.99.101", "/state/emergency")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode() == 200 || resp.statusCode() == 202).isTrue();
                        assertThat(resp.body()).contains("emergency");
                        testContext.completeNow();
                    });
                }));
    }
}
