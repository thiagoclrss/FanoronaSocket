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
        javafx.scene.control.Alert alertaFundo = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alertaFundo.setTitle("Fanorona");
        alertaFundo.setHeaderText("Bem-vindo ao Fanorona Multijogador!");
        alertaFundo.setContentText("Escolha o seu papel nesta partida:");

        javafx.scene.control.ButtonType btnCriar = new javafx.scene.control.ButtonType("Criar Partida (Servidor)");
        javafx.scene.control.ButtonType btnEntrar = new javafx.scene.control.ButtonType("Entrar (Cliente)");
        alertaFundo.getButtonTypes().setAll(btnCriar, btnEntrar);

        java.util.Optional<javafx.scene.control.ButtonType> resultado = alertaFundo.showAndWait();

        if (resultado.isEmpty()) {
            System.exit(0);
        }

        boolean isServidor = (resultado.get() == btnCriar);
        String ip = "127.0.0.1";

        if (!isServidor) {
            javafx.scene.control.TextInputDialog dialogIp = new javafx.scene.control.TextInputDialog("127.0.0.1");
            dialogIp.setTitle("Conectar à Partida");
            dialogIp.setHeaderText("Partida Local ou Remota?");
            dialogIp.setContentText("Digite o IP do servidor (deixe 127.0.0.1 para mesma máquina):");

            java.util.Optional<String> resIp = dialogIp.showAndWait();
            if (resIp.isPresent()) {
                ip = resIp.get();
            } else {
                System.exit(0);
            }
        }

        URL caminhoFxml = getClass().getResource("/tela.fxml");
        FXMLLoader loader = new FXMLLoader(caminhoFxml);
        Parent raiz = loader.load();

        FanoronaController controlador = loader.getController();
        controlador.iniciarConexao(isServidor, ip);

        Scene cena = new Scene(raiz, 1220, 800);
        palcoPrincipal.setTitle(isServidor ? "Fanorona - Servidor (Brancas)" : "Fanorona - Cliente (Pretas)");
        palcoPrincipal.setScene(cena);
        palcoPrincipal.setResizable(false);
        palcoPrincipal.show();
    }

    @Override
    public void stop() {
        System.out.println("Encerrando a interface gráfica...");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}