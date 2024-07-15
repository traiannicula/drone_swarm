package swarm_sim.helpers;

import org.jogamp.vecmath.Vector3d;

public class Aim {
	private String name;
	private Vector3d location;
	private Vector3d angle;
	private boolean damaged;

	public Aim() {

	}

	public Aim(String name, Vector3d location, Vector3d angle, boolean damaged) {
		super();
		this.name = name;
		this.location = (Vector3d) location.clone();
		this.location.scale(Utils.DIM_SCALE); 
		this.angle = angle;
		this.damaged = damaged;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector3d getLocation() {
		return location;
	}

	public void setLocation(Vector3d location) {
		this.location = location;
	}

	public Vector3d getAngle() {
		return angle;
	}

	public void setAngle(Vector3d angle) {
		this.angle = angle;
	}

	public boolean isDamaged() {
		return damaged;
	}

	public void setDamaged(boolean damaged) {
		this.damaged = damaged;
	}

	@Override
	public String toString() {
		return "Aim [name=" + name + ", location=" + location + ", angle=" + angle + ", damaged=" + damaged + "]";
	}

}