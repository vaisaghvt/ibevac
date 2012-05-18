package ibevac.agent.navigation.level1motion;

import abmcs.agent.MovingAgent;
import abmcs.agent.PhysicalAgent;
import abmcs.agent.StaticObstacle;
import abmcs.motionplanning.level1.Level1MotionPlanning;
import abmcs.motionplanning.level2.SpatialWaypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import utilities.Geometry;

/**
 * <h4> An implementation of RVO2 for Level1 motino planning. Buggy and slow
 * because obstacles are not stored in a manner compatible with RVO2 and thus
 * it has been necessary to make some adjustments.</h4>
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class RVO2 extends Level1MotionPlanning {

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
    public static final double TIME_HORIZON = 30.0;
    /**
     * TIME_HORIZON_OBSTACLE 	float (time) 	The minimal amount of time for which the
     * agent's velocities that are computed by the simulation are safe with respect
     * to obstacles. The larger this number, the sooner this agent will respond
     * to the presence of obstacles, but the less freedom the agent has in choosing
     * its velocities. Must be positive.
     */
    public static final double TIME_HORIZON_OBSTACLE = 5.0;

    public RVO2(MovingAgent agent) {
        this.me = agent;
        orcaLines = new ArrayList<Line>();
    }

    /**
     * Instructs the higher level path planners to recalculate a path to exit.
     */
    @Override
    public void reset() {
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
     * the set of perceived static and dynamic obstacles and a current location
     * (spatial waypoint) tha thte agent is trying to reach and it calculates
     * the agent's preferred velocity.
     *
     * @return Vector representation of preferred velocity
     * @see WaypointTracker#WaypointTracker(abmcs.agent.MovingAgent, java.util.List)
     * @see WaypointTracker#getWaypoint()
     */
    @Override
    public Vector2d getPreferredVelocity() {
        //determine our immediate waypoint
        if (tracker == null) {
            List<SpatialWaypoint> waypoints = me.getLevel2MotionPlanning().getSpatialWaypoints();
            if (waypoints == null) {
                return new Vector2d();
            } else {
                tracker = new WaypointTracker(me, waypoints);
            }
        }

        waypoint = tracker.getWaypoint();
        if (waypoint == null) {
            return new Vector2d();
        }

        Vector2d preferredVelocity = new Vector2d(waypoint.getPoint());
        preferredVelocity.sub(me.getPosition());

        preferredVelocity.normalize();
        preferredVelocity.scale(me.getPreferredSpeed());


        Set<? extends PhysicalAgent> neighbors = me.getPerceivedDynamicObstacles();
        Set<? extends StaticObstacle> obstacles = me.getPerceivedStaticObstacles();

        orcaLines.clear();


        TreeMap<Double, StaticObstacle> obses = new TreeMap<Double, StaticObstacle>();
        for (StaticObstacle tempObstacle : obstacles) {

            double distanceToObstacleLine;
            if (!obses.containsValue(tempObstacle)) {
                distanceToObstacleLine = Geometry.calcDistanceToLineSegment(tempObstacle.getStart(), tempObstacle.getEnd(), me.getPosition());
                obses.put(distanceToObstacleLine, tempObstacle);
            }
        }
        Vector2d newVelocity = new Vector2d(preferredVelocity);
        final double invTimeHorizonObst = 1.0f / TIME_HORIZON_OBSTACLE;

        /* Create obstacle ORCA lines. */
        for (StaticObstacle obstacleFromList : obses.values()) {

            Point2d obstacle1 = obstacleFromList.getStart();
            Point2d obstacle2 = obstacleFromList.getEnd();
//            System.out.println(obstacle1.getPoint());
            Vector2d obstacle1UnitDir = new Vector2d(obstacle2);
            obstacle1UnitDir.sub(obstacle1);
            obstacle1UnitDir.normalize();

            Vector2d obstacle2UnitDir = new Vector2d(obstacle2);
            obstacle2UnitDir.sub(obstacle2);
//            obstacle2UnitDir.normalize();

//            System.out.println("for agent at "+me.getPosition()+"Avoiding obstacle from "+ obstacle1.getPoint()+ " to " + obstacle2.getPoint());

            Vector2d relativePosition1 = new Vector2d(obstacle1);
            relativePosition1.sub(me.getPosition());

            Vector2d relativePosition2 = new Vector2d(obstacle2);
            relativePosition2.sub(me.getPosition());

            Vector2d obstacleVector = new Vector2d(obstacle2);
            obstacleVector.sub(obstacle1);


            /*
             * Check if velocity obstacle of obstacle is already taken care of by
             * previously constructed obstacle ORCA lines.
             */
            boolean alreadyCovered = false;

            for (Line orcaLine : orcaLines) {
                if (checkCovered(invTimeHorizonObst, relativePosition1,
                        relativePosition2, orcaLine, me)) {
                    alreadyCovered = true;
//                    System.out.println("Covered");
                    break;
                }
            }

            if (alreadyCovered) {
                continue;
            }

            /* Not yet covered. Check for collisions. */


            float distSq1 = (float) relativePosition1.dot(relativePosition1);

            float distSq2 = (float) relativePosition2.dot(relativePosition2);

            double radiusSq = me.getDiameter() / 2.0 * me.getDiameter() / 2.0;

            Vector2d leftLegDirection, rightLegDirection;

            Vector2d negRelativePosition1 = new Vector2d(relativePosition1);
            negRelativePosition1.negate();
            double s = (negRelativePosition1.dot(obstacleVector) / obstacleVector.dot(obstacleVector));
            Vector2d distSq = new Vector2d(obstacleVector);
            distSq.scale(-s);
            distSq.add(negRelativePosition1);
            float distSqLine = (float) distSq.dot(distSq);


            Vector2d negRelativePosition2 = new Vector2d(relativePosition2);
            negRelativePosition2.negate();


            Line line = new Line();

            if (s < 0 && distSq1 <= radiusSq) {
                /* Collision with left vertex. Ignore if non-convex. */
//                System.out.println("Left Vertex Collision");

//                    System.out.println("COllision with left vertex");
                line.point = new Point2d(0, 0);

                line.direction = new Vector2d(-relativePosition1.y, relativePosition1.x);
                line.direction.normalize();
                orcaLines.add(line);


                continue;


            } else if (s > 1 && distSq2 <= radiusSq) {
                /* Collision with right vertex. Ignore if non-convex*/
//                System.out.println("Right Vertex Collision");

                if (Geometry.det(relativePosition2, obstacle2UnitDir) >= 0) {
                    /*ignore obstacle*/
//                    System.out.println("COllision with right vertex");
                    line.point = new Point2d(0, 0);

                    line.direction = new Vector2d(-relativePosition2.y, relativePosition2.x);
                    line.direction.normalize();
                    assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                    orcaLines.add(line);
                    continue;

                }
            } else if (s >= 0 && s < 1 && distSqLine <= radiusSq) {
                /* Collision with obstacle segment. */

//                System.out.println("COllision with a segment");
                line.point = new Point2d(0, 0);

                line.direction = new Vector2d(obstacle1UnitDir);
                line.direction.negate();
                assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                orcaLines.add(line);
                continue;
            }
            /*No collision
            Compute legs. When obliquely viewed, both legs can come from a single
            vertex. Legs extend cut-off line when nonconvex vertex.
             */
//            System.out.println("No collision");
            if (s < 0 && distSqLine <= radiusSq) {
//                System.out.println("oblique view from left");
                obstacle2 = obstacle1;

//                final double LEG1 = ((distSq1 - radiusSq) < 0) ? 0 : Math.sqrt(distSq1 - radiusSq);
                final double LEG1 = Math.sqrt(Math.abs(distSq1 - radiusSq));

                leftLegDirection = new Vector2d(relativePosition1.getX() * LEG1 - relativePosition1.getY() * me.getDiameter() / 2.0, relativePosition1.getX() * me.getDiameter() / 2.0 + relativePosition1.getY() * LEG1);
                rightLegDirection = new Vector2d(relativePosition1.getX() * LEG1 + relativePosition1.getY() * me.getDiameter() / 2.0, negRelativePosition1.getX() * me.getDiameter() / 2.0 + relativePosition1.getY() * LEG1);
                leftLegDirection.scale(1.0f / distSq1);
                rightLegDirection.scale(1.0f / distSq1);
            } else if (s > 1 && distSqLine <= radiusSq) {
                /*
                 * RVO2Obstacle viewed obliquely so that
                 * right vertex defines velocity obstacle.
                 */
//                 System.out.println("oblique view from right");


                obstacle1 = obstacle2;

//                final double LEG2 = ((distSq2 - radiusSq) < 0) ? 0 : Math.sqrt(distSq2 - radiusSq);
                final double LEG2 = Math.sqrt(Math.abs(distSq2 - radiusSq));
                leftLegDirection = new Vector2d(relativePosition2.getX() * LEG2 - relativePosition2.getY() * me.getDiameter() / 2.0, relativePosition2.getX() * me.getDiameter() / 2.0 + relativePosition2.getY() * LEG2);
                rightLegDirection = new Vector2d(relativePosition2.getX() * LEG2 + relativePosition2.getY() * me.getDiameter() / 2.0, negRelativePosition2.getX() * me.getDiameter() / 2.0 + relativePosition2.getY() * LEG2);
                leftLegDirection.scale(1.0f / distSq2);
                rightLegDirection.scale(1.0f / distSq2);
            } else {
                /* Usual situation. */

//                System.out.println("The usual");

                final double LEG1 = Math.sqrt(Math.abs(distSq1 - radiusSq));
                leftLegDirection = new Vector2d(relativePosition1.getX() * LEG1 - relativePosition1.getY() * me.getDiameter() / 2.0, relativePosition1.getX() * me.getDiameter() / 2.0 + relativePosition1.getY() * LEG1);
                leftLegDirection.scale(1.0f / distSq1);


                final double LEG2 = Math.sqrt(Math.abs(distSq2 - radiusSq));
                rightLegDirection = new Vector2d(relativePosition2.getX() * LEG2 + relativePosition2.getY() * me.getDiameter() / 2.0, negRelativePosition2.getX() * me.getDiameter() / 2.0 + relativePosition2.getY() * LEG2);
                rightLegDirection.scale(1.0f / distSq2);

            }

            /*
             * Legs can never point into neighboring edge when convex vertex,
             * take cutoff-line of neighboring edge instead. If velocity projected on
             * "foreign" LEG, no constraint is added.
             */

            //final RVO2Obstacle leftNeighbor = obstacle1.getPrev();


            boolean isLeftLegForeign = false;
            boolean isRightLegForeign = false;

            Vector2d negLeftNeighborDirection = new Vector2d(obstacle1);
            negLeftNeighborDirection.sub(obstacle1);

//            negLeftNeighborDirection.normalize();

//            leftNeighborDirection.sub(obstacle1.getPoint());

//            Vector2d rightNeighborDirection = new Vector2d(rightNeighbor.getPoint());
//            rightNeighborDirection.sub(obstacle2.getPoint());


            if (Geometry.det(leftLegDirection, negLeftNeighborDirection) >= 0.0f) {
                /* Left LEG points into obstacle. */
//System.out.println("left leg into obstacle");

                leftLegDirection = new Vector2d(negLeftNeighborDirection);
                isLeftLegForeign = true;
            }


            if (Geometry.det(rightLegDirection, obstacle2UnitDir) <= 0.0f) {
                /* Right LEG points into obstacle. */
//                System.out.println("right leg into obstacle");
                rightLegDirection = new Vector2d(obstacle2UnitDir);
                isRightLegForeign = true;
            }

            /* Compute cut-off centers. */
            Point2d vectorToObstacle1 = new Point2d(obstacle1);
            vectorToObstacle1.sub(me.getPosition());
            vectorToObstacle1.scale(invTimeHorizonObst);
            final Vector2d LEFTCUTOFF = new Vector2d(vectorToObstacle1);

            Point2d vectorToObstacle2 = new Point2d(obstacle2);
            vectorToObstacle2.sub(me.getPosition());
            vectorToObstacle2.scale(invTimeHorizonObst);
            final Vector2d RIGHTCUTOFF = new Vector2d(vectorToObstacle2);

            Point2d cutOffVecTemp = new Point2d(RIGHTCUTOFF);
            cutOffVecTemp.sub(LEFTCUTOFF);
            final Vector2d CUTOFFVEC = new Vector2d(cutOffVecTemp);

            /* Project current velocity on velocity obstacle. */

            /* Check if current velocity is projected on cutoff circles. */

            Vector2d velocityMinusLeft = new Vector2d(me.getVelocity());
            velocityMinusLeft.sub(LEFTCUTOFF);

            Vector2d velocityMinusRight = new Vector2d(me.getVelocity());
            velocityMinusRight.sub(RIGHTCUTOFF);


            final double T = ((obstacle1.equals(obstacle2)) ? 0.5f : (velocityMinusLeft.dot(CUTOFFVEC) / CUTOFFVEC.dot(CUTOFFVEC)));
            final double TLEFT = (velocityMinusLeft.dot(leftLegDirection));
            final double TRIGHT = (velocityMinusRight.dot(rightLegDirection));

            if ((T < 0.0f && TLEFT < 0.0f)
                    || (obstacle1.equals(obstacle2) && TLEFT < 0.0f && TRIGHT < 0.0f)) {
                /* Project on left cut-off circle. */

//                System.out.println("Project on left cut off");

                Vector2d unitW = new Vector2d(velocityMinusLeft);
                unitW.normalize();

                line.direction = new Vector2d(unitW.getY(), -unitW.getX());
                unitW.scale(invTimeHorizonObst);
                unitW.scale(me.getDiameter() / 2.0);
                unitW.add(LEFTCUTOFF);
                line.point = new Point2d(unitW);
                assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                orcaLines.add(line);
                continue;
            } else if (T > 1.0f && TRIGHT < 0.0f) {
                /* Project on right cut-off circle. */
//                System.out.println("Project on righ cut off");
                Vector2d unitW = new Vector2d(velocityMinusRight);
                unitW.normalize();

                line.direction = new Vector2d(unitW.getY(), -unitW.getX());
                unitW.scale(invTimeHorizonObst);
                unitW.scale(me.getDiameter() / 2.0);
                unitW.add(RIGHTCUTOFF);
                line.point = new Point2d(unitW);
                assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                orcaLines.add(line);
                continue;

            }

            /*
             * Project on left LEG, right LEG, or cut-off line, whichever is closest
             * to velocity.
             */
            Vector2d vectorForCutOff = new Vector2d(CUTOFFVEC);
            vectorForCutOff.scale(T);
            vectorForCutOff.add(LEFTCUTOFF);
            vectorForCutOff.negate();
            vectorForCutOff.add(me.getVelocity());


            final double DISTSQCUTOFF = ((T < 0.0f || T > 1.0f || obstacle1.equals(obstacle2)) ? Double.MAX_VALUE : vectorForCutOff.dot(vectorForCutOff));

            Vector2d vectorForLeftCutOff = new Vector2d(leftLegDirection);
            vectorForLeftCutOff.scale(TLEFT);
            vectorForLeftCutOff.add(LEFTCUTOFF);
            vectorForLeftCutOff.negate();
            vectorForLeftCutOff.add(me.getVelocity());

            final double DISTSQLEFT = ((TLEFT < 0.0f) ? Double.MAX_VALUE : vectorForLeftCutOff.dot(vectorForLeftCutOff));


            Vector2d vectorForRightCutOff = new Vector2d(rightLegDirection);
            vectorForRightCutOff.scale(TRIGHT);
            vectorForRightCutOff.add(RIGHTCUTOFF);
            vectorForRightCutOff.negate();
            vectorForRightCutOff.add(me.getVelocity());

            final double DISTSQRIGHT = ((TRIGHT < 0.0f) ? Double.MAX_VALUE : vectorForRightCutOff.dot(vectorForRightCutOff));

            if (DISTSQCUTOFF <= DISTSQLEFT && DISTSQCUTOFF <= DISTSQRIGHT) {
                /* Project on cut-off line. */
//System.out.println("Project on cut off");

                line.direction = new Vector2d(obstacle1UnitDir);

                line.direction.negate();


                Vector2d vectorForPoint = new Vector2d(-line.direction.getY(), line.direction.getX());
                vectorForPoint.scale(invTimeHorizonObst);
                vectorForPoint.scale(me.getDiameter() / 2.0);
                vectorForPoint.add(LEFTCUTOFF);
                line.point = new Point2d(vectorForPoint);
                assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                orcaLines.add(line);

            } else if (DISTSQLEFT <= DISTSQRIGHT) { /* Project on left LEG. */

//System.out.println("Project on left leg");

                if (isLeftLegForeign) {
                    continue;
                }

                line.direction = new Vector2d(leftLegDirection);

                Vector2d vectorForPoint = new Vector2d(-line.direction.getY(), line.direction.getX());
                vectorForPoint.scale(invTimeHorizonObst);
                vectorForPoint.scale(me.getDiameter() / 2.0);
                vectorForPoint.add(LEFTCUTOFF);
                line.point = new Point2d(vectorForPoint);
                if (Math.abs(line.direction.length() - 1.0) > Geometry.EPSILON) {
                    System.out.println(line.direction.length());
                }
                assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                orcaLines.add(line);
            } else { /* Project on right LEG. */

//                System.out.println("Project on right leg");
                if (isRightLegForeign) {
                    continue;
                }


                line.direction = new Vector2d(rightLegDirection);
                line.direction.negate();

                Vector2d vectorForPoint = new Vector2d(-line.direction.getY(), line.direction.getX());
                vectorForPoint.scale(invTimeHorizonObst);
                vectorForPoint.scale(me.getDiameter() / 2.0);
                vectorForPoint.add(RIGHTCUTOFF);
                line.point = new Point2d(vectorForPoint);
                if (Math.abs(line.direction.length() - 1.0) > Geometry.EPSILON) {
                    System.out.println(line.direction.length());
                }
                assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
                orcaLines.add(line);
            }


        }
//        System.out.println("***********");
        final int numObstLines = orcaLines.size();


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
            newU.scale(0.5f);
            newU.add(me.getVelocity());

            line.point = new Point2d(newU);
            assert Math.abs(line.direction.length() - 1.0) < Geometry.EPSILON;
            orcaLines.add(line);


        }
        //These function should return the new velocity based on linear programming solution

        int lineFail = linearProgram2(orcaLines, me.getMaxSpeed(), preferredVelocity, false, newVelocity);

        if (lineFail < orcaLines.size()) {
            linearProgram3(orcaLines, numObstLines, lineFail, me.getMaxSpeed(), newVelocity);
        }
        return newVelocity;

    }

    private boolean checkCovered(double invTimeHorizonObst, Vector2d relativePosition1, Vector2d relativePosition2, Line line, PhysicalAgent agent) {

        Vector2d a = new Vector2d(relativePosition1);
        a.scale(invTimeHorizonObst);
        a.sub(line.point);
        Vector2d b = new Vector2d(relativePosition2);
        b.scale(invTimeHorizonObst);
        b.sub(line.point);

        return ((Double.compare((Geometry.det(a, line.direction) - invTimeHorizonObst * agent.getDiameter() / 2.0), -Geometry.EPSILON) >= 0)
                && (Double.compare((Geometry.det(b, line.direction) - invTimeHorizonObst * agent.getDiameter() / 2.0), -Geometry.EPSILON) >= 0));

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
