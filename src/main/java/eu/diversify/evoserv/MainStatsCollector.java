/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.diversify.evoserv;

import eu.diversify.evoserv.utils.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author franck
 */
public class MainStatsCollector {
    
    public static String my_root_folder = "C:\\home\\checkouts\\DIVERSIFY\\FFBPG\\0106_paper_manual_init";
    
    
    private static final Pattern p_server_pop = Pattern.compile("\\s*SERVER POPULATION\\s*:\\s*(\\d+)");
    
    static StringBuilder evol_graph = new StringBuilder();
    static StringBuilder utility_graph = new StringBuilder();
    static StringBuilder redundancy_graph = new StringBuilder();
    static StringBuilder sim_graph = new StringBuilder();
    
    public static void processExperiment(File xp_folder) {
        BufferedReader br = null;
        
            System.out.println("Found Experiment " + xp_folder.getName());
            
            
            sim_graph.append("\""+xp_folder.getName()+"/ExecutionSim.txt\" using 1:3 notitle with point lc rgb 'green', \\\n");
            
            for (File run_folder : xp_folder.listFiles()) {
                if (run_folder.isDirectory()) {
                    
                    if (run_folder.getName().contains("RANDOM")) {
                        evol_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/Simulation_robustness.dat\" using 1 notitle with line lc rgb 'blue', \\\n");
                        utility_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/Simulation_utility.dat\" using 1 notitle with line lc rgb 'blue', \\\n");
                        redundancy_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/Simulation_redundancy.dat\" using 1 notitle with line lc rgb 'blue', \\\n");
                        sim_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/ExecutionSim.txt\" using 1:3 notitle with point lc rgb 'blue', \\\n");
                    }
                    else if (run_folder.getName().contains("SMART")) {
                        evol_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/Simulation_robustness.dat\" using 1 notitle with line lc rgb 'red', \\\n");
                        utility_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/Simulation_utility.dat\" using 1 notitle with line lc rgb 'red', \\\n");
                        redundancy_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/Simulation_redundancy.dat\" using 1 notitle with line lc rgb 'red', \\\n");
                        sim_graph.append("\""+xp_folder.getName()+"/"+run_folder.getName()+"/ExecutionSim.txt\" using 1:3 notitle with point lc rgb 'red', \\\n");
                    }
                    
                }
            }
            
            
            // Parse initial graph
            //String initgraph = FileUtils.readTextFile(xp_folder, "InitialGraph.txt");
            /*
            Matcher m = p_server_pop.matcher(initgraph);
            if (m.find()) {
                
                System.out.println("Server population: " + Integer.parseInt(m.group(1)));
            }
            */
            

    }
    
    public static void main(String[] args) {
        generateGraphs(my_root_folder);
    }
    
    public static void generateGraphs(String root_folder) {
        
        File root = new File(root_folder);
        if (!root.isDirectory()) {
            System.err.println("Invalid root directory: " + root_folder);
            return;
        }
        
        evol_graph.append("set title 'Evolution of robustness with decentralized adaptation - Red:Smart Blue:Random'\n");
        evol_graph.append("set xlabel 'Adaptation step'\n");
        evol_graph.append("set ylabel 'Robusness index (%)'\n");
        evol_graph.append("set xrange [0:100]\n");
        evol_graph.append("set yrange [45:75]\n");
        evol_graph.append("plot ");
        
        utility_graph.append("set title 'Evolution of total utility - Red:Smart Blue:Random'\n");
        utility_graph.append("set xlabel 'Adaptation step'\n");
        utility_graph.append("set ylabel 'Robusness index (%)'\n");
        utility_graph.append("set xrange [0:100]\n");
        //utility_graph.append("set yrange [45:75]\n");
        utility_graph.append("plot ");
                
        redundancy_graph.append("set title 'Evolution of total redundancy - Red:Smart Blue:Random'\n");
        redundancy_graph.append("set xlabel 'Adaptation step'\n");
        redundancy_graph.append("set ylabel 'Robusness index (%)'\n");
        redundancy_graph.append("set xrange [0:100]\n");
        //redundancy_graph.append("set yrange [45:75]\n");
        redundancy_graph.append("plot ");
        
        sim_graph.append("set title 'Simulated Failure rates - Green:Initial Red:Smart Blue:Random'\n");
        sim_graph.append("set xlabel '# Servers down'\n");
        sim_graph.append("set ylabel 'Percentage of failed requests'\n");
        //sim_graph.append("set xrange [0:100]\n");
        //sim_graph.append("set yrange [45:75]\n");
        sim_graph.append("plot ");
        
        for (File xp_folder : root.listFiles()) {
            if (xp_folder.isDirectory()) {
                
                processExperiment(xp_folder);
            }
        }
        
        
        FileUtils.writeTextFile(root, "Robustness.plt", evol_graph.toString());
        FileUtils.writeTextFile(root, "Utility.plt", utility_graph.toString());
        FileUtils.writeTextFile(root, "Redundancy.plt", redundancy_graph.toString());
        FileUtils.writeTextFile(root, "Simulation.plt", sim_graph.toString());
        
    }
    
    
    class InitialModel {
    
        String name;
        
        public InitialModel(String name){
            this.name = name;
        }
    
    }
    
    class EvolutionSimulation {
        String name;
        
        double[] robustness;
        
        public EvolutionSimulation(String name){
            this.name = name;
        }
    }
    
    
}
