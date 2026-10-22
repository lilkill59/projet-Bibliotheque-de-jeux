package com.biblioteque_jeu;


public class Main {
    public static void main(String[] args) {
        Jeu jeu = new Jeu("J1", "Minecraft", "Sandbox");
        System.out.println("Jeu: " + jeu.getNom() + " (" + jeu.getType() + ")");
    }
}