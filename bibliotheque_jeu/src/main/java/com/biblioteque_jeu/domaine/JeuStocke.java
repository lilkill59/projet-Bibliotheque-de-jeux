package com.biblioteque_jeu.domaine;

public class JeuStocke {

    private final String id;
    private final int quantite;

    public JeuStocke(String id, int quantite) {
        this.id = id;
        this.quantite = quantite;
    }

    public String getId() {
        return id;
    }

    public int getQuantite() {
        return quantite;
    }
}
