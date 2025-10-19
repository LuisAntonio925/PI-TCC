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

		// --- CORREÇÃO AQUI ---
		// Lista de ações que NÃO exigem login
		boolean acaoPublica = "Gerenciamentos.formCadastro".equals(action) || // Permite VER o formulário
		                      "Gerenciamentos.salvar".equals(action) ||       // Permite SALVAR o formulário
		                      "Logins.form".equals(action) ||                 // Permite VER o login
		                      "Logins.logar".equals(action);                  // Permite TENTAR o login
		// --- FIM DA CORREÇÃO ---


		// Se a ação NÃO for pública E o utilizador NÃO estiver logado
		if (!acaoPublica && !session.contains("clienteId")) {
			flash.error("Você deve logar no sistema.");
			Logins.form(); // Redireciona para o login
		}
		// Se a ação for pública ou se o usuário estiver logado, continua normalmente
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