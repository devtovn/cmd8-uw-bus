
package vn.dlvn.underwriting.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PolicyIssuanceWorker {

    @JobWorker(type = "issue-policy")
    public Map<String, Object> handle(
            ActivatedJob job,
            @Variable(name = "applicantName") String applicantName,
            @Variable(name = "uwDecision") String uwDecision) {
        
        long processInstanceKey = job.getProcessInstanceKey();
        String policyNumber = "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        System.out.println("===== Issuing Policy =====");
        System.out.println("Process Instance Key: " + processInstanceKey);
        System.out.println("Applicant: " + applicantName);
        System.out.println("Decision: " + uwDecision);
        System.out.println("Policy Number: " + policyNumber);
        System.out.println("==========================");
        
        return Map.of("policyNumber", policyNumber);
    }
}
