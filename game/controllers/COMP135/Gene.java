package game.controllers.COMP135;

import java.util.ArrayList;
import java.util.HashMap;

public class Gene {
    public int into = 0;
    public int out = 0;
    public double weight = 0;
    public boolean enabled = true;
    public int innovation = 0;

    public static double weights(ArrayList<Gene> genes1, ArrayList<Gene> genes2){
        HashMap<Integer, Gene> i2 = new HashMap<>();
        for (Gene gene : genes2) {
            i2.put(gene.innovation, gene);
        }
        double sum = 0;
        double coincident = 0;
        for (Gene gene : genes1) {
            if (i2.get(gene.innovation) != null) {
                Gene gene2 = i2.get(gene.innovation);
                sum += Math.abs(gene.weight - gene2.weight);
                coincident++;
            }
        }

        return sum / coincident;
    }

    public static double disjoint(ArrayList<Gene> genes1, ArrayList<Gene> genes2){
        HashMap<Integer, Boolean> i1 = new HashMap<>();
        for (Gene g : genes1) {
            i1.put(g.innovation, true);
        }
        HashMap<Integer, Boolean> i2 = new HashMap<>();
        for (Gene g : genes2) {
            i2.put(g.innovation, true);
        }

        double disjointGenes = 0;
        for (Gene g : genes1) {
            if (i2.get(g.innovation) == null) disjointGenes++;
        }
        for (Gene g : genes2) {
            if (i1.get(g.innovation) == null) disjointGenes++;
        }

        double n = Math.max(genes1.size(), genes2.size());

        return disjointGenes / n;
    }

    public static boolean containsLink(ArrayList<Gene> genes, Gene link) {
        for (Gene g : genes) {
            if (g.into == link.into && g.out == link.out) {
                return true;
            }
        }
        return false;
    }

    public Gene(Gene g) {
        this.into = g.into;
        this.out = g.out;
        this.weight = g.weight;
        this.enabled = g.enabled;
        this.innovation = g.innovation;
    }

    public Gene(){
    }
}
