
package controllers;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import models.Cliente;
import models.Foto;
import models.Restaurante;
import models.Status;
import play.Play; 
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;

@With(Seguranca.class)
public class Restaurantes extends Controller {

    public static void formCadastrarRestaurante(Restaurante rest) {
        List<Cliente> clientes = Cliente.findAll();
        render("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
    }

    // ALTERADO: Recebe File foto em vez de Blob/File imagem
    public static void salvar(@Valid Restaurante rest, Long idCliente, File foto) {
        
        if(validation.hasErrors()) {
            params.flash(); 
            validation.keep();
            List<Cliente> clientes = Cliente.findAll();
            render("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
        }

        // 1. Salva o restaurante primeiro para garantir o ID
        rest.save();

        // 2. Lógica de Upload para Pasta (conforme vídeo)
        if (foto != null) {
            try {
                // Caminho: uploads/{id_restaurante}/
                String caminho = Play.applicationPath + "/uploads/" + rest.id;
                File pasta = new File(caminho);
                
                if (!pasta.exists()) {
                    pasta.mkdirs(); // Cria a pasta se não existir
                }

                File destino = new File(pasta, foto.getName());
                
                // Remove anterior se existir (opcional)
                if (destino.exists()) {
                    destino.delete();
                }

                // Move o arquivo
                foto.renameTo(destino);

                // 3. Salva referência no banco
                Foto novaFoto = new Foto(foto.getName());
                novaFoto.restaurante = rest;
                novaFoto.save();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        List<Restaurante> listaRest = null;
        if(busca == null || busca.trim().isEmpty()) {
            listaRest = Restaurante.find("status = ?1", Status.ATIVO).fetch();
        } else {
            listaRest = Restaurante.find("(lower(nomeDoRestaurante) like ?1 or lower(CNPJ) like ?1 or lower(categoria) like ?1) and status = ?2", "%" + busca.toLowerCase() + "%", Status.ATIVO).fetch();
        }
        render(listaRest, busca);
    }

    public static void editar(long id) {
        Restaurante rest = Restaurante.findById(id);
        List<Cliente> clientes = Cliente.findAll();
        renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
    }

    public static void remover(long id) {
        Restaurante rest = Restaurante.findById(id);
        if (rest != null) {
            rest.status = Status.INATIVO;
            rest.save();
        }
        listar2(null);
    }

    // NOVO: trocar status via AJAX
    public static void trocarStatusAjax(Long id) {
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
