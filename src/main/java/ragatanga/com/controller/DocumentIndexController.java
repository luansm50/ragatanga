package ragatanga.com.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ragatanga.com.model.IndexRequest;
import ragatanga.com.services.GeminiService;
import ragatanga.com.services.FileService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
class DocumentIndexController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private FileService fileService;

    @PostMapping("/process-file")
    public Map<String, Object> processFile(@RequestBody IndexRequest request) throws IOException {
        fileService.saveFile("34fb91da-74ef-4754-9312-d01758293c34", request.files());
        return null;
    }

    @GetMapping("/get")
    public Object getFIles() throws IOException {
        JsonArray files = fileService.getFiles("34fb91da-74ef-4754-9312-d01758293c34");
        return new Gson().fromJson(files, Object.class);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is running");
    }
}