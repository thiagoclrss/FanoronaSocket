package core;

import java.util.ArrayList;

public class FanoronaCore {
    private final Peca[][] tabuleiro;
    private Peca turnoAtual;
    private final int LINHAS = 5;
    private final int COLUNAS = 9;
    // Variáveis para gerenciar múltiplas capturas no mesmo turno
    private boolean emSequencia = false;
    private Posicao pecaAtiva = null;
    private int ultimoDl = 0;
    private int ultimoDc = 0;
    private final ArrayList<Posicao> caminhoDoTurno = new ArrayList<>();

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
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();

        // 1. Regras restritivas se estiver no meio de uma sequência de capturas
        if (emSequencia) {
            if (!origem.equals(pecaAtiva)) return false; // Deve usar a mesma peça
            if (dl == ultimoDl && dc == ultimoDc) return false; // Deve mudar de direção
            if (caminhoDoTurno.contains(destino)) return false; // Não pode cruzar o próprio caminho
        }

        // 2. Validações básicas de tabuleiro (limites, adjacência, etc.)
        if (!movimentoBasicoValido(origem, destino)) {
            return false;
        }

        // 3. Executa a lógica de captura (já remove as peças inimigas se houver)
        boolean capturou = executarCaptura(origem, destino);

        // 4. Validação final da sequência: movimentos encadeados OBRIGAM uma captura
        if (emSequencia && !capturou) {
            return false; // Movimento inválido, o tabuleiro não é alterado pois a peça não se moveu
        }

        // 5. Efetiva o movimento no tabuleiro
        moverPeca(origem, destino);

        // 6. Atualiza o estado do jogo
        if (capturou) {
            emSequencia = true;
            pecaAtiva = destino;
            ultimoDl = dl;
            ultimoDc = dc;
            caminhoDoTurno.add(origem); // Registra a casa visitada

            // Retorna true, mas NÃO alterna o turno ainda.
            return true;
        } else {
            // Movimento simples (só ocorre se não estava em sequência)
            finalizarTurno();
            return true;
        }
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

        // Tenta primeiro a captura por aproximação
        boolean capturou = capturarPorAproximacao(origem, destino);

        // Se não capturou por aproximação, tenta por afastamento
        if (!capturou) {
            capturou = capturarPorAfastamento(origem, destino);
        }

        return capturou;
    }

    private void moverPeca(Posicao origem, Posicao destino) {
        tabuleiro[destino.linha()][destino.coluna()] = tabuleiro[origem.linha()][origem.coluna()];
        tabuleiro[origem.linha()][origem.coluna()] = Peca.VAZIA;
    }

    private void alternarTurno() {
        turnoAtual = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
    }

    private boolean capturarPorAproximacao(Posicao origem, Posicao destino) {
        //dl = deslocamento na linha, dc = deslocamento na coluna
        // 1. Descobrir o vetor de direção (ex: se andou para a direita, dl = 0, dc = 1)
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();

        Peca adversario = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
        boolean capturouAlgo = false;

        // 2. Olhar para a casa imediatamente à frente do destino, mantendo a direção
        int linhaAlvo = destino.linha() + dl;
        int colunaAlvo = destino.coluna() + dc;

        // 3. Enquanto estiver dentro dos limites do tabuleiro E a casa contiver um inimigo...
        while (linhaAlvo >= 0 && linhaAlvo < LINHAS &&
                colunaAlvo >= 0 && colunaAlvo < COLUNAS &&
                tabuleiro[linhaAlvo][colunaAlvo] == adversario) {

            // Captura a peça inimiga
            tabuleiro[linhaAlvo][colunaAlvo] = Peca.VAZIA;
            capturouAlgo = true;

            // Avança para a próxima casa na mesma direção para continuar a captura em sequência
            linhaAlvo += dl;
            colunaAlvo += dc;
        }

        return capturouAlgo;
    }

    private boolean capturarPorAfastamento(Posicao origem, Posicao destino) {
        // 1. Descobrir o vetor de direção do movimento
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();

        Peca adversario = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
        boolean capturouAlgo = false;

        // 2. Olhar para a casa imediatamente ATRÁS da origem
        // Subtraímos o vetor (dl, dc) em vez de somar
        int linhaAlvo = origem.linha() - dl;
        int colunaAlvo = origem.coluna() - dc;

        // 3. Varrer a linha capturando as peças inimigas ininterruptas
        while (linhaAlvo >= 0 && linhaAlvo < LINHAS &&
                colunaAlvo >= 0 && colunaAlvo < COLUNAS &&
                tabuleiro[linhaAlvo][colunaAlvo] == adversario) {

            // Captura a peça inimiga
            tabuleiro[linhaAlvo][colunaAlvo] = Peca.VAZIA;
            capturouAlgo = true;

            // Continua recuando na mesma direção oposta
            linhaAlvo -= dl;
            colunaAlvo -= dc;
        }

        return capturouAlgo;
    }

    /**
     * Chamado quando o jogador decide encerrar suas capturas em sequência,
     * ou automaticamente após um movimento simples.
     */
    public void finalizarTurno() {
        alternarTurno();
        emSequencia = false;
        pecaAtiva = null;
        caminhoDoTurno.clear();
        ultimoDl = 0;
        ultimoDc = 0;
    }

    public Peca getPeca(int linha, int coluna) {
        return tabuleiro[linha][coluna];
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