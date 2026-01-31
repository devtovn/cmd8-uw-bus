package vn.dlvn.underwriting.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import vn.dlvn.underwriting.model.ApplicationDto;

@RestController
@RequestMapping("/api/underwriting")
public class ProcessController {

    @Autowired
    private ZeebeClient zeebeClient;
    
    @PostMapping("/start")
    public Map<String, Object> startUnderwritingProcess(@RequestBody ApplicationDto application) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantName", application.getApplicantName());
        variables.put("age", application.getAge());
        variables.put("disease", application.getDisease());
        variables.put("policyType", application.getPolicyType());
        variables.put("coverageAmount", application.getCoverageAmount());
        
        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("uw-process")
                .latestVersion()
                .variables(variables)
                .send()
                .join();
        
        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceKey", event.getProcessInstanceKey());
        response.put("processDefinitionKey", event.getProcessDefinitionKey());
        response.put("bpmnProcessId", event.getBpmnProcessId());
        response.put("version", event.getVersion());
        response.put("application", application);
        
        return response;
    }
    
    // Thêm class nội bộ hoặc tạo file riêng nếu muốn
    static class ManualDecisionRequest {
        public long processInstanceKey;
        public String manualDecision;
        public String user;
    }

    @PostMapping("/manual-decision")
    public Map<String, Object> setManualDecision(
            @RequestBody ManualDecisionRequest request
    ) {
        // Cập nhật biến manualDecision
        zeebeClient
            .newSetVariablesCommand(request.processInstanceKey)
            .variables(Map.of("manualDecision", request.manualDecision))
            .local(false)
            .send()
            .join();

        boolean completed = false;

        // Kích hoạt và hoàn thành user task nếu tìm thấy đúng assignee
        var jobs = zeebeClient
            .newActivateJobsCommand()
            .jobType("io.camunda.zeebe:userTask")
            .maxJobsToActivate(20)
            .fetchVariables("manualDecision", "assignee")
            .send()
            .join()
            .getJobs();

        for (var job : jobs) {
            if (job.getProcessInstanceKey() == request.processInstanceKey &&
                "manualReview".equals(job.getElementId())) {
                Object assigneeObj = job.getVariablesAsMap().get("assignee");
                String assignee = assigneeObj != null ? assigneeObj.toString() : "";
                if (request.user != null && request.user.equals(assignee)) {
                    zeebeClient
                        .newCompleteCommand(job.getKey())
                        .variables(Map.of(
                            "manualReviewCompleted", true,
                            "reviewTimestamp", System.currentTimeMillis(),
                            "reviewerName", request.user,
                            "reviewNotes", "Approved via API",
                            "manualDecision", request.manualDecision
                        ))
                        .send()
                        .join();
                    completed = true;
                    break;
                }
            }
        }

        return Map.of(
            "processInstanceKey", request.processInstanceKey,
            "manualDecision", request.manualDecision,
            "user", request.user,
            "taskCompleted", completed
        );
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Underwriting Service");
    }
    
}
