package ragatanga.com.model;

import java.time.LocalDateTime;

public class DocumentMetadata {
    private String fileName;
    private String geminiResponseJson;

    private LocalDateTime createdAt = LocalDateTime.now();
}
