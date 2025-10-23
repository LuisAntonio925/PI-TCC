package controllers;
import models.Status;

import java.util.List;

import models.Cliente;
import models.Restaurante;

// Import @Valid (não será usado na assinatura do salvar)
import play.data.validation.Valid;
// Import Validation para usar validation.clear(), validation.valid(), etc.
import play.data.validation.Validation;

import play.mvc.Controller;
import play.mvc.With;
import java.io.File;
import play.db.jpa.Blob; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import play.libs.MimeTypes;

@With(Seguranca.class)
public class Restaurantes extends Controller{
	
    /**
     * CORRIGIDO: Envia um objeto 'rest' vazio e a lista de clientes.
     */
	public static void formCadastrarRestaurante() {
		Restaurante rest = new Restaurante(); 
        List<Cliente> clientes = Cliente.findAll(); 
		renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
	}

	public static void parceiroRestApp() {
		render();
	}
	public static void principal() {
		render();
	}
	
	public static void listar2(String busca) {
		List<Restaurante> listaRest = null;
		if(busca == null || busca.trim().isEmpty()) { 
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
     * MÉTODO SALVAR - CORREÇÃO DEFINITIVA (Loop de Validação - Ordem Corrigida)
     * 1. REMOVIDO @Valid Restaurante rest da assinatura.
     * 2. ADICIONADO validation.clear() ANTES de qualquer validação.
     * 3. ADICIONADO validation.valid(rest) para correr a validação manualmente DEPOIS do clear.
     */
    // ANTES: public static void salvar(@Valid Restaurante rest, Long idCliente, File imagem)
	public static void salvar(Restaurante rest, Long idCliente, File imagem) { //
		
        // ---- ALTERAÇÃO PRINCIPAL NA ORDEM ----
        validation.clear(); // 1. Limpa erros antigos "grudentos" PRIMEIRO.
        // O Play já preencheu 'rest' com os dados do formulário.
        validation.valid(rest); // 2. Roda a validação automática (@Required, @MinSize, etc).
        // ------------------------------------
		
        // 3. Verifica os erros (do validation.valid) DESTA TENTATIVA
        if (validation.hasErrors()) {
            params.flash(); 
            validation.keep(); 

            List<Cliente> clientes = null;
	        if (rest.id != null) {
	             clientes = Cliente.find("?1 not member of restaurantes", rest).fetch();
	        } else {
	             clientes = Cliente.findAll();
	        }
            
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
                    rest.save(); // Salva mesmo assim
    				editar(rest.id); 
                    return; 
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

    /**
     * CORRIGIDO: Usa a variável 'rest' (não 'R').
     */
	public static void editar(long id) {
        Restaurante rest = Restaurante.findById(id); 
	    List<Cliente> clientes = Cliente.find("?1 not member of restaurantes", rest).fetch();
        renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes); 
	}

	// ... (resto dos seus métodos: remover, removerCliente, getImagem estão corretos) ...
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
			cli.restaurantes.remove(rest);
			cli.save(); 
            flash.success("Cliente desvinculado com sucesso."); 
		} else {
            flash.error("Erro ao tentar desvincular cliente.");
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