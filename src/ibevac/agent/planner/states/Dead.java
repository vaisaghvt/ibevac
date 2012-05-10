/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import java.util.Collection;
import java.util.Collections;

/**
 * <h4> The state of a dead agent. With no current goals and no state changes 
 * from this state.
 * </h4>
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Dead extends State{

    public Dead(Planner planner, IbevacAgent agent) {
        super(planner, agent);
    }

    @Override
    public Collection<Integer> getCurrentGoals() {
        return Collections.EMPTY_SET;
    }

    @Override
    public State handlePhaseChange(Phase phase) {
        return this;
    }



    
}
