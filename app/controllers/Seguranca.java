package controllers;

import models.Cliente;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http; // Importe Http

public class Seguranca extends Controller{

	@Before
	static void verificarAutenticacao() {
		// Obtém o nome da ação atual
		String action = Http.Request.current().action;

		// Verifica se a ação NÃO É a de cadastro e se o usuário NÃO está logado
		if (!"Gerenciamentos.formCadastro".equals(action) && !session.contains("clienteId")) {
			flash.error("Você deve logar no sistema.");
			Logins.form(); // Redireciona para o login
		}
		// Se for a ação formCadastro ou se o usuário estiver logado, continua normalmente
	}

	 static Cliente getClienteConectado() {
        // Verifica se a sessão contém a chave "clienteId" (definida no login)
        if (session.contains("clienteId")) {
            Long clienteId = Long.parseLong(session.get("clienteId"));
            return Cliente.findById(clienteId);
        }
        return null;
    }
}
