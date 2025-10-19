package controllers;

import java.util.List;

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.Validation; // IMPORTANTE: Adicione este import

@With(Seguranca.class)
public class Gerenciamentos extends Controller {
    
   // MODIFICADO: Agora carrega os restaurantes e o cliente para a view.
    public static void principal() {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        
        // Busca todos os restaurantes ATIVOS para exibir no feed
        List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();
        
        // Renderiza a view, passando a lista de restaurantes e o cliente
        render(restaurantes, clienteConectado); 
    }
	 public static void ListasDeGerenciamentos() {
        render();
    }
    
    // Método para preparar o formulário para um NOVO cliente
    public static void formCadastro() {
        Cliente cli = new Cliente(); // Cria um objeto vazio
        // Carrega a lista de restaurantes para o <select> no formulário
        List<Restaurante> restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch();
        renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
    }

    // Método para listar os clientes com busca
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
    
    /**
     * MÉTODO EDITAR - ATUALIZADO
     * Carrega o cliente para edição e busca a lista de restaurantes
     * que estão ATIVOS e que ainda NÃO foram vinculados a este cliente.
     */
    public static void editar(long id) {
        Cliente cli = Cliente.findById(id);
        
        // Busca restaurantes que este cliente AINDA NÃO possui na sua lista.
        List<Restaurante> restaurantesDisponiveis = Restaurante.find(
            "status = ?1 and ?2 not member of clientes", 
            Status.ATIVO, 
            cli
        ).fetch();

        renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
    }
    
    /**
     * MÉTODO SALVAR - CORRIGIDO
     * 1. Assinatura alterada para receber a 'String senha' do formulário.
     * 2. Lógica interna usa a variável 'senha' para criar o hash.
     */
    // vvvvvvvvvvvv A CORREÇÃO ESTÁ AQUI vvvvvvvvvvvv
    public static void salvar(Cliente cli, String senha, Long idRestaurante) {

        // Verifica se é uma ATUALIZAÇÃO (cliente já tem ID)
        if (cli.id != null) { 
            Cliente clienteDoBanco = Cliente.findById(cli.id);
            
            // Só atualiza a senha se o usuário digitou algo no campo 'senha'
            if (senha != null && !senha.trim().isEmpty()) {
                cli.setSenha(senha); // <<< CORREÇÃO AQUI: Usa o parâmetro 'senha'
            } else {
                // Se o campo veio vazio, mantém a senha antiga (o hash) que já estava no banco
                cli.senha = clienteDoBanco.senha;
            }
        } else {
            // É um NOVO cliente
            
            // Validação: Senha é obrigatória para novo cliente
            if (senha == null || senha.trim().isEmpty()) {
                validation.addError("senha", "Senha é obrigatória para novos cadastros");
            } else {
                cli.setSenha(senha); // <<< CORREÇÃO AQUI: Usa o parâmetro 'senha' para criar o hash
            }
        }
        
        // Se houver erros (ex: senha em branco no cadastro), volta pro formulário
        if(validation.hasErrors()) {
            params.flash(); // Manter os dados digitados (nome, email, etc)
            validation.keep(); // Manter os erros para exibir na view
            
            // Precisamos carregar a lista de restaurantes novamente para evitar erro na view
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
        }

        // Lógica para vincular restaurante (já estava correta)
        if (idRestaurante != null) {
            Restaurante rest = Restaurante.findById(idRestaurante);
            if (rest != null && !cli.restaurantes.contains(rest)) {
                cli.restaurantes.add(rest);
            }
        }
        
        cli.save(); // Salva o cliente (agora com o hash correto da senha)
        
        flash.success("Cliente salvo com sucesso!");
        
        // Redireciona para a tela de edição
        editar(cli.id); 
    }
    // ^^^^^^^^^^^ FIM DA CORREÇÃO ^^^^^^^^^^^
    
    /**
     * NOVO MÉTODO PARA REMOVER VÍNCULO COM RESTAURANTE
     * Remove um restaurante da lista de um cliente específico e salva a alteração.
     */
    public static void removerRestaurante(Long idCli, Long idRest) {
        Cliente cli = Cliente.findById(idCli);
        Restaurante rest = Restaurante.findById(idRest);

        if (cli != null && rest != null) {
            cli.restaurantes.remove(rest); // Remove da lista do lado forte
            cli.save(); // Salva o cliente para persistir a remoção
            flash.success("Vínculo com o restaurante '%s' foi removido.", rest.nomeDoRestaurante);
        } else {
            flash.error("Ocorreu um erro ao tentar remover o vínculo.");
        }
        
        // Recarrega a página de edição do cliente
        editar(idCli); 
    }

    /**
     * Método para inativar (remover logicamente) um cliente.
     */
    public static void remover(long id) {
        Cliente cli = Cliente.findById(id);
        cli.status = Status.INATIVO;
        cli.save();
        listar(null);
    }
}