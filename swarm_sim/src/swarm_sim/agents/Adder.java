package swarm_sim.agents;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousAdder;
import repast.simphony.space.continuous.ContinuousSpace;

/**
 * @author traian
 * The agents are clustered in space around origin
 * @param <T>
 */
public class Adder<T> implements ContinuousAdder<T> {
	
	int clusterFactor;  
	
	public Adder(int clusterFactor) {
		this.clusterFactor = clusterFactor;
	}

	@Override
	public void add(ContinuousSpace<T> space, T object) {
		Dimensions dims = space.getDimensions();
		double[] location = new double[dims.size()];
		findLocation(location, dims);
		while(!space.moveTo(object, location)) {
			findLocation(location, dims);
		}
	}
	
	private void findLocation(double[] location, Dimensions dims){
		double[] origin = dims.originToDoubleArray(null);
		for (int i = 0; i < location.length; i++) {
			location[i] = RandomHelper.getUniform()
					.nextDoubleFromTo(0, dims.getDimension(i)/clusterFactor-origin[i]);
		}
	}


}
