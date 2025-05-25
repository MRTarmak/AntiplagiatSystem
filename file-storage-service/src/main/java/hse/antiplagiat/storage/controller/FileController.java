package hse.antiplagiat.storage.controller;

import hse.antiplagiat.storage.dto.UploadResponseDto;
import hse.antiplagiat.storage.exception.FileNotFoundException;
import hse.antiplagiat.storage.model.FileEntity;
import hse.antiplagiat.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Файловое хранилище", description = "API для загрузки, получения и удаления файлов")
public class FileController {
    private final FileStorageService fileStorageService;

    @Operation(
            summary = "Загрузка файла",
            description = "Сохраняет новый файл в хранилище (если там нет файла с таким же содержанием)" +
                    " и возвращает id (добавленного файла или найденного).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл успешно сохранен или найден", content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UploadResponseDto.class),
                                    examples = {
                                            @ExampleObject(name = "existing_file", value = "{\"id\":\"a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8\", \"existed\": true}"),
                                            @ExampleObject(name = "new_file", value = "{\"id\":\"a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8\", \"existed\": false}")
                                    })
                    })
            }
    )
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadFile(
            @Parameter(description = "Имя файла", required = true)
            @RequestParam("name") String name,

            @Parameter(description = "Содержимое файла в виде строки", required = true)
            @RequestParam("content") String content) {

        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok(fileStorageService.storeFile(name, contentBytes));
    }

    @Operation(
            summary = "Получение файла по ID",
            description = "Возвращает содержимое файла по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл найден", content = {
                            @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "HelloWorld"))
                    }),
                    @ApiResponse(responseCode = "404", description = "Файл не найден", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = FileNotFoundException.class))
                    })
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<String> getFileById(
            @Parameter(description = "UUID файла", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {

        FileEntity file = fileStorageService.getFileById(id);
        String content = new String(file.getContent(), StandardCharsets.UTF_8);

        return ResponseEntity.ok(content);
    }

    @Operation(
            summary = "Получение файла по хэшу",
            description = "Возвращает содержимое файла по SHA-256 хэшу",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл найден", content = {
                            @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "HelloWorld"))
                    }),
                    @ApiResponse(responseCode = "404", description = "Файл не найден", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = FileNotFoundException.class))
                    })
            }
    )
    @GetMapping("/hash/{hash}")
    public ResponseEntity<String> getFileByHash(
            @Parameter(description = "SHA-256 хэш файла", example = "a1b2c3d4e5f67890... (минимум 64 символа)")
            @PathVariable String hash) {

        FileEntity file = fileStorageService.getFileByHash(hash);
        String content = new String(file.getContent(), StandardCharsets.UTF_8);

        return ResponseEntity.ok(content);
    }

    @Operation(
            summary = "Удаление файла",
            description = "Удаляет файл из хранилища по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Файл успешно удален"),
                    @ApiResponse(responseCode = "404", description = "Файл не найден", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = FileNotFoundException.class))
                    })
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "UUID файла", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {

        fileStorageService.deleteFile(id);

        return ResponseEntity.noContent().build();
    }
}