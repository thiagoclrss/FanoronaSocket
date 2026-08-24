import network.FanoronaClient;
import network.FanoronaServer;
import network.Mensagem;

public class Main {

    public static void main(String[] args) {
        int porta = 5000;

        System.out.println("=== INICIANDO TESTE DE REDE E JSON ===\n");

        // 1. Configurando e iniciando o Servidor
        FanoronaServer servidor = new FanoronaServer(porta, textoRecebido -> {
            System.out.println("[Log Servidor] JSON Bruto recebido da rede: " + textoRecebido);

            // O Jackson faz a mágica aqui: transforma a String de volta em Objeto
            Mensagem msg = Mensagem.decodificar(textoRecebido);
            System.out.println("[Log Servidor] Objeto remontado -> Comando: " + msg.comando() + " | Payload: " + msg.payload() + "\n");
        });

        // Roda o servidor em background para não travar a aplicação
        new Thread(servidor).start();

        // Pausa de 500ms para dar tempo do servidor abrir a porta antes do cliente tentar conectar
        esperar(500);

        // 2. Configurando e iniciando o Cliente
        FanoronaClient cliente = new FanoronaClient("127.0.0.1", porta, textoRecebido -> {
            System.out.println("[Log Cliente] JSON Bruto recebido da rede: " + textoRecebido);

            Mensagem msg = Mensagem.decodificar(textoRecebido);
            System.out.println("[Log Cliente] Objeto remontado -> Comando: " + msg.comando() + " | Payload: " + msg.payload() + "\n");
        });

        // Roda o cliente em background
        new Thread(cliente).start();

        // Pausa para garantir que a conexão foi estabelecida
        esperar(500);

        // 3. Simulando a troca de mensagens (O que a Interface Gráfica faria nos bastidores)
        System.out.println("--- ENVIANDO MENSAGENS ---\n");

        // A) Cliente digita algo no chat e envia
        Mensagem chatDoCliente = new Mensagem("CHAT", "Olá, servidor! Preparado para perder?");
        cliente.enviarMensagem(chatDoCliente.codificar());
        esperar(500);

        // B) Servidor responde no chat
        Mensagem chatDoServidor = new Mensagem("CHAT", "Pode vir com tudo!");
        servidor.enviarMensagem(chatDoServidor.codificar());
        esperar(500);

        // C) Cliente faz o primeiro movimento
        Mensagem jogadaDoCliente = new Mensagem("MOVE", "2,3,3,3");
        cliente.enviarMensagem(jogadaDoCliente.codificar());
        esperar(500);

        // D) Cliente desiste da partida
        Mensagem desistencia = new Mensagem("RESIGN", "");
        cliente.enviarMensagem(desistencia.codificar());
        esperar(500);

        System.out.println("=== TESTE CONCLUÍDO ===");

        // Finalizando as conexões para o programa poder encerrar
        cliente.fecharConexoes();
        servidor.fecharConexoes();
        System.exit(0);
    }

    // Método auxiliar apenas para não poluir o código com try/catch toda hora
    private static void esperar(int milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}