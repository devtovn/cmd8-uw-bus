
package vn.dlvn.underwriting.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.stereotype.Component;

@Component
public class SendEPolicyWorker {

    @JobWorker(type = "send-e-policy")
    public void handle(
            ActivatedJob job,
            @Variable(name = "applicantName") String applicantName,
            @Variable(name = "policyNumber") String policyNumber) {
        
        long processInstanceKey = job.getProcessInstanceKey();
        
        System.out.println("===== Sending E-Policy =====");
        System.out.println("Process Instance Key: " + processInstanceKey);
        System.out.println("Recipient: " + applicantName);
        System.out.println("Policy Number: " + policyNumber);
        System.out.println("Email sent successfully!");
        System.out.println("============================");
    }
}
