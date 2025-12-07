package ragatanga.com.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ragatanga.com.model.FileModel;
import ragatanga.com.model.dto.FileUpdateDTO;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public void processPendingDocuments() throws IOException, ParseException {
        Map<String, String> filters = Map.of("status", "PENDING");
        JsonArray pendingRecords = supabaseService.fetchRecords("documents", filters);

        for (JsonElement recordElem : pendingRecords) {
            uodate(recordElem);
        }
    }

    private void uodate(JsonElement recordElem) throws IOException, ParseException {
        JsonObject record = recordElem.getAsJsonObject();
        String id = record.get("id").getAsString();
        String fileName = record.get("name").getAsString();
        String folderId = record.get("folderId").getAsString();
        String status = record.get("status").getAsString();
        try {
            byte[] fileBytes = supabaseService.downloadFile(folderId, id);
            FileModel fileModel = new FileModel(folderId, fileName, Base64.getEncoder().encodeToString(fileBytes), "", status);

            Map metadata = geminiService.generateContent(fileModel);
            FileUpdateDTO dto = processInfos(id, metadata);
            supabaseService.update("documents", id, dto);
        } catch (Exception ex) {
            supabaseService.update("documents", id, new FileUpdateDTO(id, null, null, null, null, null, null, "ERROR"));
        }
    }

    private FileUpdateDTO processInfos(String id, Map metadata) throws ParseException {
        Map analiseDocumental = (Map) metadata.getOrDefault("analise_documental", new HashMap<>());
        String tipoDoc = (String) analiseDocumental.getOrDefault("classificacao_cod", "PET_SIMPLES");
        String resumo = (String) analiseDocumental.getOrDefault("resumo_conteudo", "Documento sem nome");
        String descricao = (String) analiseDocumental.getOrDefault("classificacao_desc", "");
        List<Double> embedding = geminiService.gerarEmbedding(resumo);

        String data = getData(metadata);

        return new FileUpdateDTO(id, metadata, tipoDoc, embedding, resumo, data, descricao, "PROCESSED");
    }

    private String getData(Map metadata) throws ParseException {
        Map entidadesExtraidas = (Map) metadata.getOrDefault("entidades_extraidas", new HashMap<>());
        List datasChave = (List) entidadesExtraidas.getOrDefault("datas_chave", List.of());
        String data = null;
        if (datasChave.size() > 0) {
            data = (String) ((Map) datasChave.get(0)).getOrDefault("data", null);
            data = formatData(data);
        }
        return data;
    }

    private double[] textToVector(String text, int tamanho) {
        double[] vetor = new double[tamanho];
        for (int i = 0; i < Math.min(text.length(), tamanho); i++) {
            vetor[i] = (double) text.charAt(i);
        }
        return vetor;
    }

    private String formatData(String data) throws ParseException {
        if (data == null) return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return new SimpleDateFormat("yyyy-MM-dd").format(simpleDateFormat.parse(data));
    }
}
