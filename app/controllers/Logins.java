package controllers;

import models.Cliente;
import models.Perfil;
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
            // salva o ID do cliente na sessão (vale para todos os perfis)
            session.put("clienteId", cliente.id);
            
            flash.success("Logado com sucesso!");

            // redireciona conforme o perfil
            if (cliente.perfil == Perfil.ADMINISTRADOR) {
                Gerenciamentos.principal();
            } else if (cliente.perfil == Perfil.PROPRIETARIO) {
                // proprietário vai para listagem/gestão de restaurantes dele
                Restaurantes.listar2(null);
            } else {
                // CLIENTE comum
                Gerenciamentos.principal();
            }
        }
    }
    
    public static void logout() {
        session.clear();
        flash.success("Você saiu do sistema!");
        form();
    }
}
