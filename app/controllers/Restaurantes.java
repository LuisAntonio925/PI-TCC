package controllers;
import models.Status;

import java.util.List;

import models.Cliente;
import models.Restaurante;
import play.mvc.Controller;
import play.mvc.With;

@With(Seguranca.class)
public class Restaurantes extends Controller{
    
    // ... (outros métodos permanecem iguais: formCadastrarRestaurante, parceiroRestApp, principal, listar2)
    public static void formCadastrarRestaurante() {
        render();
    }
    public static void parceiroRestApp() {
        render();
    }
    public static void principal() {
        render();
    }
    public static void listar2(String busca) {
        List<Restaurante> listaRest = null;
        if(busca == null) {
            listaRest = Restaurante.find("status <> ?1", Status.INATIVO).fetch();
        }else {
            listaRest = Restaurante.find("(lower(nomeDoRestaurante) like ?1"
                                + "or lower(CNPJ) like ?1 or lower(categoria) like ?1) and status <> ?2",
                                "%" + busca.toLowerCase() + "%",
                                    Status.INATIVO).fetch();
        }
        render(listaRest, busca);
    }

    // MÉTODO SALVAR - LÓGICA INVERTIDA
    public static void salvar(Restaurante rest, Long idCliente) {
      
        rest.save();
    
    editar(rest.id);

        
        
    }

    public static void editar(long id) {
        Restaurante R = Restaurante.findById(id);
        // Busca clientes que AINDA NÃO estão associados a este restaurante
       List<Cliente> clientes = Cliente.find("?1 not member of restaurantes", R).fetch();
        renderTemplate("Restaurantes/formCadastrarRestaurante.html", R, clientes);
    }

    public static void remover(long id) {
        Restaurante rest = Restaurante.findById(id);
        rest.status = Status.INATIVO;
        rest.save();
        
        listar2(null);
    }
    
    // MÉTODO REMOVERCLIENTE - LÓGICA INVERTIDA
    public static void removerCliente(Long idRest, Long idCli) {
        Restaurante rest = Restaurante.findById(idRest);
        Cliente cli = Cliente.findById(idCli);
        
        if (cli != null && rest != null) {
            // LÓGICA NOVA: Remove o restaurante da lista do cliente e salva o CLIENTE
            cli.restaurantes.remove(rest);
            cli.save(); // Salva a entidade que é dona da relação!
        }
        
        editar(rest.id);
    }

}
