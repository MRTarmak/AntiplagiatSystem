package hse.antiplagiat.analysis.controller;

import hse.antiplagiat.analysis.dto.AnalysisResultDto;
import hse.antiplagiat.analysis.service.FileAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "File Analysis API", description = "API для анализа текстовых файлов")
public class AnalysisController {
    private final FileAnalysisService analysisService;

    public AnalysisController(FileAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @Operation(
            summary = "Анализировать файл по его ID",
            description = "Выполняет статистический анализ текстового файла. Если анализ" +
                    "для данного файла уже существует, возвращает существующие результаты.",
            parameters = {
                    @Parameter(
                            name = "fileId",
                            description = "Уникальный ID файла для анализа",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Анализ успешно выполнен или возвращены существующие результаты",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AnalysisResultDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный запрос (например, файл пуст или не найден при попытке анализа)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Map.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Анализ не найден (если запрашиваются существующие результаты, но их нет)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Map.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера при анализе файла (например, проблемы с File Storage Service или QuickChart.io)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Map.class)
                            )
                    )
            }
    )
    @GetMapping("/{fileId}")
    public ResponseEntity<AnalysisResultDto> analyzeFile(@PathVariable UUID fileId) {
        return ResponseEntity.ok(analysisService.analyzeFile(fileId));
    }

    @Operation(
            summary = "Проверить наличие результатов анализа для файла",
            description = "Проверяет, существует ли уже результат анализа для файла с указанным ID.",
            parameters = {
                    @Parameter(
                            name = "fileId",
                            description = "Уникальный ID файла",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Возвращает true, если анализ существует, false в противном случае.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Boolean.class)
                            )
                    )
            }
    )
    @GetMapping("/exists/{fileId}")
    public ResponseEntity<Boolean> isAnalysisExists(@PathVariable UUID fileId) {
        boolean exists = analysisService.isAnalysisExists(fileId);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Удалить результаты анализа файла",
            description = "Удаляет существующие результаты анализа для файла с указанным ID.",
            parameters = {
                    @Parameter(
                            name = "fileId",
                            description = "Уникальный ID файла, результаты анализа которого нужно удалить",
                            required = true,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Анализ успешно удален (нет содержимого)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Результаты анализа для данного ID файла не найдены",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Map.class)
                            )
                    )
            }
    )
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable UUID fileId) {
        analysisService.deleteAnalysis(fileId);
        return ResponseEntity.noContent().build();
    }
}