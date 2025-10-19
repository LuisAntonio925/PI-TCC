package controllers;

import java.util.List;

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;

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
        Cliente cli = new Cliente(); // Cria um objeto vazio para evitar erros na view
        render(cli);
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
     * MÉTODO SALVAR - ATUALIZADO
     * Recebe os dados do cliente e um ID opcional de restaurante para vincular.
     * Gerencia a atualização da senha e salva o cliente e suas relações.
     */
    public static void salvar(Cliente cli, Long idRestaurante) {

        // ... (toda a lógica de salvar a senha e o cliente que já fizemos)
    if (cli.id != null) { 
        Cliente clienteDoBanco = Cliente.findById(cli.id);
        if (cli.senha == null || cli.senha.trim().isEmpty()) {
            cli.senha = clienteDoBanco.senha;
        } else {
            cli.setSenha(cli.senha);
        }
    } else {
        cli.setSenha(cli.senha);
    }

    if (idRestaurante != null) {
        Restaurante rest = Restaurante.findById(idRestaurante);
        if (rest != null && !cli.restaurantes.contains(rest)) {
            cli.restaurantes.add(rest);
        }
    }
    
    cli.save(); // Salva o cliente. Se for novo, ele ganha um ID aqui!
    
    flash.success("Cliente salvo com sucesso!");
    
    // LINHA MAIS IMPORTANTE:
    // Redireciona para a tela de edição DESTE MESMO cliente.
    // Agora que ele tem um ID, a view mostrará as opções de edição.
    editar(cli.id); 
}
    
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
