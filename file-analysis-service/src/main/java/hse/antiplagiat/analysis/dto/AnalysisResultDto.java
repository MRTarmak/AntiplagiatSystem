package hse.antiplagiat.analysis.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AnalysisResultDto {
    private UUID fileId;
    private int paragraphsCount;
    private int wordsCount;
    private int symbolsCount;
    private String wordCloudUrl;
}