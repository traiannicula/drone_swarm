
# Swarm of Drones Simulation Testbed

The concept of autonomous drone swarms is gaining significant attention. This project aims to develop an experimental platform for testing drone swarms in a simulated environment. The drones' flight patterns are modeled using the boids algorithm and implemented via the Repast Simphony agent-based modeling toolkit. Equipped with cameras, the drones can identify and engage targets within their designated areas. To enhance the realism of the simulated environment, Three.js is utilized for displaying 3D animated objects.

The project's ultimate goal is to implement the architectural diagram shown below. Currently, only a partial implementation, referred to as Part 1, has been completed.

![enter image description here](https://github.com/traiannicula/drone_swarm/blob/main/files/diagram.png)

The repository is made up of two projects:

 1. **swarm_sim** - The simulation component, developed using Repast Simphony, models and runs the simulation. It exchanges data, in JSON     format via WebSocket, with the visualization component built with     Three.js. It was developed with Repast Simphony 2.11.0     (https://repast.github.io/) and requires it to run.
    
   
 2. **swarm_viewer** - The 3D visualization component is built with Three.js and it is a VS Code Quarkus project. Quarkus serves as the  WebSocket endpoint holder and the HTTP server for the Three.js  application. It has to be started before simulation. The command to  start Quarkus in development mode is `quarkus dev`. Quarkus app is now  running at [localhost:8080](http://localhost:8080).


## Preview
A brief preview of the simulation run and 3D visualization can be seen in the video below. The left side shows the Repast viewer with the described agents, while the right side displays the simulation results in the Three.js viewer.

[![Swarm of Drones Simulation Testbed](https://img.youtube.com/vi/YyVbCgaHDXU/0.jpg)](https://youtu.be/YyVbCgaHDXU "Swarm of Drones Simulation Testbed")



