package swarm_sim.agents;

import org.jogamp.vecmath.Vector3d;
import swarm_sim.helpers.MovingAverageVector;
import swarm_sim.helpers.Utils;


public abstract class Boid {
	protected String name;
	protected Vector3d velocity = new Vector3d();
	protected Vector3d lastPosition = new Vector3d();
	private int smoothingFactor;
	protected MovingAverageVector smoothedPosition;
	protected MovingAverageVector smoothedVelocity;

	public Boid() {
		smoothingFactor = 500;
		smoothedPosition = new MovingAverageVector(smoothingFactor);
		smoothedVelocity = new MovingAverageVector(smoothingFactor);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector3d getLastPosition() {
		return lastPosition;
	}

	public Vector3d getVelocity() {
		return velocity;
	}

	public void setSmoothedPosition(MovingAverageVector smoothedPosition) {
		this.smoothedPosition = smoothedPosition;
	}

	public Vector3d getSmoothedPosition() {
		return smoothedPosition.getAverage(lastPosition);
	}

	public MovingAverageVector getSmoothedVelocity() {
		return smoothedVelocity;
	}

	public void setSmoothedVelocity(MovingAverageVector smoothedvelocity) {
		this.smoothedVelocity = smoothedvelocity;
	}

	public Vector3d getRotationAngles() {
		Vector3d speed = smoothedVelocity.getAverage(velocity);
		double yA = Utils.getYRotation(speed);
		double xA = Utils.getXRotation(speed, yA);
		return new Vector3d(xA, yA, 0.0);
	}
}
