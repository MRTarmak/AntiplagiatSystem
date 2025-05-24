package hse.antiplagiat.storage.service;

import hse.antiplagiat.storage.dto.UploadResponseDto;
import hse.antiplagiat.storage.exception.FileNotFoundException;
import hse.antiplagiat.storage.exception.FileStorageException;
import hse.antiplagiat.storage.model.FileEntity;
import hse.antiplagiat.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileRepository fileRepository;

    public UploadResponseDto storeFile(String name, byte[] content) {
        try {
            String hash = calculateHash(content);

            if (fileRepository.existsByHash(hash)) {
                FileEntity file = fileRepository.findByHash(hash)
                        .orElseThrow(() -> new FileStorageException("Existing file not found by hash"));

                return UploadResponseDto.builder()
                        .id(file.getId())
                        .existed(true)
                        .build();
            }

            FileEntity file = FileEntity.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .content(content)
                    .hash(hash)
                    .build();

            fileRepository.save(file);

            return UploadResponseDto.builder()
                    .id(file.getId())
                    .existed(false)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new FileStorageException("Error calculating file hash", e);
        }
    }

    public FileEntity getFileById(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + id));
    }

    public FileEntity getFileByHash(String hash) {
        return fileRepository.findByHash(hash)
                .orElseThrow(() -> new FileNotFoundException("File not found with hash: " + hash));
    }

    public void deleteFile(UUID id) {
        FileEntity file = getFileById(id);
        fileRepository.delete(file);
    }

    private String calculateHash(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(content);

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}