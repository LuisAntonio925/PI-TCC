package controllers;

import java.io.File;
import java.util.List;

import models.Cliente;
import models.Perfil;
import models.Restaurante;
import models.Status;
import models.Foto;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.Valid;

@With(Seguranca.class)
public class RestaurantesPerfil extends Controller {

    // Lista só dos restaurantes do proprietário logado
    public static void restaurantes() {
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if (clienteLogado == null || clienteLogado.perfil != Perfil.PROPRIETARIO) {
            flash.error("Você precisa estar logado como proprietário.");
            Logins.form();
            return;
        }

        List<Restaurante> restaurantes = Restaurante.find(
                "status = ?1 and proprietario = ?2",
                Status.ATIVO, clienteLogado
        ).fetch();

        render(restaurantes, clienteLogado);
    }

    // Form para editar um restaurante do proprietário
    public static void editarRestaurante(Long id) {
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if (clienteLogado == null || clienteLogado.perfil != Perfil.PROPRIETARIO) {
            flash.error("Você precisa estar logado como proprietário.");
            Logins.form();
            return;
        }

        Restaurante rest = Restaurante.findById(id);
        if (rest == null || rest.proprietario == null || !rest.proprietario.equals(clienteLogado)) {
            flash.error("Você não tem permissão para editar este restaurante.");
            restaurantes(); // volta para a lista correta
            return;
        }

        renderTemplate("RestaurantesPerfil/editarRestaurante.html", rest, clienteLogado);
    }

    // Salvar alterações (apenas nos restaurantes do próprio proprietário)
    public static void atualizarRestaurante(@Valid Restaurante restaurante, File foto) {
        Cliente clienteLogado = Seguranca.getClienteConectado();
        if (clienteLogado == null || clienteLogado.perfil != Perfil.PROPRIETARIO) {
            flash.error("Você precisa estar logado como proprietário.");
            Logins.form();
            return;
        }

        Restaurante restDoBanco = Restaurante.findById(restaurante.id);
        if (restDoBanco == null || restDoBanco.proprietario == null ||
            !restDoBanco.proprietario.equals(clienteLogado)) {
            flash.error("Você não tem permissão para editar este restaurante.");
            restaurantes();
            return;
        }

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            renderTemplate("RestaurantesPerfil/editarRestaurante.html", restDoBanco, clienteLogado);
            return;
        }

        // Atualiza campos permitidos
        restDoBanco.nomeDoRestaurante = restaurante.nomeDoRestaurante;
        restDoBanco.categoria = restaurante.categoria;
        restDoBanco.CNPJ = restaurante.CNPJ;
        restDoBanco.whatsapp = restaurante.whatsapp;
        restDoBanco.linkPagina = restaurante.linkPagina;
        // adicione outros campos que quiser permitir alteração

        // Upload de nova foto opcional
        if (foto != null) {
            try {
                String caminho = Play.applicationPath + "/uploads/" + restDoBanco.id;
                File pasta = new File(caminho);
                if (!pasta.exists()) {
                    pasta.mkdirs();
                }

                File destino = new File(pasta, foto.getName());
                if (destino.exists()) {
                    destino.delete();
                }

                foto.renameTo(destino);

                Foto novaFoto = new Foto(foto.getName());
                novaFoto.restaurante = restDoBanco;
                novaFoto.save();
            } catch (Exception e) {
                e.printStackTrace();
                flash.error("Erro ao salvar a nova foto do restaurante.");
            }
        }

        restDoBanco.save();
        flash.success("Restaurante atualizado com sucesso!");
        restaurantes(); // volta para a lista do proprietário
    }
}

