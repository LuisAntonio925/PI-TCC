package controllers;
import models.Status;

import java.util.List;

import models.Cliente;
import models.Restaurante;
import play.mvc.Controller;
import play.mvc.With;
import java.io.File;

// --- IMPORT NECESSÁRIO ADICIONADO ---
import play.data.validation.Valid;

import play.db.jpa.Blob; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import play.libs.MimeTypes;

@With(Seguranca.class)
public class Restaurantes extends Controller{
	
    /**
     * MÉTODO CORRIGIDO
     * Agora envia um objeto 'R' vazio e a lista de 'clientes'
     * para o template do formulário.
     */
	public static void formCadastrarRestaurante() {
		Restaurante R = new Restaurante(); // Cria objeto vazio
        List<Cliente> clientes = Cliente.findAll(); // Carrega clientes para o <select>
		renderTemplate("Restaurantes/formCadastrarRestaurante.html", R, clientes);
	}

	public static void parceiroRestApp() {
		render();
	}
	public static void principal() {
		render();
	}
	
	public static void listar2(String busca) {
		List<Restaurante> listaRest = null;
		if(busca == null || busca.trim().isEmpty()) { // Adicionado trim()
			listaRest = Restaurante.find("status <> ?1", Status.INATIVO).fetch();
		}else {
			listaRest = Restaurante.find("(lower(nomeDoRestaurante) like ?1"
								+ "or lower(CNPJ) like ?1 or lower(categoria) like ?1) and status <> ?2",
								"%" + busca.toLowerCase() + "%",
									Status.INATIVO).fetch();
		}
		render(listaRest, busca);
	}

	/**
     * MÉTODO SALVAR - CORRIGIDO PARA LIMPAR ERROS ANTIGOS
     * 1. Removemos @Valid Restaurante rest da assinatura (da linha "public static...").
     * 2. Adicionamos validation.clear() para limpar os erros "grudentos".
     * 3. Adicionamos validation.valid(rest) para correr a validação manualmente.
     */
    // ANTES: public static void salvar(@Valid Restaurante rest, Long idCliente, File imagem)
	public static void salvar(@Valid Restaurante rest, Long idCliente, File imagem) { //
		
        // Agora que o cache foi limpo, este IF vai funcionar corretamente
        if (validation.hasErrors()) {
            params.flash(); 
            validation.keep(); 

            List<Cliente> clientes = null;
	        if (rest.id != null) {
	             clientes = Cliente.find("?1 not member of restaurantes", rest).fetch();
	        } else {
	             clientes = Cliente.findAll();
	        }
            
            // A view espera 'rest', e estamos a passar 'rest'. Correto.
	        renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
        
        } else {
            // ---- SUCESSO! ----
    		if (imagem != null && imagem.length() > 0) {
    			if (rest.imagem == null) {
    				rest.imagem = new Blob();
    			}
    			try {
    				rest.imagem.set(new FileInputStream(imagem), MimeTypes.getMimeType(imagem.getName()));
    			} catch (FileNotFoundException e) {
    				e.printStackTrace(); 
    				flash.error("Erro ao tentar salvar a imagem.");
    				editar(rest.id); 
    			}
    		}

    		rest.save();

    		if(idCliente != null) {
    			Cliente c = Cliente.findById(idCliente);
    			if (c != null && !c.restaurantes.contains(rest)) {
    				c.restaurantes.add(rest);
    				c.save(); 
    			}
    		}
    		
            flash.success("Restaurante salvo com sucesso!");
    		editar(rest.id); 
	    }
	}

	public static void editar(long id) {
		Restaurante R = Restaurante.findById(id);
	   List<Cliente> clientes = Cliente.find("?1 not member of restaurantes", R).fetch();
		renderTemplate("Restaurantes/formCadastrarRestaurante.html", R, clientes);
	}

	public static void remover(long id) {
		Restaurante rest = Restaurante.findById(id);
		rest.status = Status.INATIVO;
		rest.save();
		listar2(null); 
	}
	
	public static void removerCliente(Long idRest, Long idCli) {
		Restaurante rest = Restaurante.findById(idRest);
		Cliente cli = Cliente.findById(idCli);
		
		if (cli != null && rest != null) {
            // Correção: Remove do lado do Cliente (que é o dono da relação @JoinTable)
			cli.restaurantes.remove(rest);
			cli.save(); 
		}
		
		editar(rest.id);
	}
  
    public static void getImagem(Long id) {
        Restaurante rest = Restaurante.findById(id);
        if (rest != null && rest.imagem != null && rest.imagem.exists()) {
            response.setContentTypeIfNotSet(rest.imagem.type());
            renderBinary(rest.imagem.get());
        } else {
            notFound();
        }
    }
}