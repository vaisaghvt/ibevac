package ibevac.engine;

import ibevac.agent.IbevacAgent;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.XMLManager;
import ibevac.environment.IbevacSpace;
import ibevac.utilities.Utilities;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Collection;
import javax.xml.bind.JAXBException;

import sim.engine.SimState;
import abmcs.motionplanning.level0.jbox2d.JBox2DPhysicalEnvironment;
import ibevac.EvacConstants;
import ibevac.datatracker.DataTracker;
import ibevac.datatracker.DatabaseHandler;
import ibevac.datatracker.PEDDataTracker;
import ibevac.utilities.IbevacRNG;
import sim.engine.Steppable;

/**
 * The main engine of the simulation. Extends Mason's simState
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 9/Nov/2011 $
 */
public class IbevacModel extends SimState {

    /**
     * For use in checkpoints and restarting the simulation at a particular
     * point in the run.
     */
    private static final long serialVersionUID = 2982215726952655119L;
    /**
     * The xml scenario file that is used.
     */
    private static final String SCENARIO_FILE = "evac_generic_office_building.xml";
    /**
     * actual scenario file that is used. With an idea that the other one is 
     * default and this one can be set from some xml file
     */
    private String scenarioFile = SCENARIO_FILE;
    /**
     * A reference to the space.
     */
    private IbevacSpace space;
    /**
     * This is a measure of what actual distance a pixel on the screen maps to.
     * It is neecessary in the portrayal and certain calculations.
     */
    public static double scale;
    /** 
     * A reference to itself to be used by static functions like in IBEVACRNG
     */
    public static IbevacModel model = null;
    /**
     * A collection of the agents that are generated in the simuulation. This is
     * useful to keep a track of the agents and find out useful data about each 
     * agent.
     */
    private Collection<IbevacAgent> agents;
    /**
     * The number of agents that are generated randomly and passed to the 
     * initializeAgents function of IBEvacSpace
     * 
     * @see IbevacSpace#initializeAgentsRandomly(ibevac.datatypes.CEvacuationScenario, int) 
     */
    private static int numberOfAgents = 200;
    /**
     * The number of agents that are generated randomly and passed to the 
     * initializeAgents function of IBEvacSpace
     * 
     * @see IbevacSpace#initializeManagementStaffRandomly(ibevac.datatypes.CEvacuationScenario, int) 
     */
    private static int numberOfManagers = 0;

    /**
     * By default creates a model with a random seed
     */
    public IbevacModel() {
        this(System.currentTimeMillis());
    }

    /**
     * Creates a model with the seed that is passed to it
     * @param seed 
     */
    public IbevacModel(long seed) {
        super(seed);
        System.out.println("Current seed=" + this.seed());
        model = this;
        IbevacRNG.reset();
        buildSpace();
    }

    /**
     * This function starts the simulation. It first sets up the environment 
     * and stuff and then schedules all the objecta that needs to be scheduled
     */
    @Override
    public void start() {
        super.start();
        setup();
        if (this.space == null) {
            buildSpace();
        }
        System.out.println("Starting");
        space.scheduleFireSpace(this, 1, 1.0);
        space.scheduleSmokeSpace(this, 2, 10.0);
        // System.out.println(space.getAllAgents().size());
        for (IbevacAgent agent : space.getAllAgents()) {
            // System.out.println("scheduling"+agent.getId());
            agent.schedule(this, 2, 3.0);
            agent.scheduleUpdate(this, 5, 1.0);
        }


        space.schedulePhysicalEngine(this, 4);

        schedule.scheduleRepeating(new PEDDataTracker(this, agents), 6, 1.0);
        schedule.scheduleRepeating(new WrapUp(this, space.getAllAgents()), 7, 1.0);
    }

    /**
     * Function is called at the end of the run.
     */
    @Override
    public void finish() {
        System.out.println("wrapping up");
        PEDDataTracker.instance().storeOverallStats();
        PEDDataTracker.instance().reportOverallStats();
        if (EvacConstants.WRITE_DATA) {
            PEDDataTracker.instance().storeToDatabase();
        }

    }

