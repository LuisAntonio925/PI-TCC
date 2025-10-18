// luisantonio925/projetopi-tcc/ProjetoPi-TCC-1f367715d58cce63ad3bf1ce68416d2a7aad77b1/app/controllers/Seguranca.java
package controllers;

import models.Cliente;
import models.Perfil;
import play.mvc.Before;
import play.mvc.Controller;

public class Seguranca extends Controller{
	
	@Before
	
	static void verificarAutenticacao() {
		if (!session.contains("clienteId")) { // CORREÇÃO: Verifica a chave 'clienteId'
			flash.error("Você deve logar no sistema.");
			Logins.form();
		}
	}

	 static Cliente getClienteConectado() {
        // Verifica se a sessão contém a chave "clienteId" (definida no login)
        if (session.contains("clienteId")) {
            Long clienteId = Long.parseLong(session.get("clienteId"));
            return Cliente.findById(clienteId);
        }
        return null;
    }
	
	@Before
static void verificarAdministrador() {
  	   String perfil = session.get("usuarioPerfil");
  	  
}

}
