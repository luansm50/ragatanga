package ragatanga.com.model.dto;

import ragatanga.com.model.FileModel;

import java.util.UUID;

public record FileInsertDTO(String id, String folderId, String name, String metadata, String status) {
    public static FileInsertDTO withMetadata(String folderId, FileModel model) {
        return new FileInsertDTO(UUID.randomUUID().toString(), folderId, model.name(), "", "PENDING");
    }
}
