package fptool;

import ibevac.datatypes.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;


public class Document {
    private int nextObjectId = 0;
    private File scenarioFile = null;
    private CEvacuationScenario content = null;
    private boolean hasUnsavedChanges = false;

    private HashMap<CFloor, BufferedImage> images = new HashMap<CFloor, BufferedImage>();
//	private HashMap<Integer,CObject> objects = new HashMap<Integer,CObject>();

    private CFloor currentFloor = null;
    private HashSet<CObject> selection = null;
    private Integer hoveringOverRoomId = null;
    private Integer hoveringOverStaircaseId = null;

    public Document(String filename, String name, String path, double scale) throws Exception {
        this.scenarioFile = new File(filename);

        content = new CEvacuationScenario();
        content.setName(name);
        content.setPath(path);
        content.setScale(scale);

        this.save();
    }

    public Document(File file) throws Exception {
        this.scenarioFile = file;
        this.content = (CEvacuationScenario) XMLManager.instance().unmarshal(file);

        HashSet<CObject> objects = new HashSet<CObject>();
        for (int i = 0; i < content.getFloors().size(); ++i) {
            CFloor floor = content.getFloors().get(i);
            objects.add(floor);

            //set as current floor (if not already set)
            if (currentFloor == null) {
                currentFloor = floor;
            }

            objects.addAll(floor.getRooms());
            objects.addAll(floor.getLinks());
            objects.addAll(floor.getExits());
            objects.addAll(floor.getStaircases());
            objects.addAll(floor.getCrowds());
            objects.addAll(floor.getFires());


            //do some sanity checking
            //(1) rooms must not overlap
            HashSet<CArea> tempAreas = new HashSet<CArea>();
            tempAreas.addAll(floor.getRooms());
            tempAreas.addAll(floor.getStaircases());
            for (CArea r1 : tempAreas) {
                ArrayList<CArea> o = this.findOverlappingAreas(r1, tempAreas);
                if (o.size() != 1) {
                    for (CArea a : o) {
                        if (a == r1) continue;
                        System.out.println("WARNING: area " + r1.getId() + " is overlapping with area " + a.getId());
                    }
                }
                assert (o.get(0) == r1); //must overlap with itself
            }

            //(2) all links have correct orientation
            HashMap<Integer, CArea> mapping = new HashMap<Integer, CArea>();
            for (CArea area : floor.getRooms()) mapping.put(area.getId(), area);
            for (CArea area : floor.getStaircases()) mapping.put(area.getId(), area);

            for (CLink link : floor.getLinks()) {
                if (link.getConnectingAreas().size() != 2) {
                    System.out.println("WARNING: link " + link.getId() + " has " + link.getConnectingAreas().size() + " connecting areas. Removing...");
                    objects.remove(link);
                    floor.getLinks().remove(link);
                } else {
                    CArea area0 = mapping.get(link.getConnectingAreas().get(0));
                    CArea area1 = mapping.get(link.getConnectingAreas().get(1));

                    int mnx0 = Math.min(area0.getCorner0().getX(), area0.getCorner1().getX());
                    int mny0 = Math.min(area0.getCorner0().getY(), area0.getCorner1().getY());
                    int mxx0 = Math.max(area0.getCorner0().getX(), area0.getCorner1().getX());
                    int mxy0 = Math.max(area0.getCorner0().getY(), area0.getCorner1().getY());
                    int mnx1 = Math.min(area1.getCorner0().getX(), area1.getCorner1().getX());
                    int mny1 = Math.min(area1.getCorner0().getY(), area1.getCorner1().getY());
                    int mxx1 = Math.max(area1.getCorner0().getX(), area1.getCorner1().getX());
                    int mxy1 = Math.max(area1.getCorner0().getY(), area1.getCorner1().getY());

                    //are the rooms beside it each other?
                    if (mnx1 > mxx0 || mnx0 > mxx1) {
                        //horizontal
                        if (link.getOrientation() != 1) {
                            System.out.println("WARNING: orientation of link " + link.getId() + " is " + link.getOrientation() + " when it shold be 1. Correcting...");
                            link.setOrientation(1);
                        }
                    }
                    //or are the room on top of each other?
                    else if (mny1 > mxy0 || mny0 > mxy1) {
                        //vertical
                        if (link.getOrientation() != 2) {
                            System.out.println("WARNING: orientation of link " + link.getId() + " is " + link.getOrientation() + " when it shold be 2. Correcting...");
                            link.setOrientation(2);
                        }
                    } else assert (false);
                }
            }


            //load floor plan image
            BufferedImage image = ImageIO.read(new File(floor.getImage()));
            images.put(floor, image);
        }

        //update next object id...
        for (CObject obj : objects) {
            if (obj.getId() >= nextObjectId) {
                nextObjectId = obj.getId() + 1;
            }
        }

        System.out.println("Next object id is " + nextObjectId);
    }

