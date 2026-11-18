package br.com.fiap.agrocompartilha.model;

public class Reputacao {

    private final double media;
    private final int total;

    public Reputacao(double media, int total) {
        this.media = media;
        this.total = total;
    }

    public static Reputacao semAvaliacoes() {
        return new Reputacao(0.0, 0);
    }

    public boolean temAvaliacoes() {
        return total > 0;
    }

    public double getMedia() { return media; }
    public int getTotal() { return total; }
}
