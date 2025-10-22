package controllers;
import models.Status;

import java.util.List;

import models.Cliente;
import models.Restaurante;
import play.mvc.Controller;
import play.mvc.With;
import java.io.File;

// --- IMPORTS PARA O UPLOAD ---
import play.db.jpa.Blob; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import play.libs.MimeTypes;
// -----------------------------

@With(Seguranca.class)
public class Restaurantes extends Controller{
	
	// Seus métodos formCadastrarRestaurante, parceiroRestApp, principal permanecem iguais
	public static void formCadastrarRestaurante() {
		render();
	}
	public static void parceiroRestApp() {
		render();
	}
	public static void principal() {
		render();
	}
	
	// Seu método listar2 está correto, não mexa nele
	public static void listar2(String busca) {
		List<Restaurante> listaRest = null;
		if(busca == null) {
			listaRest = Restaurante.find("status <> ?1", Status.INATIVO).fetch();
		}else {
			listaRest = Restaurante.find("(lower(nomeDoRestaurante) like ?1"
								+ "or lower(CNPJ) like ?1 or lower(categoria) like ?1) and status <> ?2",
								"%" + busca.toLowerCase() + "%",
									Status.INATIVO).fetch();
		}
		render(listaRest, busca);
	}

	// --- MÉTODO SALVAR CORRIGIDO ---
	// 1. Assinatura correta (com rest, idCliente e imagem)
	public static void salvar(Restaurante rest, Long idCliente, File imagem) { 
		
		// 2. Lógica de upload CORRIGIDA (com FileInputStream)
		if (imagem != null && imagem.length() > 0) {
			if (rest.imagem == null) {
				rest.imagem = new Blob();
			}
			
			try {
                // 3. Esta é a linha correta, usando InputStream
				rest.imagem.set(new FileInputStream(imagem), MimeTypes.getMimeType(imagem.getName()));

			} catch (FileNotFoundException e) {
				e.printStackTrace(); 
				flash.error("Erro ao tentar salvar a imagem.");
				editar(rest.id); // Se der erro, volta para a edição
			}
		}

		// Salva o restaurante (com ou sem imagem)
		rest.save();

		// Mantém sua lógica original de associar o cliente
		if(idCliente != null) {
			Cliente c = Cliente.findById(idCliente);
			if (c != null && !c.restaurantes.contains(rest)) {
				c.restaurantes.add(rest);
				c.save(); 
			}
		}
		
		// 4. Redirecionamento CORRETO (o que você usava antes)
		// Isso corrige o erro do "listar2 vermelho"
		editar(rest.id); 
	}
	// --- FIM DO MÉTODO SALVAR ---

	public static void editar(long id) {
		Restaurante R = Restaurante.findById(id);
		// Busca clientes que AINDA NÃO estão associados a este restaurante
	   List<Cliente> clientes = Cliente.find("?1 not member of restaurantes", R).fetch();
		renderTemplate("Restaurantes/formCadastrarRestaurante.html", R, clientes);
	}

	public static void remover(long id) {
		Restaurante rest = Restaurante.findById(id);
		rest.status = Status.INATIVO;
		rest.save();
		
		listar2(null); // Esta chamada está correta
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
  
     /**
     * Este método é chamado pela rota '/restaurante/{id}/imagem'
     * para servir (renderizar) a imagem do Blob.
     */
    public static void getImagem(Long id) {
        Restaurante rest = Restaurante.findById(id);

        // Verifica se o restaurante existe E se ele tem uma imagem
        if (rest != null && rest.imagem != null && rest.imagem.exists()) {
            
            // 'renderBinary' envia os dados brutos da imagem para o navegador
            // O segundo parâmetro é o "tipo" do arquivo (ex: image/png, image/jpeg)
            // que está salvo no Blob.
            response.setContentTypeIfNotSet(rest.imagem.type());
            renderBinary(rest.imagem.get());

        } else {
            // Se não houver restaurante ou imagem, retorna "Não Encontrado"
            notFound();
        }
    }
    

}