package com.wikiknowledge.rag;

import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.rag.dto.ChatRequest;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private HybridSearchService hybridSearchService;

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @InjectMocks
    private RagService ragService;

    @Test
    void returnsErrorWhenNoRelevantContext() {
        KnowledgeBase kb = knowledgeBase(1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(hybridSearchService.search(eq(1L), eq("问题"), eq(5))).thenReturn(List.of());

        List<RagEvent> events = ragService.chat(new ChatRequest(1L, "问题", null, null))
                .map(serverEvent -> serverEvent.data())
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).type()).isEqualTo("error");
    }

    @Test
    void returnsAiNotEnabledWhenChatModelMissing() {
        KnowledgeBase kb = knowledgeBase(1L);
        ChunkMatch match = chunkMatch(0.5);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(hybridSearchService.search(eq(1L), eq("问题"), eq(5))).thenReturn(List.of(match));
        when(chatModelProvider.getIfAvailable()).thenReturn(null);

        List<RagEvent> events = ragService.chat(new ChatRequest(1L, "问题", null, null))
                .map(serverEvent -> serverEvent.data())
                .collectList()
                .block();

        assertThat(events.get(0).type()).isEqualTo("error");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) events.get(0).data();
        assertThat(data.get("code")).isEqualTo("AI_NOT_ENABLED");
    }

    @Test
    void streamsStartAndDoneWithCitations() {
        KnowledgeBase kb = knowledgeBase(1L);
        ChunkMatch match = chunkMatch(0.5);
        ChatModel chatModel = mock(ChatModel.class);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(hybridSearchService.search(eq(1L), eq("问题"), eq(5))).thenReturn(List.of(match));
        when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.empty());

        List<RagEvent> events = ragService.chat(new ChatRequest(1L, "问题", null, null))
                .map(serverEvent -> serverEvent.data())
                .collectList()
                .block();

        assertThat(events).extracting(RagEvent::type).containsExactly("start", "done");
        @SuppressWarnings("unchecked")
        Map<String, Object> doneData = (Map<String, Object>) events.get(1).data();
        assertThat(doneData).containsKey("citations");
    }

    private KnowledgeBase knowledgeBase(Long id) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(id);
        kb.setStatus("ACTIVE");
        return kb;
    }

    private ChunkMatch chunkMatch(double similarity) {
        ChunkMatch match = mock(ChunkMatch.class);
        lenient().when(match.getId()).thenReturn(11L);
        lenient().when(match.getDocumentId()).thenReturn(2L);
        lenient().when(match.getSeqNo()).thenReturn(1);
        lenient().when(match.getSimilarity()).thenReturn(similarity);
        lenient().when(match.getContent()).thenReturn("维基知识库相关资料");
        return match;
    }
}
