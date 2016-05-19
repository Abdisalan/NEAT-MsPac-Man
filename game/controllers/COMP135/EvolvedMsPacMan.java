package game.controllers.COMP135;

import game.controllers.PacManController;
import game.core.Game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abdisalan on 4/22/16.
 */
public class EvolvedMsPacMan implements PacManController {
    public static final int MAX_PILL_DISTANCE = 200;
    public static final int MAX_JUNCTION_DISTANCE = 26;
    public static final int MAX_GHOST_DISTANCE = 200;

    public NEAT neuralnetwork;

    @Override
    public int getAction(Game game, long timeDue) {

        double[] inputs = collectInputs(game);

        //update the current genome network with inputs and evaluate the network
        Species s = neuralnetwork.mspacman_pool.species.get(neuralnetwork.mspacman_pool.currentSpecies);
        Genome g = s.genomes.get(neuralnetwork.mspacman_pool.currentGenome);
        g.network = neuralnetwork.updateInputs(g.network, inputs);
        int direction = neuralnetwork.evaluateNetwork(g.network);


        return direction;
    }

    public EvolvedMsPacMan() {
       neuralnetwork = new NEAT();
    }

    public static double[] collectInputs(Game game) {
        /**
         * BEGIN Collect all the UNDIRECTED INPUTS
          */
        // Proportion Pills
        // Number of regular pills left in maze
        // **adjusted to be between 0 and 1**
        double proportionPills = ((double) game.getNumberPills())/game.getNumActivePills();


        // Proportion Power Pills
        // Number of power pills left in maze
        // **adjusted to be between 0 and 1**
        double proportionPowerPills = ((double) game.getNumberPowerPills())/game.getNumActivePowerPills();


        // Number of Edible Ghosts
        // **adjusted to be between 0 and 1**
        double numEdibleGhosts = 0.0;
        for (int i = 0; i < Game.NUM_GHOSTS; i++) {
            if (game.isEdible(i)) numEdibleGhosts++;
        }

        numEdibleGhosts = numEdibleGhosts / Game.NUM_GHOSTS;


        // Remaining ghost edible time
        // **adjusted to be between 1 and 0**
        double edibleGhostTime = 0.0;
        for (int i =0; i < Game.NUM_GHOSTS; i++) {
            edibleGhostTime += game.getEdibleTime(i);
        }
        edibleGhostTime /= ((double) Game.NUM_GHOSTS * game.EDIBLE_TIME);


        // Any Ghost Edible?
        // **adjusted to be EITHER 0 or 1**
        int anyGhostEdible = (numEdibleGhosts > 0) ? 1 : 0;


        // All Threat Ghosts Present?
        // 1 if four threats are outside 0 otherwise
        int numGhostThreats = 0;
        for (int i = 0; i < Game.NUM_GHOSTS; i++) {
            if (game.getLairTime(i) > 0) numGhostThreats++;
        }
        numGhostThreats = (numGhostThreats == Game.NUM_GHOSTS) ? 1 : 0;

        // Close to Power Pill?
        // 1 if Ms. Pacman is within 10 steps of a power pill, 0 otherwise
        int closeToPowerPill = 0;
        int msPacmanLocation = game.getCurPacManLoc();
        int[] powerPillIndexes = game.getPowerPillIndicesActive();
        for (int i = 0; i < powerPillIndexes.length; i++) {
            if (game.getPathDistance(msPacmanLocation, powerPillIndexes[i]) < 10) {
                closeToPowerPill = 1;
            }
        }

        /**
         * END Collect all UNDIRECTED INPUTS
         */

        /**
         * BEGIN Collect all Directed Inputs
         *
         * TODO: Incorporate mspacman's current direction in these somehow...
         */

        // Nearest Pill Distance
        // **adjusted to be between 0 and 1**
        double nearestPillDistance = 0.0;
        int mspacmanCurrentLocation = game.getCurPacManLoc();
        int[] pills = game.getPillIndicesActive();
        int closestPill = game.getTarget(mspacmanCurrentLocation, pills, true, Game.DM.PATH);
        nearestPillDistance = game.getPathDistance(mspacmanCurrentLocation, closestPill);
        nearestPillDistance /= MAX_PILL_DISTANCE;


        // Nearest Power Pill Distance
        // **adjusted to be between 0 and 1**
        double nearestPowerPillDistance = 0.0;
        int[] powerpills = game.getPowerPillIndicesActive();
        int closestPowerPill = game.getTarget(mspacmanCurrentLocation, powerpills, true, Game.DM.PATH);
        nearestPowerPillDistance = game.getPathDistance(mspacmanCurrentLocation, closestPowerPill);
        nearestPowerPillDistance /= MAX_PILL_DISTANCE;


        // Nearest Junction distance
        // **adjusted to be between 0 and 1**
        int[] junctions = game.getJunctionIndices();
        int closestJunction = game.getTarget(mspacmanCurrentLocation, junctions, true, Game.DM.PATH);
        double nearestJunctionDistance = game.getPathDistance(mspacmanCurrentLocation, closestJunction);
        nearestJunctionDistance /= MAX_JUNCTION_DISTANCE;
        if (nearestJunctionDistance > 1) System.out.println(nearestJunctionDistance);

        // Max Pills in 30 steps
        // **adjusted to be between 0 and 1**
        double pillsWithin30steps = 0;
        for (int i = 0; i < pills.length; i++) {
            if (game.getPathDistance(mspacmanCurrentLocation, pills[i]) < 30) {
                pillsWithin30steps++;
            }
        }
        pillsWithin30steps /= pills.length;


        // Max Junctions in 30 steps
        // **adjusted to be between 0 and 1**
        double junctionsWithin30steps = 0.0;
        for (int i = 0; i < junctions.length; i++) {
            if (game.getPathDistance(mspacmanCurrentLocation, junctions[i]) < 30) {
                junctionsWithin30steps++;
            }
        }
        junctionsWithin30steps /= junctions.length;
        /**
         * END Collect all Directed Inputs
         */

        /**
         * BEGIN Ghost Sensors
         */

        // Closest Ghost Distance
        // **adjusted to be between 0 and 1**
        double closestGhostDistance = Integer.MAX_VALUE;
        for (int i = 0; i < Game.NUM_GHOSTS; i++) {
            double distance = game.getPathDistance(mspacmanCurrentLocation, game.getCurGhostLoc(i));
            if (distance < closestGhostDistance) closestGhostDistance = distance;
        }
        closestGhostDistance /= MAX_GHOST_DISTANCE;

        /**
         * END Ghost Sensors
         */


        double[] inputs = { closestGhostDistance,
                            junctionsWithin30steps,
                            pillsWithin30steps,
                            nearestJunctionDistance,
                            nearestPowerPillDistance,
                            nearestPillDistance,
                            closeToPowerPill,
                            numGhostThreats,
                            numEdibleGhosts,
                            anyGhostEdible,
                            proportionPowerPills,
                            proportionPills
        };


        return inputs;
    }
}
