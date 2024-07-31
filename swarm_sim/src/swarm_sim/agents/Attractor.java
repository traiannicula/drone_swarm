package swarm_sim.agents;

import org.jogamp.vecmath.Vector3d;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;

public class Attractor {
	private ContinuousSpace<Object> space;
	private int count;
	
	public Attractor() {
		count = 0;
	}

//	@ScheduledMethod(start = 5000, interval = 10000, priority = ScheduleParameters.LAST_PRIORITY)
	@ScheduledMethod(start = 6000)
	public void init() {
		Context<Object> context = ContextUtils.getContext(this);
		if (space == null)
			space = (ContinuousSpace<Object>) context.getProjection("space");
	
		// add 4 new attrators
		Dimensions dims = space.getDimensions();
		for (int i = 0; i < RandomHelper.getUniform().nextIntFromTo(1, 4); i++) {
			Target target = new Target();
			target.setName("target" + ++count);
			context.add(target);
			double initZ = RandomHelper.nextDoubleFromTo(dims.getDimension(2)/4, 3 * dims.getDimension(2)/4);
			double initX = -dims.getDimension(1);
			target.lastPosition.add(new Vector3d(initX, 0, initZ));
			space.moveTo(target, initX, 0, initZ);	
		}
	}
		
}
