package controllers;

import java.util.List;
import java.util.Collections; // Necessário para sort
import java.util.Comparator;  // Necessário para sort
import models.Cliente;
import models.Restaurante;
import models.Status;
import models.Perfil; 
import play.mvc.Controller;
import play.mvc.With;
import play.data.validation.Valid;

@With(Seguranca.class)
public class Gerenciamentos extends Controller {

    public static void principal() {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        List<Restaurante> restaurantes = models.Restaurante.find("status = ?1", models.Status.ATIVO).fetch();
        render(restaurantes, clienteConectado);
    }

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
        validation.clear();
        validation.valid(cli);

        if (cli.id != null) {
            Cliente clienteDoBanco = Cliente.findById(cli.id);
            if (senha == null || senha.trim().isEmpty()) {
                cli.senha = clienteDoBanco.senha;
            } else {
                cli.setSenha(senha);
            }
        } else {
            if (senha == null || senha.trim().isEmpty()) {
                validation.addError("senha", "O campo Senha e obrigatorio");
            } else {
                cli.setSenha(senha);
            }
        }

        if(validation.hasErrors()) {
            params.flash();
            validation.keep();

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

            renderTemplate("Gerenciamentos/formCadastro.html", cli, restaurantesDisponiveis);

        } else {
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

    // --- CADASTRO DE PROPRIETÁRIO ---

    public static void formCadastroProprietario() {
        Cliente cli = new Cliente();
        renderTemplate("Gerenciamentos/formCadastroProprietario.html", cli);
    }

    public static void salvarProprietario(Cliente cli, String senha) {
        if (senha != null && !senha.trim().isEmpty()) {
            cli.setSenha(senha);
        } else {
            flash.error("A senha é obrigatória.");
            validation.keep();
            formCadastroProprietario();
            return;
        }

        cli.status = Status.ATIVO;
        cli.perfil = Perfil.PROPRIETARIO;
        cli.save();

        session.put("clienteId", cli.id);
        flash.success("Bem-vindo(a), %s! Seu cadastro de proprietário foi realizado.", cli.nome);
        principal();
    }

    // --- GEOLOCALIZAÇÃO E ORDENAÇÃO (NOVO) ---
    
    // Método chamado pelo botão "Perto de Mim"
    public static void listarPorDistancia(final Double lat, final Double lon) {
        Cliente clienteConectado = Seguranca.getClienteConectado();
        
        // 1. Pega todos os restaurantes ativos
        List<Restaurante> restaurantes = Restaurante.find("status = ?1", Status.ATIVO).fetch();

        // 2. Se o cliente enviou o GPS, ordena a lista
        if (lat != null && lon != null) {
            Collections.sort(restaurantes, new Comparator<Restaurante>() {
                public int compare(Restaurante r1, Restaurante r2) {
                    // Joga pro final quem não tem GPS cadastrado
                    if(r1.latitude == null) return 1; 
                    if(r2.latitude == null) return -1;

                    // Calcula distâncias
                    double dist1 = calcularHaversine(lat, lon, r1.latitude, r1.longitude);
                    double dist2 = calcularHaversine(lat, lon, r2.latitude, r2.longitude);
                    
                    // Ordena do menor para o maior (mais perto primeiro)
                    return Double.compare(dist1, dist2);
                }
            });
            // REMOVIDO: flash.success("Restaurantes ordenados por proximidade!");
        } else {
            flash.error("Não foi possível obter sua localização.");
        }

        // 3. Renderiza a tela principal com a lista já ordenada
        render("Gerenciamentos/principal.html", restaurantes, clienteConectado);
    }

    // Fórmula matemática para calcular distância em KM
    private static double calcularHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em KM
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


