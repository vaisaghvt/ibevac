/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import ibevac.cue.Cue;
import java.util.Collection;

/**
 * The state of a trapped agent. The algorithm for determining whether the agent 
 * is trapped has not been added yet. As a result this is also not implemented
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Trapped extends State {
// TODO implement trapped behavior.
    @Override
    public Collection<Integer> getCurrentGoals() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



    public Trapped(Planner planner, IbevacAgent agent) {
        super(planner, agent);
    }

   @Override
    public State handlePhaseChange(Phase phase) {
        return this;
    }
}
