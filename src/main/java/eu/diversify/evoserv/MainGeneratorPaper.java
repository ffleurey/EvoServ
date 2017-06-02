package eu.diversify.evoserv;

import eu.diversify.evoserv.utils.FileUtils;
import eu.diversify.evoserv.MainStatsCollector;
import eu.diversify.evoserv.model.simulation.SGHExecSimulation;
import eu.diversify.evoserv.model.SGHExtinctionSequence;
import eu.diversify.evoserv.model.adaptation.SGHSimulation;
import eu.diversify.evoserv.model.SGHSystem;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ffl
 */
public class MainGeneratorPaper extends Thread {
    
    public static int NB_EXTINCTIONS = 48; // Number of random extinction sequences to calculate robustness at each steps
    public static int NB_EXTINCTIONS_THREADS = 8;

    public static ArrayList<SGHExecSimulation> simulateExecution(SGHSystem graph) {
        ArrayList<SGHExecSimulation> result = new ArrayList<SGHExecSimulation>();
        for(int i=0; i<graph.servers.size()/5+1; i++) {
            SGHExecSimulation s = new SGHExecSimulation(graph, i, 1000); s.execute();
            result.add(s);
        }
        { SGHExecSimulation s = new SGHExecSimulation(graph, graph.servers.size()/4, 1000); s.execute(); result.add(s); }
        { SGHExecSimulation s = new SGHExecSimulation(graph, graph.servers.size()/2, 1000); s.execute(); result.add(s); }
        
       return result;
    }

    public static void main(String[] args) {
        
         for (int i = 0; i<1; i++) {
            String folder = "0106_paper_manual_init";
            run_experiment(folder, SGHSystem.generateSGHSystem(250,50, false));
            //MainStatsCollector.generateGraphs("./"+folder+"/");
            folder = "0106_paper_random_init";
            run_experiment(folder, SGHSystem.generateRealisticManualSGHSystem(250,50, false));
            //MainStatsCollector.generateGraphs("./"+folder+"/");
         }
         
    }
    
    public static void run_experiment(String folder, SGHSystem graph) {
    
        if(NB_EXTINCTIONS % NB_EXTINCTIONS_THREADS != 0) {
            System.err.println("Error: NB_EXTINCTIONS_THREADS must be a multiple of NB_EXTINCTIONS !");
            System.exit(-1);
        }
        
         System.out.println( "Max Heap memory: "+
                 ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax()
    );

        File outdir = new File("./"+folder+"/" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
        outdir.mkdirs();
        System.out.println("Output Folder: " + outdir.getAbsolutePath());
        
        FileUtils.writeTextFile(outdir, "InitialGraph.txt", graph.dumpData(true));
        graph.exportGraphStatistics(outdir);
        System.out.println(graph.dumpData(false));
        /*
        try {
            graph.exportClientsToJSONFiles(outdir, "InitialGraph", new File("host_ip_list_wide.txt"));
        } catch (Exception ex) {
            Logger.getLogger(MainGeneratorPaper.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        System.out.println("Graph generated.");
        
      
        
        Long before_time = System.currentTimeMillis();
        {
            SGHExtinctionSequence[] eseqs = graph.computeRandomExtinctionSequence(NB_EXTINCTIONS, NB_EXTINCTIONS_THREADS);
            double[] avg_seq = SGHExtinctionSequence.averageExtinctionSequences(eseqs);
            //System.out.println(Arrays.toString(avg_seq));
            double robustness = SGHExtinctionSequence.averageRobustnessIndex(avg_seq);
            SGHExtinctionSequence.writeGNUPlotScriptForAll(eseqs, outdir, "Extinctions_Initial");
            System.out.println("Robustness (SGH) = " + robustness);
            SGHExecSimulation.writeResults(outdir, simulateExecution(graph));
        }
        Long after_time = System.currentTimeMillis();
        System.out.println("Time for 100 extinctions on SGH: " + (after_time - before_time));

        System.out.println("\nSimulating 10 random evolutions of the initial graph");
        for (int i=0; i<2; i++) {
            SGHSimulation sim_CR_SR = new SGHSimulation(graph, true, true, false, false, true);
            System.out.println("Random Simulation " + i  + "/10...");
            MainGeneratorPaper t = new MainGeneratorPaper("RANDOM_" + i, sim_CR_SR, outdir);
            t.start();
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainGeneratorPaper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println("\nSimulating 10 SMART evolutions of the initial graph");
        for (int i=0; i<2; i++) {
            SGHSimulation sim_CS_SS = new SGHSimulation(graph, true, true, true, true, true);
            System.out.println("SMART Simulation " + i  + "/10...");
            MainGeneratorPaper t = new MainGeneratorPaper("SMART_" + i, sim_CS_SS, outdir);
            t.start();
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainGeneratorPaper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    
    String name;
    SGHSimulation sim;
    File basedir; 
    
    public MainGeneratorPaper(String name, SGHSimulation simulation, File basedir) {
        this.name = name;
        this.sim = simulation;
        this.basedir = basedir;
    }
            
    public void run() {
        File outdir = new File(basedir, name);
        outdir.mkdir();
        
        sim.startSimulation(100);
        sim.exportRobustnessData("Simulation", outdir);
        
        SGHSystem graph = sim.system;
        FileUtils.writeTextFile(outdir, "FinalGraph.txt", graph.dumpData(true));
        
        {
            SGHExtinctionSequence[] eseqs = graph.computeRandomExtinctionSequence(NB_EXTINCTIONS, NB_EXTINCTIONS_THREADS);
            double[] avg_seq = SGHExtinctionSequence.averageExtinctionSequences(eseqs);
            System.out.println(Arrays.toString(avg_seq));
            double robustness = SGHExtinctionSequence.averageRobustnessIndex(avg_seq);
            SGHExtinctionSequence.writeGNUPlotScriptForAll(eseqs, outdir, "Extinctions_Final");
            System.out.println("Robustness = " + robustness);
            /*
            try {
                graph.exportClientsToJSONFiles(outdir, "FinalGraph", new File("host_ip_list_wide.txt"));
            } catch (Exception ex) {
                Logger.getLogger(MainGeneratorPaper.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
            graph.exportGraphStatistics(outdir);
            System.out.println(graph.dumpData(false));
            SGHExecSimulation.writeResults(outdir, simulateExecution(graph));
        }
    }
    
    
}
