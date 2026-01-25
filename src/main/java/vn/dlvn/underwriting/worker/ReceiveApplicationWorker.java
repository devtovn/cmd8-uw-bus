
package vn.dlvn.underwriting.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReceiveApplicationWorker {

    @JobWorker(type = "receive-application")
    public Map<String, Object> handle(ActivatedJob job) {
        
        long processInstanceKey = job.getProcessInstanceKey();
        
        // Get variables from process instance
        Map<String, Object> variables = job.getVariablesAsMap();
        
        String applicantName = (String) variables.getOrDefault("applicantName", "Unknown");
        Integer age = (Integer) variables.getOrDefault("age", 25);
        String disease = (String) variables.getOrDefault("disease", "NONE");
        
        System.out.println("===== Receiving Application =====");
        System.out.println("Process Instance Key: " + processInstanceKey);
        System.out.println("Applicant: " + applicantName);
        System.out.println("Age: " + age);
        System.out.println("Disease: " + disease);
        System.out.println("=================================");
        
        // Return variables for DMN evaluation
        return Map.of(
            "age", age,
            "disease", disease,
            "applicantName", applicantName
        );
    }
}
