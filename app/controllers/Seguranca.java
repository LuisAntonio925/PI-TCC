package controllers;

import models.Cliente;
import models.Perfil; // Certifique-se que este import existe
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;

public class Seguranca extends Controller {

	// Este método garante que o usuário esteja logado para acessar qualquer coisa
	// EXCETO as páginas listadas em 'unless'.
	@Before(unless = {
		"Logins.form", 				// Ver formulário de login
		"Logins.logar", 				// Tentar logar
		"Gerenciamentos.formCadastro", // Ver formulário de cadastro
		"Gerenciamentos.salvar"		// Salvar o cadastro inicial
	})
	static void verificarAutenticacao() {
		if (!session.contains("clienteId")) {
			flash.error("Você deve logar no sistema.");
			Logins.form();
		}
	}

	// Este método verifica se o usuário NÃO é admin. Se não for,
	// ele só pode acessar as páginas listadas em 'unless'.
	// Se tentar acessar qualquer outra página protegida, será bloqueado.
	@Before(unless = {
		// Páginas Públicas (já permitidas pelo verificarAutenticacao, mas listadas aqui por clareza)
		"Logins.form",
		"Logins.logar",
		"Gerenciamentos.formCadastro",
		"Gerenciamentos.salvar",
		// Ações permitidas para CLIENTE LOGADO
		"Logins.logout",					// Sair
		"Gerenciamentos.principal", 		// Ver lista principal de restaurantes
		"Restaurantes.listar2", 		// Ver a lista filtrável de restaurantes
		"ClientesPerfil.perfil", 		// Ver próprio perfil
		"ClientesPerfil.editarPerfil", 	// Ver formulário de edição do próprio perfil
		"ClientesPerfil.atualizarPerfil",	// Salvar edição do próprio perfil
		"Favoritos.index",				// Ver favoritos
		"Favoritos.alternarFavorito",		// Adicionar/Remover favorito
		"Application.index"				// Página inicial (se houver)
		// Adicione aqui QUALQUER outra action que um CLIENTE comum possa acessar
	})
	static void verificarAcesso() {
		Cliente clienteConectado = getClienteConectado();

		// Se está logado E NÃO é administrador
		if (clienteConectado != null && clienteConectado.perfil != Perfil.ADMINISTRADOR) {
			// A anotação @Before(unless=...) já cuida de permitir as páginas acima.
			// Se o código chegou aqui, significa que o CLIENTE está tentando acessar
			// uma página que NÃO está na lista 'unless', então bloqueamos.
			flash.error("Acesso restrito a administradores!");
			// Redireciona para a página principal permitida para clientes
			Gerenciamentos.principal();
		}
		// Se for admin OU se a página estiver na lista 'unless', o acesso é permitido.
	}


	// Método auxiliar para pegar o cliente logado (mantido como antes)
	 static Cliente getClienteConectado() {
        if (session.contains("clienteId")) {
            Long clienteId = Long.parseLong(session.get("clienteId"));
            return Cliente.findById(clienteId);
        }
        return null;
    }
}