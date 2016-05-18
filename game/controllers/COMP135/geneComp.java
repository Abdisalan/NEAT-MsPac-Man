package game.controllers.COMP135;

import java.util.Comparator;

public class geneComp implements Comparator<Gene> {
    @Override
    public int compare(Gene g1, Gene g2) {
        if (g1.out < g2.out) return 1;
        else return -1;
    }
}
