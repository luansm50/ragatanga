package ragatanga.com.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragatanga.com.model.FileModel;
import ragatanga.com.model.dto.FileUpdateDTO;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupabaseGeminiService {

    @Autowired
    private GeminiService geminiService;
    @Autowired
    private SupabaseService supabaseService;

    public void processPendingDocuments() throws IOException {
        Map<String, String> filters = Map.of("status", "PENDING");
        JsonArray pendingRecords = supabaseService.fetchRecords("documents", filters);

        for (JsonElement recordElem : pendingRecords) {
            JsonObject record = recordElem.getAsJsonObject();
            String id = record.get("id").getAsString();
            String fileName = record.get("name").getAsString();
            String folderId = record.get("folderId").getAsString();
            String status = record.get("status").getAsString();

            byte[] fileBytes = supabaseService.downloadFile(folderId, id);
            FileModel fileModel = new FileModel(folderId, fileName, Base64.getEncoder().encodeToString(fileBytes), "", status);

            Map metadata = geminiService.generateContent(fileModel);
            FileUpdateDTO dto = processInfos(id, metadata);
            supabaseService.update("documents", id, dto);
        }
    }

    private FileUpdateDTO processInfos(String id, Map metadata) {
        Map analiseDocumental = (Map) metadata.getOrDefault("analise_documental", new HashMap<>());
        String tipoDoc = (String) analiseDocumental.getOrDefault("classificacao_cod", "PET_SIMPLES");
        String resumo = (String) analiseDocumental.getOrDefault("resumo_conteudo", "Documento sem nome");
        double[] embedding = textToVector(resumo, 512);
        return new FileUpdateDTO(id, metadata, tipoDoc, embedding, resumo,"PROCESSED");
    }

    private double[] textToVector(String text, int tamanho) {
        double[] vetor = new double[tamanho];
        for (int i = 0; i < Math.min(text.length(), tamanho); i++) {
            vetor[i] = (double) text.charAt(i);
        }
        return vetor;
    }
}
