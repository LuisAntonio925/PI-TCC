
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
            // CORREÇÃO: Redireciona (forward) para a ação 'form' do controller 'Logins'.
            Logins.form(); // Chamada direta para um forward interno.
        }
        
        List<Restaurante> meusFavoritos = clienteConectado.restaurantes;

        List<Restaurante> outrosRestaurantes = Restaurante.find(
            "status = ?1 and ?2 not member of clientes", 
            Status.ATIVO, 
            clienteConectado
        ).fetch();

        render(meusFavoritos, outrosRestaurantes);
    }

    public static void alternarFavorito(Long idRest) {
    Cliente clienteConectado = Seguranca.getClienteConectado();
    // ... (verifica se está logado)

    Restaurante restaurante = Restaurante.findById(idRest);

    if (restaurante != null) {
        // Verifica se já está na lista
        if (clienteConectado.restaurantes.contains(restaurante)) {
            // Se estiver, remove
            clienteConectado.restaurantes.remove(restaurante);
            flash.success("'%s' foi removido dos seus favoritos.", restaurante.nomeDoRestaurante);
        } else {
            // Se não estiver, adiciona
            clienteConectado.restaurantes.add(restaurante);
            flash.success("'%s' foi adicionado aos seus favoritos!", restaurante.nomeDoRestaurante);
        }

        clienteConectado.save(); // Salva a alteração na lista de favoritos do cliente
        // File: app/controllers/Favoritos.java

// ... (método alternarFavorito)

    if (restaurante != null) {
        // ... (lógica de adicionar/remover e salvar)
        clienteConectado.save();
    }
    // CORREÇÃO: Agora volta para a página principal (Gerenciamentos.principal)
    Gerenciamentos.principal();
}
index(); // Volta para a página de favoritos
    }

    
}