import algorithm.CradScheduler;
import algorithm.BruteForce;
import io.JsonIO;
import model.ScheduleResult;
import model.SchedulingInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;

public class BenchmarkMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java BenchmarkMain <input.json> <output.json> <run_brute_force_boolean>");
            System.exit(1);
        }
        
        String inFile = args[0];
        String outFile = args[1];
        boolean runBruteForce = Boolean.parseBoolean(args[2]);
        
        SchedulingInstance instance = JsonIO.readInstance(inFile);
        CradScheduler scheduler = new CradScheduler(1.0);
        
        ScheduleResult result = scheduler.solve(instance);
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("feasible", result.feasible);
        root.put("runtime_ms", result.runtimeMs);
        if (result.feasible) {
            root.put("penalty", result.penalty);
        } else {
            root.putNull("penalty");
            root.put("violation_reason", result.violationReason);
        }
        
        if (runBruteForce) {
            BruteForce bf = new BruteForce(1.0);
            double optimal = bf.findOptimalPenalty(instance);
            if (optimal == Double.POSITIVE_INFINITY) {
                root.putNull("optimal_penalty");
            } else {
                root.put("optimal_penalty", optimal);
            }
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outFile), root);
    }
}

