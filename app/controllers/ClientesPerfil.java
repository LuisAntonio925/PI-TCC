package controllers;

import java.io.File;
import models.Cliente;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.*;

@With(Seguranca.class)
public class ClientesPerfil extends Controller {

    public static void perfil() {
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if(clienteLogado == null){
            Logins.form();
            return;
        }
        render(clienteLogado);
    }

    public static void editarPerfil(){
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if(clienteLogado == null){
            Logins.form();
            return;
        }
        renderTemplate("ClientesPerfil/editarPerfil.html", clienteLogado);
    }

    public static void atualizarPerfil(@Valid Cliente cliente, File foto){
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if(clienteLogado == null){
            Logins.form();
            return;
        }

        // validação de email único (se mudou)
        if (!clienteLogado.email.equalsIgnoreCase(cliente.email)) {
            if (Cliente.find("byEmail", cliente.email).first() != null) {
                validation.addError("cliente.email", "Este email já está cadastrado.");
            }
        }

        if(validation.hasErrors()){
            params.flash();
            validation.keep();
            editarPerfil();
            return;
        }

        // Atualiza dados básicos
        clienteLogado.nome = cliente.nome;
        clienteLogado.email = cliente.email;
        clienteLogado.telefone = cliente.telefone;

        String novaSenha = params.get("senha");
        if(novaSenha != null && !novaSenha.isEmpty()){
            clienteLogado.setSenha(novaSenha);
        }

        // NOVO: upload de nova foto (opcional)
        if (foto != null) {
            try {
                String caminho = Play.applicationPath + "/uploads/clientes/" + clienteLogado.id;
                File pasta = new File(caminho);
                if (!pasta.exists()) {
                    pasta.mkdirs();
                }

                File destino = new File(pasta, foto.getName());
                if (destino.exists()) {
                    destino.delete();
                }

                foto.renameTo(destino);

                clienteLogado.nomeFoto = foto.getName();
            } catch (Exception e) {
                e.printStackTrace();
                flash.error("Erro ao salvar a nova foto de perfil.");
            }
        }

        clienteLogado.save();
        flash.success("Perfil atualizado com sucesso!!");
        perfil();
    }
}
