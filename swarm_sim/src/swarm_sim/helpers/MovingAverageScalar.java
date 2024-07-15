package swarm_sim.helpers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public class MovingAverageScalar implements Cloneable{
    private Queue<Double> window;
    private int windowSize;
    private double windowSum;
    private int currentSize;
    
    public MovingAverageScalar(int windowSize) {
        this.windowSize = windowSize;
        this.window = new LinkedList<>();
        this.windowSum = 0.0;
        this.currentSize = 0;
    }
    
    public double getAverage(double data){
        // add data to window
        window.add(data);
        windowSum += data;
        currentSize++;

        // window exceeds the window size
        if (currentSize > windowSize){
            double removeData = window.poll();
            windowSum -= removeData;
            currentSize--;
        }

        // compute moving average
        return windowSum / currentSize;
    }

	public Queue<Double> getWindow() {
		return window;
	}

	public void setWindow(Queue<Double> window) {
		this.window = window;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MovingAverageScalar mas =  (MovingAverageScalar) super.clone();
		mas.window = this.window.stream().collect(Collectors.toCollection(LinkedList::new));
		return mas;
	}

	@Override
	public String toString() {
		return "MovingWindowAverage [window=" + window + ", windowSize=" + windowSize + ", windowSum=" + windowSum
				+ ", currentSize=" + currentSize + "]";
	}
    
}
