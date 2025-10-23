package controllers;

import java.util.List;

// Import @Valid (embora não seja usado na assinatura do salvar)
import play.data.validation.Valid; 

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
// Import Validation para usar validation.clear(), validation.valid(), etc.
import play.data.validation.Validation; 

@With(Seguranca.class)
public class Gerenciamentos extends Controller {
    
    // ... (os seus métodos principal, ListasDeGerenciamentos, formCadastro, listar, editar estão corretos) ...
    public static void principal() {
        Cliente clienteConectar = Seguranca.getClienteConectado();
        List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();
        render(restaurantes, clienteConectar); 
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
    
    /**
     * MÉTODO SALVAR - CORREÇÃO DEFINITIVA (Loop de Validação)
     * 1. Removemos @Valid Cliente cli da assinatura do método.
     * 2. Adicionamos validation.clear() ANTES de qualquer validação.
     * 3. Adicionamos validation.valid(cli) para correr a validação manualmente.
     */
    // ANTES: public static void salvar(@Valid Cliente cli, String senha, Long idRestaurante)
    public static void salvar(Cliente cli, String senha, Long idRestaurante) { //
         
        // ---- ALTERAÇÃO PRINCIPAL ----
        validation.clear(); // 1. Limpa erros antigos "grudentos" da sessão/flash
        validation.valid(cli); // 2. Valida o objeto 'cli' que o Play preencheu automaticamente
        // -----------------------------

        // Lógica de validação manual da senha (corre APÓS o clear e valid)
        if (cli.id != null) { 
            Cliente clienteDoBanco = Cliente.findById(cli.id);
            // Mantém a senha antiga se o campo veio vazio na edição
            if (senha == null || senha.trim().isEmpty()) {
                 cli.senha = clienteDoBanco.senha; // Busca a senha hash do banco
            } else {
                 cli.setSenha(senha); // Define a nova senha (será criptografada pelo setSenha)
            }
        } else {
            // Novo cliente
            if (senha == null || senha.trim().isEmpty()) {
                validation.addError("senha", "O campo Senha e obrigatorio");
            } else {
                cli.setSenha(senha); // Define a senha (será criptografada)
            }
        }
        
        // Verifica os erros (do validation.valid(cli) e do addError da senha)
        if(validation.hasErrors()) {
            params.flash(); // Mantém os dados digitados (nome, email...) nos campos
            validation.keep(); // Guarda os erros DESTA tentativa para mostrar no render
            
            // Recarrega a lista de restaurantes necessária para o <select>
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
            
            // Renderiza o formulário de novo, mostrando os erros atuais
            renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
        
        } else {
            // ---- SUCESSO! ----
            // Só entra aqui se NENHUM erro ocorreu nesta tentativa

            // Lógica de vincular restaurante
            if (idRestaurante != null) {
                Restaurante rest = Restaurante.findById(idRestaurante);
                if (rest != null && !cli.restaurantes.contains(rest)) {
                    cli.restaurantes.add(rest);
                }
            }
            
            cli.save(); 
            flash.success("Cliente salvo com sucesso!");
            editar(cli.id); // Redireciona para a edição
        }
    }
    
    // ... (resto dos seus métodos: removerRestaurante, remover) ...
     public static void removerRestaurante(Long idCli, Long idRest) {
        Cliente cli = Cliente.findById(idCli);
        Restaurante rest = Restaurante.findById(idRest);
        if (cli != null && rest != null) {
            cli.restaurantes.remove(rest); 
            cli.save(); 
            flash.success("Vinculo com o restaurante '%s' foi removido.", rest.nomeDoRestaurante);
        } else {
            flash.error("Ocorreu um erro ao tentar remover o vinculo.");
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