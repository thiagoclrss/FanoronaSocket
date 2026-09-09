package core;

import java.util.ArrayList;

public class FanoronaCore {
    private final Peca[][] tabuleiro;
    private Peca turnoAtual;
    private final int LINHAS = 5;
    private final int COLUNAS = 9;
    private boolean emSequencia = false;
    private Posicao pecaAtiva = null;
    private int ultimoDl = 0;
    private int ultimoDc = 0;
    private final ArrayList<Posicao> caminhoDoTurno = new ArrayList<>();

    public FanoronaCore() {
        tabuleiro = new Peca[LINHAS][COLUNAS];
        inicializarTabuleiro();
        turnoAtual = Peca.BRANCA;
    }

    private void inicializarTabuleiro() {

        for (int linha = 0; linha < 2; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                tabuleiro[linha][coluna] = Peca.PRETA;
            }
        }

        for (int linha = 3; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                tabuleiro[linha][coluna] = Peca.BRANCA;
            }
        }

        Peca[] linhaCentral = {
                Peca.BRANCA, Peca.PRETA, Peca.BRANCA, Peca.PRETA,
                Peca.VAZIA,
                Peca.PRETA, Peca.BRANCA, Peca.PRETA, Peca.BRANCA
        };

        for (int coluna = 0; coluna < COLUNAS; coluna++) {
            tabuleiro[2][coluna] = linhaCentral[coluna];
        }
    }

    public boolean tentarMovimento(Posicao origem, Posicao destino) {
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();

        if (emSequencia) {
            if (!origem.equals(pecaAtiva)) return false;
            if (dl == ultimoDl && dc == ultimoDc) return false;
            if (caminhoDoTurno.contains(destino)) return false;
        }

        if (!movimentoBasicoValido(origem, destino)) {
            return false;
        }

        boolean capturou = executarCaptura(origem, destino);


        if (emSequencia && !capturou) {
            return false;
        }

        moverPeca(origem, destino);

        if (capturou) {
            emSequencia = true;
            pecaAtiva = destino;
            ultimoDl = dl;
            ultimoDc = dc;
            caminhoDoTurno.add(origem);

            return true;
        } else {
            finalizarTurno();
            return true;
        }
    }

    private boolean movimentoBasicoValido(Posicao origem, Posicao destino) {

        if (origem.linha() < 0 || origem.linha() >= LINHAS || origem.coluna() < 0 || origem.coluna() >= COLUNAS ||
                destino.linha() < 0 || destino.linha() >= LINHAS || destino.coluna() < 0 || destino.coluna() >= COLUNAS) {
            return false;
        }

        if (tabuleiro[origem.linha()][origem.coluna()] != turnoAtual) {
            return false;
        }

        if (tabuleiro[destino.linha()][destino.coluna()] != Peca.VAZIA) {
            return false;
        }

        int difLinha = Math.abs(origem.linha() - destino.linha());
        int difColuna = Math.abs(origem.coluna() - destino.coluna());

        if (difLinha > 1 || difColuna > 1 || (difLinha == 0 && difColuna == 0)) {
            return false;
        }

        boolean isMovimentoDiagonal = (difLinha == 1 && difColuna == 1);

        if (isMovimentoDiagonal) {
            boolean isCruzamentoForte = (origem.linha() + origem.coluna()) % 2 == 0;
            if (!isCruzamentoForte) {
                return false;
            }
        }

        return true;
    }

    private boolean executarCaptura(Posicao origem, Posicao destino) {
        boolean capturou = capturarPorAproximacao(origem, destino);

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
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();

        Peca adversario = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
        boolean capturouAlgo = false;

        int linhaAlvo = destino.linha() + dl;
        int colunaAlvo = destino.coluna() + dc;

        while (linhaAlvo >= 0 && linhaAlvo < LINHAS &&
                colunaAlvo >= 0 && colunaAlvo < COLUNAS &&
                tabuleiro[linhaAlvo][colunaAlvo] == adversario) {

            tabuleiro[linhaAlvo][colunaAlvo] = Peca.VAZIA;
            capturouAlgo = true;

            linhaAlvo += dl;
            colunaAlvo += dc;
        }

        return capturouAlgo;
    }

    private boolean capturarPorAfastamento(Posicao origem, Posicao destino) {
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();

        Peca adversario = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;
        boolean capturouAlgo = false;
        int linhaAlvo = origem.linha() - dl;
        int colunaAlvo = origem.coluna() - dc;

        while (linhaAlvo >= 0 && linhaAlvo < LINHAS &&
                colunaAlvo >= 0 && colunaAlvo < COLUNAS &&
                tabuleiro[linhaAlvo][colunaAlvo] == adversario) {


            tabuleiro[linhaAlvo][colunaAlvo] = Peca.VAZIA;
            capturouAlgo = true;

            linhaAlvo -= dl;
            colunaAlvo -= dc;
        }

        return capturouAlgo;
    }

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

    public boolean isJogadaValida(Posicao origem, Posicao destino) {
        if (!movimentoBasicoValido(origem, destino)) {
            return false;
        }

        if (emSequencia) {
            int dl = destino.linha() - origem.linha();
            int dc = destino.coluna() - origem.coluna();

            if (!origem.equals(pecaAtiva)) return false;
            if (dl == ultimoDl && dc == ultimoDc) return false;
            if (caminhoDoTurno.contains(destino)) return false;

            if (!isCapturaPossivel(origem, destino)) return false;
        }

        return true;
    }

    public Peca getTurnoAtual() {
        return turnoAtual;
    }

    private boolean isCapturaPossivel(Posicao origem, Posicao destino) {
        int dl = destino.linha() - origem.linha();
        int dc = destino.coluna() - origem.coluna();
        Peca adversario = (turnoAtual == Peca.BRANCA) ? Peca.PRETA : Peca.BRANCA;

        int linhaAprox = destino.linha() + dl;
        int colunaAprox = destino.coluna() + dc;
        if (linhaAprox >= 0 && linhaAprox < LINHAS && colunaAprox >= 0 && colunaAprox < COLUNAS) {
            if (tabuleiro[linhaAprox][colunaAprox] == adversario) {
                return true;
            }
        }

        int linhaAfast = origem.linha() - dl;
        int colunaAfast = origem.coluna() - dc;
        if (linhaAfast >= 0 && linhaAfast < LINHAS && colunaAfast >= 0 && colunaAfast < COLUNAS) {
            if (tabuleiro[linhaAfast][colunaAfast] == adversario) {
                return true;
            }
        }

        return false;
    }

    public Peca verificarVencedor() {
        int contagemBrancas = 0;
        int contagemPretas = 0;

        for (int linha = 0; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                if (tabuleiro[linha][coluna] == Peca.BRANCA) contagemBrancas++;
                else if (tabuleiro[linha][coluna] == Peca.PRETA) contagemPretas++;
            }
        }

        if (contagemBrancas == 0) return Peca.PRETA;
        if (contagemPretas == 0) return Peca.BRANCA;

        return null;
    }
}