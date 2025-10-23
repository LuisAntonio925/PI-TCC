package controllers;

import java.util.List;

// ---- CORREÇÃO IMPORTANTE AQUI ----
// import javax.validation.Valid;  // REMOVA ESTA LINHA
import play.data.validation.Valid; // ADICIONE ESTA LINHA
// --------------------------------

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.Validation; // Este import está correto

@With(Seguranca.class)
public class Gerenciamentos extends Controller {
    
    // ... (seus métodos principal, ListasDeGerenciamentos, formCadastro, listar, editar estão OK) ...
    public static void principal() {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();
        render(restaurantes, clienteConectado); 
    }
	 public static void ListasDeGerenciamentos() {
        render();
    }
    public static void formCadastro() {
        Cliente cli = new Cliente(); 
        List<Restaurante> restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch();
        renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
    }
    public static void listar(String termo) {
        List<Cliente> listaClientes = null;
        if (termo == null || termo.trim().isEmpty()) {
            listaClientes = Cliente.find("status <> ?1", Status.INATIVO).fetch();   
        } else {
            listaClientes = Cliente.find("(lower(nome) like ?1 or lower(email) like ?1) and status <> ?2",
                        "%" + termo.toLowerCase() + "%",
                        Status.INATIVO).fetch();
        }
        render(listaClientes, termo);
    }
    public static void editar(long id) {
        Cliente cli = Cliente.findById(id);
        List<Restaurante> restaurantesDisponiveis = Restaurante.find(
            "status = ?1 and ?2 not member of clientes", 
            Status.ATIVO, 
            cli
        ).fetch();
        renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
    }
    
   public static void salvar(@Valid Cliente cli, String senha, Long idRestaurante) { //
         
        // Lógica de validação manual da senha
        if (cli.id != null) { 
            Cliente clienteDoBanco = Cliente.findById(cli.id);
            if (senha != null && !senha.trim().isEmpty()) {
                cli.setSenha(senha); 
            } else {
                cli.senha = clienteDoBanco.senha;
            }
        } else {
            if (senha == null || senha.trim().isEmpty()) {
                // Usamos a chave "senha" que definimos no conf/messages
                validation.addError("senha", "O campo Senha e obrigatorio");
            } else {
                cli.setSenha(senha); 
            }
        }
        
        // Agora que o cache foi limpo, este IF vai funcionar corretamente
        if(validation.hasErrors()) {
            params.flash(); 
            validation.keep(); // Guarda os erros SÓ para o próximo render
            
            List<Restaurante> restaurantesDisponiveis = null;
            if (cli.id != null) {
                 restaurantesDisponiveis = Restaurante.find(
                    "status = ?1 and ?2 not member of clientes", 
                    Status.ATIVO, 
                    cli
                ).fetch();
            } else {
                 restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch();
            }
            
            renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
        
        } else {
            // ---- SUCESSO! ----
            if (idRestaurante != null) {
                Restaurante rest = Restaurante.findById(idRestaurante);
                if (rest != null && !cli.restaurantes.contains(rest)) {
                    cli.restaurantes.add(rest);
                }
            }
            
            cli.save(); 
            flash.success("Cliente salvo com sucesso!");
            editar(cli.id); // Redireciona
        }
    }
    
    // ... (resto dos métodos removerRestaurante e remover estão OK) ...
    public static void removerRestaurante(Long idCli, Long idRest) {
        Cliente cli = Cliente.findById(idCli);
        Restaurante rest = Restaurante.findById(idRest);
        if (cli != null && rest != null) {
            cli.restaurantes.remove(rest); 
            cli.save(); 
            flash.success("Vínculo com o restaurante '%s' foi removido.", rest.nomeDoRestaurante);
        } else {
            flash.error("Ocorreu um erro ao tentar remover o vínculo.");
        }
        editar(idCli); 
    }
    public static void remover(long id) {
        Cliente cli = Cliente.findById(id);
        cli.status = Status.INATIVO;
        cli.save();
        listar(null);
    }
}