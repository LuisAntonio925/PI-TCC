package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Restaurante extends Model {

    @Required
    public String nomeDoRestaurante;

    @Required
    public String CNPJ;

    @Required
    public String categoria;

    public String whatsapp;   
    public String linkPagina;
    
    // ALTERADO: De Blob para Lista de Fotos
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    public List<Foto> fotos;

    @Enumerated(EnumType.STRING)
    public Status status;
    
    @ManyToMany(mappedBy="restaurantes")
    public List<Cliente> clientes;

    public Restaurante() {
        this.status = Status.ATIVO;
        this.clientes = new ArrayList<Cliente>();
        this.fotos = new ArrayList<Foto>();
    }
    
    // Método auxiliar para saber se tem foto (usado no HTML)
    public boolean temFoto() {
        return this.fotos != null && !this.fotos.isEmpty();
    }
}
