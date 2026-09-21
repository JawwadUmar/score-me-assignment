import algorithm.CradScheduler;
import io.JsonIO;
import model.ScheduleResult;
import model.SchedulingInstance;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <input.json> <output.json>");
            System.exit(1);
        }
        
        try {
            SchedulingInstance instance = JsonIO.readInstance(args[0]);
            CradScheduler scheduler = new CradScheduler(1.0); // Assuming lambda = 1.0 as default
            ScheduleResult result = scheduler.solve(instance);
            JsonIO.writeResult(result, args[1]);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
