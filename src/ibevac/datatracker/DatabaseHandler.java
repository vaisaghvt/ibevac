package ibevac.datatracker;

import ibevac.agent.planner.states.Safe;
import ibevac.agent.planner.states.State;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CRoom;
import ibevac.datatypes.CStaircase;

public class DatabaseHandler {

    private static DatabaseHandler instance = null;

    public static DatabaseHandler instance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }
    private Driver driver;
    private Connection connection;
    private String user = "vaisagh";
    private String password = "vaisaghviswanathan";

    private DatabaseHandler() {
        try {
            driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public synchronized void initiate(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public synchronized void connect() throws SQLException {
        Properties p = new Properties();
        p.put("user", user);
        p.put("password", password);
        connection = driver.connect("jdbc:mysql://155.69.144.103/ibevac_database", p);

        connection.setAutoCommit(true);
    }

    public synchronized void disconnect() throws SQLException {
        connection.close();
        connection = null;
    }

    public synchronized boolean isConnected() {
        return connection != null;
    }

    public synchronized int addRunInfo(int experimentId, String comment, int seed, int survived) throws Exception {
        if (!isConnected()) {
            connect();
        }

        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO run_info(experiment_id, timestamp, description, seed, numSurvived) VALUES (?,?,?,?,?)");

        int idx = 1;

        s.setInt(idx++, experimentId);
        s.setLong(idx++, System.currentTimeMillis());

        s.setString(idx++, comment);
        s.setInt(idx++, seed);
       
        s.setInt(idx++, survived);
        


        s.execute();
        s.close();

        int runid = -1;

        s = connection.prepareStatement("SELECT LAST_INSERT_ID() FROM run_info");
        ResultSet result = s.executeQuery();
        if (result.next()) {
            runid = result.getInt(1);
        }
        result.close();
        s.close();

        return runid;
    }

    public synchronized void removeRun(int runid) throws Exception {
        if (!isConnected()) {
            connect();
        }

        PreparedStatement s1 = connection.prepareStatement("DELETE FROM run_info WHERE runid = " + runid);
        PreparedStatement s2 = connection.prepareStatement("DELETE FROM agent_run_details WHERE runid = " + runid);
        PreparedStatement s3 = connection.prepareStatement("DELETE FROM agent_summary WHERE runid = " + runid);
        //PreparedStatement s4 = connection.prepareStatement("DELETE FROM agent_summary2 WHERE runid = " + runid);

        s1.execute();
        s2.execute();
        s3.execute();
        //s4.execute();

        s1.close();
        s2.close();
        s3.close();
        //s4.close();
    }

    public synchronized void addAgentSummary(int runid, int aid, int initRoomId,
            State state, int lifetime, DescriptiveStatistics speedStat, int evacStartTime, int evacuationTime) throws Exception {
        if (!isConnected()) {
            connect();
        }

        double meanSpeed = 0.0;
        double sdevSpeed = 0.0;
        if (speedStat.getN() > 2) {
            meanSpeed = speedStat.getMean();
            sdevSpeed = speedStat.getStandardDeviation();
        }
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO agent_summary(run_id, agent_id, room_id, survived, "
                + "lifetime, mean_speed, sd_speed, evac_start_time, evacuation_time) VALUES (?,?,?,?,?,?,?,?,?)");
        int idx = 1;
        s.setInt(idx++, runid);
        s.setInt(idx++, aid);
        s.setInt(idx++, initRoomId);
        if (state instanceof Safe) {
            s.setInt(idx++, 1);
        } else {
            s.setInt(idx++, 0);
        }
        s.setInt(idx++, lifetime);
        s.setDouble(idx++, meanSpeed);
        s.setDouble(idx++, sdevSpeed);
        s.setInt(idx++, evacStartTime);
        s.setInt(idx++, evacuationTime);
        
        s.execute();

    }

    public synchronized void addAgentDetails(int runid, int aid, double t, int floorId, double x, double y, double v) throws Exception {
        if (!isConnected()) {
            connect();
        }
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO agent_details(run_id, agent_id, time_step, floor_id, x, y, v) VALUES (?,?,?,?,?,?,?)");
        int idx = 1;
        s.setInt(idx++, runid);
        s.setInt(idx++, aid);
        s.setDouble(idx++, t);
        s.setInt(idx++, floorId);
        s.setDouble(idx++, x);
        s.setDouble(idx++, y);
        s.setDouble(idx++, v);
        s.execute();

    }
 
    public synchronized void writeAreaInfo(CEvacuationScenario scenario) throws Exception {
        //write area info to the database
        for (int floorId = 0; floorId < scenario.getFloors().size(); ++floorId) {
            CFloor floor = scenario.getFloors().get(floorId);

            //for each room...
            for (CRoom area : floor.getRooms()) {
                int x0 = area.getCorner0().getX();
                int y0 = area.getCorner0().getY();
                int x1 = area.getCorner1().getX();
                int y1 = area.getCorner1().getY();
                this.addAreaInfo(area.getId(), floorId, "room", x0, y0, x1, y1);
            }

            //for each staircase...
            for (CStaircase area : floor.getStaircases()) {
                int x0 = area.getCorner0().getX();
                int y0 = area.getCorner0().getY();
                int x1 = area.getCorner1().getX();
                int y1 = area.getCorner1().getY();
                this.addAreaInfo(area.getId(), floorId, "staircase", x0, y0, x1, y1);
            }

            //for each link...
            for (CLink area : floor.getLinks()) {
                int x0 = area.getCorner0().getX();
                int y0 = area.getCorner0().getY();
                int x1 = area.getCorner1().getX();
                int y1 = area.getCorner1().getY();
                this.addAreaInfo(area.getId(), floorId, "link", x0, y0, x1, y1);
            }

            //for each exit...
            for (CExit area : floor.getExits()) {
                int x0 = area.getCorner0().getX();
                int y0 = area.getCorner0().getY();
                int x1 = area.getCorner1().getX();
                int y1 = area.getCorner1().getY();
                this.addAreaInfo(area.getId(), floorId, "exit", x0, y0, x1, y1);
            }
        }
    }

    private synchronized void addAreaInfo(int areaId, int floor, String type, int x0, int y0, int x1, int y1) throws Exception {
        if (!isConnected()) {
            connect();
        }
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO area_info(area_id, floor, type, x0, y0, x1, y1) VALUES (?,?,?,?,?,?,?)");
        int idx = 1;
        s.setInt(idx++, areaId);
        s.setInt(idx++, floor);
        s.setString(idx++, type);
        s.setInt(idx++, x0);
        s.setInt(idx++, y0);
        s.setInt(idx++, x1);
        s.setInt(idx++, y1);
        s.execute();

    }

    public void checkAndAddExperiment(int expId, String comment) {
        if (!isConnected()) {
            try {
                connect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        try {
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO experiment_group(experiment_id, description) VALUES (?,?)");
            int idx = 1;
            s.setInt(idx++, expId);

            s.setString(idx++, comment);

            s.execute();
        } catch (SQLException ex) {
//            ex.printStackTrace();
        }
    }

    void updateRunInfo(int runId, int minStart,int maxStart, int completionTime) {
        try {
            if (!isConnected()) {
                connect();
            }
            PreparedStatement s1 = connection.prepareStatement(
                    "UPDATE run_info SET minStart=(?) WHERE run_id=(?)");
            s1.setInt(1, minStart);
            s1.setInt(2, runId);
            s1.execute();
            PreparedStatement s2 = connection.prepareStatement(
                    "UPDATE run_info SET maxStart=(?) WHERE run_id=(?)");
            s2.setInt(1, maxStart);
            s2.setInt(2, runId);
            s2.execute();
            PreparedStatement s3 = connection.prepareStatement(
                    "UPDATE run_info SET completionTime=(?) WHERE run_id=(?)");
            s3.setInt(1, completionTime);
            s3.setInt(2, runId);
            s3.execute();
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
