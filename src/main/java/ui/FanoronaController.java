package ui;

import core.FanoronaCore;
import core.Peca;
import core.Posicao;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import network.FanoronaClient;
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
    @FXML private Label statusTurno;


    // --- Lógica e Rede ---
    private FanoronaCore jogo;
    private FanoronaServer servidor = null;
    private FanoronaClient cliente = null;
    private Posicao pecaSelecionada = null;
    private boolean jogoFinalizado = false;
    private Peca minhaCor;

    /**
     * O método initialize() é chamado automaticamente pelo JavaFX
     * logo após a tela (FXML) ser carregada e injetada.
     */
    @FXML
    public void initialize() {
        jogo = new FanoronaCore();
        desenharTabuleiroNaTela();
    }

    public void iniciarConexao(boolean isServidor, String ipServidor) {
        if (isServidor) {
            minhaCor = Peca.BRANCA;
            servidor = new FanoronaServer(5000, textoRecebido -> {
                Mensagem msg = Mensagem.decodificar(textoRecebido);
                Platform.runLater(() -> processarMensagemDaRede(msg));
            });
            new Thread(servidor).start();
        } else {
            minhaCor = Peca.PRETA;
            cliente = new FanoronaClient(ipServidor, 5000, textoRecebido -> {
                Mensagem msg = Mensagem.decodificar(textoRecebido);
                Platform.runLater(() -> processarMensagemDaRede(msg));
            });
            new Thread(cliente).start();
        }
        atualizarStatusTurno();
    }

    private void enviarPacoteRede(String pacoteJson) {
        if (servidor != null) servidor.enviarMensagem(pacoteJson);
        if (cliente != null) cliente.enviarMensagem(pacoteJson);
    }

    private void processarMensagemDaRede(Mensagem msg) {
        switch (msg.comando()) {
            case "MOVE" -> {
                String[] coords = msg.payload().split(",");
                Posicao origem = new Posicao(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                Posicao destino = new Posicao(Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));

                jogo.tentarMovimento(origem, destino);
                atualizarTabuleiroNaTela();

                Peca vencedor = jogo.verificarVencedor();
                if (vencedor != null) {
                    anunciarVencedor(vencedor);
                }
                atualizarStatusTurno();
            }
            case "PASS" -> {
                jogo.finalizarTurno();
                atualizarTabuleiroNaTela();
                atualizarStatusTurno();
            }
            case "CHAT" -> {
                chatArea.appendText("Adversário: " + msg.payload() + "\n");
            }
            case "FORFEIT"  -> {
                anunciarDesistencia(minhaCor, false);
            }
        }
    }

    @FXML
    public void enviarChat() {
        String texto = inputChat.getText();
        if (!texto.isBlank()) {
            chatArea.appendText("Você: " + texto + "\n");
            Mensagem msg = new Mensagem("CHAT", texto);
            enviarPacoteRede(msg.codificar());
            inputChat.clear();
        }
    }

    @FXML
    public void desistirPartida() {
        if (jogoFinalizado) return;

        enviarPacoteRede(new Mensagem("FORFEIT", "").codificar());

        Peca vencedor = (minhaCor == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
        anunciarDesistencia(vencedor, true);
    }

    @FXML
    public void passarTurno() {
        if (jogoFinalizado) return;
        if (jogo.getTurnoAtual() != minhaCor) {
            System.out.println("Ação bloqueada: Não é o seu turno.");
            return;
        }
        jogo.finalizarTurno();
        pecaSelecionada = null;
        atualizarTabuleiroNaTela();
        System.out.println("Turno encerrado. Agora jogam as: " + jogo.getTurnoAtual());
        enviarPacoteRede(new Mensagem("PASS", "").codificar());
        atualizarStatusTurno();
    }

    private void atualizarStatusTurno() {
        if (jogoFinalizado) {
            statusTurno.setText("Partida Encerrada");
            statusTurno.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
            return;
        }

        if (jogo.getTurnoAtual() == minhaCor) {
            statusTurno.setText("É a sua vez!");
            statusTurno.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        } else {
            statusTurno.setText("Turno do adversário...");
            statusTurno.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        }
    }

    private void desenharTabuleiroNaTela() {
        atualizarTabuleiroNaTela();
    }

    private void atualizarTabuleiroNaTela() {
        tabuleiroGrid.getChildren().clear();

        for (int linha = 0; linha < 5; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                StackPane casa = new StackPane();
                casa.setPrefSize(80, 80);

                javafx.scene.shape.Line linhaH = new javafx.scene.shape.Line(0, 0, 80, 0);
                linhaH.setStrokeWidth(2);

                javafx.scene.shape.Line linhaV = new javafx.scene.shape.Line(0, 0, 0, 80);
                linhaV.setStrokeWidth(2);

                casa.getChildren().addAll(linhaH, linhaV);

                if ((linha + coluna) % 2 == 0) {
                    javafx.scene.shape.Line diag1 = new javafx.scene.shape.Line(0, 0, 80, 80);
                    diag1.setStrokeWidth(2);

                    javafx.scene.shape.Line diag2 = new javafx.scene.shape.Line(80, 0, 0, 80);
                    diag2.setStrokeWidth(2);

                    casa.getChildren().addAll(diag1, diag2);
                }

                Peca pecaCore = jogo.getPeca(linha, coluna);
                if (pecaCore != Peca.VAZIA) {
                    javafx.scene.shape.Circle pecaVisual = new javafx.scene.shape.Circle(28);
                    pecaVisual.setFill(pecaCore == Peca.BRANCA ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.BLACK);
                    pecaVisual.setStroke(javafx.scene.paint.Color.DARKGRAY);
                    pecaVisual.setStrokeWidth(2);

                    if (pecaSelecionada != null && pecaSelecionada.linha() == linha && pecaSelecionada.coluna() == coluna) {
                        javafx.scene.effect.DropShadow brilho = new javafx.scene.effect.DropShadow();
                        brilho.setColor(javafx.scene.paint.Color.CYAN);
                        brilho.setRadius(20);
                        brilho.setSpread(0.5);
                        pecaVisual.setEffect(brilho);
                    }

                    casa.getChildren().add(pecaVisual);
                } else if (pecaSelecionada != null) {
                    Posicao destinoTeste = new Posicao(linha, coluna);
                    if (jogo.isJogadaValida(pecaSelecionada, destinoTeste)) {
                        Circle marcadorDestino = new Circle(10);
                        marcadorDestino.setFill(Color.PURPLE.brighter());
                        marcadorDestino.setOpacity(0.7);
                        casa.getChildren().add(marcadorDestino);
                    }
                }


                int l = linha;
                int c = coluna;
                casa.setOnMouseClicked(event -> processarCliqueNaCasa(l, c));

                tabuleiroGrid.add(casa, coluna, linha);

            }
        }
    }

    private void processarCliqueNaCasa(int linha, int coluna) {
        if (jogoFinalizado) return;
        Posicao clicada = new Posicao(linha, coluna);

        if (pecaSelecionada == null) {
            Peca pecaClicada = jogo.getPeca(linha, coluna);
            if (pecaClicada != Peca.VAZIA &&
                pecaClicada == jogo.getTurnoAtual() &&
                pecaClicada == minhaCor) {
                pecaSelecionada = clicada;
            }
        } else {
            if (pecaSelecionada.equals(clicada)) {
                pecaSelecionada = null;
            } else {
                boolean sucesso = jogo.tentarMovimento(pecaSelecionada, clicada);

                if (sucesso) {
                    System.out.println("Movimento executado!");
                    String payload = pecaSelecionada.linha() + "," + pecaSelecionada.coluna() + "," +
                            clicada.linha() + "," + clicada.coluna();

                    enviarPacoteRede(new Mensagem("MOVE", payload).codificar());

                    Peca vencedor = jogo.verificarVencedor();
                    if (vencedor != null) {
                        anunciarVencedor(vencedor);
                    }
                } else {
                    System.out.println("Movimento inválido.");
                }

                pecaSelecionada = null;
            }
        }
        atualizarTabuleiroNaTela();
        atualizarStatusTurno();
    }

    private void anunciarVencedor(Peca vencedor) {
        jogoFinalizado = true;

        String nomeVencedor = (vencedor == Peca.BRANCA) ? "Brancas" : "Pretas";

        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alerta.setTitle("Fim de Jogo!");
        alerta.setHeaderText("Temos um vencedor!");
        alerta.setContentText("A equipe das peças " + nomeVencedor + " capturou todas as peças adversárias e venceu a partida!");

        alerta.showAndWait();
    }

    private void anunciarDesistencia(Peca vencedor, boolean fuiEuQuemDesistiu) {
        jogoFinalizado = true;

        statusTurno.setText("Partida Encerrada");
        statusTurno.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        String mensagem = fuiEuQuemDesistiu ?
                "Você desistiu da partida. O adversário venceu!" :
                "O adversário abandonou o jogo. Você venceu por desistência!";

        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alerta.setTitle("Fim de Jogo!");
        alerta.setHeaderText(fuiEuQuemDesistiu ? "Derrota" : "Vitória!");
        alerta.setContentText(mensagem);

        alerta.showAndWait();
    }
}