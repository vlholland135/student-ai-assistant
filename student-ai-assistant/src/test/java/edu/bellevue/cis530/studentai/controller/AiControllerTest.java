package edu.bellevue.cis530.studentai.controller;

import edu.bellevue.cis530.studentai.dto.AiQuestionRequest;
import edu.bellevue.cis530.studentai.dto.AiTextResponse;
import edu.bellevue.cis530.studentai.dto.ContextHelpRequest;
import edu.bellevue.cis530.studentai.dto.RagHelpRequest;
import edu.bellevue.cis530.studentai.dto.StudyPlanRequest;
import edu.bellevue.cis530.studentai.dto.StudyPlanResponse;
import edu.bellevue.cis530.studentai.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for AiController.
 * A fake AiService is used so these tests run without an OpenAI API key
 * and without making any real calls to the model.
 */
@WebMvcTest(AiController.class)
@Import(AiControllerTest.FakeServiceConfig.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FakeServiceConfig {
        @Bean
        AiService aiService() {
            return new AiService() {
                @Override
                public AiTextResponse askQuestion(AiQuestionRequest request) {
                    return new AiTextResponse("ask: " + request.getQuestion(), "openai");
                }

                @Override
                public StudyPlanResponse createStudyPlan(StudyPlanRequest request) {
                    return new StudyPlanResponse(
                            "Plan for " + request.getStudentName(),
                            List.of("Step one", "Step two", "Step three"),
                            List.of("RAG", "Vector store", "Embedding", "Advisor"),
                            "You can do this.");
                }

                @Override
                public AiTextResponse contextHelp(ContextHelpRequest request) {
                    return new AiTextResponse("context: " + request.getQuestion(), "openai");
                }

                @Override
                public AiTextResponse ragHelp(RagHelpRequest request) {
                    return new AiTextResponse("rag: " + request.getQuestion(), "openai");
                }
            };
        }
    }

    @Test
    void askReturnsAnswerAndProvider() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"What is Spring AI?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("ask: What is Spring AI?"))
                .andExpect(jsonPath("$.provider").value("openai"));
    }

    @Test
    void studyPlanReturnsStructuredFields() throws Exception {
        String body = """
                {
                  "studentName": "Mia",
                  "topic": "RAG in Spring AI",
                  "problem": "I understand prompts but I do not understand how RAG works.",
                  "goal": "Be ready for the Week 10 lab."
                }
                """;

        mockMvc.perform(post("/api/ai/study-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Plan for Mia"))
                .andExpect(jsonPath("$.actionItems.length()").value(3))
                .andExpect(jsonPath("$.importantTerms.length()").value(4))
                .andExpect(jsonPath("$.encouragement").exists());
    }

    @Test
    void contextHelpReturnsAnswer() throws Exception {
        mockMvc.perform(post("/api/ai/context-help")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"What is prompt stuffing?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("context: What is prompt stuffing?"));
    }

    @Test
    void ragHelpReturnsAnswer() throws Exception {
        mockMvc.perform(post("/api/ai/rag-help")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"What is RAG?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("rag: What is RAG?"));
    }

    @Test
    void blankQuestionIsRejected() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void studyPlanWithMissingFieldIsRejected() throws Exception {
        mockMvc.perform(post("/api/ai/study-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentName\": \"Mia\", \"topic\": \"RAG\"}"))
                .andExpect(status().isBadRequest());
    }
}
