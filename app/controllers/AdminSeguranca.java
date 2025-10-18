package controllers;

import models.Perfil;
import play.mvc.Before;
import play.mvc.Controller;
import controllers.Restaurantes;

/**
 * Esta classe de segurança verifica duas coisas:
 * 1. O usuário está logado (herdado de Seguranca.class).
 * 2. O usuário logado tem o perfil de ADMINISTRADOR.
 */
public class AdminSeguranca extends Seguranca {

    @Before
    static void verificarAdmin() {
        // 1. Verifica se está logado (chama o @Before da classe Pai 'Seguranca')
        // Se não estiver, já será redirecionado para Logins.form()
        // NOTA: Assumindo que o método em Seguranca.java se chama verificarAutenticacao()
        // Se o nome for diferente (como 'verificar()'), ajuste aqui.
        verificarAutenticacao();

        // 2. Se está logado, verifica o perfil
        String perfilSessao = session.get("cliente.perfil");

        // Se o perfil não for ADMINISTRADOR, proíbe o acesso.
        if (!Perfil.ADMINISTRADOR.name().equals(perfilSessao)) {
            flash.error("Acesso negado. Você não tem permissão de administrador.");
            // Redireciona para a home de clientes
            Restaurantes.listar2(null);
        }
    }
}