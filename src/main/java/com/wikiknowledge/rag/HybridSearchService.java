package com.wikiknowledge.rag;

import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.ChunkMatchResult;
import com.wikiknowledge.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 混合检索服务：向量检索 + 关键词检索，使用 RRF 融合排序。 */
@Service
public class HybridSearchService {

    private static final double RRF_K = 60.0;

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public HybridSearchService(ChunkRepository chunkRepository,
                               EmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    /**
     * 执行混合检索并返回融合排序后的切片。
     *
     * @param knowledgeBaseId 知识库 ID
     * @param query           用户问题
     * @param topK            返回数量
     * @return 融合排序后的切片列表
     */
    public List<ChunkMatch> search(Long knowledgeBaseId, String query, int topK) {
        int fetch = Math.max(topK * 2, 10);
        List<ChunkMatch> vectorResults = chunkRepository.searchSimilar(
                knowledgeBaseId,
                embeddingService.toVectorLiteral(embeddingService.embed(query)),
                fetch
        );
        List<ChunkMatch> keywordResults = chunkRepository.searchKeywords(
                knowledgeBaseId,
                query,
                fetch
        );
        return rrf(vectorResults, keywordResults, topK);
    }

    /**
     * Reciprocal Rank Fusion：把两个检索结果的排名融合成一个分数。
     */
    private List<ChunkMatch> rrf(List<ChunkMatch> vectorResults,
                                 List<ChunkMatch> keywordResults,
                                 int topK) {
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, Double> bestSimilarity = new HashMap<>();
        Map<Long, ChunkMatch> byId = new HashMap<>();
        addRanking(vectorResults, scores, bestSimilarity, byId);
        addRanking(keywordResults, scores, bestSimilarity, byId);

        List<Map.Entry<Long, Double>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<ChunkMatch> results = sorted.stream()
                .limit(topK)
                .map(entry -> {
                    ChunkMatch match = byId.get(entry.getKey());
                    return new ChunkMatchResult(
                            match.getId(),
                            match.getDocumentId(),
                            match.getKnowledgeBaseId(),
                            match.getContent(),
                            match.getSeqNo(),
                            bestSimilarity.getOrDefault(entry.getKey(), 0.0)
                    );
                })
                .collect(java.util.stream.Collectors.toList());
        return results;
    }

    private void addRanking(List<ChunkMatch> results,
                            Map<Long, Double> scores,
                            Map<Long, Double> bestSimilarity,
                            Map<Long, ChunkMatch> byId) {
        for (int i = 0; i < results.size(); i++) {
            ChunkMatch match = results.get(i);
            scores.merge(match.getId(), 1.0 / (RRF_K + i + 1), Double::sum);
            byId.putIfAbsent(match.getId(), match);
            double similarity = match.getSimilarity() == null ? 0.0 : match.getSimilarity();
            bestSimilarity.merge(match.getId(), similarity, Math::max);
        }
    }
}
