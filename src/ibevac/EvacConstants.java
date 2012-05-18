package ibevac;

/**
 * <h4>Constants that are used by the simulation.</h4>
 * <p/>
 * <h4>Source: http://en.wikipedia.org/wiki/Personal_space </h4>
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public final class EvacConstants {

    /**
     * Personal space refers to the region within which people start feeling
     * uncomfortable
     * Unit : cm
     */
    public static final double PERSONAL_SPACE_RADIUS = 45; //unit: [cm]

    /**
     * Social space is the region in which agents communicate with other agents
     * Unit : cm
     */
    public static final double PERSONAL_SOCIAL_RADIUS = 120; //unit: [cm]

    /**
     * Used to generate agent's preferred speed.
     * Unit : m/sec
     */
    public static final double AGENT_MIN_SPEED = 1.0; //1.0 m/s = 3.6 km/h

    /**
     * Used to generate agent's preferred speed.
     * Unit : m/sec
     */
    public static final double AGENT_MAX_SPEED = 3.5; //3.5 m/s = 12.6 km/h

    /**
     * Used to generate agent's preferred speed.
     * Unit : m/sec
     */
    public static final double AGENT_AVG_SPEED = 2.0; //2.0 m/s = 7.2 km/h

    /**
     * Used to generate agent's preferred speed.
     * Unit : m/sec
     */
    public static final double AGENT_STDDEV_SPEED = 0.2; //2.0 m/s = 7.2 km/h

    /**
     * Used to generate agent's mass. Used by the physics engine.
     * Unit : kg
     */
    public static final double AGENT_MIN_MASS = 50;

    /**
     * Used to generate agent's mass. Used by the physics engine.
     * Unit : kg
     */
    public static final double AGENT_MAX_MASS = 120;

    /**
     * Used to generate agent's mass. Used by the physics engine.
     * Unit : kg
     */
    public static final double AGENT_AVG_MASS = 75;

    /**
     * Used to generate agent's mass. Used by the physics engine.
     * Unit : kg
     */
    public static final double AGENT_STDDEV_MASS = 10;

    /**
     * Used to generate agent size.
     * Unit : cm
     */
    public static final double AGENT_MIN_DIAMETER = 40;

    /**
     * Used to generate agent size.
     * Unit : cm
     */
    public static final double AGENT_MAX_DIAMETER = 80;

    /**
     * Sensor range for the agent.
     * Unit : multiplied with the agent's diameter;
     */
    public static final int SENSOR_RANGE = 5;

    /**
     * The maximum sensor range possible for any agent
     * Unit : cm
     * Note : This is the product of sensor range and agent diameter
     */
    public static final double MAX_SENSOR_RANGE = SENSOR_RANGE * AGENT_MAX_DIAMETER;
    public static boolean DUMP_AGENT_DETAILS = false;
    public static final boolean WRITE_DATA = true;


    private EvacConstants() {
    }
}
