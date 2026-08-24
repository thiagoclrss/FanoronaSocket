package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FanoronaServer implements Runnable {
    private final int porta;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader entrada;
    private PrintWriter saida;
    private static final Logger logger = Logger.getLogger(FanoronaServer.class.getName());

    // Callback para enviar mensagens recebidas de volta para o Core/UI
    private final Consumer<String> aoReceberMensagem;

    public FanoronaServer(int porta, Consumer<String> aoReceberMensagem) {
        this.porta = porta;
        this.aoReceberMensagem = aoReceberMensagem;
    }

    @Override
    public void run() {
        try {
            // 1. Abre a porta e aguarda a conexão do outro jogador
            serverSocket = new ServerSocket(porta);
            logger.log(Level.INFO, "Servidor aguardando conexão na porta " + porta + "...");

            // A thread fica bloqueada aqui até o cliente conectar
            clientSocket = serverSocket.accept();
            logger.log(Level.INFO, "Adversário conectado: " + clientSocket.getInetAddress().getHostAddress());

            // 2. Prepara os canais de leitura e escrita
            entrada = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            saida = new PrintWriter(clientSocket.getOutputStream(), true);

            // 3. Loop infinito escutando as mensagens do adversário
            String mensagemRecebida;
            while ((mensagemRecebida = entrada.readLine()) != null) {
                // Quando recebe algo pela rede, dispara o evento para a interface
                if (aoReceberMensagem != null) {
                    aoReceberMensagem.accept(mensagemRecebida);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro na comunicação do servidor: " + e.getMessage(), e);
        } finally {
            fecharConexoes();
        }
    }

    /**
     * Método chamado pela nossa interface/core para enviar uma jogada ou chat para o adversário.
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
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao fechar conexões: " + e.getMessage(), e);
        }
    }
}
