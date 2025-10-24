package controllers;

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.Logger; // Importar o Logger
import play.mvc.Controller;
import play.mvc.With;
import java.util.List;

@With(Seguranca.class)
public class Favoritos extends Controller {

    public static void index() {
        Cliente clienteConectado = Seguranca.getClienteConectado();

        if (clienteConectado == null) {
            Logins.form();
            return; // Adicionado return para parar a execução aqui
        }

        List<Restaurante> meusFavoritos = clienteConectado.restaurantes;

        List<Restaurante> outrosRestaurantes = Restaurante.find(
            "status = ?1 and ?2 not member of clientes",
            Status.ATIVO,
            clienteConectado
        ).fetch();

        render(meusFavoritos, outrosRestaurantes, clienteConectado); // Passa clienteConectado para a view index tbm
    }

    public static void alternarFavorito(Long idRest) {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        if (clienteConectado == null) {
            Logger.warn("Cliente não conectado tentando favoritar (Redirecionando para login).");
            flash.error("Você precisa estar logado para favoritar.");
            Logins.form();
            return; // Importante parar aqui
        }

        Restaurante restaurante = Restaurante.findById(idRest);

        if (restaurante != null) {
            Logger.info("Alternando favorito para Rest ID: %d, Cliente ID: %d", idRest, clienteConectado.id);
            
            boolean jaFavorito = clienteConectado.restaurantes.contains(restaurante); 
            Logger.info("Restaurante estava nos favoritos? %s", jaFavorito);

            if (jaFavorito) {
                clienteConectado.restaurantes.remove(restaurante); // Remove da lista na memória
                flash.success("'%s' foi removido dos seus favoritos.", restaurante.nomeDoRestaurante);
                Logger.info("Removido. Lista na memória agora tem %d itens.", clienteConectado.restaurantes.size());
            } else {
                clienteConectado.restaurantes.add(restaurante); // Adiciona na lista na memória
                flash.success("'%s' foi adicionado aos seus favoritos!", restaurante.nomeDoRestaurante);
                Logger.info("Adicionado. Lista na memória agora tem %d itens.", clienteConectado.restaurantes.size());
            }

            try {
                // Salva o objeto Cliente. Isso deve persistir a mudança na relação ManyToMany.
                clienteConectado.save(); 
                Logger.info("Cliente %d salvo com sucesso.", clienteConectado.id);
            } catch (Exception e) {
                 Logger.error(e, "Erro ao salvar cliente %d após alternar favorito %d.", clienteConectado.id, restaurante.id);
                 flash.error("Ocorreu um erro ao salvar o favorito.");
                 // Redireciona mesmo com erro para não prender o usuário
                 Gerenciamentos.principal(); // Redireciona de volta para a principal
                 return; // Importante parar aqui em caso de erro
            }

        } else {
            flash.error("Restaurante não encontrado.");
            Logger.warn("Tentativa de alternar favorito para restaurante inexistente ID: %d", idRest);
        }

        // --- PONTO CRÍTICO ---
        // Redireciona DE VOLTA para a página principal após a ação
        Logger.info("Redirecionando para Gerenciamentos.principal().");
        Gerenciamentos.principal(); 
        // --- Garanta que NÃO HÁ um index() aqui ---
    }
}
