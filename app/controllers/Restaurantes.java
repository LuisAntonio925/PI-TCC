package controllers;
import models.Status;

import java.util.List;

import models.Cliente;
import models.Restaurante;

// Import @Valid (embora não seja usado na assinatura do salvar)
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
     * ALTERADO:
     * Envia um objeto 'rest' vazio e a lista de clientes.
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
     * MÉTODO SALVAR - CORREÇÃO DEFINITIVA (Loop de Validação)
     * 1. Removemos @Valid Restaurante rest da assinatura.
     * 2. Adicionamos validation.clear() ANTES de qualquer validação.
     * 3. Adicionamos validation.valid(rest) para correr a validação manualmente.
     */
    // ANTES: public static void salvar(@Valid Restaurante rest, Long idCliente, File imagem)
	public static void salvar(Restaurante rest, Long idCliente, File imagem) { //
		
        // ---- ALTERAÇÃO PRINCIPAL ----
        validation.clear(); // 1. Limpa erros antigos "grudentos"
        validation.valid(rest); // 2. Valida o objeto 'rest' que o Play preencheu
        // -----------------------------
		
        // Verifica os erros do validation.valid(rest)
        if (validation.hasErrors()) {
            params.flash(); // Mantém os dados digitados nos campos
            validation.keep(); // Guarda os erros DESTA tentativa para mostrar no render

            // Recarrega a lista de clientes para o <select>
            List<Cliente> clientes = null;
	        if (rest.id != null) {
	             clientes = Cliente.find("?1 not member of restaurantes", rest).fetch();
	        } else {
	             clientes = Cliente.findAll();
	        }
            
            // Renderiza o formulário de novo, mostrando os erros atuais
	        renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes);
        
        } else {
            // ---- SUCESSO! ----
            // Só entra aqui se NENHUM erro ocorreu nesta tentativa

            // Lógica de upload de imagem
    		if (imagem != null && imagem.length() > 0) {
    			if (rest.imagem == null) {
    				rest.imagem = new Blob();
    			}
    			try {
    				rest.imagem.set(new FileInputStream(imagem), MimeTypes.getMimeType(imagem.getName()));
    			} catch (FileNotFoundException e) {
    				e.printStackTrace(); 
    				flash.error("Erro ao tentar salvar a imagem.");
    				// Mesmo com erro na imagem, vamos para a edição para não perder outros dados
                    // Mas salvamos o restaurante antes
                    rest.save(); 
    				editar(rest.id); 
                    return; // Importante sair aqui para não executar o resto
    			}
    		}

    		rest.save(); // Salva o restaurante (com ou sem imagem nova)

            // Lógica de associar cliente
    		if(idCliente != null) {
    			Cliente c = Cliente.findById(idCliente);
    			if (c != null && !c.restaurantes.contains(rest)) {
    				c.restaurantes.add(rest);
    				c.save(); 
    			}
    		}
    		
            flash.success("Restaurante salvo com sucesso!");
    		editar(rest.id); // Redireciona para a edição
	    }
	}

    /**
     * ALTERADO:
     * Usa a variável 'rest' (não 'R') para ser consistente.
     */
	public static void editar(long id) {
        Restaurante rest = Restaurante.findById(id); 
	    List<Cliente> clientes = Cliente.find("?1 not member of restaurantes", rest).fetch();
        renderTemplate("Restaurantes/formCadastrarRestaurante.html", rest, clientes); 
	}

	// ... (resto dos seus métodos: remover, removerCliente, getImagem) ...
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