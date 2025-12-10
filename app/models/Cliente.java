package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import net.sf.oval.constraint.MinSize; // Mantido para o campo 'nome'
import play.data.validation.Email;
import play.data.validation.Required; // Import está aqui
import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
public class Cliente extends Model {

    @Required
    public String nome;

    @Required
    public String telefone;

    @Required
    @Email
    public String email;

    public String senha; // A validação disto é manual no controller

    // NOVO: nome do arquivo da foto
    public String nomeFoto;

    @Enumerated(EnumType.STRING)
    public Perfil perfil;

    public void setSenha(String s){
        senha = Crypto.passwordHash(s);
    }

    @Enumerated(EnumType.STRING)
    public Status status;

    public Cliente() {
        this.status = Status.ATIVO;
        this.restaurantes = new ArrayList<Restaurante>();
    }

    @ManyToMany
    @JoinTable(name="cliente_restaurante")
    public List<Restaurante> restaurantes;

    // Método auxiliar para saber se tem foto
    public boolean temFoto() {
        return nomeFoto != null && !nomeFoto.isEmpty();
    }
}
