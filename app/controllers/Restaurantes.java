package controllers;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import models.Cliente;
import models.Foto;
import models.Perfil;
import models.Restaurante;
import models.Status;
import play.Play; 
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;

@With(Seguranca.class)
public class Restaurantes extends Controller {

    public static void formCadastrarRestaurante(Restaurante rest) {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        if (clienteConectado == null ||
            (clienteConectado.perfil != Perfil.ADMINISTRADOR && clienteConectado.perfil != Perfil.PROPRIETARIO)) {
            flash.error("Você não tem permissão para cadastrar restaurante.");
            Gerenciamentos.principal();
            return;
        }

        List<Cliente> clientes = Cliente.findAll(); // se quiser, pode esconder essa lista para PROPRIETARIO
        render("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
    }

    // Recebe File foto em vez de Blob/File imagem
    public static void salvar(@Valid Restaurante rest, Long idCliente, File foto) {

        Cliente clienteConectado = Seguranca.getClienteConectado();
        if (clienteConectado == null ||
            (clienteConectado.perfil != Perfil.ADMINISTRADOR && clienteConectado.perfil != Perfil.PROPRIETARIO)) {
            flash.error("Você não tem permissão para salvar restaurante.");
            Gerenciamentos.principal();
            return;
        }

        if (validation.hasErrors()) {
            params.flash(); 
            validation.keep();
            List<Cliente> clientes = Cliente.findAll();
            render("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
        }

        // Define o proprietário no cadastro (novo restaurante)
        if (rest.id == null) {
            rest.proprietario = clienteConectado;
        }

        rest.save();

        // Lógica de upload de foto (mantém a que você já tinha)
        if (foto != null) {
            try {
                String caminho = Play.applicationPath + "/uploads/" + rest.id;
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
                novaFoto.restaurante = rest;
                novaFoto.save();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Vincular a um cliente (se usar esse vínculo além do proprietário)
        if (idCliente != null) {
            Cliente c = Cliente.findById(idCliente);
            if (c != null && !c.restaurantes.contains(rest)) {
                c.restaurantes.add(rest);
                c.save();
            }
        }

        flash.success("Restaurante salvo com sucesso!");
        listar2(null);
    }

    public static void listar2(String busca) {
        Cliente clienteConectado = Seguranca.getClienteConectado();

        List<Restaurante> listaRest = null;

        // Se ADMIN: vê todos; se PROPRIETARIO: vê só os próprios (se quiser)
        if (clienteConectado != null && clienteConectado.perfil == Perfil.PROPRIETARIO) {
            if (busca == null || busca.trim().isEmpty()) {
                listaRest = Restaurante.find("status = ?1 and proprietario = ?2",
                                             Status.ATIVO, clienteConectado).fetch();
            } else {
                listaRest = Restaurante.find(
                        "(lower(nomeDoRestaurante) like ?1 or lower(CNPJ) like ?1 or lower(categoria) like ?1) " +
                        "and status = ?2 and proprietario = ?3",
                        "%" + busca.toLowerCase() + "%", Status.ATIVO, clienteConectado
                ).fetch();
            }
        } else {
            // ADMIN e CLIENTE veem todos ativos
            if (busca == null || busca.trim().isEmpty()) {
                listaRest = Restaurante.find("status = ?1", Status.ATIVO).fetch();
            } else {
                listaRest = Restaurante.find(
                        "(lower(nomeDoRestaurante) like ?1 or lower(CNPJ) like ?1 or lower(categoria) like ?1) " +
                        "and status = ?2",
                        "%" + busca.toLowerCase() + "%", Status.ATIVO
                ).fetch();
            }
        }

        render(listaRest, busca, clienteConectado);
    }

    public static void editar(long id) {
        Restaurante rest = Restaurante.findById(id);
        Cliente clienteConectado = Seguranca.getClienteConectado();

        if (rest == null) {
            flash.error("Restaurante não encontrado.");
            listar2(null);
            return;
        }

        // Verifica permissão: PROPRIETARIO só edita o que é dele
        if (clienteConectado.perfil == Perfil.PROPRIETARIO &&
            (rest.proprietario == null || !rest.proprietario.equals(clienteConectado))) {
            flash.error("Você não tem permissão para editar este restaurante.");
            listar2(null);
            return;
        }

        List<Cliente> clientes = Cliente.findAll();
        renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
    }

    public static void remover(long id) {
        Restaurante rest = Restaurante.findById(id);
        Cliente clienteConectado = Seguranca.getClienteConectado();

        if (rest != null) {
            // Verifica permissão: PROPRIETARIO só remove o que é dele
            if (clienteConectado.perfil == Perfil.PROPRIETARIO &&
                (rest.proprietario == null || !rest.proprietario.equals(clienteConectado))) {
                flash.error("Você não tem permissão para remover este restaurante.");
                listar2(null);
                return;
            }

            rest.status = Status.INATIVO;
            rest.save();
        }
        listar2(null);
    }

    // trocar status via AJAX (idealmente só ADMIN)
    public static void trocarStatusAjax(Long id) {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        if (clienteConectado == null || clienteConectado.perfil != Perfil.ADMINISTRADOR) {
            response.status = 403;
            renderText("Apenas administradores podem alterar o status.");
            return;
        }

        Restaurante rest = Restaurante.findById(id);
        if (rest == null) {
            response.status = 404;
            renderText("Restaurante não encontrado");
            return;
        }

        // Alterna o status
        if (rest.status == Status.ATIVO) {
            rest.status = Status.INATIVO;
        } else {
            rest.status = Status.ATIVO;
        }
        rest.save();

        // Retorna JSON com o novo status
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", rest.id);
        resp.put("status", rest.status.toString());

        renderJSON(resp);
    }
}
