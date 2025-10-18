package controllers;

import models.Cliente;
import play.libs.Crypto;
import play.mvc.Controller;

public class Logins extends Controller{

    public static void form() {
        render();
    }
    
    public static void logar(String email, String senha) {
        Cliente cliente = Cliente.find("email = ?1 and senha = ?2",
                email, Crypto.passwordHash(senha)).first();
        if (cliente == null) {
            flash.error("email ou senha inválidos");
            form();
        } else {
            // ===== LINHA ALTERADA AQUI =====
            // ANTES: session.put("usuarioLogado", cliente.email);
            // DEPOIS: Salve o ID do cliente, não o email.
            session.put("clienteId", cliente.id);
            // ===== FIM DA ALTERAÇÃO =====
            
            flash.success("Logado com sucesso!");
            Gerenciamentos.principal();
        }
    }
    
    public static void logout() {
        session.clear();
        flash.success("Você saiu do sistema!");
        form();
    }
}