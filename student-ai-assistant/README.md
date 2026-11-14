# Student AI Assistant

Assignment 10.2 for CIS 530. This is a Spring Boot application that uses Spring AI and OpenAI to answer student questions in four ways: a basic prompt, structured output mapped to a Java DTO, context stuffing with course notes, and introductory Retrieval Augmented Generation (RAG) over a small in memory vector store.

## Tech Stack

* Java 25
* Spring Boot 3.5.7
* Spring AI 1.1.0 (OpenAI chat and embedding models)
* SimpleVectorStore with QuestionAnswerAdvisor for RAG
* Lombok and Jakarta Validation
* JUnit 5 and MockMvc for tests

## Project Structure

```
src/main/java/edu/bellevue/cis530/studentai/
  StudentAiAssistantApplication.java
  config/
    StudentAiConfig.java          ChatClient, VectorStore, and startup document loader
  controller/
    AiController.java             The four REST endpoints
  dto/
    AiQuestionRequest.java
    AiTextResponse.java
    ContextHelpRequest.java
    RagHelpRequest.java
    StudyPlanRequest.java
    StudyPlanResponse.java
  service/
    AiService.java                Service interface
    DocumentLoaderService.java    Reads the text files from resources
    impl/
      AiAssistantService.java     Prompting, structured output, stuffing, and RAG logic
src/main/resources/
  application.properties
  docs/
    week10_notes.txt
    spring_ai_faq.txt
    rag_basics.txt
```

## Running the Application

1. Set your OpenAI key in the same terminal (or IntelliJ run configuration) used to start the app.

   macOS or Linux:
   ```
   export OPENAI_API_KEY=your_real_api_key_here
   ```
   Windows PowerShell:
   ```
   $env:OPENAI_API_KEY="your_real_api_key_here"
   ```

2. Start the application:
   ```
   mvn spring-boot:run
   ```

3. The console should show Tomcat started on port 8080 and the message `RAG documents loaded into the vector store.`

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/ai/ask` | Sends the question straight to the model and returns plain text |
| POST | `/api/ai/study-plan` | Returns a structured study plan mapped into `StudyPlanResponse` |
| POST | `/api/ai/context-help` | Stuffs `week10_notes.txt` into the prompt before asking the question |
| POST | `/api/ai/rag-help` | Retrieves the most relevant documents from the vector store and sends them as context |

Sample requests for every endpoint are in `requests.http`, which can be run directly from IntelliJ HTTP Client or copied into Postman.

## Running the Tests

```
mvn test
```

The controller tests use a fake `AiService`, so they run without an API key and do not call OpenAI. They check that each endpoint returns the expected JSON and that validation rejects blank or missing fields. `DocumentLoaderServiceTest` confirms the resource files load and that all three RAG documents carry the correct source metadata.

## Notes on Changes From the Handout

A few pieces of the provided starter code would not compile as written, so they were adjusted:

* `AiService` now declares `contextHelp(ContextHelpRequest)` and `ragHelp(RagHelpRequest)` so the interface matches the implementation and the controller.
* The DTOs use Lombok `@Data`, which generates getters such as `getStudentName()`. The service calls these getters instead of record style accessors like `studentName()`.
* Missing Lombok imports and a missing semicolon in `AiTextResponse` were added.
* `RagHelpRequest` was created, since it was referenced but not provided.
* The RAG text content from Part 4 was placed in `docs/spring_ai_faq.txt` and `docs/rag_basics.txt` to match the file paths used by `DocumentLoaderService`.

## Reflection

**What is the difference between a basic prompt endpoint and a structured output endpoint?**

The basic `/ask` endpoint sends the question to the model and returns whatever text comes back. Nothing about the shape of the answer is guaranteed, so it works well for a person reading it but is hard for other code to use. The `/study-plan` endpoint calls `.entity(StudyPlanResponse.class)`, which has Spring AI add format instructions to the prompt and then convert the JSON reply into a Java object. The application gets named fields and real lists it can rely on rather than a block of text it would have to parse by hand.

**What is the difference between stuffing the prompt and RAG?**

Stuffing the prompt means the application pastes an entire document into every request, whether or not all of it is relevant. That is simple and works for one small file, but it becomes expensive and eventually runs past the context window as the material grows. RAG first converts the documents into embeddings and stores them in a vector store. When a question arrives, only the most similar pieces are retrieved and added to the prompt by the `QuestionAnswerAdvisor`. The model still sees outside context, but it is selected per question, which scales to far more material.

**Which part of the assignment was most challenging, and why?**

The most challenging part was getting the starter code to compile, since the interface, DTOs, and service implementation did not agree with each other. Working through those errors required understanding how Lombok generates accessors and how Spring resolves the service interface for the controller, which made the overall flow of the application much clearer.
