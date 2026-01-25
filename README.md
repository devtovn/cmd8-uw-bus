
# Underwriting Camunda 8 Project

Demo project for New Business Underwriting using Camunda 8 (Zeebe + DMN + Workers).

## Modules
- bpmn/: BPMN & DMN definitions
- src/: Spring Boot Java workers

## Run
1. Start Camunda 8 (Docker / SaaS)
2. mvn clean package
3. java -jar target/cmd8-uw-bus.jar
