package com.biblioteque_jeu;

public class Jeu {

    private String nom;
    private String type;

    public Jeu(String nom, String type) {

        this.nom = nom;
        this.type = type;
    }

    public String getNom() {
        return nom;
    }

    public String getType() {
        return type;
    }
}
