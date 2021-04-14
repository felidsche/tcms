package lights;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * class with methods that are used to manage the state of the traffic lights
 */
public class StateManager {

    private final static Logger LOGGER = Logger.getLogger("StateManager");
    private final static SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss z");
    private static Date date = new Date(System.currentTimeMillis());

    private static int SWITCH_TIME = 2000; // ms
    private static LightState currentState = LightState.EMERGENCY;

    /**
     * Method to change the state
     * @param state
     * @return valid
     */
    public static Boolean setState(LightState state) {

        boolean valid = false;

        if(state == LightState.RED && currentState != LightState.RED) {
            switchWithTransition(state);
            valid = true;
            return valid;
        } else if(state == LightState.GREEN && currentState != LightState.GREEN) {
            switchWithTransition(state);
            valid = true;
            return valid;
        } else if (state == LightState.EMERGENCY) {
            currentState = state;
            return true;
        }
        return valid;
    }

    /**
     * Method to switch the traffic light mocking a natural behaviour
     * @param state
     * @throws InterruptedException
     */
    public static void switchWithTransition(LightState state) {
        currentState = state;
        /*
        Thread thread = new Thread(){
            public void run(){
                LOGGER.info("Thread Running");
                try{
                    currentState = LightState.YELLOW;
                    Thread.sleep(SWITCH_TIME);
                    currentState = state;
                } catch(InterruptedException e) {
                    LOGGER.info("Service: StateManager" + "|" + "The following exception occured: " + e.getMessage() + "|" + "at time:" + formatter.format(date));
                    Thread.currentThread().interrupt();
                    LOGGER.info("light switch thread was interrupted");
                }
            }
        };

        thread.start();
        */
    }

    /**
     * Method to receive the current state
     * @return currentState
     */
    public static LightState getState() {
        return currentState;
    }
}
