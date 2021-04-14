package vehicle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Vehicle extends AbstractVerticle {

    private String token ="";
    private final String username = System.getenv("VEHICLE_SECRET_USERNAME");
    private final String password = System.getenv("VEHICLE_SECRET_PASSWORD");
    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    private ArrayList<String> lightIDs = new ArrayList<>(Arrays.asList(
            "nsc1", "nsc2", "snc1", "snc2", "ewc1", "ewc2", "wec1", "wec2"
    ));

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Vehicle.class.getName());
    }

    @Override
    public void start() {

        JWTAuth authProvider = initKeyStore();
        try {
            token = generateToken(authProvider, username, password);
        } catch (NullPointerException e) {

            LOGGER.warning("Token could not be generated" + e.getMessage());
        }

        Router router = Router.router(vertx);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("X-PINGARUNER");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

        router.get("/").handler(this::hello);
        router.get("/requestgreen/:lightId").handler(this::requestGreenLight);

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private void hello(RoutingContext rc) {
        rc.response().end("Vehicle Service");
    }

    // TODO change service discovery with absolute external url?
    private void requestGreenLight(RoutingContext rc) {
        String id = rc.request().getParam("lightId");
        if (!lightIDs.contains(id)) {
            rc.response().setStatusCode(400).end("not a valid light");
        } else {
            ServiceDiscovery.create(vertx, serviceDiscovery ->
                    HttpEndpoint.getWebClient(serviceDiscovery, service -> service.getName().equals("lights-tcms"), ar -> {
                                if (ar.failed()) {
                                    // TODO exception handling
                                    LOGGER.info("tcms service unavailable");
                                    rc.response().setStatusCode(500).end("tcms unavailable");
                                } else {
                                    ar.result().get("/vehiclerequest?lightId=" + id).as(BodyCodec.string()).send(request -> {
                                        if (request.failed()) {
                                            LOGGER.info("request failed: " + request.cause().getMessage());
                                            rc.response().setStatusCode(500).end("request failed");
                                        } else {
                                            if (request.result().statusCode() != 200) {
                                                LOGGER.info("response_code from tcms " + "!= 200: " + request.result().statusMessage());
                                                rc.response().setStatusCode(request.result().statusCode()).end(request.result().statusMessage());
                                            } else {
                                                LOGGER.info("response: " + request.result().body());
                                                rc.response().setStatusCode(200).end(request.result().body());
                                            }
                                        }
                                    });
                                }
                            }
                    ));
        }
    }

    /*
     * Mehod to setup the Keystore to use Algorithms
     */
    private JWTAuth initKeyStore() {

        JWTAuthOptions config = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()

                        .setPath("/deployment/keystore.jceks")
                        .setPassword("secret"));

        JWTAuth authProvider = JWTAuth.create(vertx, config);

        return authProvider;
    }

    /*
     * Issues JWT if correct username and passwords are passed
     */
    private String generateToken(JWTAuth authProvider, String username, String password) {
        String token = "";
        if (this.username.equals(username) && this.password.equals(password)) {
            token = authProvider.generateToken(new JsonObject().put("sub", this.username), new JWTOptions());
            // now for any request to protected resources you should pass this string in the HTTP header Authorization as:
            // Authorization: Bearer <token>

        } else {
            LOGGER.warning("Token could not be generated");

        }
        return token;
    }
}


