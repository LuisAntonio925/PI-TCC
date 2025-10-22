package models;

import java.util.ArrayList; // Importe ArrayList
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;

import play.db.jpa.Blob;
import play.db.jpa.Model;

@Entity
public class Restaurante extends Model {
    
    public String nomeDoRestaurante;
    public String CNPJ;
    public String categoria;
    
    public Blob imagem;
    
    @Enumerated(EnumType.STRING)
    public Status status;
    
    public Restaurante() {
        this.status = Status.ATIVO;
        this.clientes = new ArrayList<Cliente>(); // Boa prática inicializar a lista
    }
    
    // ANTES: @ManyToMany @JoinTable(name="cliente_restaurante")
    // DEPOIS:
    @ManyToMany(mappedBy="restaurantes") // Agora usa mappedBy
    public List<Cliente> clientes;


}
