/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner.states;

import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import java.util.Collection;

/**
 * <h4> The State of the agent.Defines the current state of the agents and the 
 * different actions and consequences of being in a state. It also stores the
 * state that iwll be next transitioned to.
 * </h4>
 * 
 * <h4>This abstract class enables the use of a strategy pattern so that the specific 
 * implementation used is irrelevant to the rest of the program.</h4>
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public abstract class State {

    /**
     * A reference to the planner
     */
    protected final Planner planner;
    /**
     * A reference to the agent
     */
    protected final IbevacAgent agent;

    public State(Planner planner, IbevacAgent agent) {
        this.planner = planner;
        this.agent = agent;
    }

    /**
     * Each state will have its set of goals that the agent should try to reach 
     * in this state. This has to be implemented by each subclass
     * @return A collection of integers indicating the current goals of the agent.
     */
    abstract public Collection<Integer> getCurrentGoals();

    /**
     * A boolean indicating whether there are certain actions to be completed
     * in this state. It is set to false by default so that there is no need to 
     * unnecessarilly repeat this code if there are no actions
     * 
     */
    public boolean hasActions() {
        return false;
    }

    /**
     * This function describes the set of actions to be completed by the agent in
     * the current state. By default set to throw an exception because it is not 
     * supposed to be called unless hasActions() is set to true which should 
     * only happen if this method is overridden. 
     */
    public void executeActions() {
        throw new UnsupportedOperationException();
    }

//    abstract public State getNextState();
//
//    abstract public boolean isConsistentWith(Cue cue);
    
    /**
     * It changes the state of the agent to the 
     * next state if and when required depending on what has been passed to it 
     * as phase
     * @param phase
     * @return the new State
     */
    abstract public State handlePhaseChange(Phase phase);
}
