package models;

import java.util.ArrayList; 
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import net.sf.oval.constraint.MinSize;

import javax.persistence.Enumerated;
import javax.persistence.EnumType;

import play.data.validation.Min;
import play.data.validation.Required; // Import está aqui
import play.db.jpa.Blob;
import play.db.jpa.Model;

@Entity
public class Restaurante extends Model {
    
    @Required
    public String nomeDoRestaurante;


    @Required 
    public String CNPJ;

    @Required
    public String categoria;
    
    public Blob imagem;
    
    @Enumerated(EnumType.STRING)
    public Status status;
    
    public Restaurante() {
        this.status = Status.ATIVO;
        this.clientes = new ArrayList<Cliente>(); 
    }
    
    @ManyToMany(mappedBy="restaurantes") 
    public List<Cliente> clientes;
}