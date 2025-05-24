package hse.antiplagiat.storage.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class UploadResponseDto {
    private UUID id;
    private boolean existed;
}