/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.datatracker;

import ibevac.agent.IbevacAgent;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.io.Files;
import ibevac.agent.planner.states.Default;
import ibevac.agent.planner.states.Escaping;
import ibevac.agent.planner.states.Exploring;
import ibevac.agent.planner.states.Milling;
import ibevac.engine.IbevacModel;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

import sim.engine.SimState;

/**
 * @author vaisaghvt
 */
public class PEDDataTracker implements DataTracker {

    public static int experimentId = -1;
    public static String comment;
    private final IbevacModel model;
    public static final String TRACKER_TYPE = "PED";
    private final ArrayListMultimap<Integer, Integer> areaIdsForAgent;
    private int deadNumber;
    private int safeNumber;
    private int defaultNumber;
    private int exploringNumber;
    private int millingNumber;
    private int escapingNumber;
    //    private boolean runCompleted;
    public static PEDDataTracker instance = null;
    private final Collection<? extends IbevacAgent> agentList;

    public PEDDataTracker(IbevacModel model, Collection<? extends IbevacAgent> agents) {

        this.model = model;

        this.agentList = agents;
//        energySpentByAgent = ArrayListMultimap.create();
        areaIdsForAgent = ArrayListMultimap.create();


        for (IbevacAgent agent : model.getAgentList()) {
            areaIdsForAgent.put(agent.getId(), agent.getCurrentAreaId());
        }
        instance = this;
    }

    public static PEDDataTracker instance() {
        assert instance != null;
        return instance;
    }

    @Override
    public void step(SimState ss) {
        for (IbevacAgent agent : agentList) {
            //determine speed since last step
//            double t = model.schedule.getTime();


            if (agent.getPrevPosition() != null && agent.getCurrentFloorId() == agent.getPrevFloorIdx()) {
//                    double ps = this.getPreferredSpeed();

                double dx = agent.getPosition().x - agent.getPrevPosition().x;
                double dy = agent.getPosition().y - agent.getPrevPosition().y;
                double ds = 0.1 * Math.hypot(dx, dy); //unit: [m]
                //NOTE: it should actually be 0.01*Math()... However, for some reason this is off by a
                //factor of 10... something weird going on here.

                double dt = agent.getLevel0MotionPlanning().getTimestep() / 1000; //unit: [s]
                double v = ds / dt; //unit: [m/s]

                agent.getSpeedStat().addValue(v);
                //TODO : not going to be used for now. But when needed this database writing part should happen only once or osmething.
//                if (EvacConstants.DUMP_AGENT_DETAILS) {
//                    try {
//                        DatabaseHandler.instance().addAgentDetails(runid, agent.getId(),
//                                t, getCurrentFloorId(), this.getPosition().x,
//                                this.getPosition().y, v);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
            }
        }
    }

    public void addAgentNewAreaId(Integer agentId, Integer areaId) {
        assert areaIdsForAgent.containsKey(agentId);
        this.areaIdsForAgent.put(agentId, areaId);
    }

    public void storeOverallStats() {
        deadNumber = 0;
        safeNumber = 0;
        exploringNumber = 0;
        millingNumber = 0;
        defaultNumber = 0;
        escapingNumber = 0;

        for (IbevacAgent agent : model.getAgentList()) {
            if (agent.isDead()) {
                deadNumber++;

            } else if (agent.isSafe()) {
                safeNumber++;
            } else if (agent.getState() instanceof Escaping) {
                escapingNumber++;
            } else if (agent.getState() instanceof Exploring) {
                exploringNumber++;
            } else if (agent.getState() instanceof Milling) {
                millingNumber++;
            } else if (agent.getState() instanceof Default) {
                defaultNumber++;
            }


        }


    }

    public void reportOverallStats() {
        System.out.println("Number of agents:" + model.getAgentList().size()
                + ", \nNumber Dead:" + deadNumber
                + ", \nNumber Safe:" + safeNumber
                + ", \nNumber Exploring/ Milling:" + (exploringNumber + millingNumber)
                + ", \nNumber Escaping:" + escapingNumber
                + ", \nNumber doing nothing:" + defaultNumber
                + "\n**********");


    }

    @Override
    public String trackerType() {
        return TRACKER_TYPE;
    }

    @Override
    public void storeToFile() {
//        assert runCompleted = true;
//        String currentFolder = "data"
//                + File.separatorChar + this.trackerType()
//                + File.separatorChar + model.seed()
//                + File.separatorChar;
//
//        String testFile = currentFolder + "test";
//        try {
//            Files.createParentDirs(new File(testFile));
//        } catch (IOException ex) {
//            Logger.getLogger(PEDDataTracker.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//
//
//
//        writeToFileAgentNumberList(currentFolder + IbevacModel.model.seed() + "_"
//                + "AreaIds", areaIdsForAgent);
//
//        writeToFileStats(currentFolder + IbevacModel.model.seed() + "_"
//                + "Stats");
    }

    /**
     * Binary file of the following format created in specified file location:
     * <p/>
     * number of agents
     * agentId numberOfData data1 data2 ...
     *
     * @param <E>
     * @param fileName
     * @param dataForAgent
     */
    public static <E extends Number> void writeToFileAgentNumberList(String fileName, ArrayListMultimap<Integer, Integer> dataForAgent) {
        try {
            File file = new File(fileName);
            System.out.println("Creating " + fileName);

            DataOutputStream writer = null;


            writer = new DataOutputStream(new FileOutputStream(file));

            //Write byte value number of agents
            writer.writeByte(dataForAgent.keySet().size());

            for (Integer agentId : dataForAgent.keySet()) {

                writer.writeByte(dataForAgent.get(agentId).size());

                for (Integer areaId : dataForAgent.get(agentId)) {
                    writer.writeByte(areaId);
                }
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeToFileStats(String fileName) {
        try {
            File file = new File(fileName);
            System.out.println("Creating " + fileName);


            PrintWriter writer = null;
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            writer.println("Number of agents: " + this.areaIdsForAgent.keySet().size());
            writer.println("Number died: " + this.deadNumber);
            writer.println("Number safe: " + this.safeNumber);

            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int getNumberSafe() {
        return this.safeNumber;
    }

    public int getNumberDead() {
        return this.deadNumber;

    }

    @Override
    public void storeToDatabase() {
        int minStart = Integer.MAX_VALUE;
        int completionTime = 0;
        int maxStart = Integer.MIN_VALUE;
        try {
            //        assert runCompleted = true;
//            completionTime = (int) model.schedule.getSteps();

            int runId = DatabaseHandler.instance().addRunInfo(PEDDataTracker.experimentId, PEDDataTracker.comment, (int) model.seed(), safeNumber);
            for (IbevacAgent agent : this.agentList) {
                DatabaseHandler.instance().addAgentSummary(runId, agent.getId(), agent.getHomeId(),
                        agent.getState(), agent.getLifeTime(), agent.getSpeedStat(), agent.getEvacStartTime(), agent.getEvacuationTime());
                if (agent.getEvacStartTime() < minStart) {
                    minStart = agent.getEvacStartTime();
                }
                if (agent.getEvacStartTime() > maxStart) {
                    maxStart = agent.getEvacStartTime();
                }

                if (agent.getLifeTime() > completionTime) {
                    completionTime = agent.getLifeTime();
                } else if (agent.getEvacuationTime() > completionTime) {
                    completionTime = agent.getEvacuationTime();
                }

            }

            DatabaseHandler.instance().updateRunInfo(runId, minStart, maxStart, completionTime);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
