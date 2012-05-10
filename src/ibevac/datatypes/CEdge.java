package ibevac.datatypes;

import java.util.HashMap;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CEdge extends DefaultWeightedEdge {
	private static final long serialVersionUID = 1L;

	private int id0 = -1;
	private int id1 = -1;
	private CArea area0 = null;
//	private CArea area1 = null;
	private CLink link = null;
	
	public CEdge(int id0, int id1, HashMap<Integer,CArea> rmapping) {
		this.id0 = id0;
		this.id1 = id1;
		this.area0 = rmapping.get(id0);
//		this.area1 = rmapping.get(id1);
		this.link = null;
	}

	public CEdge(CLink link, HashMap<Integer,CArea> rmapping) {
		this.link = link;
		if(link.getConnectingAreas().size() == 1) {
			this.id0 = link.getConnectingAreas().get(0);
			this.id1 = -1;
		}
		else if(link.getConnectingAreas().size() == 2) {
			this.id0 = link.getConnectingAreas().get(0);
			this.id1 = link.getConnectingAreas().get(1);
		}
		else assert(false);

		this.area0 = rmapping != null ? rmapping.get(id0) : null;
//		this.area1 = rmapping.get(id1);
	}
	
	public CLink link() {
		return link;
	}
	
	public int id0() {
		return id0;
	}
	
	public int id1() {
		return id1;
	}
	
	public CPoint[] getWaypoints() {
		CPoint[] wp = new CPoint[] {new CPoint(), new CPoint()};
		
		int mnx = Math.min(link.getCorner0().getX(), link.getCorner1().getX());
		int mny = Math.min(link.getCorner0().getY(), link.getCorner1().getY());
		int mxx = Math.max(link.getCorner0().getX(), link.getCorner1().getX());
		int mxy = Math.max(link.getCorner0().getY(), link.getCorner1().getY());
		
		if(link.getOrientation() == 1) {
			//horizontal
			int d = mxy - mny;
			int y = mny + d/2;
			int x0 = mnx - d/2;
			int x1 = mxx + d/2;
			
			wp[0].setX(x0);
			wp[0].setY(y);
			wp[1].setX(x1);
			wp[1].setY(y);
		}
		else if(link.getOrientation() == 2) {
			//vertical			
			int d = mxx - mnx;
			int x = mnx + d/2;
			int y0 = mny - d/2;
			int y1 = mxy + d/2;
			
			wp[0].setX(x);
			wp[0].setY(y0);
			wp[1].setX(x);
			wp[1].setY(y1);
		}
		else assert(false);
		
		//area0 might be null (if rmapping was null) -- should only be the case for drawing purposes!
		if(area0 != null) {
			mnx = Math.min(area0.getCorner0().getX(), area0.getCorner1().getX());
			mny = Math.min(area0.getCorner0().getY(), area0.getCorner1().getY());
			mxx = Math.max(area0.getCorner0().getX(), area0.getCorner1().getX());
			mxy = Math.max(area0.getCorner0().getY(), area0.getCorner1().getY());
			
			//ensure that wp[0] is the one in area with id0, 
			//i.e., ensure that wp[0] <=> id0 and wp[1] <=> id1
			if(wp[0].getX() < mnx || wp[0].getX() > mxx || wp[0].getY() < mny || wp[0].getY() > mxy) {
				CPoint temp = wp[0];
				wp[0] = wp[1];
				wp[1] = temp;
			}
		}
		
		return wp;
	}
	
	public int getWPSize() {
		int mnx = Math.min(link.getCorner0().getX(), link.getCorner1().getX());
		int mny = Math.min(link.getCorner0().getY(), link.getCorner1().getY());
		int mxx = Math.max(link.getCorner0().getX(), link.getCorner1().getX());
		int mxy = Math.max(link.getCorner0().getY(), link.getCorner1().getY());
		
		if(link.getOrientation() == 1) {
			//horizontal
			return (int) (0.8*(mxy - mny));
		}
		else if(link.getOrientation() == 2) {
			return (int) (0.8*(mxx - mnx));
		}
		else assert(false);
		
		return -1;
	}
}
