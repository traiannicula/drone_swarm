package swarm_sim.agents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.jogamp.vecmath.Vector3d;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;
import swarm_sim.helpers.Utils;

/**
 * @author traian
 * 
 *         Prey moves based on boids simulation rules: separation, alignment and
 *         cohesion. It tries to escape from predators and in the same time is
 *         attracted by an Attractor. The purpose of the Attractor is to keep
 *         the preys into a confined box. When a target is detected the Prey is
 *         transfered to synchronized collections of TargetManager for
 *         processing. Prey can be converted to Killer when targetKillNumber
 *         parameter value is reached.
 */
public class Prey extends Boid {

	private List<Prey> neighbors;
	private List<Predator> predators; // List of Predators
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	private Parameters param;

	public Prey() {
		super();
		neighbors = new ArrayList<>();
		predators = new ArrayList<>();
		param = RunEnvironment.getInstance().getParameters();
	}

	/**
	 * init() method executes only once. It finds the closest neighbors of current
	 * agent.
	 */
	@ScheduledMethod(start = 0)
	public void init() {
		if (context == null)
			context = ContextUtils.getContext(this);
		if (space == null)
			space = (ContinuousSpace<Object>) context.getProjection("space");
		// Param: initialPreyNeighborDistance
		int initialPreyNeighborDistance = (Integer) param.getValue("initialPreyNeighborDistance");
		// Param: maxPreyNeighbors
		int maxPreyNeighbors = (Integer) param.getValue("maxPreyNeighbors");

		ContinuousWithin<Object> query = new ContinuousWithin<>(context, this, initialPreyNeighborDistance);
		NdPoint q = space.getLocation(this);
		lastPosition = new Vector3d(q.getX(), q.getY(), q.getZ());

		// finds distance to each neighbors
		TreeMap<Double, Prey> foundNeigh = new TreeMap<>();
		query.query().forEach(o -> {
			if (o instanceof Prey neigh) {
				NdPoint p = space.getLocation(neigh);
				Vector3d neighPosition = new Vector3d(p.getX(), p.getY(), p.getZ());
				Vector3d distanceToNeighbor = new Vector3d();
				distanceToNeighbor.sub(neighPosition, lastPosition);
				foundNeigh.put(distanceToNeighbor.length(), neigh);
			}
		});

		// selects the closest neighbors
		int count = 0;
		for (Map.Entry<Double, Prey> e : foundNeigh.entrySet()) {
			neighbors.add(e.getValue());
			count++;
			if (count >= maxPreyNeighbors)
				break;
		}
		// Initially find the predators and keep track of them here
		Context context = ContextUtils.getContext(this);
		Iterator<Predator> iter = context.getAgentLayer(Predator.class).iterator();
		while (iter.hasNext()) {
			predators.add(iter.next());
		}
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		Vector3d velocityUpdate = new Vector3d();
		// PREY
		// Param: timeScale
		double timeScale = param.getDouble("timeScale");
		// Param: preySpacing
		double preySpacing = param.getDouble("preySpacing");
		// Param: preyAttractForce
		double preyAttractForce = param.getDouble("preyAttractForce");
		// Param: preyRepelForce
		double preyRepelForce = param.getDouble("preyRepelForce");
		// Param: preyFearRadius
		double preyFearRadius = (Double) param.getValue("preyFearRadius");
		// Param: preyFearForce
		double preyFearForce = (Double) param.getValue("preyFearForce");
		// Param: preyAcceleration
		double preyAcceleration = param.getDouble("preyAcceleration");
		// Param: preyMaxSpeed
		double preyMaxSpeed = param.getDouble("preyMaxSpeed");
		// Param: maxPreyNeighbors
		int maxPreyNeighbors = (Integer) param.getValue("maxPreyNeighbors");

		// ATTRACTOR
		// Param: preyAttractorForce
		double preyAttractorForce = (Double) param.getValue("preyAttractorForce");
		// Param: preyAttractorRadius
		double preyAttractorRadius = (Double) param.getValue("preyAttractorRadius");

		// TARGETS
		// Param: targetKillNumber
		int targetKillNumber = (int) param.getInteger("targetKillNumber");
		// Param: targetAttractionForce

		// vector resultant of vector distances from current to neighbors
		// scaled with preyAttractForce
		neighbors.forEach(neigh -> {
			Vector3d distanceToNeighbor = new Vector3d();
			distanceToNeighbor.sub(neigh.getLastPosition(), lastPosition);
			// too far, get closer
			if (distanceToNeighbor.lengthSquared() >= preySpacing * preySpacing) {
				distanceToNeighbor.scale(preyAttractForce);
				velocityUpdate.add(distanceToNeighbor);
			}
			// too close, fly away
			else {
				distanceToNeighbor.scale(preyRepelForce);
				velocityUpdate.add(distanceToNeighbor);
			}
		});
		// calculate center of mass
		velocityUpdate.scale(1.0 / maxPreyNeighbors);

		// Avoid the predators
		// Loop through each predator and get the vector to it
		predators.forEach(pred -> {
			// Get the vector from this boid to its neighbor
			Vector3d distanceToFalcon = new Vector3d();
			distanceToFalcon.sub(pred.getLastPosition(), lastPosition);
			// If it is within range, add the vector to the repulsion vector
			if (distanceToFalcon.lengthSquared() < preyFearRadius * preyFearRadius) {
				// The closer the predator is, the more weight it will have
				Vector3d v = new Vector3d(0, 0, 0);
				v.normalize(distanceToFalcon);
				v.scale(preyFearForce);
				velocityUpdate.add(v);
			}
		});

		// ATTRACTION
		List<Attractor> attractors = context.getObjectsAsStream(Attractor.class).map(a -> (Attractor) a)
				.sorted(Comparator.comparingDouble(a -> {
					NdPoint p = space.getLocation(a);
					Vector3d attrPosition = new Vector3d(p.getX(), p.getY(), p.getZ());
					Vector3d distanceToAttr = new Vector3d();
					distanceToAttr.sub(attrPosition, lastPosition);
					return distanceToAttr.lengthSquared();
				})).collect(Collectors.toList());

		Attractor attractor = null;
		if (attractors.size() > 0)
			attractor = attractors.get(0);

		if (attractor != null) {
			NdPoint p = space.getLocation(attractor);
			Vector3d attractorPos = new Vector3d(p.getX(), p.getY(), p.getZ());
			Vector3d distanceToAttractor = new Vector3d();
			distanceToAttractor.sub(attractorPos, this.getLastPosition());
			if (distanceToAttractor.lengthSquared() < preyAttractorRadius * preyAttractorRadius) {
				Vector3d v = new Vector3d(0, 0, 0);
				v.normalize(distanceToAttractor);
				v.scale(preyAttractorForce);
				velocityUpdate.add(v);
			}
		}

		// TARGETS
		context.getObjectsAsStream(Target.class).map(t -> (Target) t).filter(t -> !t.isEngaged())
				.sorted(Comparator.comparingDouble(t -> {
					NdPoint p = space.getLocation(t);
					Vector3d trgPosition = new Vector3d(p.getX(), p.getY(), p.getZ());
					Vector3d distanceToTarget = new Vector3d();
					distanceToTarget.sub(trgPosition, lastPosition);
					return distanceToTarget.lengthSquared();
				})).findFirst().ifPresent(t -> {
					Set<Prey> preyList = TargetManager.getActiveTargets().get(t);
					if (preyList != null) {
						if (preyList.size() < targetKillNumber)
							TargetManager.getActiveTargets().get(t).add(this);
					} else {
						preyList = new CopyOnWriteArraySet<>();
						preyList.add(this);
						TargetManager.getActiveTargets().put(t, preyList);
					}

				});

		velocityUpdate.scale(preyAcceleration * timeScale);
		velocity.add(velocityUpdate);
		if (velocity.lengthSquared() > preyMaxSpeed * preyMaxSpeed) {
			velocity.normalize();
			velocity.scale(preyMaxSpeed);	
		} 
		// Update the position of the boid
		velocity.scale(timeScale);
		lastPosition.add(velocity);


		Vector3d pos = smoothedPosition.getAverage(lastPosition);
		Vector3d speed = smoothedVelocity.getAverage(velocity);
		// debug zone
//		double yA = Utils.getYRotation(speed);
//		double xA = Utils.getXRotation(speed, yA);
//		if (name.equals("drone1")) {
//			Utils.writeToFile( Math.sqrt(lastPosition.lengthSquared()) + System.lineSeparator());
//		}

	}

	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void move() {
		space.moveByDisplacement(this, velocity.x, velocity.y, velocity.z);
	}

}