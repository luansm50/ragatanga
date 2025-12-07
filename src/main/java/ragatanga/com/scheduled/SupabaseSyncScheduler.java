package ragatanga.com.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ragatanga.com.services.SupabaseGeminiService;

@Service
public class SupabaseSyncScheduler {

    @Autowired
    private SupabaseGeminiService supabaseGeminiService;

    @Scheduled(fixedRate = 5000) // 60.000ms = 1 minuto
    public void processPendingFiles() {
        try {
            supabaseGeminiService.processPendingDocuments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
