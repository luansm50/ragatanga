package ragatanga.com.services;

import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragatanga.com.model.dto.FileInsertDTO;
import ragatanga.com.model.FileModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class FileService {
    @Autowired
    private SupabaseService supabaseService;

    public void saveFile(String folderId, List<FileModel> files) throws IOException {
        for (FileModel file : files) {
            FileInsertDTO payload = FileInsertDTO.withMetadata(folderId, file);
            supabaseService.insert("documents", payload);
            supabaseService.uploadFile(folderId, payload.id(), file.base64());
        }
    }

    public JsonArray getFiles(String uuid) throws IOException {
        return supabaseService.fetchRecords("documents", Map.of("folderId", uuid));
    }
}
