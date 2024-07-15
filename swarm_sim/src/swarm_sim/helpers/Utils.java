package swarm_sim.helpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.jogamp.vecmath.Vector3d;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import repast.simphony.engine.environment.RunEnvironment;

public class Utils { 
	private final static double MIN_VAL = 0.001;
	private static final double TIME_SCALE = RunEnvironment.getInstance().getParameters().getDouble("timeScale");
	private static final double PREY_MAX_SPEED = RunEnvironment.getInstance().getParameters().getDouble("preyMaxSpeed");
	private static final double MAX_PITCH = Math.toRadians(15.0); // pitch is hardcoded for now
	public final static double DIM_SCALE = 0.2; // scale dimension for viewer

	public static void main(String[] args) throws JsonProcessingException {


	}

	/**
	 * It computes rotation on y axis. It works only when the angle between velocity
	 * falls in range 0 - 90 (deg)
	 */
	public static double getYRotation(Vector3d v) {
		double x = (Math.abs(v.x) < MIN_VAL ? 0.0 : v.x);
		double z = (Math.abs(v.z) < MIN_VAL ? 0.0 : v.z);
		double a = Math.atan2(x, z);
		double yAngle = (a >= 0 ? a : (2 * Math.PI + a));
		return yAngle;
	}

	public static double getXRotation(Vector3d v, double yAngle) {
		double x = (Math.abs(v.x) < MIN_VAL ? 0.0 : v.x);
		double z = (Math.abs(v.z) < MIN_VAL ? 0.0 : v.z);
		double localZ = z * Math.cos(yAngle) + x * Math.sin(yAngle);
		return clamp(localZ / (TIME_SCALE * PREY_MAX_SPEED)) * MAX_PITCH;
	}

	public static String agentsToJson(List<Drone> drones, List<Aim> targets) {
		ObjectMapper om = new ObjectMapper();
		Scene scene = new Scene(drones, targets);
		String json = null;
		try {
			json = om.writeValueAsString(scene);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static double normalizeAngle(double angle) {
		angle = angle % (2 * Math.PI);
		if (angle < 0) {
			angle += 2 * Math.PI;
		}
		return angle;
	}

	private static double clamp(double value) {
		if (value < -1.0)
			return -1.0;
		if (value > 1.0)
			return 1.0;
		return value;
	}

	public static void writeToFile(String content) {
		Path path = Paths.get("debugData.txt");
		try {
			Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.APPEND,
					StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}