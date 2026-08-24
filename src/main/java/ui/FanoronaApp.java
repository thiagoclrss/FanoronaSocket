package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class FanoronaApp extends Application {

    @Override
    public void start(Stage palcoPrincipal) throws Exception {
        // 1. Localiza o arquivo visual FXML
        URL caminhoFxml = getClass().getResource("/tela.fxml");

        if (caminhoFxml == null) {
            System.err.println("Erro crítico: O arquivo /tela.fxml não foi encontrado.");
            System.exit(1);
        }

        // 2. O FXMLLoader lê o arquivo e instancia o Controlador automaticamente
        FXMLLoader loader = new FXMLLoader(caminhoFxml);
        Parent raiz = loader.load();

        // 3. Configura a janela (resolução de 800x600 como base inicial)
        Scene cena = new Scene(raiz, 1024, 768);

        palcoPrincipal.setTitle("Fanorona - Trabalho de Sockets");
        palcoPrincipal.setScene(cena);
        palcoPrincipal.setResizable(false); // Trava o redimensionamento para não distorcer o tabuleiro
        palcoPrincipal.show();
    }

    /**
     * O método stop() é disparado automaticamente quando o usuário clica
     * no "X" para fechar a janela. É vital para matar as Threads do servidor/cliente.
     */
    @Override
    public void stop() {
        System.out.println("Encerrando a interface gráfica...");
        // Força o encerramento da JVM, matando qualquer Thread de Socket que ficou pendente
        System.exit(0);
    }

    public static void main(String[] args) {
        // Dispara o ciclo de vida interno do JavaFX, que eventualmente chama o start()
        launch(args);
    }
}