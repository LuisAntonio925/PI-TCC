package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import play.db.jpa.Model;

@Entity
public class Foto extends Model {
    
    public String nome;
    
    @ManyToOne
    public Restaurante restaurante;
    
    public Foto(String nome) {
        this.nome = nome;
    }
}
