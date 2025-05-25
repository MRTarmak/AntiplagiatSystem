package hse.antiplagiat.analysis.controller;

import hse.antiplagiat.analysis.dto.AnalysisResultDto;
import hse.antiplagiat.analysis.service.FileAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final FileAnalysisService analysisService;

    @GetMapping("/{fileId}")
    public ResponseEntity<AnalysisResultDto> analyzeFile(@PathVariable UUID fileId) throws Exception {
        return ResponseEntity.ok(analysisService.analyzeFile(fileId));
    }

    @GetMapping("/exists/{fileId}")
    public ResponseEntity<Boolean> isAnalysisExists(@PathVariable UUID fileId) {
        boolean exists = analysisService.isAnalysisExists(fileId);
        return ResponseEntity.ok(exists);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable UUID fileId) {
        analysisService.deleteAnalysis(fileId);
        return ResponseEntity.noContent().build();
    }
}