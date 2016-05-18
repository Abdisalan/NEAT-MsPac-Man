package game.controllers.COMP135;

import java.util.ArrayList;

public class Pool {
    public ArrayList<Species> species;
    public int generation = 0;
    public int innovation = NEAT.NUM_OUTPUTS; //TODO: what is this???
    public int currentSpecies = 0;
    public int currentGenome = 0;
    public int maxFitness = 0;

    public static void addToSpecies(Genome child, Pool p) {
        boolean foundSpecies = false;
        for (Species s : p.species) {
            if (!foundSpecies && Species.sameSpecies(child, s.genomes.get(0))) {
                s.genomes.add(child);
                foundSpecies = true;
            }
        }

        if (!foundSpecies) {
            Species childSpecies = new Species();
            childSpecies.genomes.add(child);
            p.species.add(childSpecies);
        }
    }

    public Pool() {
        species = new ArrayList<>();
    }
}
