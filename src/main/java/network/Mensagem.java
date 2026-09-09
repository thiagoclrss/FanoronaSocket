package network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;


public record Mensagem(String comando, String payload) {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(Mensagem.class.getName());

    public static Mensagem decodificar(String json) {
        try {
            return mapper.readValue(json, Mensagem.class);
        } catch (JsonProcessingException e) {
            logger.log(Level.WARNING, "Falha ao decodificar JSON: " + json, e);
            return new Mensagem("INVALID", json);
        }
    }

    public String codificar() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Falha ao serializar mensagem", e);
            return "{\"comando\":\"ERROR\", \"payload\":\"Falha na serializacao\"}";
        }
    }
}