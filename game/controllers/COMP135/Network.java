package game.controllers.COMP135;

import java.util.Collections;
import java.util.HashMap;

public class Network {
    public HashMap<Integer, Neuron> neurons;

    public Network() {
        neurons = new HashMap<>();

        //setup input Neurons
        for (int i = 0; i < NEAT.NUM_INPUTS; i++) {
            neurons.put(i, new Neuron());
        }

        //setup output Neurons with space for hidden Neurons
        for (int i = NEAT.MAX_NODES; i < (NEAT.NUM_OUTPUTS + NEAT.MAX_NODES); i++) {
            neurons.put(i, new Neuron());
        }
    }
    // default constructor setup
    public Network(Genome g) {
        neurons = new HashMap<>();

        //setup input Neurons
        for (int i = 0; i < NEAT.NUM_INPUTS; i++) {
            neurons.put(i, new Neuron());
        }

        //setup output Neurons with space for hidden Neurons
        for (int i = NEAT.MAX_NODES; i < (NEAT.NUM_OUTPUTS + NEAT.MAX_NODES); i++) {
            neurons.put(i, new Neuron());
        }

        //sort all the genes in this genome
        Collections.sort(g.genes, new geneComp());

        // add Neurons for enabled genes
        for(Gene ge: g.genes){
            if (ge.enabled) {
                if (neurons.get(ge.out) == null) {
                    neurons.put(ge.out, new Neuron());
                }
                Neuron n = neurons.get(ge.out);
                n.incoming.put(n.incoming.size(), ge);
                if (this.neurons.get(ge.into) == null) {
                    this.neurons.put(ge.into, new Neuron());
                }
            }
        }
    }
}
