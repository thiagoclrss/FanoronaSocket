package network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

// O uso de 'record' no Java cria automaticamente construtores, getters e setters.
public record Mensagem(String comando, String payload) {

    // O ObjectMapper é a ferramenta do Jackson que faz a mágica da conversão.
    // Ele é instanciado apenas uma vez (static) para economizar memória.
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(Mensagem.class.getName());

    /**
     * Pega o texto JSON que chegou pelo Socket e transforma de volta no objeto Mensagem.
     */
    public static Mensagem decodificar(String json) {
        try {
            return mapper.readValue(json, Mensagem.class);
        } catch (JsonProcessingException e) {
            logger.log(Level.WARNING, "Falha ao decodificar JSON: " + json, e);
            return new Mensagem("INVALID", json);
        }
    }

    /**
     * Pega os dados deste objeto e transforma em uma String JSON para enviar pelo Socket.
     */
    public String codificar() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Falha ao serializar mensagem", e);
            return "{\"comando\":\"ERROR\", \"payload\":\"Falha na serializacao\"}";
        }
    }
}