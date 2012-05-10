package ibevac.gui;

import ibevac.agent.AgentDescriptionModule.AgentType;
import ibevac.agent.IbevacAgent;
import ibevac.agent.navigation.level1motion.WaypointTracker;
import ibevac.agent.planner.states.Escaping;
import ibevac.engine.IbevacModel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;

/**
 * This class is responsible for drawing (portraying) the agent and the agent
 * related componenets for each agent.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class AgentPortrayal extends SimplePortrayal2D {

    /**
     * 
     */
    private static final long serialVersionUID = 2911263821021608067L;
    /**
     * Strictly speaking this is not the velocity. Rather it draws a straight 
     * line from the current agent location to the agent's current aim. or 
     * waypoint.
     * @see WaypointTracker
     */
    boolean showVelocity = false;
    Color agentColor;

    /**
     * Draws a circular agent and depending on the boolean flag draws the "velocity"
     * too.
     * @param object
     * @param gg
     * @param info 
     */
    @Override
    public void draw(Object object, Graphics2D gg, DrawInfo2D info) {


        IbevacAgent agent = (IbevacAgent) object;
        if (agent.getType() == AgentType.MANAGEMENT) {
            agentColor = Color.BLACK;

        }else {
            agentColor = Color.BLUE;
        }
        if (agent.isDead()) {
            gg.setColor(Color.red);
        } else if (agent.getState() instanceof Escaping) {
            gg.setColor(Color.GREEN);
        } else {
            gg.setColor(agentColor);
        }


        int offset = agent.getSpace().getOffset(agent.getCurrentFloorId());

        Point2d position = agent.getPosition();
        Vector2d velocity = agent.getVelocity();
        Point2d destination = new Point2d(velocity);
        destination.add(position);

        Point2d point1 = agent.translateToLogicalLocation(position, offset);
        int x1 = (int) (point1.x / IbevacModel.scale);
        int y1 = (int) (point1.y / IbevacModel.scale);

//		Point2d point2 = agent.translateToLogicalLocation(destination, offset);

        int r = (int) ((agent.getEffectiveDiameter() / IbevacModel.scale) / 2.0);
        gg.fillOval(x1 - r, y1 - r, 2 * r, 2 * r);
        
        

        if (showVelocity && agent.getLevel1MotionPlanning().getCurrentSpatialWaypoint() != null) {
            gg.setColor(Color.black);
            Point2d point2 = agent.getLevel1MotionPlanning().getCurrentSpatialWaypoint().getPoint();
            point2 = agent.translateToLogicalLocation(point2, offset);
            int x2 = (int) (point2.x / IbevacModel.scale);
            int y2 = (int) (point2.y / IbevacModel.scale);
//            System.out.println(x1 +","+ y1 + " to " + x2 +","+ y2);
            gg.drawLine(x1, y1, x2, y2);
        }



    }

    /**
     * This function checks whether an object should be returned when an object 
     * search is done or when drawing. It is also used to determine whether the
     * inspector should be called. The elipse is made to be slightly larger 
     * to ensure that the object is drawn /detected.
     * 
     * @param object
     * @param range
     * @return 
     */
    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
        IbevacAgent agent = (IbevacAgent) object;

        int offset = agent.getSpace().getOffset(agent.getCurrentFloorId());

        Point2d position = agent.getPosition();


        Point2d point1 = agent.translateToLogicalLocation(position, offset);
        int x1 = (int) (point1.x / IbevacModel.scale);
        int y1 = (int) (point1.y / IbevacModel.scale);

        int r = (int) ((agent.getEffectiveDiameter() / IbevacModel.scale)) + 5;

        Ellipse2D.Double ellipse = new Ellipse2D.Double(x1 - r, y1 - r, 2 * r, 2 * r);

        return (ellipse.contains(range.clip.x, range.clip.y));

    }

    /**
     * Delegates to the agent inspector the task of creating an appropriate 
     * inspector to learn more about the agent.
     * @param wrapper
     * @param state
     * @return 
     */
    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
        return new AgentInspector(super.getInspector(wrapper, state), wrapper,
                state);
    }
}
