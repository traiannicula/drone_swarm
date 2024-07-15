package swarm_sim.agents;

import org.jogamp.vecmath.Vector3d;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;

/**
 * @author traian
 *  Killer instances are generated from Prey by TargetManage class, when the number of Prey reached targetKillNumber value.
 *  Killer has only one purpose to reach target as soon as possible. All kinematic values are transfer from Prey to Killer.
 *  There is a limit on Killer max speed set by preyMaxSpeed parameter.
 */
public class Killer extends Boid {
	private Context context;
	private ContinuousSpace<Object> space;
	private Target target;
	
	
	public Killer() {
		super();
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		Vector3d velocityUpdate = new Vector3d();
		Parameters param = RunEnvironment.getInstance().getParameters();
		// Param: timeScale
		double timeScale = param.getDouble("timeScale");
		// Param: preyAcceleration
		double preyAcceleration = param.getDouble("preyAcceleration");
		// Param: preyMaxSpeed
		double preyMaxSpeed = param.getDouble("preyMaxSpeed");
		// Param: targetAttractionForce
		double targetAttractionForce = (double) param.getDouble("targetAttractionForce");
		// Param: targetAttractionRadius
		double targetAttractionRadius = (double) param.getDouble("targetAttractionRadius");

		if (context == null)
			context = ContextUtils.getContext(this);
		if (space == null)
			space = (ContinuousSpace<Object>) context.getProjection("space");
	
		NdPoint p = space.getLocation(target);
		Vector3d targetPos = new Vector3d(p.getX(), p.getY(), p.getZ());
		Vector3d distanceToTarget = new Vector3d();
		distanceToTarget.sub(targetPos, this.getLastPosition());
		if (distanceToTarget.lengthSquared() < targetAttractionRadius * targetAttractionRadius) {
			Vector3d v = new Vector3d(0, 0, 0);
			v.normalize(distanceToTarget);
			v.scale(targetAttractionForce);
			velocityUpdate.add(v);
		} else {
			distanceToTarget.scale(targetAttractionForce);
			velocityUpdate.add(distanceToTarget);
		}

		velocityUpdate.scale(preyAcceleration * timeScale);
		velocity.add(velocityUpdate);
		
		// make them go faster
		double killerMaxSpeed = 3 * preyMaxSpeed;
		if (velocity.lengthSquared() > killerMaxSpeed * killerMaxSpeed) {
			velocity.normalize();
			velocity.scale(killerMaxSpeed);
		}
		// Update the position of the boid
		velocity.scale(timeScale);
		lastPosition.add(velocity);
		
		Vector3d pos = smoothedPosition.getAverage(lastPosition);
		Vector3d speed = smoothedVelocity.getAverage(velocity);

//		space.moveByDisplacement(this, velocity.x, velocity.y, velocity.z);
	}

	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void move() {
		space.moveByDisplacement(this, velocity.x, velocity.y, velocity.z);
	}
	
	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}
	
}
