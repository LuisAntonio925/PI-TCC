package models;

import java.util.ArrayList; // Importe ArrayList
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable; // Importe JoinTable
import javax.persistence.ManyToMany;

import net.sf.oval.constraint.MinSize;
import play.data.validation.Email;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
public class Cliente extends Model {

    
    @Required
    @MinSize(3)
    public String nome;
     
    @Min(8)
    @Max(15)
    public String telefone;

    @Required
    @Email
    public String email;

    @Required
    @Min(8)
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
