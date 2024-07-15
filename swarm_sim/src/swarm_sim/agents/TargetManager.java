package swarm_sim.agents;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jogamp.vecmath.Vector3d;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;
import swarm_sim.helpers.MovingAverageVector;

/**
 * @author traian
 * 
 * TaskManager keeps a collection of Map type having the keys as Target and the values as Set of Prey.
 * When Set size exceeds the value of targetKillNumber parameter all Prey from Set are converted to Killer. 
 * The Prey from Set are deleted from context. Then all keys associated with sets are deleted/
 *
 */
public class TargetManager {
	private static Map<Target, Set<Prey>> activeTargets = new ConcurrentHashMap<>();
	private ContinuousSpace<Object> space;

	public static Map<Target, Set<Prey>> getActiveTargets() {
		return activeTargets;
	}
	
	@ScheduledMethod(start = 1, interval = 5, priority = ScheduleParameters.LAST_PRIORITY)
	public void launchAttack() {
		Parameters param = RunEnvironment.getInstance().getParameters();
		Set<Target> keysTodelete = new HashSet<>();
		// Param: targetKillNumber
		int targetKillNumber = (int)param.getInteger("targetKillNumber");	
		activeTargets.forEach((k, v) -> {
			if (v.size()>= targetKillNumber ) {				
				v.forEach(e -> {
					Context<Object> context = ContextUtils.getContext(e);
					space = (ContinuousSpace<Object>) context.getProjection("space");
					NdPoint p = space.getLocation(e);
					Killer killer = new Killer();
					killer.setName(e.getName());
					killer.setTarget(k);
					killer.lastPosition = (Vector3d) e.lastPosition.clone();
					killer.velocity = (Vector3d) e.velocity.clone();
					try {
						killer.setSmoothedVelocity((MovingAverageVector) e.getSmoothedVelocity().clone());
					} catch (CloneNotSupportedException ex) {
						ex.printStackTrace();
					}
					context.add(killer);
					space.moveTo(killer, p.getX(), p.getY(), p.getZ());	
					context.remove(e);
				});
				keysTodelete.add(k);
				k.setEngaged(true);
			}		
		});		
		keysTodelete.forEach(activeTargets::remove);
	}
}