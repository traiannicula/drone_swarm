package swarm_sim.helpers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

import org.jogamp.vecmath.Vector3d;


/**
 * @author trnnc
 * It is not doing what is meant to do.
 * It tends to reduce vector size after many iterations.
 * I will give up for now.
 *
 */
public class MovingAverageVector implements Cloneable {
	private Queue<Vector3d> window;
	private int windowSize;
	private Vector3d windowSum;
	private int currentSize;

	public MovingAverageVector(int windowSize) {
		this.windowSize = windowSize;
		this.window = new LinkedList<>();
		this.windowSum = new Vector3d(0.0, 0.0, 0.0);
		this.currentSize = 0;
	}

	public Vector3d getAverage(Vector3d v) {
		// cloning needed
		Vector3d cv = (Vector3d) v.clone();
		window.add(cv);
		windowSum.add(cv);
		currentSize++;
		// If window exceeds the window size, remove the oldest data point
		if (currentSize > windowSize) {
			Vector3d removePoint = window.poll();
			windowSum.sub(removePoint);
			currentSize--;
		}
		Vector3d res  = (Vector3d) windowSum.clone();
		res.scale(1.0 / currentSize);
		return res;
	}	
	
	public Queue<Vector3d> getWindow() {
		return window;
	}

	public void setWindow(Queue<Vector3d> window) {
		this.window = window;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MovingAverageVector mav = (MovingAverageVector) super.clone();
		mav.window = this.window.stream().collect(Collectors.toCollection(LinkedList::new));
		return mav;
	}

	@Override
	public String toString() {
		return "MovingAverageVector [window=" + window + ", windowSize=" + windowSize + ", windowSum=" + windowSum
				+ ", currentSize=" + currentSize + "]";
	}
	
	public static void main(String[] args) {
		MovingAverageVector mav = new MovingAverageVector(4);
		Vector3d v = new Vector3d(1.0, 0.0, 0.0);
		System.out.println(mav.getAverage(v));
		v = new Vector3d(1.0, 0.0, 0.0);
		System.out.println(mav.getAverage(v));
		v = new Vector3d(1.0, 0.0, 0.0);
		System.out.println(mav.getAverage(v));
		v = new Vector3d(1.0, 0.0, 0.0);
		System.out.println(mav.getAverage(v));
		v = new Vector3d(1.0, 0.0, 0.0);
		System.out.println(mav.getAverage(v));
		v = new Vector3d(1.0, 0.0, 0.0);
		System.out.println(mav.getAverage(v));
		v.z = 100;
		System.out.println(mav);
		
	}
}