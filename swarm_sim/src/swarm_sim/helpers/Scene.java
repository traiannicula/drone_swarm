package swarm_sim.helpers;

import java.util.List;

public class Scene {
	private List<Drone> drones;
	private List<Aim> targets;

	public Scene() {

	}

	public Scene(List<Drone> drones, List<Aim> targets) {
		this.drones = drones;
		this.targets = targets;
	}

	public List<Drone> getDrones() {
		return drones;
	}

	public void setDrones(List<Drone> drones) {
		this.drones = drones;
	}

	public List<Aim> getTargets() {
		return targets;
	}

	public void setTargets(List<Aim> targets) {
		this.targets = targets;
	}

}
