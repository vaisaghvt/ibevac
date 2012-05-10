/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner;

import ibevac.agent.knowledge.environment.EnvironmentKnowledgeModule;

import ibevac.agent.knowledge.event.EventKnowledgeModule;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.states.State;
import java.util.Collection;
import javax.vecmath.Point2d;

/**
 * <h4>The Planning module. Manages the strategy and current state of the agent.
 * </h4>
 * 
 * <h4>This interface enables the use of a strategy pattern so that the specific 
 * implementation used is irrelevant to the rest of the program.</h4>
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public interface Planner {

    /**
     * Returns a collections of the goal options that the agent has at the 
     * current point of time
     * @return 
     */
    public Collection<Integer> getCurrentGoalAreaIds();

    /**
     * Kill self
     * 
     * @see ibevac.agent.planner.states.Dead
     */
    public void transitionToDead();

    /**
     * Change self to safe state
     * 
     * @see ibevac.agent.planner.states.State
     */
    public void transitionToSafe();

    /**
     * 
     * @return  true if there are any actions to be done in current state
     *          false if no actions to be done in current state
     * 
     * @see State#hasActions() 
     */
    public boolean hasActions();

    /**
     * Execute the actions that are stored for the current state
     * 
     * @see State#executeActions() 
     */
    public void executeActions();

    /**
     * calling this method informs the agent that this phase has come. It is 
     * called through a command pattern by the EventKnowledge Module. And it 
     * generally causes a change in the current state.
     * @param phase the phase that the agent has just perceived or it will react to next
     * @see EventKnowledgeModule#updateState() 
     */
    public void informAbout(Phase phase);

    /**
     * Sends a reuest to the event knowledge module to reset all phases. This 
     * happens when a perception or state change of the agent causes it to go 
     * back to it's default behavior
     * 
     */
    public void requestCompleteReset();

    /**
     * Changes the state to passed state
     * @param state  The new state of the agent at the end of this
     */
    public void setState(State state);

    /**
     * Simple setter to set the spatial location of current/chosen goal from the 
     * list of goal points for this particular state.
     * @param point 
     */
    public void setChosenGoal(Point2d point);

    /**
     * Gets the current State of the agent
     * @return current state 
     */
    public State getState();

    
    /**
     * Simple getter to get the spatial location of current/chosen goal from the 
     * list of goal points for this particular state.
     * @param point 
     */
    public Point2d getChosenGoal();

    public EnvironmentKnowledgeModule getEnvironmentKnowledge();
}
