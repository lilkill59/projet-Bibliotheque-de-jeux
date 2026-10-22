package com.biblioteque_jeu;

public class Main {
    public static void main(String[] args) {
        StockageJeu stockage = new StockageJeu();

        Jeu jeu1 = new Jeu("The Legend of Zelda: Breath of the Wild", "Action-aventure");
        Jeu jeu2 = new Jeu("Super Mario Odyssey", "Plateforme");
        Jeu jeu3 = new Jeu("Minecraft", "Sandbox");

        stockage.ajouterJeu(jeu1);
        stockage.ajouterJeu(jeu2);
        stockage.ajouterJeu(jeu3);

        System.out.println("Taille du stockage : " + stockage.getTaille());

        for (int i = 0; i < stockage.getTaille(); i++) {
            Jeu jeu = stockage.getJeu(i);
            System.out.println("Nom du jeu : " + jeu.getNom() + ", Type : " + jeu.getType());
        }

        stockage.supprimerJeu(1); // Supprime le deuxième jeu (Super Mario Odyssey)

        System.out.println("\nAprès suppression d'un jeu :");
        System.out.println("Taille du stockage : " + stockage.getTaille());

        for (int i = 0; i < stockage.getTaille(); i++) {
            Jeu jeu = stockage.getJeu(i);
            System.out.println("Nom du jeu : " + jeu.getNom() + ", Type : " + jeu.getType());
        }
    }
}