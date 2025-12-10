package controllers;

import models.Cliente;
import models.Perfil; // Certifique-se que este import existe
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;

public class Seguranca extends Controller {

    // Garante que o usuário esteja logado para acessar qualquer coisa
    // EXCETO as páginas listadas em 'unless'.
    @Before(unless = {
        "Logins.form",                         // Ver formulário de login
        "Logins.logar",                        // Tentar logar
        "Gerenciamentos.formCadastro",         // Ver formulário de cadastro CLIENTE
        "Gerenciamentos.salvar",               // Salvar o cadastro inicial CLIENTE
        "Gerenciamentos.formCadastroProprietario", // cadastro proprietário
        "Gerenciamentos.salvarProprietario"        // salvar proprietário
    })
    static void verificarAutenticacao() {
        if (!session.contains("clienteId")) {
            flash.error("Você deve logar no sistema.");
            Logins.form();
        }
    }

    // Restringe actions só para ADMIN, exceto as listadas em 'unless'
    @Before(unless = {
        // Páginas públicas / sem login
        "Logins.form",
        "Logins.logar",
        "Gerenciamentos.formCadastro",
        "Gerenciamentos.salvar",
        "Gerenciamentos.formCadastroProprietario",
        "Gerenciamentos.salvarProprietario",

        // Ações permitidas para QUALQUER cliente logado (CLIENTE + PROPRIETARIO + ADMIN)
        "Logins.logout",                       // Sair
        "Gerenciamentos.principal",            // Home com restaurantes
        "Restaurantes.listar2",                // Lista filtrável de restaurantes

        // LIBERAR CADASTRO/EDIÇÃO PARA PROPRIETARIO E ADMIN
        "Restaurantes.formCadastrarRestaurante",
        "Restaurantes.salvar",
        "Restaurantes.editar",
        "Restaurantes.remover",
        "Restaurantes.trocarStatusAjax",       // (permissão fina checada no controller)

        // PERFIL DO CLIENTE
        "ClientesPerfil.perfil",
        "ClientesPerfil.editarPerfil",
        "ClientesPerfil.atualizarPerfil",

        // FAVORITOS
        "Favoritos.index",
        "Favoritos.alternarFavorito",
        "Favoritos.favoritarAjax",

        // PERFIL DE RESTAURANTES DO PROPRIETÁRIO
        "RestaurantesPerfil.restaurantes",
        "RestaurantesPerfil.editarRestaurante",
        "RestaurantesPerfil.atualizarRestaurante",

        "Application.index"                    // Página inicial (se houver)
        // Adicione aqui outras actions que o CLIENTE/PROPRIETARIO pode acessar
    })
    static void verificarAcesso() {
        Cliente clienteConectado = getClienteConectado();

        // Se está logado E NÃO é administrador
        if (clienteConectado != null && clienteConectado.perfil != Perfil.ADMINISTRADOR) {
            // Se chegou aqui, é porque tentou acessar algo FORA da lista 'unless'
            flash.error("Acesso restrito a administradores!");
            Gerenciamentos.principal();
        }
        // Se for admin OU a action estiver em 'unless', passa direto.
    }

    // Método auxiliar para pegar o cliente logado
    static Cliente getClienteConectado() {
        if (session.contains("clienteId")) {
            String clienteIdStr = session.get("clienteId"); // Pega como String
            Logger.info("Encontrado clienteId na sessão: %s", clienteIdStr); // Log 1
            try {
                Long clienteId = Long.parseLong(clienteIdStr); // Converte para Long
                Cliente cliente = Cliente.findById(clienteId); // Busca no banco
                if (cliente != null) {
                    Logger.info("Cliente %d encontrado no banco.", cliente.id); // Log 2
                } else {
                    Logger.warn("Cliente com ID %d não encontrado no banco!", clienteId); // Log 3
                }
                return cliente;
            } catch (NumberFormatException e) {
                 Logger.error("clienteId na sessão não é um número válido: %s", clienteIdStr); // Log 4
                 session.remove("clienteId"); // Remove o ID inválido
                 return null;
            } catch (Exception e) {
                Logger.error(e, "Erro ao buscar cliente por ID %s", clienteIdStr); // Log 5
                return null;
            }
        }
        Logger.info("Nenhum clienteId encontrado na sessão."); // Log 6
        return null;
    }
}



