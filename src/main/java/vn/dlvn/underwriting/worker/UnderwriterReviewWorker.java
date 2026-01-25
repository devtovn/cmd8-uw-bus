package vn.dlvn.underwriting.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker for User Task: Underwriter Review
 * 
 * NOTE: In production, User Tasks are typically handled through Tasklist UI
 * by human underwriters. This worker is for automated testing/demo purposes.
 * 
 * To use Tasklist instead:
 * 1. Access Tasklist UI (if available in c8run)
 * 2. Find "Underwriter Review" task
 * 3. Underwriter reviews application and makes decision
 */
@Component
public class UnderwriterReviewWorker {

    @JobWorker(type = "io.camunda.zeebe:userTask")
    public void handle(
            ActivatedJob job,
            JobClient client,
            @Variable(name = "applicantName") String applicantName,
            @Variable(name = "age") Integer age,
            @Variable(name = "disease") String disease) {
        
        long processInstanceKey = job.getProcessInstanceKey();
        String taskName = job.getCustomHeaders().getOrDefault("io.camunda.zeebe:taskDefinition", "Unknown Task");
        
        // Only handle "Underwriter Review" task
        if (!"manualReview".equals(job.getElementId())) {
            // Skip this task, let other workers or Tasklist handle it
            return;
        }
        
        System.out.println("===== Underwriter Review =====");
        System.out.println("Process Instance Key: " + processInstanceKey);
        System.out.println("Task: " + taskName);
        System.out.println("Applicant: " + applicantName);
        System.out.println("Age: " + age);
        System.out.println("Disease: " + disease);
        System.out.println("Simulating underwriter manual review...");
        
        // Simulate underwriter decision
        // In real scenario, underwriter would review all documents and make informed decision
        String reviewNotes = "Application reviewed by senior underwriter. " +
                            "Applicant shows good health indicators. " +
                            "Approved for standard policy.";
        
        Map<String, Object> outputVariables = Map.of(
            "manualReviewCompleted", true,
            "reviewTimestamp", System.currentTimeMillis(),
            "reviewerName", "John Smith (Senior Underwriter)",
            "reviewNotes", reviewNotes,
            "manualDecision", "APPROVED"
        );
        
        System.out.println("Manual review completed - APPROVED");
        System.out.println("Review notes: " + reviewNotes);
        System.out.println("==============================");
        
        // Complete the user task
        client.newCompleteCommand(job.getKey())
            .variables(outputVariables)
            .send()
            .join();
    }
}
