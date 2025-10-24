package controllers;

import java.util.List;

// Import @Valid (não será usado na assinatura do salvar)
import play.data.validation.Valid;

import models.Cliente;
import models.Restaurante;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
// Import Validation para usar validation.clear(), validation.valid(), etc.
import play.data.validation.Validation;

@With(Seguranca.class)
public class Gerenciamentos extends Controller {
	
	// ... (outros métodos como ListasDeGerenciamentos, formCadastro, etc.) ...

	public static void principal() {
		Cliente clienteConectar = null; // Começa como nulo
		Long clienteId = null; // Variável para guardar o ID

		// --- GARANTE A BUSCA ATUALIZADA DO CLIENTE ---
		if (session.contains("clienteId")) {
			try {
				clienteId = Long.parseLong(session.get("clienteId"));
				// Busca o cliente do banco de dados *nesta* requisição
				clienteConectar = Cliente.findById(clienteId);

				// (...) Verificação Adicional Opcional

			} catch (NumberFormatException e) {
				Logins.logout(); // Exemplo: força logout
			} catch (Exception e) {
				play.Logger.error(e, "Erro ao buscar cliente em Gerenciamentos.principal");
				Logins.logout(); // Ou outra tratativa
			}
		}
		// ----------------------------------------------

		if (clienteConectar == null && !session.contains("clienteId")) { // Verifica se NÃO está logado E não deu erro antes
			// Se não conseguiu buscar o cliente logado, volta para o login
			Logins.form();
			return; // Adiciona return para parar a execução aqui
		}
		// Se clienteConectar for null APESAR de ter clienteId na sessão,
		// pode ser um erro interno ou ID inválido que já foi tratado acima (logout).
		// Prossegue para o render, mas o menu não mostrará o nome.

		List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();

		// ----- CORREÇÃO AQUI -----
		// Renomeia a variável ao passar para a view
		Cliente clienteAtual = clienteConectar;
		render(restaurantes, clienteAtual);
		// -------------------------

	}

	// ... (resto dos seus métodos: formCadastro, listar, editar, salvar, removerRestaurante, remover) ...
	public static void ListasDeGerenciamentos(){
	 render();
 }

 public static void formCadastro() {
	 Cliente cli = new Cliente();
	 List<Restaurante> restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch();
	 renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
 }
 public static void listar(String termo) {
	 List<Cliente> listaClientes = null;
	 if (termo == null || termo.trim().isEmpty()) {
		 listaClientes = Cliente.find("status <> ?1", Status.INATIVO).fetch();
	 } else {
		 listaClientes = Cliente.find("(lower(nome) like ?1 or lower(email) like ?1) and status <> ?2",
							   "%" + termo.toLowerCase() + "%",
							   Status.INATIVO).fetch();
	 }
	 render(listaClientes, termo);
 }
 public static void editar(long id) {
	 Cliente cli = Cliente.findById(id);
	 List<Restaurante> restaurantesDisponiveis = Restaurante.find(
		 "status = ?1 and ?2 not member of clientes",
		 Status.ATIVO,
		 cli
	 ).fetch();
	 renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);
 }

 public static void salvar(Cliente cli, String senha, Long idRestaurante) {


	 // ---- ALTERAÇÃO PRINCIPAL NA ORDEM ----
	 validation.clear(); // 1. Limpa TODOS os erros antigos PRIMEIRO.

	 // O Play já preencheu 'cli' com os dados do formulário automaticamente.
	 validation.valid(cli); // 2. Roda a validação automática (@Required, @MinSize, etc).

	 // 3. Roda a validação manual da senha AGORA.
	 if (cli.id != null) {
		 // Edição
		 Cliente clienteDoBanco = Cliente.findById(cli.id);
		 if (senha == null || senha.trim().isEmpty()) {
			   // Mantém a senha antiga se o campo veio vazio
			   cli.senha = clienteDoBanco.senha;
		 } else {
			   // Define a nova senha (será criptografada pelo setSenha)
			   cli.setSenha(senha);
		 }
		 // Na edição, a senha não é obrigatória, então não adicionamos erro se estiver vazia.
	 } else {
		 // Novo cliente: Senha é obrigatória
		 if (senha == null || senha.trim().isEmpty()) {
			 // Adiciona o erro da senha AQUI, depois do clear e valid
			 validation.addError("senha", "O campo Senha e obrigatorio");
		 } else {
			   // Define a senha (será criptografada)
			   cli.setSenha(senha);
		 }
	 }
	 // ---- FIM DA ALTERAÇÃO NA ORDEM ----

	 // 4. Verifica os erros (do valid E do addError) DESTA TENTATIVA
	 if(validation.hasErrors()) {
		 params.flash(); // Mantém os dados digitados (nome, email...) nos campos
		 validation.keep(); // Guarda os erros atuais para mostrar no render

		 // Recarrega a lista de restaurantes necessária para o <select>
		 List<Restaurante> restaurantesDisponiveis = null;
		 if (cli.id != null) {
			  restaurantesDisponiveis = Restaurante.find(
				 "status = ?1 and ?2 not member of clientes",
				 Status.ATIVO,
				 cli
			 ).fetch();
		 } else {
			  restaurantesDisponiveis = models.Restaurante.find("status = ?1", Status.ATIVO).fetch();
		 }

		 // Renderiza o formulário de novo, mostrando os erros atuais
		 renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);

	 } else {
		 // ---- SUCESSO! ----
		 if (idRestaurante != null) {
			 Restaurante rest = Restaurante.findById(idRestaurante);
			 if (rest != null && !cli.restaurantes.contains(rest)) {
				 cli.restaurantes.add(rest);
			 }
		 }

		 cli.save();
		 flash.success("Cliente salvo com sucesso!");
		 editar(cli.id);
	 }
 }


  public static void removerRestaurante(Long idCli, Long idRest) {
	 Cliente cli = Cliente.findById(idCli);
	 Restaurante rest = Restaurante.findById(idRest);
	 if (cli != null && rest != null) {
		  cli.restaurantes.remove(rest);
		  cli.save();
		  flash.success("Vinculo com o restaurante '%s' foi removido.", rest.nomeDoRestaurante);
	 } else {
		  flash.error("Ocorreu um erro ao tentar remover o vinculo.");
	 }
	 editar(idCli);
 }
 public static void remover(long id) {
	 Cliente cli = Cliente.findById(id);
	 cli.status = Status.INATIVO;
	 cli.save();
	 listar(null);
 }
}