    /**
     * Called by the start() and initialises the environment and agents and 
     * other details from the 
     */
    private void buildSpace() {
        CEvacuationScenario scenario;

        agents = null;
        try {

            File inputFile = new File(scenarioFile);
            if (inputFile.exists()) {
//               
                scenario = (CEvacuationScenario) XMLManager.instance().unmarshal(inputFile);
                scale = scenario.getScale();
//                try {
//                    DatabaseHandler.instance().writeAreaInfo(scenario);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
                Utilities.convertPixelsToMetricUnits(scenario);

                space = new IbevacSpace(new JBox2DPhysicalEnvironment(),
                        scenario, this);
//                agents = space.initializeAgentsDefault(scenario);
                agents = space.initializeAgentsRandomly(scenario, numberOfAgents);
                agents.addAll(space.initializeManagementStaffRandomly(scenario, numberOfManagers));
            } else {
                throw new FileNotFoundException();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    /**
     * Called to clear up the current state to recreate everthing for starting 
     * the simulation (both for first time and for reruns each time stop is pressed
     */
    private void setup() {
        this.space = null;
        IbevacAgent.numberOfAgents = 0;
    }

    /**
     * Get the width of the floor
     * @param floorNumber
     * @return 
     */
    public int getWidth(int floorNumber) {
        return space.getWidth(floorNumber);
    }

    /**
     * Get the height of the floor.
     * @param floorNumber
     * @return 
     */
    public int getHeight(int floorNumber) {
        return space.getHeight(floorNumber);
    }

    /**
     * A reference to Space
     * @return 
     */
    public IbevacSpace getSpace() {
        return space;
    }

    /**
     * 
     * @param args
     * 
     * @see SimState#doLoop(java.lang.Class, java.lang.String[]) 
     */
    public static void main(String[] args) {

//        doLoop(IbevacModel.class, args);
//        System.exit(0);
        System.out.println("starting");
        int numberOfTimes = 1;
        int reportTime = -1;
        long seed = System.currentTimeMillis();
        // should we load from checkpoint?

        for (int x = 0; x < args.length - 1; x++) // "-fromcheckpoint" can't be the last string
        {
            if (args[x].equals("-repeat")) {


                try {
                    numberOfTimes = Integer.parseInt(args[x + 1]);
                } catch (Exception e) {
                    System.err.println("Repeat parameter wrong. Default of 1 set.");
                    numberOfTimes = 1;
                }
            } else if (args[x].equals("-seed")) {


                try {
                    seed = Long.parseLong(args[x + 1]);
                } catch (Exception e) {
                    System.err.println("Seed parameter wrong. Default of systemTime used.");
                    seed = System.currentTimeMillis();
                }
            } else if (args[x].equals("-time")) {
                try {
                    reportTime = Integer.parseInt(args[x + 1]);
                } catch (Exception e) {
                    System.err.println("reportTime parameter wrong. Default of -1 used.");
                    reportTime = -1;
                }
            }

        }

        System.err.println("Running loop for " + numberOfTimes);

        assert numberOfTimes >= 1 && reportTime >= 0;

        runLoop(seed, reportTime, numberOfTimes);

        System.exit(0);  // make sure any threads finish up

    }

    public static void runLoop(long seed, int reportTime, int numberOfTimes) {
        for (int i = 0; i < numberOfTimes; i++) {
            model = new IbevacModel(seed);
            System.out.println("Seed :" + model.seed());
            model.start();
            seed++;
            long steps;
            do {
                if (!model.schedule.step(model)) {
                    break;
                }
                steps = model.schedule.getSteps();
                if (reportTime != -1 && steps % reportTime == 0) {
                    System.err.println("Steps: " + steps + " Time: " + model.schedule.getTime());

                    PEDDataTracker.instance().storeOverallStats();
                    PEDDataTracker.instance().reportOverallStats();

                }
            } while (true);
            model.finish();

        }
    }

    public static void runLoop(long seed, int reportTime, int NUMBER_OF_REPLICATIONS, int NUMBER_OF_AGENTS,int NUMBER_OF_MANAGEMENT, int experimentId, String experimentComment) {
        numberOfAgents = NUMBER_OF_AGENTS;
        numberOfManagers = NUMBER_OF_MANAGEMENT;
        PEDDataTracker.experimentId = experimentId;
        PEDDataTracker.comment = experimentComment;
        runLoop(seed, reportTime, NUMBER_OF_REPLICATIONS);
    }

    public Collection<IbevacAgent> getAgentList() {
        return agents;
    }
}
