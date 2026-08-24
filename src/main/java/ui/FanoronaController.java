package ui;

import core.FanoronaCore;
import core.Peca;
import core.Posicao;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import network.FanoronaServer;
import network.Mensagem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FanoronaController {

    // --- Elementos da Interface Gráfica injetados do FXML ---
    @FXML private GridPane tabuleiroGrid;
    @FXML private TextArea chatArea;
    @FXML private TextField inputChat;

    // --- Lógica e Rede ---
    private FanoronaCore jogo;
    private FanoronaServer servidor; // Poderia ser o FanoronaClient dependendo da tela

    /**
     * O método initialize() é chamado automaticamente pelo JavaFX
     * logo após a tela (FXML) ser carregada e injetada.
     */
    @FXML
    public void initialize() {
        jogo = new FanoronaCore();
        desenharTabuleiroNaTela();
        iniciarRede();
    }

    private void iniciarRede() {
        // Inicializa o servidor e define o que fazer ao receber mensagens
        servidor = new FanoronaServer(5000, textoRecebido -> {
            Mensagem msg = Mensagem.decodificar(textoRecebido);

            // REGRA DE OURO DO JAVAFX:
            // Como a rede roda em uma Thread separada em background, ela NÃO PODE
            // alterar a interface gráfica diretamente. Precisamos usar o Platform.runLater()
            // para jogar a atualização de volta para a Thread principal da UI.
            Platform.runLater(() -> processarMensagemDaRede(msg));
        });

        new Thread(servidor).start();
    }

    private void processarMensagemDaRede(Mensagem msg) {
        switch (msg.comando()) {
            case "MOVE" -> {
                // Aqui você extrairia as coordenadas do msg.payload()
                // Chamaria jogo.tentarMovimento(origem, destino)
                atualizarTabuleiroNaTela();
            }
            case "CHAT" -> {
                chatArea.appendText("Adversário: " + msg.payload() + "\n");
            }
        }
    }

    /**
     * Método acionado pelo botão "Enviar" do chat na interface
     */
    @FXML
    public void enviarChat() {
        String texto = inputChat.getText();
        if (!texto.isBlank()) {
            chatArea.appendText("Você: " + texto + "\n");
            Mensagem msg = new Mensagem("CHAT", texto);
            servidor.enviarMensagem(msg.codificar());
            inputChat.clear();
        }
    }

    private void desenharTabuleiroNaTela() {
        atualizarTabuleiroNaTela();
    }

    private void atualizarTabuleiroNaTela() {
        // Limpa a grade visual para redesenhar o estado atualizado do Core
        tabuleiroGrid.getChildren().clear();

        for (int linha = 0; linha < 5; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                // Cria a "casa" (um contêiner invisível para receber o clique)
                StackPane casa = new StackPane();
                casa.setPrefSize(80, 80); // Aumentamos o tamanho da casa para 80x80

                // 1. Desenhando as linhas (Fundo)
                javafx.scene.shape.Line linhaH = new javafx.scene.shape.Line(0, 0, 80, 0); // Linha Horizontal
                linhaH.setStrokeWidth(2);

                javafx.scene.shape.Line linhaV = new javafx.scene.shape.Line(0, 0, 0, 80); // Linha Vertical
                linhaV.setStrokeWidth(2);

                casa.getChildren().addAll(linhaH, linhaV);

                // Se for um cruzamento forte (soma par), adiciona as diagonais
                if ((linha + coluna) % 2 == 0) {
                    javafx.scene.shape.Line diag1 = new javafx.scene.shape.Line(0, 0, 80, 80);
                    diag1.setStrokeWidth(2);

                    javafx.scene.shape.Line diag2 = new javafx.scene.shape.Line(80, 0, 0, 80);
                    diag2.setStrokeWidth(2);

                    casa.getChildren().addAll(diag1, diag2);
                }

                // 2. Desenhando a Peça (Frente)
                Peca pecaCore = jogo.getPeca(linha, coluna);
                if (pecaCore != Peca.VAZIA) {
                    // Círculo um pouco maior agora que a casa é maior
                    javafx.scene.shape.Circle pecaVisual = new javafx.scene.shape.Circle(28);
                    pecaVisual.setFill(pecaCore == Peca.BRANCA ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.BLACK);

                    pecaVisual.setStroke(javafx.scene.paint.Color.DARKGRAY);
                    pecaVisual.setStrokeWidth(2);

                    casa.getChildren().add(pecaVisual);
                }

                // 3. Capturando o Clique
                int l = linha;
                int c = coluna;
                casa.setOnMouseClicked(event -> processarCliqueNaCasa(l, c));

                // 4. Adiciona a casa ao layout do JavaFX (Coluna, Linha)
                tabuleiroGrid.add(casa, coluna, linha);

            }
        }
    }

    private void processarCliqueNaCasa(int linha, int coluna) {
        System.out.println("Clicou na casa: Linha " + linha + ", Coluna " + coluna);
        // A lógica de selecionar origem e destino vai aqui
    }
}