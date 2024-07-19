package swarm_sim.agents;

import org.jogamp.vecmath.Vector3d;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;
import swarm_sim.helpers.Utils;



/**
 * @author traian
 * Target class is resposible with movement on linear trajectory. There is slight leaning angle for those lines.
 * Another purpose of this class is to assess self damage when the number of Killers in its vecinity (targetKillRadius) reaches
 * the value of targetKillNumber parameter. In this instance the Target is assumed damaged and stops. 
 */
public class Target extends Boid {
	private Context context;
	private ContinuousSpace<Object> space;
	private boolean engaged;
	private boolean damaged;
	private double yAngle;
	private Parameters param;

	public Target() {
		param = RunEnvironment.getInstance().getParameters();
		// Param: targetMaxSpeed
		double timeScale = param.getDouble("timeScale");
		// Param: targetAttractionRadius
		double targetMaxSpeed = param.getDouble("targetMaxSpeed");
		yAngle = Utils.normalizeAngle(RandomHelper.getUniform().nextDoubleFromTo (Math.PI / 5, 4 * Math.PI / 5));
		Vector3d delta = new Vector3d( Math.sin(yAngle), 0,  Math.cos(yAngle));
		delta.normalize();
		delta.scale(targetMaxSpeed);
		delta.scale(timeScale);
		velocity.add(delta);
	}
	

	@ScheduledMethod(start = 1, interval = 1)
	public void move() { 
		// Param: timeScale

		double targetAttractionRadius = (double) param.getDouble("targetAttractionRadius");
		if (lastPosition.x < 400 && !damaged) {
			if (context == null)
				context = ContextUtils.getContext(this);
			if (space == null)
				space = (ContinuousSpace<Object>) context.getProjection("space");
			lastPosition.add(velocity);
			space.moveByDisplacement(this, velocity.x, 0, velocity.z);
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void assessDamage(){
		// Param: targetAttractionRadius
		double targetKillRadius = (double) param.getDouble("targetKillRadius");
		// Param: targetKillNumber
		int targetKillNumber = (int)param.getInteger("targetKillNumber");
		if (context == null)
			context = ContextUtils.getContext(this);
		if (space == null)
			space = (ContinuousSpace<Object>) context.getProjection("space");
		ContinuousWithin<Object> query = new ContinuousWithin<>(context, this, targetKillRadius);
		int count = 0;
		for (Object o : query.query()){
			if (o instanceof Killer k) {
				count++;
			}
		}
		if (count >= targetKillNumber)
			damaged = true;
	}
	
	public boolean isEngaged() {
		return engaged;
	}

	public void setEngaged(boolean engaged) {
		this.engaged = engaged;
	}

	public boolean isDamaged() {
		return damaged;
	}

	public void setDamaged(boolean damaged) {
		this.damaged = damaged;
	}

	@Override
	public Vector3d getRotationAngles() {	
		return new Vector3d(0.0, yAngle, 0.0);
	}
}

