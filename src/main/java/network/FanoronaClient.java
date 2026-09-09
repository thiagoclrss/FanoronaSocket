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

    private final Consumer<String> aoReceberMensagem;

    public FanoronaClient(String host, int porta, Consumer<String> aoReceberMensagem) {
        this.host = host;
        this.porta = porta;
        this.aoReceberMensagem = aoReceberMensagem;
    }

    @Override
    public void run() {
        try {
            logger.info("Tentando conectar ao servidor em " + host + ":" + porta + "...");
            socket = new Socket(host, porta);
            logger.info("Conectado com sucesso ao servidor!");

            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintWriter(socket.getOutputStream(), true);

            String mensagemRecebida;
            while ((mensagemRecebida = entrada.readLine()) != null) {
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