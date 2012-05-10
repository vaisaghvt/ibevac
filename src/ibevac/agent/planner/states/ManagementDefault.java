
package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import java.util.Collection;
import java.util.Collections;

/**
 * <h4> The state of an agent by default. It tries to move home. From here it 
 * either starts escpaing or exploring based on cues.
 * </h4>
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class ManagementDefault extends State {

    public ManagementDefault(Planner planner, IbevacAgent agent) {
        super(planner, agent);
//        System.out.println("Back here :( ");
    }
    
    

    @Override
    public Collection<Integer> getCurrentGoals() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return Collections.singleton(agent.getHomeId());
    }

    @Override
    public State handlePhaseChange(Phase phase) {

        switch (phase) {
            case FIRE:
                return new Escaping(planner, agent);

            case UNUSUAL:
                return new Exploring(planner, agent);
//                return new Escaping(planner, agent);
            case NONE:
                return this;


        }
        assert false;
        return null;
    }
}
