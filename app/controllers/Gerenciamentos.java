package controllers;

import java.util.List;
import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.Valid;

@With(Seguranca.class)
public class Gerenciamentos extends Controller {

    /**
     * MODIFICADO: Método principal simplificado e corrigido.
     * Usa Seguranca.getClienteConectado() para evitar código duplicado
     * e passa a variável com o nome correto para a view.
     */
    public static void principal() {
        // 1. Busca o cliente usando o método centralizado da classe Seguranca
        Cliente clienteConectado = Seguranca.getClienteConectado();

        // 2. Busca os restaurantes ativos
        List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();

        // 3. Renderiza passando 'clienteConectado' (nome esperado pelo seu HTML)
        render(restaurantes, clienteConectado);
    }

    public static void ListasDeGerenciamentos(){
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

    public static void salvar(Cliente cli, String senha, Long idRestaurante) {
        validation.clear();
        validation.valid(cli);

        if (cli.id != null) {
            Cliente clienteDoBanco = Cliente.findById(cli.id);
            if (senha == null || senha.trim().isEmpty()) {
                cli.senha = clienteDoBanco.senha;
            } else {
                cli.setSenha(senha);
            }
        } else {
            if (senha == null || senha.trim().isEmpty()) {
                validation.addError("senha", "O campo Senha e obrigatorio");
            } else {
                cli.setSenha(senha);
            }
        }

        if(validation.hasErrors()) {
            params.flash();
            validation.keep();

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
            if (idRestaurante != null) {
                Restaurante rest = Restaurante.findById(idRestaurante);
                if (rest != null && !cli.restaurantes.contains(rest)) {
                    cli.restaurantes.add(rest);
                }
            }

            cli.save();
            flash.success("Cliente salvo com sucesso!");
            editar(cli.id);
        }
    }

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
