/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.environment;

import ibevac.cue.FireAlarmCue;
import ibevac.datatypes.SmallRoom;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ibevac.EvacConstants;
import ibevac.agent.IbevacAgent;
import ibevac.cue.Cue;
import ibevac.cue.FireCue;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CFloor;
import ibevac.engine.IbevacModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.vecmath.Point2d;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * This class is responsible for handling and storing cues.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
class CueSpace {

    /**
     * Stores all the cues for a particular room
     */
    Multimap<SmallRoom, Cue> cuesForSmallRoom;
    /**
     * Stores all the small rooms in a particulr room/area
     */
    Multimap<Integer, SmallRoom> smallRoomsForArea;
    /**
     * Stores the fields in which all the cuse are stored.
     */
    private ArrayList<Continuous2D> cueSpaces;

    /**
     * Initializes all the small rooms so that they are ready to store cues in 
     * them.
     * @param allRooms
     * @param maxSmallRoomLength
     * @param listOfFloors
     * @param space 
     */
    CueSpace(Collection<CArea> allRooms, double maxSmallRoomLength, Collection<CFloor> listOfFloors, IbevacSpace space) {
        cuesForSmallRoom = HashMultimap.create();
        smallRoomsForArea = HashMultimap.create();
        cueSpaces = new ArrayList<Continuous2D>();
        for (CFloor floor : listOfFloors) {
            cueSpaces.add(new Continuous2D(
                    floor.getWidth() / IbevacModel.scale,
                    floor.getHeight() / IbevacModel.scale,
                    EvacConstants.MAX_SENSOR_RANGE / IbevacModel.scale));
        }
        for (CArea area : allRooms) {
//            assert area.getCorner0().getX() < area.getCorner1().getX();
//            assert area.getCorner1().getY() < area.getCorner1().getY();

            int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
            int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());

            int roomWidth = Math.abs(area.getCorner1().getX() - area.getCorner0().getX());
            int roomLength = Math.abs(area.getCorner1().getY() - area.getCorner0().getY());
            if (roomLength <= maxSmallRoomLength && roomWidth <= maxSmallRoomLength) {
                //Room size is within limits
                double locationX = mnx + ((double) roomWidth / 2.0);
                double locationY = mny + ((double) roomLength / 2.0);
                SmallRoom tempRoom = new SmallRoom(roomLength, roomWidth,
                        new Point2d(locationX, locationY), area, space.getFloorByAreaId(area.getId()));
                this.smallRoomsForArea.put(area.getId(), tempRoom);
                cueSpaces.get(space.getFloorByAreaId(area.getId())).
                        setObjectLocation(tempRoom, new Double2D(locationX / IbevacModel.scale, locationY / IbevacModel.scale));
            } else {
                int idealNumberOfRoomsOnLength = 1;
                int idealNumberOfRoomsOnWidth = 1;
                double idealLength = roomLength;
                double idealWidth = roomWidth;
                while (idealWidth >= maxSmallRoomLength) {


                    idealWidth = (double) roomWidth / (++idealNumberOfRoomsOnWidth);
                }
                while (idealLength >= maxSmallRoomLength) {
                    idealLength = (double) roomLength / (++idealNumberOfRoomsOnLength);
                }
//                System.out.println(area.getId() + ":" + idealNumberOfRoomsOnLength + "," + idealNumberOfRoomsOnWidth);
                for (int i = 0; i < idealNumberOfRoomsOnWidth; i++) {
                    for (int j = 0; j < idealNumberOfRoomsOnLength; j++) {
                        double locationX = mnx + i * idealWidth + ((double) idealWidth / 2.0);
                        double locationY = mny + j * idealLength + ((double) idealLength / 2.0);
                        SmallRoom tempRoom = new SmallRoom(idealLength, idealWidth,
                                new Point2d(locationX, locationY),
                                area, space.getFloorByAreaId(area.getId()));
                        this.smallRoomsForArea.put(area.getId(), tempRoom);
                        cueSpaces.get(space.getFloorByAreaId(area.getId())).
                                setObjectLocation(tempRoom, new Double2D(locationX / IbevacModel.scale, locationY / IbevacModel.scale));

                    }
                }
            }

//            int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
//            int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());
//           System.out.println(area.getId());
//            System.out.println(mnx + "," + mny);
//            System.out.println(mxx + "," + mxy);
//            for (SmallRoom room : this.smallRoomsForArea.get(area.getId())) {
//                System.out.println(room);
//            }
        }
    }

    /**
     * Uses the small room field to get all the small rooms in a radius of a 
     * particualr position. And then returns all the cuse in these small rooms.
     * @param me
     * @param radius
     * @return 
     */
    public Bag getCuesInRadius(IbevacAgent me, double radius) {

        Bag smallRooms = cueSpaces.get(me.getCurrentFloorId()).getObjectsExactlyWithinDistance(
                new Double2D(
                me.getLogicalPosition().getX() / IbevacModel.scale,
                me.getLogicalPosition().getY() / IbevacModel.scale),
                radius);
        Bag cuesToReturn = new Bag();
        for (Object object : smallRooms) {

            SmallRoom tempRoom = (SmallRoom) object;
            cuesToReturn.addAll(this.cuesForSmallRoom.get(tempRoom));
        }

        return cuesToReturn;
    }

    /**
     * Uses the small room field to get all the small rooms in a radius of a 
     * particualr position. And then returns all the cuse in these small rooms.
     * @param me
     * @param radius
     * @return 
     */
    public int getArea(int x, int y, int floorId, double radius) {

        Bag smallRooms = cueSpaces.get(floorId).getObjectsExactlyWithinDistance(
                new Double2D(
                x / IbevacModel.scale,
                y / IbevacModel.scale),
                radius);
        assert smallRooms.size() != 0;

        for (Object object : smallRooms) {
            SmallRoom tempRoom = (SmallRoom) object;
            return tempRoom.getAreaId();
        }
        return -1;
    }

    /**
     * Puts the passed cue into all the small rooms in this area
     * @param cue
     * @param area 
     */
    public void putCueInArea(Cue cue, CArea area) {

//        scaledLocation.scale(IbevacModel.scale);
//System.out.println(cue.getClass()+"put at "+ area.getId());
//        System.out.println(scaledLocation + ","+area.getId());
        for (SmallRoom room : this.smallRoomsForArea.get(area.getId())) {
//            System.out.println(k++);
            if (!cuesForSmallRoom.get(room).contains(cue)) {
                cuesForSmallRoom.put(room, cue);
            }
        }
    }

    /**
     * Determines the small room location for a passed x,y
     * @param x
     * @param y
     * @param areaId
     * @return 
     */
    SmallRoom determineSmallRoomOfLocation(int x, int y, int areaId) {

        for (SmallRoom room : this.smallRoomsForArea.get(areaId)) {
//            System.out.println(k++);
            if (room.contains(new Point2d(x, y))) {
                return room;

            }
        }
        return null;
    }

    void putCueInSmallRoom(Cue cue, SmallRoom room) {
        cuesForSmallRoom.put(room, cue);
    }

    public void removeCueFromSmallRoom(Cue cue, SmallRoom room) {
        cuesForSmallRoom.remove(room, cue);
    }

    /**
     * Removes the said cue from all small rooms in this area.
     * @param cue
     * @param areaId 
     */
    void removeCueFromRoom(FireAlarmCue cue, int areaId) {
        for (SmallRoom room : this.smallRoomsForArea.get(areaId)) {
//            System.out.println(k++);
            if (cuesForSmallRoom.get(room).contains(cue)) {
//                System.out.println("Before:"+cuesForSmallRoom.get(room).size());
                cuesForSmallRoom.remove(room, cue);
//                System.out.println("After:"+cuesForSmallRoom.get(room).size());
            }

        }
    }
}
