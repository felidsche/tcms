package vehicleTest;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class VehicleTest {

    private String tcmsHost = "192.168.99.101";
    private int tcmsPort = 30742;

    private String vehicleHost = "192.168.99.101";
    private int vehiclePort = 30938;


    /**
     * Integration test to verify that the functionality to request a green light is working
     * @param vertx
     */
    @Test
    public void testEmergencyVehicleRequestGreen(Vertx vertx, VertxTestContext testContext){
        // get any id
        String id = "ewc1";
        WebClient webClient = WebClient.create(vertx);

        webClient.get(tcmsPort, tcmsHost, "/vehiclerequest?lightId=" + id)
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        testContext.completeNow();
                    });
                }));

    }

    /**
     * Integration test to verify that the functionality to request a green light is working
     * @param vertx
     */
    @Test
    public void testInvalidLightIds(Vertx vertx, VertxTestContext testContext){
        WebClient webClient = WebClient.create(vertx);

        webClient.get(vehiclePort, vehicleHost, "/requestgreen/notvalid")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(400);
                        testContext.completeNow();
                    });
                }));

    }

}
