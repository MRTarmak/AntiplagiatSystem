package hse.antiplagiat.storage.controller;

import hse.antiplagiat.storage.dto.UploadResponseDto;
import hse.antiplagiat.storage.exception.FileNotFoundException;
import hse.antiplagiat.storage.model.FileEntity;
import hse.antiplagiat.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            description = "Сохраняет новый текстовый файл в хранилище (если файл с таким же содержанием не существует)." +
                    " Возвращает ID (добавленного или уже существующего файла) и флаг 'existed'. Имя файла берется из заголовков загружаемого файла.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные файла для загрузки. Содержимое файла должно быть в формате .txt. Имя файла будет автоматически получено из заголовков.",
                    required = true
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл успешно сохранен или найден", content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UploadResponseDto.class),
                                    examples = {
                                            @ExampleObject(name = "existing_file", value = "{\"id\":\"a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8\", \"existed\": true}"),
                                            @ExampleObject(name = "new_file", value = "{\"id\":\"a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8\", \"existed\": false}")
                                    })
                    }),
                    @ApiResponse(responseCode = "400", description = "Неверный запрос (например, пустой файл или неверный тип)", content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"message\": \"Cannot store empty file.\"}"))
                    })
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> uploadFile(
            @RequestPart("file") MultipartFile file) {

        return ResponseEntity.ok(fileStorageService.storeFile(file));
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