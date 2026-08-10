package com.wikiknowledge.ai;

import com.wikiknowledge.domain.Chunk;
import com.wikiknowledge.repository.ChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VectorizationService {

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public VectorizationService(ChunkRepository chunkRepository,
                                EmbeddingService embeddingService) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public void vectorize(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            float[] embedding = embeddingService.embed(chunk.getContent());
            chunkRepository.updateEmbedding(chunk.getId(), embeddingService.toVectorLiteral(embedding));
        }
    }
}
