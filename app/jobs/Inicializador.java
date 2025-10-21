package jobs;

import models.Cliente;
import models.Perfil;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Inicializador extends Job {
	
	@Override
	public void doJob() throws Exception {

			
			
            
			//fixo no mexe no perfil
			Cliente Maria = new Cliente();
			Maria.nome = "João da Silva";
			Maria.email = "Luiza@gmail.com";
			Maria.setSenha("11111"); // CORREÇÃO: Chama o setter para criptografar a senha.
			Maria.perfil = Perfil.CLIENTE;
			Maria.save();

			Cliente pedro = new Cliente();
			pedro.nome = "Pedro Augusto";
			pedro.email = "admin@restapp.com";
			pedro.setSenha("12345");
			pedro.perfil = Perfil.ADMINISTRADOR;
			pedro.save();		
		}
			
		

}
