package swarm_sim.agents;

import java.util.Iterator;

import org.jogamp.vecmath.Vector3d;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

public class Predator extends Boid {
	private Prey prey;
	private Vector3d attackVector;
	private ContinuousSpace<Object> space;
	private Context<Object> context;

	public Predator() {
		attackVector = new Vector3d();
	}

	@ScheduledMethod(start = 0)
	public void init() {
		context = ContextUtils.getContext(this);
		space = (ContinuousSpace<Object>) context.getProjection("space");
		// Save the initial position
		NdPoint q = space.getLocation(this);
		lastPosition = new Vector3d(q.getX(), q.getY(), q.getZ());
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		Parameters param = RunEnvironment.getInstance().getParameters();
		// Param: killRadius
		double killRadius = (Double) param.getValue("killRadius");
		// Modify our velocity update vector to take into account acceleration over time
		double preyAcceleration = (Double) param.getValue("preyAcceleration");
		// Param: predAcceleration
		double predAcceleration = (Double) param.getValue("predAcceleration");
		// Param: predMaxSpeed
		double predMaxSpeed = (Double) param.getValue("predMaxSpeed");
		// Param: timeScale
		double timeScale = (Double) param.getValue("timeScale");

		// Vector which will modify the boids velocity vector
		Vector3d velocityUpdate = new Vector3d();

		// How close the predator has to be to the prey to kill it
		if (prey == null || attackVector.lengthSquared() < killRadius * killRadius) {
			Iterator<Object> iter = context.getRandomObjects(Prey.class, 1).iterator();
			if (iter.hasNext())
				prey = (Prey) iter.next();
		}

		if (prey != null) {
			attackVector.sub(prey.getLastPosition(), lastPosition);
			velocityUpdate.add(attackVector);
		}

		// Update the velocity of the boid
		velocityUpdate.scale(predAcceleration * preyAcceleration * timeScale);
		// Apply the update to the velocity
		velocity.add(velocityUpdate);
		// If our velocity vector exceeds the max speed, throttle it back to the
		// MAX_SPEED
		if (velocity.length() > predMaxSpeed) {
			velocity.normalize();
			velocity.scale(predMaxSpeed);
		}
		// Update the position of the boid
		velocity.scale(timeScale);
		lastPosition.add(velocity);
		space.moveByDisplacement(this, velocity.x, velocity.y, velocity.z);
		
	}
}
