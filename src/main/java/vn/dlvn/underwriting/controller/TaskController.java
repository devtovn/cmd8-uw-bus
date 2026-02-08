package vn.dlvn.underwriting.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.camunda.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private ZeebeClient zeebeClient;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    
    /**
     * Complete upload additional docs task specifically
     */
    @PostMapping("/upload-docs/complete")
    public ResponseEntity<Map<String, Object>> completeUploadDocsTask(
            @RequestBody UploadDocsRequest request) {
        
        try {
            // Find active upload docs task
            var jobs = zeebeClient
                .newActivateJobsCommand()
                .jobType("io.camunda.zeebe:userTask")
                .maxJobsToActivate(20)
                .fetchVariables()
                .send()
                .join()
                .getJobs();
            
            var uploadDocsJob = jobs.stream()
                .filter(job -> "requestDocs".equals(job.getElementId()))
                .filter(job -> request.processInstanceKey == null || 
                              job.getProcessInstanceKey() == request.processInstanceKey)
                .findFirst()
                .orElse(null);
            
            if (uploadDocsJob == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            
            // Complete the upload docs task
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentsUploaded", true);
            variables.put("uploadedFileName", request.fileName);
            variables.put("uploadTimestamp", LocalDateTime.now().toString());
            variables.put("fileSize", request.fileSize);
            variables.put("additionalInfo", request.additionalInfo);
            //variables.put("disease", "NONE");  // Reset disease to avoid loop
            
            zeebeClient
                .newCompleteCommand(uploadDocsJob.getKey())
                .variables(variables)
                .send()
                .join();
            
            var response = new HashMap<String, Object>();
            response.put("success", true);
            response.put("jobKey", uploadDocsJob.getKey());
            response.put("processInstanceKey", uploadDocsJob.getProcessInstanceKey());
            response.put("message", "Upload docs task completed successfully");
            response.put("variables", variables);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to complete upload docs task: " + e.getMessage()));
        }
    }
    
    /**
     * Proxy endpoint for Camunda user-tasks search API
     * This avoids CORS issues when calling from frontend
     */
    @PostMapping("/user-tasks/search")
    public ResponseEntity<Map<String, Object>> searchUserTasks(@RequestBody Map<String, Object> searchRequest) {
        try {
            System.out.println("=== PROXY USER TASKS SEARCH ===");
            System.out.println("Search request: " + searchRequest);
            
            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity with the search request body
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(searchRequest, headers);
            
            // Forward the request to Camunda API
            String camundaUrl = "http://localhost:8080/v2/user-tasks/search";
            ResponseEntity<Map> response = restTemplate.exchange(
                camundaUrl, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            System.out.println("Camunda API response status: " + response.getStatusCode());
            System.out.println("Camunda API response body: " + response.getBody());
            
            // Return the response from Camunda API
            return ResponseEntity.status(response.getStatusCode())
                .body((Map<String, Object>) response.getBody());
            
        } catch (Exception e) {
            System.err.println("Error proxying user-tasks search: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to search user tasks: " + e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
                ));
        }
    }
    
    /**
     * Proxy endpoint for Camunda user-task completion API
     * This avoids CORS issues when calling from frontend
     */
    @PostMapping("/user-tasks/{userTaskKey}/completion")
    public ResponseEntity<Map<String, Object>> completeUserTask(
            @PathVariable String userTaskKey, 
            @RequestBody Map<String, Object> completionRequest) {
        try {
            System.out.println("=== PROXY USER TASK COMPLETION ===");
            System.out.println("User Task Key: " + userTaskKey);
            System.out.println("Completion request: " + completionRequest);
            
            // Add additional payload before completion
            Map<String, Object> additionalVariables = Map.of(
                "age", 35,
                "applicantName", "Luu Thi B",
                "coverageAmount", 50000,
                "disease", "MINOR",
                "policyType", "Life Insurance",
                "uwDecision", "REQUEST_DOC"
            );
            
            // Merge additional variables with the existing completion request
            if (completionRequest.containsKey("variables")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> variables = (Map<String, Object>) completionRequest.get("variables");
                variables.putAll(additionalVariables);
            } else {
                completionRequest.put("variables", additionalVariables);
            }
            
            System.out.println("Final completion request with additional payload: " + completionRequest);
            
            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity with the completion request body
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(completionRequest, headers);
            
            // Forward the request to Camunda API
            String camundaUrl = "http://localhost:8080/v2/user-tasks/" + userTaskKey + "/completion";
            ResponseEntity<Map> response = restTemplate.exchange(
                camundaUrl, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            System.out.println("Camunda API response status: " + response.getStatusCode());
            System.out.println("Camunda API response body: " + response.getBody());
            
            // Handle the response from Camunda API
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            
            // If Camunda API returns null/empty body (which is common for successful completions)
            if (responseBody == null) {
                responseBody = new HashMap<>();
                responseBody.put("success", true);
                responseBody.put("message", "User task completed successfully");
                responseBody.put("userTaskKey", userTaskKey);
                responseBody.put("timestamp", LocalDateTime.now().toString());
            }
            
            // Return the response from Camunda API
            return ResponseEntity.status(response.getStatusCode())
                .body(responseBody);
            
        } catch (Exception e) {
            System.err.println("Error proxying user-task completion: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to complete user task: " + e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()
                ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP", 
            "service", "Task Service",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
    
    // Request DTOs
    static class UploadDocsRequest {
        public Long processInstanceKey;
        public String fileName;
        public String fileSize;
        public String additionalInfo;
    }
}