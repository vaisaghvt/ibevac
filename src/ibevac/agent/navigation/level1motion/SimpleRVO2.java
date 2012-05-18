package ibevac.agent.navigation.level1motion;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import abmcs.agent.MovingAgent;
import abmcs.agent.PhysicalAgent;
import abmcs.motionplanning.level1.Level1MotionPlanning;
import abmcs.motionplanning.level2.SpatialWaypoint;
import ibevac.agent.IbevacAgent;
import ibevac.engine.IbevacModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import utilities.Geometry;

/**
 * <h4> A simpler implementation of RVO2 for Level1 motion planning. It does
 * collision avoidance based on dynamic obstacles only. Static obstacles are
 * not even considered. Collisions are still avoided because of the physics engine
 * below. However, the prefered velocity chosen might sometimes be very impractical
 * </h4>
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class SimpleRVO2 extends Level1MotionPlanning {

    private final MovingAgent me;
    private WaypointTracker tracker;
    private SpatialWaypoint waypoint;
    /**
     * Stores the orcalines for calculation
     */
    final List<Line> orcaLines;
    /**
     * TIME_HORIZON 	float (time) 	The minimal amount of time for which the
     * agent's velocities that are computed by the simulation are safe with
     * respect to other agents. The larger this number, the sooner this agent
     * will respond to the presence of other agents, but the less freedom the
     * agent has in choosing its velocities. Must be positive.
     */
    public static final double TIME_HORIZON = 2.0;
    public boolean goalReached = false;

    public SimpleRVO2(MovingAgent agent) {
        this.me = agent;
        orcaLines = new ArrayList<Line>();
    }

    /**
     * Instructs the higher level path planners to recalculate a path to exit.
     */
    @Override
    public void reset() {
        if (tracker != null && tracker.reachedGoal()) {
            this.goalReached = true;
        }
        tracker = null;

    }

    /**
     * Get's the current spatial waypoint the agent is trying to reach.
     *
     * @return
     */
    @Override
    public SpatialWaypoint getCurrentSpatialWaypoint() {
        return waypoint;
    }

    /**
     * Uses RVO2 algorithm to calculate a velocity that will avoid collisions for
     * the next few time steps (based on specified parameters). This uses as input
     * the set of perceived dynamic obstacles and a current location
     * (spatial waypoint) that the agent is trying to reach and it calculates
     * the agent's preferred velocity.
     *
     * @return Vector representation of preferred velocity
     * @see WaypointTracker#WaypointTracker(abmcs.agent.MovingAgent, java.util.List)
     * @see WaypointTracker#getWaypoint()
     */
    @Override
    public Vector2d getPreferredVelocity() {
        //determine our immediate waypoint
        if (goalReached) {
            return new Vector2d(0, 0);
        }
        if (tracker == null) {
            List<SpatialWaypoint> waypoints = me.getLevel2MotionPlanning().getSpatialWaypoints();
            if (waypoints == null) {
                return new Vector2d(0, 0);
            } else {
                tracker = new WaypointTracker(me, waypoints);
            }
        }

        waypoint = tracker.getWaypoint();

        if (waypoint == null) {
            this.reset();
            return new Vector2d(0, 0);
        }


        Vector2d preferredVelocity = new Vector2d(waypoint.getPoint());
        preferredVelocity.sub(me.getPosition());

        preferredVelocity.normalize();
        preferredVelocity.scale(me.getPreferredSpeed());


        Set<? extends PhysicalAgent> neighbors = me.getPerceivedDynamicObstacles();


        orcaLines.clear();
        if (preferredVelocity.length() == 0) {
            System.out.println("fuck yuo");
        }
//        if (((IbevacAgent) me).getId() == 1) {
//            System.out.println(preferredVelocity);
//        }
        Vector2d newVelocity = new Vector2d(preferredVelocity);


        final double invTimeHorizon = 1.0f / TIME_HORIZON;

        /* Create agent ORCA lines. */
        for (PhysicalAgent otherAgent : neighbors) {

            if (otherAgent.equals(me)) {
                continue;
            }

            Vector2d relativePosition = new Vector2d(otherAgent.getPosition());
            relativePosition.sub(me.getPosition());


            Vector2d relativeVelocity = new Vector2d(me.getVelocity());
            relativeVelocity.sub(otherAgent.getVelocity());

            double distSq = relativePosition.dot(relativePosition);
            double combinedRadius = me.getDiameter() / 2.0 + otherAgent.getDiameter() / 2.0;

            double combinedRadiusSq = Math.pow(combinedRadius, 2.0f);

            Line line = new Line();
            Vector2d u;

            if (distSq > combinedRadiusSq) {
                /* No collision. */
                Vector2d w = new Vector2d(relativePosition);
                w.scale(invTimeHorizon);
                w.sub(relativeVelocity);
                w.negate();

                /* Vector from cutoff center to relative velocity. */
                final double wLengthSq = w.dot(w);

                final double dotProduct1 = w.dot(relativePosition);

                if (dotProduct1 < 0.0f && Math.pow(dotProduct1, 2.0f) > combinedRadiusSq * wLengthSq) {
                    /* Project on cut-off circle. */
                    final double wLength = Math.sqrt(wLengthSq);
                    Vector2d unitW = new Vector2d(w);
                    unitW.scale(1.0f / wLength);


                    line.direction = new Vector2d(unitW.getY(), -unitW.getX());
                    u = new Vector2d(unitW);
                    u.scale((combinedRadius * invTimeHorizon) - wLength);
                } else {
                    /* Project on legs. */

//                    final double LEG = ((distSq - combinedRadiusSq) > 0) ? Math.sqrt(distSq - combinedRadiusSq) : 0;
                    final double LEG = Math.sqrt(Math.abs(distSq - combinedRadiusSq));

                    if (Geometry.det(relativePosition, w) > 0.0f) {
                        /* Project on left LEG. */

                        line.direction = new Vector2d(
                                relativePosition.getX() * LEG - relativePosition.getY() * combinedRadius,
                                relativePosition.getX() * combinedRadius + relativePosition.getY() * LEG);
                        line.direction.scale(1.0f / distSq);
                    } else {
                        /* Project on right LEG. */

                        line.direction = new Vector2d(
                                relativePosition.getX() * LEG + relativePosition.getY() * combinedRadius,
                                -relativePosition.getX() * combinedRadius + relativePosition.getY() * LEG);
                        line.direction.scale(-1.0f / distSq);
                    }

                    final double dotProduct2 = relativeVelocity.dot(line.direction);
                    u = new Vector2d(line.direction);
                    u.scale(dotProduct2);
                    u.sub(relativeVelocity);

                }
            } else {
                /* Collision. */
//                System.out.println("Collision!!!");

                final double invTimeStep = 1.0f / me.getLevel0MotionPlanning().getTimestep();

                Vector2d w = new Vector2d(relativePosition);
                w.scale(invTimeStep);
                w.sub(relativeVelocity);

                w.negate();

                double wLength = w.length();

                Vector2d unitW = new Vector2d(w);
                unitW.scale(1.0 / wLength);

                line.direction = new Vector2d(unitW.getY(), -unitW.getX());
                u = new Vector2d(unitW);
                u.scale((combinedRadius * invTimeStep) - wLength);


            }
            Vector2d newU = new Vector2d(u);
            if (otherAgent.getVelocity().length() != 0) {
                newU.scale(0.5f);
            } else {
                newU.scale(1.0f);
            }
            newU.add(me.getVelocity());

            line.point = new Point2d(newU);
            assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
            orcaLines.add(line);


        }
        //These function should return the new velocity based on linear programming solution

        int lineFail = linearProgram2(orcaLines, me.getMaxSpeed(), preferredVelocity, false, newVelocity);

        if (lineFail < orcaLines.size()) {
            linearProgram3(orcaLines, 0, lineFail, me.getMaxSpeed(), newVelocity);
        }
        return newVelocity;

    }

    private boolean linearProgram1(List<Line> lines, int lineNo, double radius, Vector2d optVelocity, boolean directionOpt, Vector2d result) {


        Vector2d lineNoPoint = new Vector2d(lines.get(lineNo).point);
        Vector2d lineNoDirection = new Vector2d(lines.get(lineNo).direction);
        double dotProduct = lineNoPoint.dot(lineNoDirection);

        //   final double detProduct = det(lines.get(lineNo).direction, lineNoPoint);
        //final double detProduct2 = lineNoPoint.dot(lineNoPoint);
        final double discriminant = Math.pow(dotProduct, 2.0) + Math.pow(radius, 2.0f) - lineNoPoint.dot(lineNoPoint);

        if (Double.compare(discriminant, Geometry.EPSILON) < 0) {
            /* Max speed circle fully invalidates line lineNo. */
            return false;
        }

        final double sqrtDiscriminant = Math.sqrt(discriminant);
        double tLeft = -(dotProduct) - sqrtDiscriminant;
        double tRight = -(dotProduct) + sqrtDiscriminant;

        for (int i = 0; i < lineNo; ++i) {
            final double denominator = Geometry.det(lineNoDirection, lines.get(i).direction);
            Vector2d tempVector = new Vector2d(lineNoPoint);
            tempVector.sub(new Vector2d(lines.get(i).point));
            final double numerator = Geometry.det(lines.get(i).direction, tempVector);

            if (Double.compare(
                    Math.abs(denominator), Geometry.EPSILON) <= 0) {
                /* Lines lineNo and i are (almost) parallel. */

                if (Double.compare(numerator, Geometry.EPSILON) < 0) {
                    /* Line i fully invalidates line lineNo. */
                    return false;
                } else {
                    /* Line i does not impose constraint on line lineNo. */
                    continue;
                }
            }

            final double t = numerator / denominator;
            if (denominator >= 0) {
                /* Line i bounds line lineNo on the right. */
                tRight = Math.min(tRight, t);
            } else {
                /* Line i bounds line lineNo on the left. */
                tLeft = Math.max(tLeft, t);
            }

            if (tLeft > tRight) {
                return false;
            }
        }

        if (directionOpt) {
            /* Optimize direction. */
            Vector2d tempLineNoDirection = new Vector2d(lineNoDirection);
            if (Double.compare(optVelocity.dot(tempLineNoDirection), -Geometry.EPSILON) > 0) {
                /* Take right extreme. */
                tempLineNoDirection.scale(tRight);
            } else {
                /* Take left extreme. */
                tempLineNoDirection.scale(tLeft);
            }
            tempLineNoDirection.add(new Vector2d(lineNoPoint));
            result.x = tempLineNoDirection.x;
            result.y = tempLineNoDirection.y;
        } else {
            /* Optimize closest point. */
            Vector2d tempOptVector = new Vector2d(optVelocity);
            tempOptVector.sub(lineNoPoint);
            final double t = lineNoDirection.dot(tempOptVector);
            Vector2d tempLineNoDirection = new Vector2d(lineNoDirection);
            if (Double.compare(t, tLeft) < 0) {
                tempLineNoDirection.scale(tLeft);
            } else if (Double.compare(t, tRight) > 0) {
                tempLineNoDirection.scale(tRight);
            } else {
                tempLineNoDirection.scale(t);
            }
            tempLineNoDirection.add(new Vector2d(lineNoPoint));
            result.x = tempLineNoDirection.x;
            result.y = tempLineNoDirection.y;

        }

        return true;
    }

    private int linearProgram2(List<Line> lines, double radius, Vector2d optVelocity, boolean directionOpt, Vector2d result) {


        if (directionOpt) {
            /*
             * Optimize direction. Note that the optimization velocity is of unit
             * length in this case.
             */
            if (Double.compare(Math.abs(optVelocity.length() - 1), Geometry.EPSILON) > 0) {
                System.out.println("what?? how??" + optVelocity.length());
            }
            Vector2d tempOpt = new Vector2d(optVelocity);

            result.x = tempOpt.x;
            result.y = tempOpt.y;
            result.scale(radius);
        } else if (optVelocity.dot(optVelocity) > Math.pow(radius, 2.0f)) {
            /* Optimize closest point and outside circle. */

            result.x = optVelocity.x;
            result.y = optVelocity.y;
            result.normalize();//mhl: why normalize
            result.scale(radius);
        } else {
            /* Optimize closest point and inside circle. */

            result.x = optVelocity.x;
            result.y = optVelocity.y;
        }

        for (int i = 0; i < lines.size(); ++i) {

            Vector2d tempPoint = new Vector2d(lines.get(i).point);
            tempPoint.sub(new Vector2d(result));


            if (Double.compare(
                    Geometry.det(lines.get(i).direction, tempPoint), 0) > 0) {
                /* Result does not satisfy constraint i. Compute new optimal result. */
                Vector2d tempResult = new Vector2d(result);
                if (!linearProgram1(lines, i, radius, optVelocity, directionOpt, result)) {
                    result.x = tempResult.x;
                    result.y = tempResult.y;
                    return i;
                }
            }
        }

        return lines.size();
    }

    private void linearProgram3(List<Line> lines, int numObstLines, int beginLine, double radius, Vector2d result) {

        double distance = 0.0f;

        for (int i = beginLine; i < lines.size(); i++) {
            Vector2d tempPoint = new Vector2d(lines.get(i).point);
            tempPoint.sub(result);

            if (Geometry.det(lines.get(i).direction, tempPoint) > distance) {
                /* Result does not satisfy constraint of line i. */
                List<Line> projLines = new ArrayList<Line>();
                for (int j = 0; j < numObstLines; j++) {
                    projLines.add(new Line(lines.get(j)));

                }

                for (int j = numObstLines; j < i; j++) {
                    Line line = new Line();

                    double determinant = Geometry.det(lines.get(i).direction, lines.get(j).direction);
                    if (Double.compare(Math.abs(determinant), Geometry.EPSILON) <= 0) {
                        /* Line i and line j are (almost) parallel. */
                        if (Double.compare(lines.get(i).direction.dot(lines.get(j).direction), -Geometry.EPSILON) > 0) {
                            /* Line i and line j point in the same direction. */
                            continue;
                        } else {
                            /* Line i and line j point in opposite direction. */
                            line.point = new Point2d(lines.get(j).point);
                            line.point.add(lines.get(i).point);
                            line.point.scale(0.5f);

                        }
                    } else {

                        Vector2d tempVector = new Vector2d(lines.get(i).point);
                        tempVector.sub(new Vector2d(lines.get(j).point));
                        Vector2d newTempVector = new Vector2d(lines.get(i).direction);
                        newTempVector.scale(Geometry.det(lines.get(j).direction, tempVector) / determinant);

                        line.point = new Point2d(lines.get(i).point);
                        line.point.add(newTempVector);


                    }
                    line.direction = new Vector2d(lines.get(j).direction);
                    line.direction.sub(lines.get(i).direction);
                    line.direction.normalize();

                    projLines.add(line);
                }

                final Vector2d tempResult = new Vector2d(result);

                if (linearProgram2(projLines, radius, new Vector2d(-lines.get(i).direction.y, lines.get(i).direction.x), true, result) < projLines.size()) {
                    /* This should in principle not happen.  The result is by definition
                     * already in the feasible region of this linear program. If it fails,
                     * it is due to small floating point error, and the current result is
                     * kept.
                     */
//
                    result.x = tempResult.x;
                    result.y = tempResult.y;

//                    result.x = 0.0f;
//                    result.y = 0.0f;

                }

                Vector2d tempVector = new Vector2d(lines.get(i).point);
                tempVector.sub(result);
                distance = Geometry.det(lines.get(i).direction, tempVector);
            }
        }
    }
}
