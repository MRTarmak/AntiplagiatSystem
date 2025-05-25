package hse.antiplagiat.analysis.service;

import hse.antiplagiat.analysis.dto.AnalysisResultDto;
import hse.antiplagiat.analysis.exception.AnalysisNotFoundException;
import hse.antiplagiat.analysis.exception.FileAnalysisException;
import hse.antiplagiat.analysis.model.AnalysisResultEntity;
import hse.antiplagiat.analysis.repository.AnalysisResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class FileAnalysisService {
    private final RestClient loadBalancedRestClient;
    private final RestClient nonLoadBalancedRestClient;
    private final AnalysisResultRepository analysisResultRepository;

    private static final Logger log = LoggerFactory.getLogger(FileAnalysisService.class);

    private static final String FILE_STORAGE_API_URL = "http://FILE-STORAGE-SERVICE/api/files/{fileId}";
    private static final String WORD_CLOUD_API_URL = "https://quickchart.io/wordcloud";

    public FileAnalysisService(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedBuilder,
            @Qualifier("nonLoadBalancedRestClientBuilder") RestClient.Builder nonLoadBalancedBuilder,
            AnalysisResultRepository analysisResultRepository) {
        this.loadBalancedRestClient = loadBalancedBuilder.build();
        this.nonLoadBalancedRestClient = nonLoadBalancedBuilder.build();
        this.analysisResultRepository = analysisResultRepository;
    }

    public AnalysisResultDto analyzeFile(UUID fileId) {
        if (analysisResultRepository.existsByFileId(fileId)) {
            AnalysisResultEntity result = analysisResultRepository.findByFileId(fileId)
                    .orElseThrow(() -> new AnalysisNotFoundException("Existing analysis result not found by file ID: " + fileId));
            return mapToDto(result);
        }

        try {
            String content = fetchFileContent(fileId);

            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("File content is empty or not found for ID: " + fileId);
            }

            int paragraphsCount = countParagraphs(content);
            int wordsCount = countWords(content);
            int symbolsCount = content.length();
            String wordCloudUrl = generateWordCloudUrl(content);

            AnalysisResultEntity entity = AnalysisResultEntity.builder()
                    .fileId(fileId)
                    .paragraphsCount(paragraphsCount)
                    .wordsCount(wordsCount)
                    .symbolsCount(symbolsCount)
                    .wordCloudUrl(wordCloudUrl)
                    .build();

            analysisResultRepository.save(entity);

            return mapToDto(entity);

        } catch (Exception e) {
            log.error("Error analyzing file with ID: {}", fileId, e);
            throw new FileAnalysisException("Failed to analyze file", e);
        }
    }

    public Boolean isAnalysisExists(UUID fileId) {
        return analysisResultRepository.existsByFileId(fileId);
    }

    public void deleteAnalysis(UUID fileId) {
        AnalysisResultEntity result = analysisResultRepository.findByFileId(fileId)
                .orElseThrow(() -> new AnalysisNotFoundException("Analysis result not found with file ID: " + fileId));
        analysisResultRepository.delete(result);
    }

    private String fetchFileContent(UUID fileId) {
        try {
            return loadBalancedRestClient.get()
                    .uri(FILE_STORAGE_API_URL, fileId)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Error fetching file content from file-storage-service for ID: {}", fileId, e);
            throw new FileAnalysisException("Failed to fetch file content from file-storage-service", e);
        }
    }

    private String generateWordCloudUrl(String text) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("maxNumWords", 100);
        requestBody.put("backgroundColor", "#ffffff");
        requestBody.put("fontFamily", "sans-serif");
        requestBody.put("removeStopwords", true);
        requestBody.put("cleanWords", true);
        requestBody.put("language", "en");
        requestBody.put("minWordLength", 3);

        try {
            return nonLoadBalancedRestClient.post()
                    .uri(WORD_CLOUD_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Error generating word cloud for text: {}", text.substring(0, Math.min(text.length(), 50)), e);
            throw new FileAnalysisException("Failed to generate word cloud", e);
        }
    }

    private int countParagraphs(String content) {
        return (int) Arrays.stream(content.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .count();
    }

    private int countWords(String content) {
        return content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
    }

    private AnalysisResultDto mapToDto(AnalysisResultEntity entity) {
        return AnalysisResultDto.builder()
                .fileId(entity.getFileId())
                .paragraphsCount(entity.getParagraphsCount())
                .wordsCount(entity.getWordsCount())
                .symbolsCount(entity.getSymbolsCount())
                .wordCloudUrl(entity.getWordCloudUrl())
                .build();
    }
}