    public CEvacuationScenario getContent() {
        return content;
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public String name() {
        return content.getName();
    }

    public int numberOfFloors() {
        return content.getFloors().size();
    }

    public void selectFloor(int floorIdx) {
        this.currentFloor = content.getFloors().get(floorIdx);
    }

    public void addFloor(String imageFilename) throws Exception {
        CFloor floor = new CFloor();
        floor.setId(nextObjectId++);
        floor.setImage(imageFilename);

        //load floor plan image
        BufferedImage image = ImageIO.read(new File(floor.getImage()));
        images.put(floor, image);

        floor.setWidth(image.getWidth());
        floor.setHeight(image.getHeight());

        content.getFloors().add(floor);
        currentFloor = floor;

        hasUnsavedChanges = true;
    }

    public void removeCurrentFloor() {
        if (currentFloor != null) {
            images.remove(currentFloor);
            content.getFloors().remove(currentFloor);

            if (content.getFloors().size() > 0) {
                currentFloor = content.getFloors().get(0);
            } else {
                currentFloor = null;
            }

            hasUnsavedChanges = true;
        }
    }

    public CFloor getFloors(int floorIdx) {
        return content.getFloors().get(floorIdx);
    }

    public void addRoom(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        CPoint p0 = new CPoint();
        p0.setX(mnx);
        p0.setY(mny);

        CPoint p1 = new CPoint();
        p1.setX(mxx);
        p1.setY(mxy);

        CRoom room = new CRoom();
        room.setId(nextObjectId++);
        room.setCorner0(p0);
        room.setCorner1(p1);

        currentFloor.getRooms().add(room);
        hasUnsavedChanges = true;
    }

    public void addLink(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        CPoint p0 = new CPoint();
        p0.setX(mnx);
        p0.setY(mny);

        CPoint p1 = new CPoint();
        p1.setX(mxx);
        p1.setY(mxy);

        CLink link = new CLink();
        link.setId(nextObjectId++);
        link.setCorner0(p0);
        link.setCorner1(p1);

        HashSet<CArea> areas = new HashSet<CArea>();
        areas.addAll(currentFloor.getRooms());
        areas.addAll(currentFloor.getStaircases());

        ArrayList<CArea> overlapping = this.findOverlappingAreas(link, areas);
        if (overlapping.size() == 2) {
            //determine the orientation of the link
            CArea area0 = overlapping.get(0);
            CArea area1 = overlapping.get(1);
            int mnx0 = Math.min(area0.getCorner0().getX(), area0.getCorner1().getX());
            int mny0 = Math.min(area0.getCorner0().getY(), area0.getCorner1().getY());
            int mxx0 = Math.max(area0.getCorner0().getX(), area0.getCorner1().getX());
            int mxy0 = Math.max(area0.getCorner0().getY(), area0.getCorner1().getY());
            int mnx1 = Math.min(area1.getCorner0().getX(), area1.getCorner1().getX());
            int mny1 = Math.min(area1.getCorner0().getY(), area1.getCorner1().getY());
            int mxx1 = Math.max(area1.getCorner0().getX(), area1.getCorner1().getX());
            int mxy1 = Math.max(area1.getCorner0().getY(), area1.getCorner1().getY());

            //are the rooms beside it each other?
            if (mnx1 > mxx0 || mnx0 > mxx1) {
                //horizontal
                link.setOrientation(1);
            }
            //or are the room on top of each other?
            else if (mny1 > mxy0 || mny0 > mxy1) {
                //vertical
                link.setOrientation(2);
            } else assert (false);

            link.getConnectingAreas().add(area0.getId());
            link.getConnectingAreas().add(area1.getId());

            currentFloor.getLinks().add(link);
            hasUnsavedChanges = true;
        } else {
            System.out.println("INFO: cannot create link with other than two overlapping areas!");
        }
    }

    public void addExit(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        CPoint p0 = new CPoint();
        p0.setX(mnx);
        p0.setY(mny);

        CPoint p1 = new CPoint();
        p1.setX(mxx);
        p1.setY(mxy);

        CExit exit = new CExit();
        exit.setId(nextObjectId++);
        exit.setCorner0(p0);
        exit.setCorner1(p1);

        HashSet<CArea> areas = new HashSet<CArea>(currentFloor.getRooms());
        ArrayList<CArea> overlapping = this.findOverlappingAreas(exit, areas);
        if (overlapping.size() == 1) {
            CArea area0 = overlapping.get(0);
            exit.getConnectingAreas().add(area0.getId());

            if (area0.getCorner0().getX() < mnx && area0.getCorner1().getX() > mxx) {
                exit.setOrientation(1);
            } else if (area0.getCorner0().getY() < mny && area0.getCorner1().getY() > mxy) {
                exit.setOrientation(2);
            } else {
                assert false;
            }

            currentFloor.getExits().add(exit);
            hasUnsavedChanges = true;
        } else {
            System.out.println("INFO: cannot create exit with other than one overlapping areas!");
        }
    }

    public void addStaircase(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        CPoint p0 = new CPoint();
        p0.setX(mnx);
        p0.setY(mny);

        CPoint p1 = new CPoint();
        p1.setX(mxx);
        p1.setY(mxy);

        CStaircase staircase = new CStaircase();
        staircase.setId(nextObjectId++);
        staircase.setCorner0(p0);
        staircase.setCorner1(p1);

        currentFloor.getStaircases().add(staircase);
        hasUnsavedChanges = true;
    }

    public void addFire(int x1, int y1, int x2, int y2) {
        int mnx = Math.min(x1, x2);
        int mxx = Math.max(x1, x2);
        int mny = Math.min(y1, y2);
        int mxy = Math.max(y1, y2);

        CPoint p0 = new CPoint();
        p0.setX(mnx);
        p0.setY(mny);

        CPoint p1 = new CPoint();
        p1.setX(mxx);
        p1.setY(mxy);

        CFire fire = new CFire();
        fire.setId(nextObjectId++);
        fire.setCorner0(p0);
        fire.setCorner1(p1);

        currentFloor.getFires().add(fire);
        hasUnsavedChanges = true;
    }


    public void selectObjects(int x1, int y1, int x2, int y2) {
        this.selection = new HashSet<CObject>();

        HashSet<CArea> temp = new HashSet<CArea>();
        temp.addAll(currentFloor.getRooms());
        temp.addAll(currentFloor.getLinks());
        temp.addAll(currentFloor.getExits());
        temp.addAll(currentFloor.getStaircases());
        temp.addAll(currentFloor.getFires());

        for (CArea area : temp) {
            int mnx = Math.min(x1, x2);
            int mxx = Math.max(x1, x2);
            int mny = Math.min(y1, y2);
            int mxy = Math.max(y1, y2);

            CPoint p0 = area.getCorner0();
            CPoint p1 = area.getCorner1();

            if ((p0.getX() >= mnx && p0.getX() <= mxx && p0.getY() >= mny && p0.getY() <= mxy) &&
                    (p1.getX() >= mnx && p1.getX() <= mxx && p1.getY() >= mny && p1.getY() <= mxy)) {
                this.selection.add(area);
            }
        }

        if (this.selection.isEmpty()) this.selection = null;
    }

    public void unselectObjects() {
        this.selection = null;
    }

    public Set<CObject> selection() {
        return selection;
    }

    public void removeSelection() {
        if (selection != null) {
            for (CObject obj : selection) {
                currentFloor.getRooms().remove(obj);
                currentFloor.getLinks().remove(obj);
                currentFloor.getExits().remove(obj);
                currentFloor.getStaircases().remove(obj);
                currentFloor.getFires().remove(obj);
            }
        }
    }

//	public Set<CStaircase> getStaircases() {
//		if(currentFloor == null) return null;
//		return new LinkedHashSet<CStaircase>(currentFloor.getStaircases());
//	}

    public Map<String, CStaircaseGroup> getStaircaseGroups() {
        HashMap<String, CStaircaseGroup> mapping = new HashMap<String, CStaircaseGroup>();

        for (CStaircaseGroup group : content.getStaircaseGroups()) {
            mapping.put(group.getName(), group);
        }

        return mapping;
    }

    public void updateStaircaseGroups(Map<String, Set<CStaircase>> mapping) {
        content.getStaircaseGroups().clear();

        for (String groupName : mapping.keySet()) {
            CStaircaseGroup group = new CStaircaseGroup();
            group.setId(nextObjectId++);
            group.setName(groupName);

            for (CStaircase staircase : mapping.get(groupName)) {
                group.getStaircaseIds().add(staircase.getId());
            }

            content.getStaircaseGroups().add(group);
        }

        hasUnsavedChanges = true;
    }

    public CCrowd getCrowdForRoomId(int roomId) {
        if (currentFloor != null) {
            //searching for the crowd and the room is suboptimal but whatever...
            //this is not performance critical
            for (CCrowd crowd : currentFloor.getCrowds()) {
                if (crowd.getRoomId() == roomId) {
                    return crowd;
                }
            }

            CRoom room = null;
            for (CRoom r : currentFloor.getRooms()) {
                if (r.getId() == roomId) {
                    room = r;
                    break;
                }
            }

            CCrowd crowd = new CCrowd();
            crowd.setId(nextObjectId++);
            crowd.setRoomId(roomId);

            int mnx = Math.min(room.getCorner0().getX(), room.getCorner1().getX());
            int mny = Math.min(room.getCorner0().getY(), room.getCorner1().getY());
            int mxx = Math.max(room.getCorner0().getX(), room.getCorner1().getX());
            int mxy = Math.max(room.getCorner0().getY(), room.getCorner1().getY());

            int dx = (int) (0.1 * (mxx - mnx));
            int dy = (int) (0.1 * (mxy - mny));

            CPoint p0 = new CPoint();
            CPoint p1 = new CPoint();
            p0.setX(mnx + dx);
            p0.setY(mny + dy);
            p1.setX(mxx - dx);
            p1.setY(mxy - dy);

            crowd.setCorner0(p0);
            crowd.setCorner1(p1);

            crowd.setKnowledge(0);
            crowd.setSize(0);

            currentFloor.getCrowds().add(crowd);
            return crowd;
        }

        return null;
    }

    public void updateCrowd(CCrowd crowd) {
        if (currentFloor != null) {
            if (crowd.getSize() == 0) {
                currentFloor.getCrowds().remove(crowd);
            }
        }
    }

    public Integer isHoveringOverRoom(int x, int y) {
        hoveringOverRoomId = null;

        if (currentFloor != null) {
            for (CRoom area : currentFloor.getRooms()) {
                int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
                int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
                int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
                int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                    hoveringOverRoomId = area.getId();
                    return hoveringOverRoomId;
                }
            }
        }

        return null;
    }

