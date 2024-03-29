package ibevac.environment;

import ibevac.datatypes.SmallRoom;
import ibevac.cue.SmokeCue;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CStaircase;
import ibevac.engine.IbevacModel;
import ibevac.utilities.IbevacRNG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.DoubleGrid2D;

/**
 * This class is responsible for handling smoke propagation. It uses a difference
 * euations to model fick's law of diffusion in a Cellular automata. Smoke generators
 * are points at which smoke start and are created at random locations by the fire
 * as it spreads
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public final class SmokeSpace implements Steppable {

    /**
     * The amount of smoke generated by default by a smoke generator if no other
     * value of amount of smoke generated is specified
     */
    public static final float DEFAULT_GENERATION_VALUE = 0.1f;
    /**
     * The probability that at a particular timestep a genreator generates smoke
     */
//    public static final float GENERATION_PROB = 0.5f;

    /**
     * THe rate at which smoke spreads
     */
    public static final float DIFFUSION_CONSTANT = 0.021f;
    /**
     * Maximum amount of smoke in a particular cell.
     */
    public static final double SMOKE_LIMIT = 5000;
    /**
     * A serial ID that will be used for checkpoints
     */
    private static final long serialVersionUID = 1L;

    /**
     * The actual diffusion constant that is used.
     */
    private double actualDiffusionConstant;
    /**
     * A reference to the steppable that can be used for startin or stopping the
     * steppable if/ when neces
     */
    public Stoppable stoppable = null;
    /**
     * The list of fields storing the smoke in DoubleGrid2D
     */
    private final ArrayList<DoubleGrid2D> smokeSpaces;
    /**
     * The set of cells. Stored seperately from the DoubleGrid to enable easy
     * access and editting.
     */
    private Cell[][][] cells = null;
    /**
     * The number of cells in the x direction
     */
    private int width = 0;
    /**
     * The number of cells in the y direction. Controlled by the resolution.
     */
    private int height = 0;
    /**
     * This is the number of pixels per cell. A large value means larger cells are used
     * A smaller value results in lower number of pixels per cell.
     */
    private int resolution = 1;
    /**
     * The set of smoke generators which are points from which smoke is created
     * They can be considered to be flamable
     */
    private final HashSet<SmokeGenerator> smokeGenerators = new HashSet<SmokeGenerator>();
    /**
     * The list of small rooms that already have smoke. Increases the efficiency
     * of determining which rooms have smoke already.
     */
    private final HashMap<SmallRoom, Float> smallRoomsWithSmoke = new HashMap<SmallRoom, Float>();
    /**
     * The list of small rooms that already have smoke. Increases the efficiency
     * of determining which rooms have smoke already.
     */
    private HashSet<SmallRoom> smallRoomsWithGenerators = new HashSet<SmallRoom>();
    /**
     * The set of smokes that haven't already saturated with smoke or will start
     * smoking reasonably soon.
     */
    private final HashSet<Cell> smokingCells = new HashSet<Cell>();
    /**
     * A reference to IBEVAC SPace
     */
    private final IbevacSpace space;
    /**
     * Internal variable used to keep a track of cells to be added from spreading cellss
     */
    private final HashSet<Cell> cellsToBeAdded = new HashSet<Cell>();
    /**
     * omternal vairable used to keep a track of cells to be removed from spreading cells
     */
    private final HashSet<Cell> cellsToBeRemoved = new HashSet<Cell>();
    /**
     * omternal vairable used to keep a track of cells to be removed from spreading cells
     */
    private final HashSet<SmokeGenerator> generatorsToBeRemoved = new HashSet<SmokeGenerator>();

    /**
     * @param scenario
     * @param resolution in number of cm per grid.
     * @param space
     * @see SmokeSpace#resolution
     */
    public SmokeSpace(CEvacuationScenario scenario, int resolution, IbevacSpace space) {


        this.resolution = resolution;

        this.space = space;
        actualDiffusionConstant = (DIFFUSION_CONSTANT * 10000.0) / (resolution * resolution);

        for (CFloor floor : scenario.getFloors()) {
            if (floor.getWidth() > width) {
                width = floor.getWidth();
            }
            if (floor.getHeight() > height) {
                height = floor.getHeight();
            }
        }
        width /= resolution;
        height /= resolution;
//        System.out.println(width);
        cells = new Cell[scenario.getFloors().size() * 2][width][height];
        smokeSpaces = new ArrayList<DoubleGrid2D>(scenario.getFloors().size());

        for (int i = 0;
             i < scenario.getFloors().size();
             ++i) {
            smokeSpaces.add(new DoubleGrid2D(width, height));
        }

        for (int i = 0;
             i < cells.length;
             ++i) {
            for (int j = 0; j < cells[i].length; ++j) {
                for (int k = 0; k < cells[i][j].length; ++k) {
                    cells[i][j][k] = new Cell(i, j, k);
                }
            }
        }

        for (Cell[][] cell : cells) {
            for (int j = 0; j < cell.length; ++j) {
                for (int k = 0; k < cell[j].length; ++k) {
                    cell[j][k].initializeNeighbors();
                }
            }
        }
        // initialise the cells

        for (int i = 0;
             i < scenario.getFloors().size(); i++) {
            int l = 2 * i;
//            if (!fireAlarmRunning) {
//                startFireAlarm();
//            }
            CFloor floor = scenario.getFloors().get(i);

            HashSet<CArea> areas = new HashSet<CArea>();
            areas.addAll(floor.getRooms());
            areas.addAll(floor.getStaircases());
            areas.addAll(floor.getLinks());

            for (CArea area : areas) {
                int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX())
                        / resolution - 1;
                int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY())
                        / resolution - 1;
                int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX())
                        / resolution - 1;
                int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY())
                        / resolution - 1;

                for (int j = mnx; j < mxx; ++j) {
                    for (int k = mny; k < mxy; ++k) {
                        cells[l][j][k].setState(SmokableState.SMOKABLE);
                    }
                }

                if (area instanceof CStaircase) {
                    for (int j = mnx; j < mxx; ++j) {
                        for (int k = mny; k < mxy; ++k) {
                            if (l > 0) {
                                cells[l - 1][j][k].setState(SmokableState.SMOKABLE);
                            }
                            if (l < cells.length - 1) {
                                cells[l + 1][j][k].setState(SmokableState.SMOKABLE);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Besides scheduling and storring the steppable. This function sets the
     * value of diffusion constant appropriately based on the value of the
     * timestep.s
     *
     * @param model
     * @param ordering
     * @param timestep
     */
    public void schedule(IbevacModel model, int ordering, double timestep) {
        actualDiffusionConstant *= (timestep / 10.0);
        this.stoppable = model.schedule.scheduleRepeating(this, ordering,
                timestep);
    }

    /**
     * The step function that is executed by the schedule. This first generates smoek
     * at the generators and then uses the fick's law of diffusion to calculate the
     * new smoke at all locations in a CA based approach.
     *
     * @param state
     */
    @Override
    public void step(SimState state) {


        for (Cell cell : smokingCells) {
            cell.calcNewValue();
        }
        smokingCells.removeAll(cellsToBeRemoved);
        cellsToBeRemoved.clear();
        for (Cell cell : smokingCells) {
            cell.update();
        }


        smokingCells.addAll(cellsToBeAdded);
        cellsToBeAdded.clear();
        if (state.schedule.getSteps() == 500) {
            System.out.println("Slow already");
        }
        for (SmokeGenerator generator : this.smokeGenerators) {
//            System.out.println("generating");
//            if (IbevacRNG.instance().nextDouble() < SmokeSpace.GENERATION_PROB) {
            generator.generate();
//            }
            if (generator.isFinished()) {
                generatorsToBeRemoved.add(generator);
            }
        }
        smokeGenerators.removeAll(generatorsToBeRemoved);
        generatorsToBeRemoved.clear();
//        System.out.println(smokingCells.size());

    }


    /**
     * Add a generator at a particular cell
     *
     * @param i
     * @param j
     * @param k
     */
    void addGenerator(int i, int j, int k) {
        i /= resolution;
        j /= resolution;
        k /= resolution;

//        System.out.println(" adding a generator");
        if (this.cells[i][j][k] != null) {
//      System.out.println(" noy null"); 
//            SmallRoom smallRoom = space.getSmallRoomOfPoint(j * resolution, k * resolution, i / 2);
//            if (smallRoomsWithGenerators.contains(smallRoom)) {
//                return;
//            } else {
//                smallRoomsWithGenerators.add(smallRoom);
            this.smokeGenerators.add(new SmokeGenerator(cells[i][j][k], SmokeSpace.DEFAULT_GENERATION_VALUE));
//            }
        }
    }

    public DoubleGrid2D getSmokeSpace(int floorNumber) {
        return smokeSpaces.get(floorNumber);
    }

    /**
     * Returns the amount of smoke in that room. sum of all the individual
     * smoke in that small room.
     *
     * @param room
     * @return
     */
    float getSmokeInRoom(SmallRoom room) {
        if (smallRoomsWithSmoke.containsKey(room)) {
            return this.smallRoomsWithSmoke.get(room);
        } else {
            return 0;
        }
    }

    /**
     * returns if there is greater than a threshold value of smoke
     *
     * @param floorIdx
     * @param x
     * @param y
     * @return
     */
    public boolean isAreaSmoky(int floorIdx, int x, int y) {
        int i = 2 * floorIdx;
        int j = x / resolution;
        int k = y / resolution;

        if (j < 0 || j >= cells[i].length || k < 0 || k >= cells[i][j].length) {
            return true;
        }

        Cell cell = cells[i][j][k];
        return cell.getSmoke() >= SMOKE_LIMIT * 0.5;


    }

    /**
     * Enum variable indicating whether smoke can spread to this cell
     */
    public enum SmokableState {

        /**
         * Smoke can spread here.
         */
        SMOKABLE,
        /**
         * Smoke cannot spread here.
         */
        UNSMOKABLE
    }

    private class SmokeGenerator {

        /**
         * The location of the smoke gnerator
         */
        private final Cell location;
        /**
         * The asmount of smoke generated by the smoke generator when it does
         * release some smoke
         */
        private final float generationValue;
        private final int hash;

        public SmokeGenerator(Cell cell) {
            location = cell;
            generationValue = SmokeSpace.DEFAULT_GENERATION_VALUE;

            hash = 17 * 3 + this.location.hashCode();

        }

        public SmokeGenerator(Cell cell, float value) {
            location = cell;
            generationValue = value;
            SmokeSpace.this.smokingCells.add(cell);
            hash = 17 * 3 + this.location.hashCode();
            smokingCells.addAll(cell.immediateNeighbours);
        }

        public void generate() {
            location.setSmoke(location.getSmoke() + generationValue);
        }

        public boolean isFinished() {
            return location.getSmoke() == SMOKE_LIMIT;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SmokeGenerator other = (SmokeGenerator) obj;
            return this.location.equals(other.location);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private class Cell {

        private int i = -1;
        private int j = -1;
        private int k = -1;
        /**
         * The actual amount of smoke in that cell
         */
        private float smoke = 0;
        private final Set<Cell> immediateNeighbours;
        private final Set<Cell> allNeighbours;
        private SmokableState smokable;
        /**
         * temporary storage to store amount of smoke which will be stored into
         * actual smoke value only when it has been used to update all
         */
        private float tempValue = 0;
        private float prevValue = -1;
        private int hash;

        public Cell(int i, int j, int k) {
            this.i = i;
            this.j = j;
            this.k = k;
            if (i % 2 == 0) {
                smokeSpaces.get(i / 2).set(j, k, 0.0);
            }
            immediateNeighbours = new HashSet<Cell>();
            allNeighbours = new HashSet<Cell>();
            smokable = SmokableState.UNSMOKABLE;
            hash = 3;
            hash = 47 * hash + this.i;
            hash = 47 * hash + this.j;
            hash = 47 * hash + this.k;
        }

        public void setSmoke(float value) {
//            if (value < 0.001) {
//                this.smoke = 0;
//            } else {
            if (value != 0) {
                SmokeSpace.this.cellsToBeAdded.addAll(this.allNeighbours);
//                    for(Cell neighbour:this.neighbors){
//                        SmokeSpace.this.cellsToBeAdded.addAll(neighbour.neighbors);
//                    }
            }

            if (value < SmokeSpace.SMOKE_LIMIT) {
                this.smoke = value;
                if (i % 2 == 0) {
                    smokeSpaces.get(i / 2).set(j, k, smoke);
                }

            } else {
                this.smoke = (float) SmokeSpace.SMOKE_LIMIT;

            }

            if (i % 2 == 0) {
                SmallRoom smallRoom = space.getSmallRoomOfPoint(j * resolution, k * resolution, i / 2);
                if (smallRoom != null) {
                    if (!smallRoomsWithSmoke.containsKey(smallRoom)) {
                        space.putCueInSmallRoom(new SmokeCue(j * resolution, k * resolution, smoke), smallRoom);
                        smallRoomsWithSmoke.put(smallRoom, smoke);
                    } else {
                        space.putCueInSmallRoom(
                                new SmokeCue(
                                        j * resolution, k * resolution, smoke + smallRoomsWithSmoke.get(smallRoom)),
                                smallRoom);
                        smallRoomsWithSmoke.put(smallRoom, smoke + smallRoomsWithSmoke.get(smallRoom));
                    }
                }
                smokeSpaces.get(i / 2).set(j, k, smoke);
            }

//            }
        }

        private void setState(SmokableState smokableState) {
            this.smokable = smokableState;
        }

        private float getSmoke() {
            return this.smoke;
        }

        private void calcNewValue() {
            if (this.smoke >= SmokeSpace.SMOKE_LIMIT || this.smokable == SmokableState.UNSMOKABLE) {
                cellsToBeRemoved.add(this);
                return;
            }

            this.tempValue = this.smoke;
            for (Cell cell : immediateNeighbours) {
                // if (cell.getSmoke() > 0.1) {
                tempValue += actualDiffusionConstant * cell.getSmoke();
                // }
            }

            tempValue -= 4 * actualDiffusionConstant * this.smoke;
            if (Math.abs(tempValue - this.smoke) < 0.0001 || Math.abs(tempValue - this.prevValue) < 0.0001) {
                cellsToBeRemoved.add(this);
            }
        }

        private void update() {
            prevValue = smoke;
            this.setSmoke(tempValue);
//            if(this.smoke>0.1){
////                System.out.println("Why am I not being printed?");
//            }
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cell other = (Cell) obj;
            if (this.i != other.i) {
                return false;
            }
            if (this.j != other.j) {
                return false;
            }
            return this.k == other.k;

        }

        public int hashCode() {

            return hash;
        }

        public void initializeNeighbors() {
            if (j > 0) {
                immediateNeighbours.add(cells[i][j - 1][k]); // west
                allNeighbours.add(cells[i][j - 1][k]); // west
            }
            if (j < width - 1) {
                immediateNeighbours.add(cells[i][j + 1][k]); // east
                allNeighbours.add(cells[i][j + 1][k]); // west
            }
            if (k > 0) {
                immediateNeighbours.add(cells[i][j][k - 1]); // north
                allNeighbours.add(cells[i][j][k - 1]); // west
            }
            if (k < height - 1) {
                immediateNeighbours.add(cells[i][j][k + 1]); // south
                allNeighbours.add(cells[i][j][k + 1]); // south
            }
            if (j > 0 && k > 0) {
                allNeighbours.add(cells[i][j - 1][k - 1]); // north-west
            }
            if (j > 0 && k < height - 1) {
                allNeighbours.add(cells[i][j - 1][k + 1]); // south-west
            }
            if (j < width - 1 && k > 0) {
                allNeighbours.add(cells[i][j + 1][k - 1]); // north-east
            }
            if (j < width - 1 && k < height - 1) {
                allNeighbours.add(cells[i][j + 1][k + 1]); // south-east
            }
            if (i > 0) {

                immediateNeighbours.add(cells[i - 1][j][k]); // center
                allNeighbours.add(cells[i - 1][j][k]); // center


            }
            if (i < cells.length - 1) {

                immediateNeighbours.add(cells[i + 1][j][k]); // center
                allNeighbours.add(cells[i + 1][j][k]); // center

            }
        }
    }
}
