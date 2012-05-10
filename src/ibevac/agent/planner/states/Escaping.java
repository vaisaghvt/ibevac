/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import ibevac.cue.FastAgentCue;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <h4> Escaping Behavior to head directly to the closest exit. The agent is
 * set to running speed and asked to move as fast as possible to the exit. The 
 * exit is the only goal in this state.
 * </h4>
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Escaping extends State {

    private FastAgentCue fastAgentCue;
    private final Set<Integer> currentGoals;

    public Escaping(Planner planner, IbevacAgent agent) {
        super(planner, agent);
//        System.out.println(agent.getId()+" escaping");
        agent.setToRunningSpeed();
      agent.updateEvacStartTime();
            currentGoals = new HashSet<Integer>();
        
        currentGoals.clear();
        currentGoals.add(planner.getEnvironmentKnowledge().getExitId());
    }

    @Override
    public Collection<Integer> getCurrentGoals() {
       return currentGoals;
    }



    @Override
    public State handlePhaseChange(Phase phase) {

        return this;
       
    }
}
