package lights;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.auth.*;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Logger;

public class Lights extends AbstractVerticle {

    private static String ID = System.getenv("ID");
    private final Logger LOGGER = Logger.getLogger("StateManager");
    private JWTAuth authProvider;
    private boolean authenticated = false;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Lights.class.getName());
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);
        initEndpoints(router);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080);
    }

    /*
     * sets routes for endpoints that will be talked to by the StateManager
     */
    private void initEndpoints(Router router) {
        router.get("/").handler(rc -> rc.response().end("Light_ID: " + ID));

        router.get("/state").handler(rc -> rc.response().end("Light_ID: " + ID + ", State: " + StateManager.getState()));

        router.post("/state/green").handler(rc -> this.changeState(rc, LightState.GREEN));
        router.post("/state/red").handler(rc -> this.changeState(rc, LightState.RED));
        router.post("/state/emergency").handler(rc -> this.changeState(rc, LightState.EMERGENCY));
    }

    private void changeState(RoutingContext rc, LightState color) {
        if(StateManager.getState().equals(color)) {
            rc.response().setStatusCode(200).end(color.toString().toLowerCase());
        } else if(StateManager.setState(color)) {
            rc.response().setStatusCode(202).end(color.toString().toLowerCase());
        } else {
            rc.response().setStatusCode(403).end("NOT turning light " + ID + " " + color.toString().toLowerCase() + " ILLEGAL_STATE_TRANSITION");
        }
    }

}
