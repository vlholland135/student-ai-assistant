package edu.bellevue.cis530.studentai.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confirms the resource files are in the right place and load correctly.
 */
class DocumentLoaderServiceTest {

    private final DocumentLoaderService loader = new DocumentLoaderService();

    @Test
    void loadsWeek10Notes() {
        String notes = loader.loadWeek10Notes();

        assertThat(notes).contains("Stuffing the prompt");
        assertThat(notes).contains("RAG is different");
    }

    @Test
    void loadsThreeRagDocumentsWithSources() {
        List<Document> documents = loader.loadRagDocuments();

        assertThat(documents).hasSize(3);
        assertThat(documents)
                .extracting(document -> document.getMetadata().get("source"))
                .containsExactly("week10_notes.txt", "spring_ai_faq.txt", "rag_basics.txt");
        assertThat(documents).allSatisfy(document -> assertThat(document.getText()).isNotBlank());
    }
}
