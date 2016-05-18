package game.controllers.COMP135;

import java.util.ArrayList;
import java.util.HashMap;

public class Genome {
    public ArrayList<Gene> genes;
    public Network network;

    public int fitness = 0;
    public int adjustedFitness = 0;
    public int maxNeurons;
    public int globalRank;

    public double CONNECTIONS_MUTATION_CHANCE = 0.25;
    public double LINK_MUTATION_CHANCE = 2.0;
    public double BIAS_MUTATION_CHANCE = 0.40;
    public double NODE_MUTATION_CHANCE = 0.50;
    public double ENABLE_MUTATION_CHANCE = 0.2;
    public double DISABLE_MUTATION_CHANCE = 0.4;
    public double STEP_SIZE = 0.1;

    // basic genome
    public static Genome basicGenome() {
        Genome g = new Genome();
        g.maxNeurons = NEAT.NUM_INPUTS;
        g.mutate(g);
        return g;
    }

    // mutates the weight of every gene
    public void pointMutate(Genome g) {
        double step = g.STEP_SIZE;

        for (Gene gene : g.genes) {
            if (Math.random() < NEAT.PERTURB_CHANCE) {
                gene.weight = gene.weight + (Math.random() * step * 2) - step; // WHY???
            } else {
                gene.weight = (Math.random() * 4) - 2; // chance for negative number...
            }
        }
    }

    // creates a random link between two nodes with a weight
    public void linkMutate(Genome g, boolean forceBias) {
        int neuron1 = Neuron.randomNeuron(g.genes, false); // could contain input neuron
        int neuron2 = Neuron.randomNeuron(g.genes, true);  // does not contain input neuron

        Gene newLink = new Gene();

        // quit if they're both input nodes
        if (neuron1 <= NEAT.NUM_INPUTS && neuron2 <= NEAT.NUM_INPUTS) {
            return;
        }

        // swap output and input
        if (neuron2 <= NEAT.NUM_INPUTS) { // TODO this will affect what input node pacman gets....
            int tmp = neuron1;
            neuron1 = neuron2;
            neuron2 = tmp;
        }

        newLink.into = neuron1;
        newLink.out = neuron2;
        if (forceBias) newLink.into = NEAT.NUM_INPUTS;

        if (Gene.containsLink(g.genes, newLink)) return; // don't want to repeat any nodes...

        newLink.innovation = 10; // TODO Make a global POOL for this
        newLink.weight = (Math.random()* 4) - 2; // could create negative weight

        g.genes.add(newLink);
    }

    public void nodeMutate(Genome g) {
        if (g.genes.size() == 0) return;

        g.maxNeurons += 1;

        //get a random gene
        Gene gene1 = g.genes.get((int) (Math.random()*g.genes.size()));

        //give up if its not enabled
        if (gene1.enabled == false) return;

        //disable any lucky enabled gene
        gene1.enabled = false;

        //add another gene to the list
        Gene gene2 = new Gene(gene1);
        gene2.out = g.maxNeurons;
        gene2.weight = 1;
        gene2.innovation = 10; //TODO Make a global
        gene2.enabled = true;
        g.genes.add(gene2);

        // connect
        Gene gene3 = new Gene(gene1);
        gene3.into = g.maxNeurons;
        gene3.innovation = 10; //TODO Make a global
        gene3.enabled = true;
        g.genes.add(gene3);
    }

    public void enableDisableMutate(Genome g, boolean enable) {
        ArrayList<Gene> candidates = new ArrayList<>();
        for (Gene gene : g.genes) {
            if (gene.enabled != enable) candidates.add(gene);
        }

        if (candidates.size() == 0) return;

        Gene gene1 = candidates.get((int) (Math.random() * candidates.size()));
        gene1.enabled = !gene1.enabled;
    }

