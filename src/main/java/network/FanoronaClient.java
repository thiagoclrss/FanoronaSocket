package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FanoronaClient implements Runnable {
    private final String host;
    private final int porta;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;
    private static final Logger logger = Logger.getLogger(FanoronaClient.class.getName());

    // Callback para enviar mensagens recebidas de volta para o Core/UI
    private final Consumer<String> aoReceberMensagem;

    public FanoronaClient(String host, int porta, Consumer<String> aoReceberMensagem) {
        this.host = host;
        this.porta = porta;
        this.aoReceberMensagem = aoReceberMensagem;
    }

    @Override
    public void run() {
        try {
            // 1. Tenta conectar ao IP (host) e porta do servidor
            logger.info("Tentando conectar ao servidor em " + host + ":" + porta + "...");
            socket = new Socket(host, porta);
            logger.info("Conectado com sucesso ao servidor!");

            // 2. Prepara os canais de leitura e escrita
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintWriter(socket.getOutputStream(), true);

            // 3. Loop infinito escutando as mensagens do servidor
            String mensagemRecebida;
            while ((mensagemRecebida = entrada.readLine()) != null) {
                // Quando recebe algo pela rede, dispara o evento
                if (aoReceberMensagem != null) {
                    aoReceberMensagem.accept(mensagemRecebida);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro na comunicação do cliente: " + e.getMessage(), e);
        } finally {
            fecharConexoes();
        }
    }

    /**
     * Método chamado para enviar uma jogada ou chat para o servidor.
     */
    public void enviarMensagem(String mensagem) {
        if (saida != null) {
            saida.println(mensagem);
        }
    }

    public void fecharConexoes() {
        try {
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao fechar conexões: " + e.getMessage(), e);
        }
    }
}