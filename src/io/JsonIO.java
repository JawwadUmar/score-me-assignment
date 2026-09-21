package io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.ScheduleResult;
import model.SchedulingInstance;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonIO {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static SchedulingInstance readInstance(String filePath) throws IOException {
        JsonNode root = mapper.readTree(new File(filePath));
        
        JsonNode tasksNode = root.get("tasks");
        JsonNode conflictsNode = root.get("conflicts");
        JsonNode resourcesNode = root.get("resources");
        JsonNode capacitiesNode = root.get("capacities");
        JsonNode windowsNode = root.get("windows");
        JsonNode weightsNode = root.get("weights");
        
        int n = tasksNode.size();
        Map<String, Task> tasks = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            String id = tasksNode.get(i).asText();
            JsonNode resNode = resourcesNode.get(i);
            double[] res = new double[4];
            for (int d = 0; d < 4; d++) res[d] = resNode.get(d).asDouble();
            
            JsonNode winNode = windowsNode.get(i);
            int lower = winNode.get(0).asInt();
            int upper = winNode.get(1).asInt();
            
            double weight = weightsNode.get(i).asDouble();
            
            tasks.put(id, new Task(id, res, lower, upper, weight));
        }
        
        List<int[]> conflicts = new ArrayList<>();
        for (JsonNode cNode : conflictsNode) {
            String id1 = tasksNode.get(cNode.get(0).asInt()).asText();
            String id2 = tasksNode.get(cNode.get(1).asInt()).asText();
            // SchedulingInstance constructor expects int array, but it uses String.valueOf(edge[0]).
            // This is a bit problematic because the constructor takes int[] and does String.valueOf(edge[0]).
            // Since the IDs are "T0", "T1", we should just pass the indices, or modify SchedulingInstance.
            conflicts.add(new int[]{cNode.get(0).asInt(), cNode.get(1).asInt()});
        }
        
        int K = root.get("K").asInt();
        double[][] capacities = new double[K][4];
        for (int i = 0; i < K; i++) {
            for (int d = 0; d < 4; d++) {
                capacities[i][d] = capacitiesNode.get(i).get(d).asDouble();
            }
        }
        
        return new SchedulingInstance(tasks, conflicts, capacities);
    }
    
    public static void writeResult(ScheduleResult result, String filePath) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        
        if (result.feasible) {
            ObjectNode assignmentNode = mapper.createObjectNode();
            for (Map.Entry<String, Integer> entry : result.assignment.entrySet()) {
                assignmentNode.put(entry.getKey(), entry.getValue());
            }
            root.set("assignment", assignmentNode);
            root.put("penalty", result.penalty);
        } else {
            ObjectNode assignmentNode = mapper.createObjectNode();
            for (Map.Entry<String, Integer> entry : result.assignment.entrySet()) {
                assignmentNode.put(entry.getKey(), entry.getValue());
            }
            root.set("assignment", assignmentNode);
            root.putNull("penalty");
        }
        
        root.put("runtime_ms", result.runtimeMs);
        root.put("feasible", result.feasible);
        root.put("violation_reason", result.violationReason);
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), root);
    }
}
