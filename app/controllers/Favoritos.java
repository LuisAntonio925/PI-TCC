package controllers;

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
import java.util.List;

@With(Seguranca.class)
public class Favoritos extends Controller {

    public static void index() {
        Cliente clienteConectado = Seguranca.getClienteConectado();

        if (clienteConectado == null) {
            Logins.form();
            return;
        }

        List<Restaurante> meusFavoritos = clienteConectado.restaurantes;

        List<Restaurante> outrosRestaurantes = Restaurante.find(
            "status = ?1 and ?2 not member of clientes",
            Status.ATIVO,
            clienteConectado
        ).fetch();

        render(meusFavoritos, outrosRestaurantes, clienteConectado);
    }

    public static void alternarFavorito(Long idRest) {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        if (clienteConectado == null) {
            flash.error("Você precisa estar logado para favoritar.");
            Logins.form();
            return;
        }

        Restaurante restaurante = Restaurante.findById(idRest);

        if (restaurante != null) {
            if (clienteConectado.restaurantes.contains(restaurante)) {
                clienteConectado.restaurantes.remove(restaurante);
                flash.success("'%s' foi removido dos favoritos.", restaurante.nomeDoRestaurante);
            } else {
                clienteConectado.restaurantes.add(restaurante);
                flash.success("'%s' foi adicionado aos favoritos!", restaurante.nomeDoRestaurante);
            }
            clienteConectado.save();
        } else {
            flash.error("Restaurante não encontrado.");
        }
        Gerenciamentos.principal();
    }

    // MÉTODO AJAX
    public static void favoritarAjax(Long idRest) {
        Cliente clienteConectado = Seguranca.getClienteConectado();

        if (clienteConectado == null) {
            response.status = 401;
            renderText("Não autorizado");
            return;
        }

        Restaurante restaurante = Restaurante.findById(idRest);
        if (restaurante == null) {
            response.status = 404;
            renderText("Restaurante não encontrado");
            return;
        }

        boolean agoraEhFavorito;

        if (clienteConectado.restaurantes.contains(restaurante)) {
            clienteConectado.restaurantes.remove(restaurante);
            agoraEhFavorito = false;
        } else {
            clienteConectado.restaurantes.add(restaurante);
            agoraEhFavorito = true;
        }

        clienteConectado.save();

        renderJSON(agoraEhFavorito);
    }
}
