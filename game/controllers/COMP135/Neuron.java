package game.controllers.COMP135;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Neuron {
    public HashMap<Integer, Gene> incoming;
    public double value = 0;

    public static int randomNeuron(ArrayList<Gene> genes, boolean nonInput) {
        HashMap<Integer, Boolean> neurons = new HashMap<>();

        if (nonInput == false) {
            for (int i = 0; i < NEAT.NUM_INPUTS; i++) {
                neurons.put(i, true);
            }
        }

        for (int i = NEAT.MAX_NODES; i < NEAT.MAX_NODES + NEAT.NUM_OUTPUTS; i++) {
            neurons.put(i, true);
        }

        for (Gene g : genes) {
            if (nonInput == false || g.into > NEAT.NUM_INPUTS) {
                neurons.put(g.into, true);
            }
            if (nonInput == false || g.out > NEAT.NUM_INPUTS) {
                neurons.put(g.out, true);
            }
        }

        // chose a random neuron and return that key
        int count = neurons.size();
        int n = (int) (Math.random()*count);
        Set<Integer> keys = neurons.keySet();
        for (Integer i : keys) {
            n = n - 1;
            if (n == 0) return i;
        }

        return 0;
    }

    public Neuron(){
        this.incoming = new HashMap<>();
    }

    // copy constructor
    public Neuron(Neuron n){
        this.incoming = new HashMap<>();
        for (Integer i : n.incoming.keySet()) {
            this.incoming.put(i, new Gene(n.incoming.get(i)));
        }
        this.value = n.value;
    }
}
