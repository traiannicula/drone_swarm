package swarm_sim.agents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.BouncyBorders;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.InfiniteBorders;
import repast.simphony.space.graph.Network;

public class SwarmBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		Parameters param = RunEnvironment.getInstance().getParameters();
		// Param: initialNumPrey
		int initialNumPrey = (Integer) param.getValue("initialNumPrey");
		// Param: initialNumPred
		int initialNumPred = (Integer) param.getValue("initialNumPred");
//		BouncyBorders border = new BouncyBorders();
		InfiniteBorders border = new InfiniteBorders<>();

		ContinuousSpaceFactory csFctory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = csFctory.createContinuousSpace("space", context, new Adder<>(4), border, 300,
				200, 300);
//		ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).createContinuousSpace("space", context,
//				new Adder<>(4), border, new double[]{300, 300, 300}, new double[]{0, 100, 0});
		Network network = NetworkFactoryFinder.createNetworkFactory(null).createNetwork("track", context, false);
		// Add the prey
		List<Prey> preyList = new ArrayList<Prey>();
		for (int i = 0; i < initialNumPrey; i++) {
			Prey prey = new Prey();
			prey.setName("drone" + i);
			preyList.add(prey);
		}
		context.addAll(preyList);

		// Add the predators
		for (int i = 0; i < initialNumPred; i++) {
			context.add(new Predator());
		}

		// Add one attractor
		Attractor attractor = new Attractor();
		context.add(attractor);
		space.moveTo(attractor, 150, 100, 150);

		// Add targetManager
		TargetManager tm = new TargetManager();
		context.add(tm);

		// Add SceneUpdater
		SceneUpdater su = new SceneUpdater();
		context.add(su);

		// clean up debug data
		Path path = Paths.get("debugData.txt");
		try {
			if (Files.exists(path))
				Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RunEnvironment.getInstance().endAt(12000); 

		return context;
	}

}
