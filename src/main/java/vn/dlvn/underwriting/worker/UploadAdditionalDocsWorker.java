package vn.dlvn.underwriting.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Worker for User Task: Upload Additional Docs
 * 
 * NOTE: In production, User Tasks are typically handled through Tasklist UI
 * by human users. This worker is for automated testing/demo purposes.
 * 
 * To use Tasklist instead:
 * 1. Access Tasklist UI (if available in c8run)
 * 2. Find "Upload Additional Docs" task
 * 3. Complete task with additional documents
 */
@Component
public class UploadAdditionalDocsWorker {

    @JobWorker(type = "io.camunda.zeebe:userTask")
    public void handle(
            ActivatedJob job,
            JobClient client,
            @Variable(name = "applicantName") String applicantName) {
        
        long processInstanceKey = job.getProcessInstanceKey();
        String taskName = job.getCustomHeaders().getOrDefault("io.camunda.zeebe:taskDefinition", "Unknown Task");
        
        // Only handle "Upload Additional Docs" task
        if (!"requestDocs".equals(job.getElementId())) {
            // Skip this task, let other workers or Tasklist handle it
            return;
        }
        
        System.out.println("===== Upload Additional Docs =====");
        System.out.println("Process Instance Key: " + processInstanceKey);
        System.out.println("Applicant: " + applicantName);
        System.out.println("Task: " + taskName);
        System.out.println("Simulating document upload...");
        
        // Simulate user uploading additional documents
        // After uploading docs, set disease = "NONE" to avoid infinite loop
        // This way, when process loops back to Auto Underwriting DMN,
        // it will evaluate to AUTO_APPROVE instead of REQUEST_DOC again
        Map<String, Object> outputVariables = Map.of(
            "documentsUploaded", true,
            "uploadTimestamp", System.currentTimeMillis(),
            "additionalInfo", "Medical reports and insurance history uploaded",
            "disease", "NONE"  // Reset disease after docs uploaded → DMN will approve
        );
        
        System.out.println("Documents uploaded successfully!");
        System.out.println("Setting disease='NONE' to approve on next DMN evaluation");
        System.out.println("==================================");
        
        // Complete the user task
        client.newCompleteCommand(job.getKey())
            .variables(outputVariables)
            .send()
            .join();
    }
}
