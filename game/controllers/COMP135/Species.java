package game.controllers.COMP135;

import java.util.ArrayList;

public class Species {
    public static double DELTA_DISJOINT = 2.0;
    public static double DELTA_WEIGHTS = 0.4;
    public static double DELTA_THRESHOLD = 1.0;
    public int topFitness;
    public int staleness;
    public double averageFitness;

    public ArrayList<Genome> genomes = new ArrayList<>();

    public static boolean sameSpecies(Genome g1, Genome g2) {
        double dd = DELTA_DISJOINT*Gene.disjoint(g1.genes, g2.genes);
        double dw = DELTA_WEIGHTS*Gene.weights(g1.genes, g2.genes);
        return dd + dw < DELTA_THRESHOLD;
    }
}
