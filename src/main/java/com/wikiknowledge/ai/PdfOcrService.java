package com.wikiknowledge.ai;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * PDF OCR 兜底：当 PDF 文本层无法正确提取（乱码）时，逐页渲染图片并调用千问视觉模型识别文字。
 */
@Service
public class PdfOcrService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final int maxPages;
    private final int dpi;

    public PdfOcrService(@Value("${spring.ai.openai.api-key}") String apiKey,
                         @Value("${spring.ai.openai.base-url}") String baseUrl,
                         @Value("${app.ai.ocr-model:qwen-vl-plus}") String model,
                         @Value("${app.ai.ocr-max-pages:100}") int maxPages,
                         @Value("${app.ai.ocr-dpi:150}") int dpi) {
        this.apiKey = apiKey;
        this.model = model;
        this.maxPages = maxPages;
        this.dpi = dpi;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl + "/v1/chat/completions")
                .build();
    }

    public String extractText(InputStream pdfStream) throws IOException {
        byte[] bytes = pdfStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            int limit = Math.min(pageCount, maxPages);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                String pageText = ocrPage(renderer, i);
                if (pageText != null && !pageText.isBlank()) {
                    result.append("【第").append(i + 1).append("页】\n").append(pageText).append("\n\n");
                }
            }
            return result.toString();
        }
    }

    private String ocrPage(PDFRenderer renderer, int pageIndex) throws IOException {
        BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        String dataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", "请完整识别并输出这张图片中的所有文字，保留标题、段落和换行。"),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        )
                )),
                "max_tokens", 4096
        );

        OcrResponse response = restClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(OcrResponse.class);
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "";
        }
        String content = response.choices().get(0).message().content();
        return content == null ? "" : content;
    }

    private record OcrResponse(List<Choice> choices) {
    }

    private record Choice(Message message) {
    }

    private record Message(String content) {
    }
}
