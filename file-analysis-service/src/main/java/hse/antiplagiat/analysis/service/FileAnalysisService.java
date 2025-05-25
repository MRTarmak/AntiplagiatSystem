package hse.antiplagiat.analysis.service;

import hse.antiplagiat.analysis.dto.AnalysisResultDto;
import hse.antiplagiat.analysis.exception.AnalysisNotFoundException;
import hse.antiplagiat.analysis.exception.FileAnalysisException;
import hse.antiplagiat.analysis.model.AnalysisResultEntity;
import hse.antiplagiat.analysis.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileAnalysisService {
    private final RestTemplate restTemplate;
    private final AnalysisResultRepository analysisResultRepository;

    private static final String FILE_STORAGE_SERVICE_URL = "http://file-storage-service/api/files";
    private static final String WORD_CLOUD_API_URL = "http://wordcloudapi/generate";

    public AnalysisResultDto analyzeFile(UUID fileId) throws Exception {
        if (analysisResultRepository.existsByFileId(fileId)) {
            AnalysisResultEntity result = analysisResultRepository.findByFileId(fileId)
                    .orElseThrow(() -> new FileAnalysisException("Existing result not found by file id"));

            return AnalysisResultDto.builder()
                    .fileId(result.getFileId())
                    .paragraphsCount(result.getParagraphsCount())
                    .wordsCount(result.getWordsCount())
                    .symbolsCount(result.getSymbolsCount())
                    .wordCloudUrl(result.getWordCloudUrl())
                    .build();
        }

        ResponseEntity<String> response = restTemplate.getForEntity(
                FILE_STORAGE_SERVICE_URL + "/{id}",
                String.class,
                Collections.singletonMap("id", fileId.toString())
        );

        if (response.getStatusCode().isError()) {
            throw new Exception("Error fetching file from storage service");
        }

        String content = response.getBody();

        if (content == null || content.isEmpty()) {
            throw new Exception("Empty file content received");
        }

        int paragraphs = countParagraphs(content);
        int words = countWords(content);
        int symbols = content.length();

//        Map<String, Object> wordCloudRequest = Map.of("text", content);
//        ResponseEntity<String> cloudResponse = restTemplate.postForEntity(WORD_CLOUD_API_URL, wordCloudRequest, String.class);
//        String wordCloudUrl = cloudResponse.getBody();

        AnalysisResultEntity result = AnalysisResultEntity.builder()
                .fileId(fileId)
                .paragraphsCount(paragraphs)
                .wordsCount(words)
                .symbolsCount(symbols)
//                .wordCloudUrl(wordCloudUrl)
                .build();

        analysisResultRepository.save(result);

        return AnalysisResultDto.builder()
                .fileId(result.getFileId())
                .paragraphsCount(result.getParagraphsCount())
                .wordsCount(result.getWordsCount())
                .symbolsCount(result.getSymbolsCount())
                .wordCloudUrl(result.getWordCloudUrl())
                .build();
    }

    public Boolean isAnalysisExists(UUID fileId) {
        return analysisResultRepository.existsByFileId(fileId);
    }

    public void deleteAnalysis(UUID fileId) {
        AnalysisResultEntity result = analysisResultRepository.findById(fileId)
                .orElseThrow(() -> new AnalysisNotFoundException("Analysis result not found with file ID: " + fileId));
        analysisResultRepository.delete(result);
    }

    private int countParagraphs(String content) {
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .toList()
                .size();
    }

    private int countWords(String content) {
        return content.trim().split("\\s+").length;
    }
}