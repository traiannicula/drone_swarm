package swarm_sim.agents;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;
import swarm_sim.helpers.Aim;
import swarm_sim.helpers.Drone;
import swarm_sim.helpers.Utils;
import swarm_sim.helpers.WsClient;



public class SceneUpdater {
	Context<Object> context;
	private WsClient wsc;
	private long start;

	@ScheduledMethod(start = 0)
	public void init() {
		start = System.currentTimeMillis();
		if (context == null)
			context = ContextUtils.getContext(this);
		try {
			wsc = new WsClient(new URI("ws://localhost:8080/chat/sim"));
			wsc.connect();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down WS connection");
                wsc.close();
            }
        });
	}

	@ScheduledMethod(start = 20, interval = 1,  priority = ScheduleParameters.LAST_PRIORITY)
	public void update() {
		    long current = System.currentTimeMillis();
		    if (current - start > 30) {
				String json = contextToJson();
				if (wsc.isOpen())
					wsc.send(json);	
				start = current;
		    }

	}

	private String contextToJson() {
		List<Drone> drones = context.getObjectsAsStream(Prey.class).map(p -> (Prey) p).map(p -> new Drone(p.getName(),
				p.getSmoothedPosition(), p.getRotationAngles()))
				.collect(Collectors.toList());
		drones.addAll(context
				.getObjectsAsStream(Killer.class).map(k -> (Killer) k).map(k -> new Drone(k.getName(),
						k.getLastPosition(), k.getRotationAngles()))
				.toList());
		List<Aim> targets = context.getObjectsAsStream(Target.class).map(t -> (Target) t)
				.map(t -> new Aim(t.getName(), t.getLastPosition(), t.getRotationAngles(), t.isDamaged())).toList();
		return Utils.agentsToJson(drones, targets);
	}

}