    public void mutate(Genome g) {

        //adjust the mutation rate of the genome into two arbitrary buckets
        g.CONNECTIONS_MUTATION_CHANCE *= (Math.random() > 0.5) ? 0.95 : 1.05263;
        g.LINK_MUTATION_CHANCE        *= (Math.random() > 0.5) ? 0.95 : 1.05263;
        g.BIAS_MUTATION_CHANCE        *= (Math.random() > 0.5) ? 0.95 : 1.05263;
        g.NODE_MUTATION_CHANCE        *= (Math.random() > 0.5) ? 0.95 : 1.05263;
        g.ENABLE_MUTATION_CHANCE      *= (Math.random() > 0.5) ? 0.95 : 1.05263;
        g.DISABLE_MUTATION_CHANCE     *= (Math.random() > 0.5) ? 0.95 : 1.05263;
        g.STEP_SIZE                   *= (Math.random() > 0.5) ? 0.95 : 1.05263;

        //randomly mutate everywhere
        if (Math.random() < g.CONNECTIONS_MUTATION_CHANCE) pointMutate(g);

        for (double i = g.LINK_MUTATION_CHANCE; i > 0; i--) {
            if (Math.random() < i) linkMutate(g, false);
        }
        for (double i = g.BIAS_MUTATION_CHANCE; i > 0; i--) {
            if (Math.random() < i) linkMutate(g, true);
        }
//        for (double i = g.NODE_MUTATION_CHANCE; i > 0; i--) {
//            if (Math.random() < i) nodeMutate(g);
//        }
        for (double i = g.ENABLE_MUTATION_CHANCE; i > 0; i--) {
            if (Math.random() < i) enableDisableMutate(g, true);
        }
        for (double i = g.DISABLE_MUTATION_CHANCE; i > 0; i--) {
            if (Math.random() < i) enableDisableMutate(g, false);
        }
    }


    // constructor
    public Genome() {
        genes = new ArrayList<>();
        network = new Network(this);
    }

    // deep copy constructor
    public Genome(Genome g) {
        this.genes = new ArrayList<>();
        for (int i = 0; i < g.genes.size(); i++) {
            this.genes.add(new Gene(g.genes.get(i)));
        }

        //copy over network
        for (Integer i : g.network.neurons.keySet()) {
            this.network.neurons.put(i, new Neuron(g.network.neurons.get(i))); // deep copy
        }

        this.fitness = g.fitness;
        this.adjustedFitness = g.adjustedFitness;
        this.CONNECTIONS_MUTATION_CHANCE = g.CONNECTIONS_MUTATION_CHANCE;
        this.LINK_MUTATION_CHANCE = g.LINK_MUTATION_CHANCE;
        this.BIAS_MUTATION_CHANCE = g.BIAS_MUTATION_CHANCE;
        this.NODE_MUTATION_CHANCE = g.NODE_MUTATION_CHANCE;
        this.ENABLE_MUTATION_CHANCE = g.ENABLE_MUTATION_CHANCE;
        this.DISABLE_MUTATION_CHANCE = g.DISABLE_MUTATION_CHANCE;
        this.STEP_SIZE = g.STEP_SIZE; // maybe this shouldn't be copied....
    }

    // cross over two Genomes constructor
    public Genome(Genome g1, Genome g2) {
        //make sure g1 has a higher fitness
        if (g2.fitness > g1.fitness) {
            Genome temp = g1;
            g1 = g2;
            g2 = temp;
        }

        Genome child = new Genome();
        //make a dictionary that matches the genes' innovation of g2 to the gene
        HashMap<Integer, Gene> innovations = new HashMap<>();
        for (Gene gene2 : g2.genes) {
            innovations.put(gene2.innovation, gene2); // copy the gene or put reference??
        }

        //distribute the genes to the child randomly from the two Genomes
        for (Gene gene1 : g1.genes) {
            Gene gene2 = innovations.get(gene1.innovation);
            int rand = (int) (Math.random()*2); // 50% chance for inheriting Genome1 vs Genome2
            if (gene2 != null && rand == 1 && gene2.enabled) {
                child.genes.add(new Gene(gene2));
            } else {
                child.genes.add(new Gene(gene1));
            }
        }

        //set the maxneurons for child
        child.maxNeurons = Math.max(g1.maxNeurons, g2.maxNeurons);

        //pass over all mutation rates of more fit Genome 1
        child.CONNECTIONS_MUTATION_CHANCE = g1.CONNECTIONS_MUTATION_CHANCE;
        child.LINK_MUTATION_CHANCE = g1.LINK_MUTATION_CHANCE;
        child.BIAS_MUTATION_CHANCE = g1.BIAS_MUTATION_CHANCE;
        child.NODE_MUTATION_CHANCE = g1.NODE_MUTATION_CHANCE;
        child.ENABLE_MUTATION_CHANCE = g1.ENABLE_MUTATION_CHANCE;
        child.DISABLE_MUTATION_CHANCE = g1.DISABLE_MUTATION_CHANCE;
        child.STEP_SIZE = g1.STEP_SIZE;
    }
}
