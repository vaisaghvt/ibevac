/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import ibevac.cue.Cue;
import ibevac.utilities.IbevacRNG;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a state of discussion and information exchange. During the Milling 
 * process the agents gather and wait at a corridor/gathering point waiting for 
 * some information that will cause it to make a decision. A timer is implemented
 * to put an upper limit on the amount of time this milling takes place. So that
 *  after a pont the agent iether escapes or goes back to wkr without wasting further time
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Milling extends State {

    private final Set<Integer> currentGoals;
    private int timer;

    public Milling(Planner planner, IbevacAgent agent) {
        super(planner, agent);
//        System.out.println(agent.getId()+ "Milling");
        currentGoals = new HashSet<Integer>();

        currentGoals.clear();
        currentGoals.addAll(planner.getEnvironmentKnowledge().getCorridors());
        currentGoals.remove(agent.getCurrentAreaId());

        timer = 50 + IbevacRNG.instance().nextInt(300);
    }

    @Override
    public Collection<Integer> getCurrentGoals() {
        return currentGoals;
    }

    @Override
    public State handlePhaseChange(Phase phase) {

        switch (phase) {
            case FIRE:
                return new Escaping(planner, agent);

            case UNUSUAL:
//                System.out.println("Escape");
//                return new Escaping(planner, agent);
return this;
            case NONE:
                return new Default(planner, agent);


        }
        assert false;
        return null;
    }

    @Override
    public void executeActions() {
        if (agent.isGoalReached()) {
            timer--;

            if (timer == 0) {
                planner.setState(new Default(planner, agent));
            }
        }
    }

    @Override
    public boolean hasActions() {
        return false;
    }
}
