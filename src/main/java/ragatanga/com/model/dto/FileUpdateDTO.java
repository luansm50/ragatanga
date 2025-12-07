package ragatanga.com.model.dto;

import java.util.Map;

public record FileUpdateDTO(String id, Map metadata, String tipoDoc, double[] embedding, String resumo, String dataEvento, String status) {

}
