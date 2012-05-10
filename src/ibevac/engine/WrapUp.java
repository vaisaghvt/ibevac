/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.engine;

import ibevac.agent.IbevacAgent;
import java.util.Set;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * As of now this steppable simply checks if the simulation is over. Eventually,
 * This function
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
class WrapUp implements Steppable {

    private final Set<IbevacAgent> agents;
    private final IbevacModel state;

    public WrapUp(IbevacModel model, Set<IbevacAgent> agentList) {
        this.agents = agentList;
        this.state = model;
    }

    @Override
    public void step(SimState ss) {
        boolean staircaseBurn = false;
        boolean exitBurn = false;
        if (state.getSpace().areStairCasesBurning()) {
            staircaseBurn = true;
        }
        if (state.getSpace().areAllExitsBurning()) {
            exitBurn = true;
        }
        for (IbevacAgent agent : agents) {
            if (!(agent.isSafe() || agent.isDead())) {
                if (exitBurn) {
                    agent.kill();
                } else if (staircaseBurn
                        && agent.getCurrentFloorId() > 0) {
                    agent.kill();

                }
            }
        }

        for (IbevacAgent agent : agents) {
            if (!agent.isSafe() && !agent.isTrapped() && !agent.isDead()) {
                return;
            }
        }

        System.out.println(
                "Wrapping up!");


        state.kill();
    }
}
