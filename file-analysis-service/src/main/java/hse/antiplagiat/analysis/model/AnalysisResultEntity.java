package hse.antiplagiat.analysis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "analysis_results", schema = "public")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultEntity {
    @Id
    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "paragraphs_count", nullable = false)
    private int paragraphsCount;

    @Column(name = "words_count", nullable = false)
    private int wordsCount;

    @Column(name = "symbols_count", nullable = false)
    private int symbolsCount;

    @Column(name = "word_cloud_url")
    private String wordCloudUrl;
}