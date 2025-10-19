package controllers;

import models.Cliente;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.*;

public class ClientesPerfil extends Controller{

    public static void perfil() {
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if(clienteLogado == null){
            Logins.form();
        }
        render(clienteLogado);

    }

    public static void editarPerfil(){
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if(clienteLogado == null){
            Logins.form();
        }
        renderTemplate("ClientesPerfil/editarPerfil.html", clienteLogado);
    }
     public static void atualizarPerfil(@Valid Cliente cliente){
         Cliente clienteLogado = Seguranca.getClienteConectado();
        if(clienteLogado == null){
            Logins.form();
        }
        // *** ADICIONAR VALIDAÇÃO DE EMAIL ÚNICO (se necessário) ***
        // É importante garantir que o novo email não esteja em uso por outro cliente.
        // Adicione uma validação personalizada ou verifique manualmente antes de salvar.
        // Exemplo de verificação manual (simplificada):

        if (!clienteLogado.email.equalsIgnoreCase(cliente.email)) { // Só valida se o email mudou
             if (Cliente.find("byEmail", cliente.email).first() != null) {
                 validation.addError("cliente.email", "Este email já está cadastrado.");
             }
         }

        if(validation.hasErrors()){
            params.flash();
            validation.keep();
            editarPerfil();
        }

        clienteLogado.nome = cliente.nome;
        clienteLogado.nome = cliente.email;
        clienteLogado.nome = cliente.telefone;
        String novaSenha = params.get("senha");

        if(novaSenha != null && !novaSenha.isEmpty()){
            clienteLogado.senha = novaSenha;
        }
        clienteLogado.save();
        flash.success("Perfil atualizado com sucesso!!");
        perfil();



     }
    
}
