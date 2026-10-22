public class Jeu {

    private String id;
    private String nom;
    private String type;


    public String getId() {
        return id;
    }
    public String getNom() {
        return nom;
    }
    public String getType() {
        return type;
    }
    Jeu(String id, String nom, String type) {
        this.id = id;
        this.nom = nom;
        this.type = type;
    }
}