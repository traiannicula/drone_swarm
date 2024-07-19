import * as THREE from "three";
import { OrbitControls } from "three/addons/controls/OrbitControls.js";

// LOAD CONFIG
async function loadConfig(url) {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  const config = await response.json();
  return config;
}

loadConfig("config.json")
  .then((config) => {
    init(config);
  })
  .catch((error) => {
    console.error("Error loading config:", error);
  });

async function init(config) {
  // SCENE
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0xcee7f7);
  scene.fog = new THREE.Fog(scene.background, 1, 5000);
  // scene.fog = new THREE.Fog(0xa0a0a0, 100, 130);

  let defaultCamera = new THREE.PerspectiveCamera(
    75,
    window.innerWidth / window.innerHeight,
    0.1,
    1000
  );

  defaultCamera.position.y = 50;
  defaultCamera.name = "Defaul camera";
  let camera = defaultCamera;

  // GROUND

  const groundGeo = new THREE.PlaneGeometry(10000, 10000);
  const groundMat = new THREE.MeshLambertMaterial({ color: 0xffffff });
  groundMat.color.setHSL(0.095, 1, 0.75);

  const ground = new THREE.Mesh(groundGeo, groundMat);
  ground.position.y = 0.0;
  ground.rotation.x = -Math.PI / 2;
  ground.receiveShadow = true;
  scene.add(ground);

  const renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  document.body.appendChild(renderer.domElement);

  // LIGHTS
  const light = new THREE.DirectionalLight(0xffffff, 5.0);
  light.position.x = 3;
  light.position.y = 40;
  light.position.z = 2;
  light.castShadow = true; // default false
  scene.add(light);

  // CONTROLS

  const controls = new OrbitControls(camera, renderer.domElement);
  controls.maxPolarAngle = (0.9 * Math.PI) / 2;
  controls.enableZoom = true;
  let activeControls = controls;

  // CUBES TO MARK AREA
  // Create a cylinder geometry
  const dim = config.world_size * config.dim_scale;
  const geometry = new THREE.CylinderGeometry(0.1, 0.1, dim, 32); // Top radius, bottom radius, height, radial segments
  const material = new THREE.MeshBasicMaterial({ color: 0xff0000 }); // Red color
  // const geometry = new THREE.BoxGeometry(0.5, 1.5, 0.5);
  // const material = new THREE.MeshBasicMaterial({ color: 0x00ff00 });
  const cube1 = new THREE.Mesh(geometry, material);
  scene.add(cube1);

  const cube2 = new THREE.Mesh(geometry, material);
  cube2.position.x = dim;
  cube2.position.z = dim;
  scene.add(cube2);

  const cube3 = new THREE.Mesh(geometry, material);
  cube3.position.x = 0;
  cube3.position.z = dim;
  scene.add(cube3);

  const cube4 = new THREE.Mesh(geometry, material);
  cube4.position.x = dim;
  cube4.position.z = 0;
  scene.add(cube4);

  console.log(config.world_size * config.dim_scale);

  // CHANGE CAMERA
  document.addEventListener("keydown", (event) => {
    if (event.code === "KeyC") {
      if (camera === defaultCamera) {
        const drone = scene.getObjectByName("drone150");
        if (drone) {
          camera = drone.getObjectByName("PerspectiveCamera");
          activeControls = null; // Disable controls for drone camera
          console.log("Switched to Drone Camera");
        }
      } else {
        camera = defaultCamera;
        activeControls = controls; // Re-enable controls for default camera
        console.log("Switched to Default Camera");
      }
    }
  });

  // LOADER
  const loader = new THREE.ObjectLoader();

  // LOAD DRONE
  const drone_sample = await loader.loadAsync("./models/drone_sample.json");
  drone_sample.position.y = -3.0;
  scene.add(drone_sample);

  // LOAD CAR
  const car_sample = await loader.loadAsync("./models/sample_sport_car.json");
  car_sample.position.y = -3.0;
  scene.add(car_sample);

  // LOAD FLAME
  const flame_sample = await loader.loadAsync("./models/flame.json");
  flame_sample.position.y = -6;
  scene.add(flame_sample);


  let drones = new Map();
  let targets = new Map();
  let isCamSet = false;
  let simObj;

  // smoothing atempt
  let alpha = 0.30; // Smoothing factor, between 0 and 1
  let smoothingFactor = 0.2; // Adjust this value between 0 (no smoothing) and 1 (maximum smoothing)

  function smoothAngle(currentAngle, previousAngle) {
    let smoothedAngle =
      previousAngle * (1 - smoothingFactor) + currentAngle * smoothingFactor;
    return normalizeAngle(smoothedAngle);
  }

  function smoothAngleEMA(currentAngle, previousAngle) {
    let smoothedAngle = alpha * currentAngle + (1 - alpha) * previousAngle;
    return normalizeAngle(smoothedAngle);
  }

  function normalizeAngle(angle) {
    angle = angle % (2 * Math.PI);
    if (angle < 0) {
      angle += 2 * Math.PI;
    }
    return angle;
  }

  window.addEventListener("resize", onWindowResize);

  function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  }

  function animate() {
    if (simObj != null) {
      simObj.drones.forEach((d) => {
        let drone = drones.get(d.name);
        if (drone == null) {
          drone = drone_sample.clone();
          drone.name = d.name;
          drones.set(d.name, drone);
          scene.add(drone);
        }
        drone.position.x = d.location.x;
        drone.position.y = d.location.y;
        drone.position.z = d.location.z;

        // YAW - rotation about Y axis in global system
        if (drone.rotation.y !== d.angle.y) {
          drone.rotation.y = smoothAngle(d.angle.y, drone.rotation.y);
          // drone.rotation.y = smoothAngleEMA(d.angle.y, drone.rotation.y);
        }
        // drone.rotation.y = d.angle.y;

        // PITCH- rotation about X axis - drone local system
        if (drone.children[0].rotation.x !== d.angle.x) {
          drone.children[0].rotation.x = smoothAngle(d.angle.x, drone.children[0].rotation.x);
          // drone.children[0].rotation.x = smoothAngleEMA(d.angle.x, drone.children[0].rotation.x);
        }
        // drone.children[0].rotation.x = d.angle.x;
      });

      simObj.targets.forEach((t) => {
        let target = targets.get(t.name);
        if (target == null) {
          target = car_sample.clone();
          target.name = t.name;
          targets.set(t.name, target);
          scene.add(target);
        }
        if (t.damaged == true){
          let flame = flame_sample.clone();
          flame.position.x = t.location.x;
          flame.position.y = -0.6;
          flame.position.z = t.location.z;
          scene.add(flame);
          targets.delete(t.name);
        } else {
          target.position.x = t.location.x;
          target.position.y = t.location.y;
          target.position.z = t.location.z;
  
          target.rotation.x = 0.0;
          target.rotation.y = t.angle.y + Math.PI;
          target.rotation.z = 0.0;
        }
      });
    }

    if (activeControls) {
      activeControls.update();
    }

    renderer.render(scene, camera);
    requestAnimationFrame(animate);
  }

  animate();

  // WS start
  const endpoint = "ws://localhost:8080/chat/viewer";
  let wscon;

  function connectToWS() {
    if (wscon !== undefined) {
      wscon.close();
    }

    wscon = new WebSocket(endpoint);

    wscon.onmessage = function (event) {
      // console.time("functionExecutionTime");
      simObj = JSON.parse(event.data);
      // console.timeEnd("functionExecutionTime");
    };

    wscon.onopen = function (evt) {
      console.log("onopen.");
    };

    wscon.onclose = function (evt) {
      console.log("onclose.");
    };

    wscon.onerror = function (evt) {
      console.log("Error!");
    };
  }

  function sendMsg() {
    wscon.send(message);
  }

  function closeConn() {
    wscon.close();
  }

  connectToWS();
}
