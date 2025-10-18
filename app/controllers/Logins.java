package controllers;

import models.Cliente;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;
import controllers.Restaurantes; 

public class Logins extends Controller {

	public static void form() {
		render();
	}
	
	public static void logar(String login, String senha) {
		
		Cliente c = Cliente.find("login = ?1 and senha = ?2", login, senha).first();
		
		if (c == null) {
			flash.error("Login ou senha inválidos");
			form();
		}
		
		
		// Limpa a sessão antiga e salva os novos dados
		session.clear(); 
		session.put("cliente.login", c.login);
		session.put("cliente.nome", c.nome);
		// Salva o Perfil como String na sessão
		session.put("cliente.perfil", c.perfil.name()); 
		
		// Redireciona com base no perfil
		if (c.perfil == models.Perfil.ADMINISTRADOR) {
			Gerenciamentos.principal();
		} else {
			Restaurantes.listar2(senha);
		}
	}
	
	public static void logout() {
		session.clear();
		flash.success("Você saiu do sistema.");
		form();
	}
}