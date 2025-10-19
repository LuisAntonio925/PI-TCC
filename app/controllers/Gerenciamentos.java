package controllers;

import java.util.List;

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.Validation; // Importe a validação
import play.Logger; // Importe o Logger para depuração (opcional, mas útil)

@With(Seguranca.class)
public class Gerenciamentos extends Controller {

    // ... (outros métodos como principal, principal2, listar, editar permanecem iguais) ...

    public static void principal() {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();
        render(restaurantes, clienteConectado);
    }

    public static void ListasDeGerenciamentos() {
        render();
    }

    // Método para preparar o formulário para um NOVO cliente
    public static void formCadastro() {
        Cliente cli = new Cliente(); // Cria um objeto vazio
        List<Restaurante> restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch(); // Carrega restaurantes
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
     * MÉTODO SALVAR - CORRIGIDO
     * 1. A assinatura foi alterada para receber a 'String senha' do formulário.
     * 2. A lógica interna agora usa essa variável 'senha' para criar o hash.
     */
    public static void salvar(Cliente cli, String senha, Long idRestaurante) { // <<< PONTO CRÍTICO DA CORREÇÃO

        Logger.info("Gerenciamentos.salvar - Tentando salvar cliente: %s", cli.email);

        // Verifica se é uma ATUALIZAÇÃO (cliente já tem ID)
        if (cli.id != null) {
            Cliente clienteDoBanco = Cliente.findById(cli.id);
            // Só atualiza a senha se o usuário digitou algo no campo 'senha'
            if (senha != null && !senha.trim().isEmpty()) {
                Logger.info("Atualizando senha para cliente ID: %d", cli.id);
                cli.setSenha(senha); // <<< CORREÇÃO AQUI: Usa o parâmetro 'senha'
            } else {
                // Se o campo veio vazio, mantém a senha antiga (o hash) que já estava no banco
                cli.senha = clienteDoBanco.senha;
                 Logger.info("Mantendo senha antiga para cliente ID: %d", cli.id);
            }
        } else {
            // É um NOVO cliente
            Logger.info("Cadastrando novo cliente: %s", cli.email);
            // Validação: Senha é obrigatória para novo cliente
            if (senha == null || senha.trim().isEmpty()) {
                validation.addError("senha", "Senha é obrigatória para novos cadastros");
                 Logger.warn("Erro de validação: Senha em branco para novo cadastro.");
            } else {
                cli.setSenha(senha); // <<< CORREÇÃO AQUI: Usa o parâmetro 'senha' para criar o hash
                 Logger.info("Hash da senha gerado para novo cliente: %s", cli.senha);
            }
        }

        // Se houver erros de validação (ex: senha em branco no cadastro), volta pro formulário
        if(validation.hasErrors()) {
            params.flash(); // Manter os dados digitados (nome, email, etc)
            validation.keep(); // Manter os erros para exibir na view
            Logger.warn("Erros de validação encontrados. Retornando ao formulário.");

            // Recarregar a lista de restaurantes para a view
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
             Logger.info("Tentando vincular restaurante ID: %d", idRestaurante);
            Restaurante rest = Restaurante.findById(idRestaurante);
            if (rest != null && !cli.restaurantes.contains(rest)) {
                cli.restaurantes.add(rest);
                 Logger.info("Restaurante ID %d vinculado.", idRestaurante);
            }
        }

        try {
            cli.save(); // Salva o cliente (agora com o hash correto da senha)
            Logger.info("Cliente ID %d salvo/atualizado com sucesso.", cli.id);
            flash.success("Cliente salvo com sucesso!");
             // Redireciona para a tela de edição
            editar(cli.id);
        } catch (Exception e) {
            Logger.error(e, "Erro ao salvar cliente ID %d", cli.id);
            flash.error("Ocorreu um erro ao salvar o cliente.");
            // Recarrega a lista e re-renderiza o form em caso de erro no save
            List<Restaurante> restaurantesDisponiveis = null;
             if (cli.id != null) {
                 restaurantesDisponiveis = Restaurante.find(
                     "status = ?1 and ?2 not member of clientes", Status.ATIVO, cli
                 ).fetch();
             } else {
                 restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch();
             }
             validation.keep(); // Manter erros se houver
            renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
        }
    }

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

    public static void remover(long id) {
        Cliente cli = Cliente.findById(id);
        cli.status = Status.INATIVO;
        cli.save();
        listar(null);
    }
}