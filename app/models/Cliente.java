package models;

import java.util.ArrayList; // Importe ArrayList
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable; // Importe JoinTable
import javax.persistence.ManyToMany;

import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
public class Cliente extends Model {
    
    public static final String login = null;
    public String nome;
    public String telefone;
    public String nivel;
    
    public String email;
    public String senha;

    @Enumerated(EnumType.STRING)
    public Perfil perfil;

    //criptografar a senha;
    public void setSenha(String s){
        senha = Crypto.passwordHash(s);
    }

    @Enumerated(EnumType.STRING)
    public Status status;
    
    public Cliente() {
        this.status = Status.ATIVO;
        this.restaurantes = new ArrayList<Restaurante>(); // Boa prática inicializar a lista
    }
    
    // ANTES: @ManyToMany(mappedBy="clientes")
    // DEPOIS:
    @ManyToMany
    @JoinTable(name="cliente_restaurante") // Movemos o JoinTable para cá
    public List<Restaurante> restaurantes;
}
