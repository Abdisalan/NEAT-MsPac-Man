package game.controllers.COMP135;

import java.util.*;

public class NEAT {

    /* class members
     */
    public Pool mspacman_pool;
    public static int POPULATION = 5000;
    public static int NUM_INPUTS = 12;
    public static int NUM_OUTPUTS = 4;
    public static int STALE_SPECIES = 15;
    public static double PERTURB_CHANCE = 0.90;
    public static double CROSSOVER_CHANCE = 0.75;

    public static int MAX_NODES = 1000;
    public static int MAX_TIME = 8000;

    public NEAT() {
        mspacman_pool = new Pool();
        for (int i = 0; i < POPULATION; i++) {
            Genome basic = Genome.basicGenome();
            Pool.addToSpecies(basic, mspacman_pool);
        }
    }

    // get inputs and update them in the network
    public Network updateInputs(Network net, double[] inputs) {
        for (int i = 0; i < NUM_INPUTS; i++) {
            net.neurons.get(i).value = inputs[i];
        }

        return net;
    }

    // evaluates the network and comes up with a direction for pacman to go
    public int evaluateNetwork(Network net) {

        // go through each of the nodes and evaluate connections
        for (Integer i : net.neurons.keySet()) {
            double sum = 0;
            for (Integer j : net.neurons.get(i).incoming.keySet()) {
                Gene incoming = net.neurons.get(i).incoming.get(j);
                Neuron neuron2 = net.neurons.get(incoming.into);
                sum += incoming.weight * neuron2.value;
            }

            if (net.neurons.get(i).incoming.size() > 0) {
                net.neurons.get(i).value = sigmoid(sum);
            }
        }

        // choose the output with the highest value as output
        // output nodes in order: up right down left = 0 1 2 3

        int maxOutputIndex = MAX_NODES;
        double maxOutputValue = Integer.MIN_VALUE;
        for (int i = MAX_NODES; i < MAX_NODES + NUM_OUTPUTS; i++) {
            Neuron output = net.neurons.get(i);
//            System.out.println(output.value);
            if (output.value > maxOutputValue) {
                maxOutputIndex = i;
//                System.out.println(i);
                maxOutputValue = output.value;
            }
        }
        maxOutputIndex -= MAX_NODES;

        return maxOutputIndex;
    }

