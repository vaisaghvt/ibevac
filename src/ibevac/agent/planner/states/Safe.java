package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import java.util.Collection;
import java.util.Collections;

/**
 * The state of a safe agent. Similar to a dead agent. It doesn't have a goal 
 * and it also has no phase transitions.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Safe extends State {

   public  Safe(Planner planner, IbevacAgent agent) {
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
