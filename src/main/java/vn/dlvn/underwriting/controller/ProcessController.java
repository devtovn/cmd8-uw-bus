package vn.dlvn.underwriting.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.dlvn.underwriting.model.ApplicationDto;

import java.util.HashMap;
import java.util.Map;

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
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Underwriting Service");
    }
    
}
