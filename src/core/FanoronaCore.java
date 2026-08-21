package core;

public class FanoronaCore {
    private final Peca[][] tabuleiro;
    private Peca turnoAtual;
    private final int LINHAS = 5;
    private final int COLUNAS = 9;

    public FanoronaCore() {
        tabuleiro = new Peca[LINHAS][COLUNAS];
        inicializarTabuleiro();
        turnoAtual = Peca.BRANCA; // Tradicionalmente as brancas começam
    }

    private void inicializarTabuleiro() {
        // Preencher matriz: 2 linhas iniciais para Pretas, 2 finais para Brancas
        // A linha do meio (linha 2) tem peças intercaladas, com o centro vazio.
        // (A implementação detalhada deste loop vai aqui)

        // Preenche as duas primeiras linhas com as peças Pretas
        for (int linha = 0; linha < 2; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                tabuleiro[linha][coluna] = Peca.PRETA;
            }
        }

        // Preenche as duas últimas linhas com as peças Brancas
        for (int linha = 3; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                tabuleiro[linha][coluna] = Peca.BRANCA;
            }
        }

        // Define a configuração intercalada da linha central (linha 2)
        Peca[] linhaCentral = {
                Peca.BRANCA, Peca.PRETA, Peca.BRANCA, Peca.PRETA,
                Peca.VAZIA,  // Centro exato do tabuleiro
                Peca.PRETA, Peca.BRANCA, Peca.PRETA, Peca.BRANCA
        };

        for (int coluna = 0; coluna < COLUNAS; coluna++) {
            tabuleiro[2][coluna] = linhaCentral[coluna];
        }
    }

    /**
     * Tenta executar um movimento e retorna se foi bem sucedido.
     */
    public boolean tentarMovimento(Posicao origem, Posicao destino) {
        if (!movimentoBasicoValido(origem, destino)) {
            return false;
        }

        boolean capturou = executarCaptura(origem, destino);

        // No Fanorona, se você não captura nada, o movimento é simples e o turno acaba.
        // Se capturou, precisaremos gerenciar a possibilidade de múltiplas capturas (sequência).

        moverPeca(origem, destino);

        // Lógica simplificada de troca de turno (precisará de ajuste para múltiplas capturas)
        if (!capturou) {
            alternarTurno();
        }

        return true;
    }

    private boolean movimentoBasicoValido(Posicao origem, Posicao destino) {
        // 1. Validar se as posições estão dentro dos limites da matriz (0 a 4, e 0 a 8)
        // 2. Validar se a peça na origem pertence ao jogador do turno atual
        // 3. Validar se o destino está VAZIO
        // 4. Validar se o destino é adjacente à origem
        // 5. IMPORTANTE: Validar se o movimento diagonal é permitido naquele cruzamento específico

        // 1. Validar se as posições estão dentro dos limites do tabuleiro
        if (origem.linha() < 0 || origem.linha() >= LINHAS || origem.coluna() < 0 || origem.coluna() >= COLUNAS ||
                destino.linha() < 0 || destino.linha() >= LINHAS || destino.coluna() < 0 || destino.coluna() >= COLUNAS) {
            return false; // Fora do tabuleiro
        }

        // 2. Validar se a peça na origem pertence ao jogador atual
        if (tabuleiro[origem.linha()][origem.coluna()] != turnoAtual) {
            return false; // Tentando mover peça do adversário ou casa vazia
        }

        // 3. Validar se a casa de destino está vazia
        if (tabuleiro[destino.linha()][destino.coluna()] != Peca.VAZIA) {
            return false; // Destino ocupado
        }

        // 4. Validar se o destino é adjacente (distância máxima de 1 casa)
        int difLinha = Math.abs(origem.linha() - destino.linha());
        int difColuna = Math.abs(origem.coluna() - destino.coluna());

        if (difLinha > 1 || difColuna > 1 || (difLinha == 0 && difColuna == 0)) {
            return false; // Destino muito longe ou é a mesma casa da origem
        }

        // 5. Validar a regra das diagonais (cruzamentos fortes e fracos)
        boolean isMovimentoDiagonal = (difLinha == 1 && difColuna == 1);

        if (isMovimentoDiagonal) {
            // Se tentou mover na diagonal, a origem PRECISA ser um cruzamento forte (soma par)
            boolean isCruzamentoForte = (origem.linha() + origem.coluna()) % 2 == 0;
            if (!isCruzamentoForte) {
                return false; // Tentou andar na diagonal a partir de um ponto sem linha diagonal
            }
        }

        // Se passou por todas as barreiras, o movimento básico é válido!
        return true;
    }

    private boolean executarCaptura(Posicao origem, Posicao destino) {
        // Aqui entra a regra de ouro do Fanorona:
        // Calcular o vetor de direção (ex: linha + 1, coluna 0)
        // Verificar Captura por APROXIMAÇÃO: olhar a próxima casa na mesma direção após o destino
        // Verificar Captura por AFASTAMENTO: olhar a casa anterior na direção oposta à origem

        // Retorna true se removeu alguma peça adversária do tabuleiro
        return false; // placeholder
    }

    private void moverPeca(Posicao origem, Posicao destino) {
        tabuleiro[destino.linha()][destino.coluna()] = tabuleiro[origem.linha()][origem.coluna()];
        tabuleiro[origem.linha()][origem.coluna()] = Peca.VAZIA;
    }

    private void alternarTurno() {
        turnoAtual = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
    }

    //metodo para testar funcionamento da logica enquanto não temos UI
    public void imprimirTabuleiro() {
        System.out.println("  0 1 2 3 4 5 6 7 8 (Colunas)");
        for (int linha = 0; linha < LINHAS; linha++) {
            System.out.print(linha + " ");
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                switch (tabuleiro[linha][coluna]) {
                    case PRETA -> System.out.print("P ");
                    case BRANCA -> System.out.print("B ");
                    case VAZIA -> System.out.print("- ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}