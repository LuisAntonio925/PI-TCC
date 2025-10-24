package controllers;

import java.util.List;
import models.Cliente;
import models.Restaurante;
import models.Status;
import play.Logger; // Importar o Logger
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

@With(Seguranca.class)
public class Gerenciamentos extends Controller {

	public static void formCadastro() {
		render();
	}

	public static void salvar(Restaurante rest) {
		rest.status = Status.ATIVO;
		rest.save();
		flash.success("Restaurante " + rest.nomeDoRestaurante + " salvo com sucesso!");
		listar();
	}

	public static void listar() {
		List<Restaurante> restaurantes = Restaurante.find("status = ?1", Status.ATIVO).fetch();
		render(restaurantes);
	}

// ... dentro da classe Gerenciamentos ...

public static void principal() {
    Cliente clienteAtual = Seguranca.getClienteConectado(); 
    
    // *** FORÇAR RECARREGAMENTO DO CLIENTE ***
    if (clienteAtual != null && clienteAtual.id != null) {
        Long clienteId = clienteAtual.id; // Guarda o ID
        try {
            // Desanexa o objeto atual da sessão JPA para evitar cache
            JPA.em().detach(clienteAtual); 
            // Busca o cliente NOVAMENTE do banco de dados usando o ID
            clienteAtual = Cliente.findById(clienteId); 
            Logger.info("Cliente ID: %d RECARREGADO do banco antes de renderizar principal.", clienteId);
        } catch (Exception e) {
             Logger.error(e, "Erro ao tentar recarregar Cliente ID: %d", clienteId);
             // Continua com o objeto original em caso de erro, mas loga
        }
    }
    // *** FIM DO RECARREGAMENTO FORÇADO ***
    
    // Log com o objeto (possivelmente recarregado)
    if (clienteAtual != null) {
        Logger.info("Renderizando principal para cliente ID: %d. Contagem de favoritos no objeto: %d", 
                    clienteAtual.id, 
                    (clienteAtual.restaurantes != null ? clienteAtual.restaurantes.size() : 0));
    } else {
        Logger.info("Renderizando principal para cliente não logado (null).");
    }
                    
    List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();
    render(restaurantes, clienteAtual); // Usa a variável clienteAtual
}


	public static void editar(Long id) {
		Restaurante rest = Restaurante.findById(id);
		renderTemplate("Gerenciamentos/formCadastro.html", rest);
	}

	public static void remover(Long id) {
		Restaurante rest = Restaurante.findById(id);
		rest.status = Status.INATIVO;
		rest.save();
		listar();
	}

	public static void ListasDeGerenciamentos() {
		render();
	}
}