    public Integer isHoveringOverStaircase(int x, int y) {
        hoveringOverStaircaseId = null;

        if (currentFloor != null) {
            for (CRoom area : currentFloor.getStaircases()) {
                int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
                int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
                int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
                int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                    hoveringOverStaircaseId = area.getId();
                    return hoveringOverStaircaseId;
                }
            }
        }

        return null;
    }


    public BufferedImage image() {
        try {
            if (currentFloor != null) {
                BufferedImage image = new BufferedImage(currentFloor.getWidth(), currentFloor.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

                Graphics g = image.getGraphics();

                g.drawImage(images.get(currentFloor), 0, 0, null);

                for (CArea area : currentFloor.getRooms()) {
                    if ((selection != null && selection.contains(area)) || (hoveringOverRoomId != null && hoveringOverRoomId == area.getId())) {
                        this.drawArea(g, area, Color.RED, "R(" + area.getId() + ")");
                    } else {
                        this.drawArea(g, area, Color.BLUE, "R(" + area.getId() + ")");
                    }
                }

                for (CLink link : currentFloor.getLinks()) {
                    if (selection != null && selection.contains(link)) {
                        this.fillArea(g, link, Color.RED, "L(" + link.getId() + ")");
                        g.setColor(Color.RED);
                    } else {
                        this.fillArea(g, link, Color.CYAN, "L(" + link.getId() + ")");
                        g.setColor(Color.CYAN);
                    }

                    CEdge edge = new CEdge(link, null);
                    CPoint[] wp = edge.getWaypoints();
                    int d = edge.getWPSize();
                    int d2 = d / 2;

                    g.drawOval(wp[0].getX() - d2, wp[0].getY() - d2, d, d);
                    g.drawOval(wp[1].getX() - d2, wp[1].getY() - d2, d, d);
                    g.drawLine(wp[0].getX(), wp[0].getY(), wp[1].getX(), wp[1].getY());
                }

                for (CExit exit : currentFloor.getExits()) {
                    if (selection != null && selection.contains(exit)) {
                        this.fillArea(g, exit, Color.RED, "E(" + exit.getId() + ")");
                        g.setColor(Color.RED);
                    } else {
                        this.fillArea(g, exit, Color.GREEN, "E(" + exit.getId() + ")");
                        g.setColor(Color.GREEN);
                    }

                    CEdge edge = new CEdge(exit, null);
                    CPoint[] wp = edge.getWaypoints();
                    int d = edge.getWPSize();
                    int d2 = d / 2;

                    if (wp[0].getX() == null || wp[0].getY() == null) {
                        System.out.println("die bitch");
                    }
                    g.drawOval(wp[0].getX() - d2, wp[0].getY() - d2, d, d);
                    g.drawOval(wp[1].getX() - d2, wp[1].getY() - d2, d, d);
                    g.drawLine(wp[0].getX(), wp[0].getY(), wp[1].getX(), wp[1].getY());
                }

                for (CArea area : currentFloor.getStaircases()) {
                    if ((selection != null && selection.contains(area)) || (hoveringOverStaircaseId != null && hoveringOverStaircaseId == area.getId())) {
                        this.drawArea(g, area, Color.RED, "R(" + area.getId() + ")");
                    } else {
                        this.drawArea(g, area, Color.MAGENTA, "R(" + area.getId() + ")");
                    }
                }

                for (CArea area : currentFloor.getCrowds()) {
                    if (selection != null && selection.contains(area)) {
                        this.fillArea(g, area, Color.RED, "C(" + area.getId() + ")");
                    } else {
                        this.fillArea(g, area, Color.LIGHT_GRAY, "C(" + area.getId() + ")");
                    }
                }

                for (CArea area : currentFloor.getFires()) {
                    if (selection != null && selection.contains(area)) {
                        this.fillArea(g, area, Color.RED, "F(" + area.getId() + ")");
                    } else {
                        this.fillArea(g, area, Color.ORANGE, "F(" + area.getId() + ")");
                    }
                }

                return image;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void drawArea(Graphics g, CArea area, Color color, String label) {
        int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        g.setColor(color);
        g.drawRect(mnx, mny, mxx - mnx, mxy - mny);
        g.drawString(label, mnx + 4, mxy - 4);
    }

    private void fillArea(Graphics g, CArea area, Color color, String label) {
        int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        g.setColor(color);
        g.fillRect(mnx, mny, mxx - mnx, mxy - mny);

        g.setColor(Color.BLACK);
        g.drawString(label, mnx + 4, mxy - 4);
    }

    public void save() throws Exception {
        String xml = XMLManager.instance().marshal(content);

        PrintStream out = new PrintStream(scenarioFile);
        out.println(xml);
        out.close();
    }

    private ArrayList<CArea> findOverlappingAreas(CArea area, Set<CArea> areas) {
        int mnx1 = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mny1 = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxx1 = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mxy1 = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        ArrayList<CArea> overlapping = new ArrayList<CArea>();
        for (CArea a : areas) {
            int mnx2 = Math.min(a.getCorner0().getX(), a.getCorner1().getX());
            int mny2 = Math.min(a.getCorner0().getY(), a.getCorner1().getY());
            int mxx2 = Math.max(a.getCorner0().getX(), a.getCorner1().getX());
            int mxy2 = Math.max(a.getCorner0().getY(), a.getCorner1().getY());

            if (!(mnx2 > mxx1 || mxx2 < mnx1 || mny2 > mxy1 || mxy2 < mny1)) {
                overlapping.add(a);
            }
        }

        return overlapping;
    }
}
