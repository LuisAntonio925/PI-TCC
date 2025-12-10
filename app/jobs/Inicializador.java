package jobs;

import models.Cliente;
import models.Perfil;
import models.Status;
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
			Maria.status = Status.ATIVO;
			Maria.save();

			Cliente pedro = new Cliente();
			pedro.nome = "Pedro Augusto";
			pedro.email = "admin@restapp.com";
			pedro.setSenha("12345");
			pedro.perfil = Perfil.ADMINISTRADOR;
			pedro.status = Status.ATIVO;
			pedro.save();	
			
			Cliente luana = new Cliente();
			luana.nome = "luana";
			luana.email = "admin26@restapp.com";
			luana.setSenha("1234567");
			luana.perfil = Perfil.ADMINISTRADOR;
			luana.status = Status.ATIVO;
			luana.save();	
			
			Cliente carlos = new Cliente();
			carlos.nome = "carlos";
			carlos.email = "proprietario@restapp.com";
			carlos.setSenha("987654321");	
			carlos.perfil = Perfil.PROPRIETARIO;
			carlos.status = Status.ATIVO;
			carlos.save();	
	
		}
			
		

}
