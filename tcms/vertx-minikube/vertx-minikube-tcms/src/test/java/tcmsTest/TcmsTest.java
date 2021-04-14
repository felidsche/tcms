package tcmsTest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.*;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TcmsTest {

    private String tcmsHost = "192.168.99.101";
    private int tcmsPort = 30742;

    private String lightsHost = "192.168.99.101";
    private int lightsPort = 31969;

    private JWTAuth authProvider;


    // {"sub":"Paulo","exp":1747055313,"iat":1431695313,"permissions":["read","write","execute"],"roles":["admin","developer","user"]}
    private static final String JWT_VALID = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJQYXVsbyIsImV4cCI6MTc0NzA1NTMxMywiaWF0IjoxNDMxNjk1MzEzLCJwZXJtaXNzaW9ucyI6WyJyZWFkIiwid3JpdGUiLCJleGVjdXRlIl0sInJvbGVzIjpbImFkbWluIiwiZGV2ZWxvcGVyIiwidXNlciJdfQ.pzKccAGaKJAw_EqJMHvpzt9NVK6dC3cQMqQD1jPqoiM";

    /**
     * Initializes Key setup
     */
    private JWTAuthOptions getConfig() {
        return new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("keystore.jceks")
                        .setType("jceks")
                        .setPassword("secret"));
    }

    /**
     * Test, if the root can be reached
     *
     * @param vertx
     * @param testContext
     */
    @Test
    @DisplayName("#1 Check if service is up and running")
    void testRootRoute(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);

        webClient.get(tcmsPort, tcmsHost, "/")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body().contains("Hello"));
                        testContext.completeNow();
                    });
                }));
    }

    /**
     * Test, if the state can be queried
     *
     * @param vertx
     * @param testContext
     */
    @Test
    @DisplayName("#2 State Query Test")
    void queryState(Vertx vertx, VertxTestContext testContext) {
        WebClient webClient = WebClient.create(vertx);

        webClient.get(tcmsPort, tcmsHost, "/state")
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    System.out.println(resp);
                    testContext.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body()).contains("lights-nsc1");
                        assertThat(resp.body()).contains("lights-nsc2");
                        assertThat(resp.body()).contains("lights-nsp1");
                        assertThat(resp.body()).contains("lights-nsp2");
                        assertThat(resp.body()).contains("lights-snc1");
                        assertThat(resp.body()).contains("lights-snc2");
                        assertThat(resp.body()).contains("lights-snp1");
                        assertThat(resp.body()).contains("lights-snp2");
                        assertThat(resp.body()).contains("lights-ewc1");
                        assertThat(resp.body()).contains("lights-ewc2");
                        assertThat(resp.body()).contains("lights-ewp1");
                        assertThat(resp.body()).contains("lights-ewp2");
                        assertThat(resp.body()).contains("lights-wec1");
                        assertThat(resp.body()).contains("lights-wec2");
                        assertThat(resp.body()).contains("lights-wep1");
                        assertThat(resp.body()).contains("lights-wep2");
                        testContext.completeNow();
                    });
                }));
    }

    /**
     * Test, if the token can be generated
     * Tcms should be refactored so that we can call the actual Tcms.generateToken()-Method not a replica.
     *
     * @param vertx
     */
    @Test
    public void testGenerateNewToken(Vertx vertx) {

        JsonObject payload = new JsonObject()
                .put("sub", "Paulo")
                .put("exp", 1747055313)
                .put("iat", 1431695313)
                .put("permissions", new JsonArray()
                        .add("read")
                        .add("write")
                        .add("execute"))
                .put("roles", new JsonArray()
                        .add("admin")
                        .add("developer")
                        .add("user"));
        authProvider = JWTAuth.create(vertx, getConfig());
        String token = authProvider.generateToken(payload, new JWTOptions().setSubject("Paulo"));
        assertNotNull(token);
        assertEquals(JWT_VALID, token);
    }

    /**
     * Integration test to try to access Lights from outside the cluster
     *
     * @param vertx
     */
    @Test
    public void testLightsServiceDiscoryFromOutside(Vertx vertx) {
        String serviceName = "lights-ewc1";
        ServiceDiscovery.create(vertx, discovery ->
                // Retrieve a web client
                HttpEndpoint.getWebClient(discovery, svc -> svc.getName().equals(serviceName), ar -> {
                    assertTrue(ar.failed());
                })

        );
    }

    /**
     * sets the state to emergency and verifies that it worked
     * @param vertx
     */
    @Test
    public void testTCMSEmployeeEmergencyMode(Vertx vertx, VertxTestContext testContext) {
        String desiredState = "emergency";

        // set state to emergency
        WebClient webClient = WebClient.create(vertx);

        webClient.post(lightsPort, lightsHost, "/state/" + desiredState)
                .as(BodyCodec.string())
                .send(testContext.succeeding(resp -> {
                    testContext.verify(() -> {
                        assertTrue(resp.statusCode() == 200 || resp.statusCode() == 202);
                        System.out.println(resp.statusCode());
                        testContext.completeNow();
                    });
                }));

    }

}
