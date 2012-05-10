package ibevac.utilities;

import ec.util.MersenneTwisterFast;
import ibevac.engine.IbevacModel;

/**
 * The random number generator to be used in the model. It ensures that all random
 * numbers are produced from the given specified seed only
*  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */

public class IbevacRNG {

    private static IbevacRNG instance = null;
    private MersenneTwisterFast random;

    /**
     * Resets the RNG to the correct seed when the simulation is stopped and
     * resetted
     */
    public static void reset() {
        assert IbevacModel.model != null;
        System.out.println("resetting seed");
        instance = new IbevacRNG();
    }

    /**
     * Being a singleton class only the static instance is available to the 
     * outside world.
     * @return 
     */
    public static IbevacRNG instance() {
        if (instance == null) {
            instance = new IbevacRNG();
        }
        return instance;
    }

    private IbevacRNG() {
        assert IbevacModel.model != null;
        random = new MersenneTwisterFast(IbevacModel.model.seed());
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    public int nextInt(int n) {
        return random.nextInt(n);
    }

    public double nextGaussian() {
        return random.nextGaussian();
    }
}
