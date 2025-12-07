package ragatanga.com.model.dto;

import java.util.List;
import java.util.Map;

public record FileUpdateDTO(String id, Map metadata, String tipoDoc, List<Double> embedding, String resumo, String dataEvento, String descricao, String status) {

}