    public static double sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
    }

    public void setCurrentFitness(int fitness) {
        Species s = mspacman_pool.species.get(mspacman_pool.currentSpecies);
        Genome g = s.genomes.get(mspacman_pool.currentGenome);
        g.fitness = fitness;
    }

    public void rankGlobally(Pool p){ //currently depending on Pool from argument
        ArrayList<Genome> global = new ArrayList<>();
        for (Species s : p.species) {
            for (Genome g : s.genomes) {
                global.add(g);
            }
        }

        Collections.sort(global, new genomeComp());

        for (int i = 0; i < global.size(); i++) {
            global.get(i).globalRank = i + 1;
        }
    }

    public void calculateAverageFitness(Species s) {
        double total = 0;
        for (Genome g : s.genomes) {
            total += g.globalRank;
        }

        s.averageFitness = total / s.genomes.size();
    }

    public double totalAverageFitness(Pool p) {
        double total = 0;
        for (Species s : p.species) {
            total += s.averageFitness;
        }

        return total;
    }

    // removes half the species unless we set cut to one to true
    public void cullSpecies(boolean cutToOne, Pool p) { //depending on Pool from argument
       for (Species s : p.species) {
           Collections.sort(s.genomes, new genomeComp());

           int remaining = (int) Math.ceil(((double) s.genomes.size()) / 2);

           if (cutToOne) remaining = 1;

           while (s.genomes.size() > remaining) {
               s.genomes.remove(s.genomes.size() - 1);
           }

       }
    }

    public Genome breedChild(Species s) {
        Genome child = null;
        if (Math.random() < CROSSOVER_CHANCE) {
            Genome g1 = s.genomes.get((int) (Math.random()*s.genomes.size()));
            Genome g2 = s.genomes.get((int) (Math.random()*s.genomes.size()));
            child = new Genome(g1, g2); //crossover the two genomes
        } else {
            Genome g = s.genomes.get((int) (Math.random()*s.genomes.size()));
            child = new Genome(g);
        }
        if (child != null) child.mutate(child);
        return child;
    }

    public void removeStaleSpecies(Pool p) { // put Pool in the parameters temporarily
        ArrayList<Species> survived = new ArrayList<>();
        for (Species s : p.species) {
            Collections.sort(s.genomes, new genomeComp());

            if (s.genomes.get(0).fitness > s.topFitness) {
                s.topFitness = s.genomes.get(0).fitness;
                s.staleness = 0;
            } else {
                s.staleness += 1;
            }

            if (s.staleness < STALE_SPECIES || s.topFitness >= p.maxFitness) {
               survived.add(s);
            }
        }
        p.species = survived;
    }

    public void removeWeakSpecies(Pool p) { // TODO just remove all references of Pool in parameters...
        ArrayList<Species> survived = new ArrayList<>();
        double sum = totalAverageFitness(p);
        for (Species s : p.species) {
            double breed = Math.floor((s.averageFitness / sum) * POPULATION);
            if (breed >= 1) {
                survived.add(s);
            }
        }

        p.species = survived;
    }

    public void newGeneration(Pool p) {
        cullSpecies(false, p); // cull the bottom half of each species
        rankGlobally(p);
        removeStaleSpecies(p);
        rankGlobally(p);
        for (Species s : p.species) {
            calculateAverageFitness(s);
        }

        removeWeakSpecies(p);
        double sum = totalAverageFitness(p);
        ArrayList<Genome> children = new ArrayList<>();
        for (Species s : p.species) {
            double breed = Math.floor((s.averageFitness / sum) * POPULATION) - 1;
            for (double i = 0; i < breed; i++) { //TODO ????
               children.add(breedChild(s));
            }
        }
        cullSpecies(true, p); // cull all but the top member of each species
        while (children.size() + p.species.size() < POPULATION) {
            Species s = p.species.get((int)(Math.random() * p.species.size()));
            children.add(breedChild(s));
        }

        for (Genome child : children) {
            Pool.addToSpecies(child, p);
        }

        p.generation += 1;

        //Maybe save the generation in some kind of file.....
    }

    public void nextGenome() {
        mspacman_pool.currentGenome++;
        if (mspacman_pool.currentGenome >= mspacman_pool.species.get(mspacman_pool.currentSpecies).genomes.size()) {
            mspacman_pool.currentGenome = 0;
            mspacman_pool.currentSpecies++;
            if (mspacman_pool.currentSpecies >= mspacman_pool.species.size()) {
                newGeneration(mspacman_pool);
                mspacman_pool.generation++;
                mspacman_pool.currentSpecies = 0;
            }
        }
    }

    public boolean fitnessAlreadyMeasured() {
        Species s = mspacman_pool.species.get(mspacman_pool.currentSpecies);
        Genome g = s.genomes.get(mspacman_pool.currentGenome);
        return g.fitness != 0;
    }

    public void displayGenome(Genome g) {
        System.out.println("Genome:");
        System.out.println("fitness:"+Integer.toString(g.fitness));
    }

    public void playTop() {
        int maxFitness = 0;
        int maxs = 0, maxg = 0;

        for (Species s : mspacman_pool.species) {
            for (Genome g : s.genomes) {
                if (g.fitness > maxFitness) {
                    maxFitness = g.fitness;
                    maxs = mspacman_pool.species.indexOf(s);
                    maxg = s.genomes.indexOf(g);
                }
            }
        }

        mspacman_pool.currentSpecies = maxs;
        mspacman_pool.currentGenome = maxg;
        mspacman_pool.maxFitness = maxFitness;
    }
}
