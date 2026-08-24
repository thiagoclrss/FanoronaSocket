package core;

// Representa uma coordenada na matriz (linha x coluna)
public record Posicao(int linha, int coluna) {
    // Método auxiliar para verificar se duas posições são adjacentes
    public boolean isAdjacente(Posicao outra) {
        int difLinha = Math.abs(this.linha - outra.linha);
        int difColuna = Math.abs(this.coluna - outra.coluna);
        return (difLinha <= 1 && difColuna <= 1) && !(difLinha == 0 && difColuna == 0);
    }
}