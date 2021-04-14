package tcms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tcms extends AbstractVerticle {

    private int SWITCH_INTERVAL = 20;
    private int GREEN_LIGHT_DURATION = 10;

    private HashMap<String, String> serviceStates = new HashMap<>();
    private Boolean hasAnyErrors = false;
    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            switchLights(0);
        }
    };
    private String token ="";
    private final String username = System.getenv("TCMS_SECRET_USERNAME");
    private final String password = System.getenv("TCMS_SECRET_PASSWORD");

    Future<?> future;

    // these light pairs should always have the same state
    // switch would be: lightStatePairs[0].each { light.turnRed() }; lightStatePairs[1].each { light.turnGreen() }
    // assuming no errors
    private String[][] lightStatePairs = {
            { "lights-nsc1", "lights-nsc2", "lights-nsp1", "lights-nsp2",
            "lights-snc1", "lights-snc2", "lights-snp1", "lights-snp2" },
            { "lights-ewc1", "lights-ewc2", "lights-ewp1", "lights-ewp2",
            "lights-wec1", "lights-wec2", "lights-wep1", "lights-wep2" }
    };

    private int activeLightGroup = -1;

    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Tcms.class.getName());
    }

    @Override
    public void start() {
        initServiceStates();
        initInstructionSchedule(exec);
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

        router.get("/").handler(rc -> rc.response().end("Hello from the TCMS!"));
        router.get("/state").handler(this::state);
        router.get("/emergency").handler(this::emergency);
        router.get("/emergency_end").handler(this::endEmergency);
        router.get("/vehiclerequest").handler(this::processGreenLightRequest);

        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private void state(RoutingContext rc) {
        rc.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(serviceStates));
    }

    /**
     * starts a schedule that switches lights periodically
     */
    private void initInstructionSchedule(ScheduledExecutorService exec) {
        LOGGER.info("initiating instruction loop");

        future = exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                switchLights(0);
            }
        }, 0, SWITCH_INTERVAL, TimeUnit.SECONDS);
    }
    public static String get() {
        return "Hello JUnit 5";
    }

    /**
     * switches light groups
     * if lights haven't been initialized first set them to yellow blinking
     */
    private void switchLights(int errorCount) {
        if(errorCount < 5) {
            try {
                if (activeLightGroup == -1) {
                    resetLights();
                } else {
                    switchLights(activeLightGroup, "red");
                    switchLights((activeLightGroup + 1) % 2, "green");
                    activeLightGroup = (activeLightGroup + 1) % 2;
                }
            } catch (LightsInstructionException exc) {
                LOGGER.info("Couldn't transition lights, retrying...");
                switchLights(++errorCount);
            }
        } else {
            // if more than 5 errors occurred, trying one final time to set lights to emergency state
            try {
                resetLights();
            } catch (LightsInstructionException exc) {
                LOGGER.info("Couldn't transition lights :(");
            }
        }
    }

    /**
     * reset all lights to emergency
     * @throws LightsInstructionException if at least one light failed transitioning
     */
    private void resetLights() throws LightsInstructionException {
        switchLights(0, "emergency");
        switchLights(1, "emergency");
        activeLightGroup = 1;
    }

    /**
     * switches a specific light group to a state
     * @param groupId group to switch
     * @param desiredState state to switch to; must map to RESTful route name in lights services
     * @throws LightsInstructionException if at least one light failed transitioning
     */
    private void switchLights(int groupId, String desiredState) throws LightsInstructionException {
        hasAnyErrors = false;
        LOGGER.info("switching group " + groupId + " to " + desiredState);
        for(String serviceName : lightStatePairs[groupId]) {
            LOGGER.info("switching light " + serviceName + " to " + desiredState);
            ServiceDiscovery.create(vertx, discovery ->
                    // Retrieve a web client
                    HttpEndpoint.getWebClient(discovery, svc -> svc.getName().equals(serviceName), ar -> {
                        if (ar.failed()) {
                            LOGGER.info("service unavailable: " + serviceName);
                        } else {
                            if (token != "") {
                                ar.result().post("/state/" + desiredState).putHeader("Authorization", "Bearer " + token).as(BodyCodec.string()).send(request -> {
                                    if (request.failed()) {
                                        LOGGER.info("request failed: " + serviceName + ": " + request.cause().getMessage());
                                        hasAnyErrors = true;
                                    } else {
                                        // 200 means light was already in desired state; 202 means it successfully changed to desired state
                                        if (request.result().statusCode() != 202 && request.result().statusCode() != 200) {
                                            LOGGER.info("response_code for " + serviceName + " != 202: " + request.result().statusMessage());
                                            hasAnyErrors = true;
                                        } else {
                                            LOGGER.info("response for " + serviceName + ": " + request.result().body());
                                            serviceStates.put(serviceName, request.result().body());
                                        }
                                    }
                                });
                            } else {
                                LOGGER.warning("Lights could not be interacted with since the TCMS is not authenticated.");
                            }

                        }
                    })
            );
        }
        if(hasAnyErrors) {
            throw new LightsInstructionException("Not all Lights switched correctly");
        }
    }

    private void emergency(RoutingContext rc) {
        try {
            //cancel instead of shutdown
            future.cancel(true);

            // reset
            resetLights();
        } catch (LightsInstructionException ex){
            LOGGER.info("reset failed");
            LOGGER.log(Level.INFO, ex.getMessage());
        }
        finally{
            rc.request().response().setStatusCode(200).end("request processed");
        }
    }

    private void endEmergency(RoutingContext rc) {
        future.cancel(true);
        future = exec.scheduleAtFixedRate(runnable, 0, SWITCH_INTERVAL, TimeUnit.SECONDS);
    }

    private void processGreenLightRequest(RoutingContext rc){

        String id = rc.request().getParam("lightId");

        try {
            //cancel instead of shutdown
            future.cancel(true);

            //switch
            int group = id.contains("nsc") || id.contains("snc") ? 0 : 1;
            LOGGER.log(Level.INFO, "SLX switching :" + group + " to green because of " + id);
            switchLights(group, "green");

            group = id.contains("nsc") || id.contains("snc") ? 1 : 0;
            LOGGER.log(Level.INFO, "SLX switching :" + group + " to red because of " + id);
            switchLights(group, "red");

            //restart
            future = exec.scheduleAtFixedRate(runnable, GREEN_LIGHT_DURATION, SWITCH_INTERVAL, TimeUnit.SECONDS);


        } catch (LightsInstructionException ex){
            LOGGER.info("Processing light was not valid");
            LOGGER.log(Level.INFO, ex.getMessage());
        }
        finally{
            rc.request().response().setStatusCode(200).end("request processed");
        }

    }

    private void initServiceStates() {
        serviceStates.put("lights-ewc1", "unknown");
        serviceStates.put("lights-ewc2", "unknown");
        serviceStates.put("lights-ewp1", "unknown");
        serviceStates.put("lights-ewp2", "unknown");
        serviceStates.put("lights-nsc1", "unknown");
        serviceStates.put("lights-nsc2", "unknown");
        serviceStates.put("lights-nsp1", "unknown");
        serviceStates.put("lights-nsp2", "unknown");
        serviceStates.put("lights-snc1", "unknown");
        serviceStates.put("lights-snc2", "unknown");
        serviceStates.put("lights-snp1", "unknown");
        serviceStates.put("lights-snp2", "unknown");
        serviceStates.put("lights-wec1", "unknown");
        serviceStates.put("lights-wec2", "unknown");
        serviceStates.put("lights-wep1", "unknown");
        serviceStates.put("lights-wep2", "unknown");
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

