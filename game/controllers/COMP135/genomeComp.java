package game.controllers.COMP135;

import java.util.Comparator;

/**
 * Created by abdisalan on 5/18/16.
 */
public class genomeComp implements Comparator<Genome> {
    @Override
    public int compare(Genome g1, Genome g2) {
        if (g1.fitness < g2.fitness) return 1;
        else return -1;
    }
}
