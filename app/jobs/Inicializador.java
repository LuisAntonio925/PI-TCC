// luisantonio925/projetopi-tcc/ProjetoPi-TCC-1f367715d58cce63ad3bf1ce68416d2a7aad77b1/app/jobs/Inicializador.java
package jobs;

import models.Cliente;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Inicializador extends Job {
	
	@Override
	public void doJob() throws Exception {

			
			Cliente joao = new Cliente();
			joao.nome = "João da Silva";
			joao.email = "joaossilva@gmail.com";
			joao.setSenha("1111"); // CORREÇÃO: Chama o setter para criptografar a senha.
			joao.nivel = "Perfil.CLIENTE";
			joao.save();
			
			Cliente Luiza = new Cliente();
			joao.nome = "João da Silva";
			joao.email = "Luiza@gmail.com";
			joao.setSenha("11111"); // CORREÇÃO: Chama o setter para criptografar a senha.
			joao.nivel = "Perfil.CLIENTE";
			joao.save();
            
			//fixo no mexe no perfil
			Cliente Maria = new Cliente();
			joao.nome = "João da Silva";
			joao.email = "Luiza@gmail.com";
			joao.setSenha("11111"); // CORREÇÃO: Chama o setter para criptografar a senha.
			joao.nivel = "Perfil.CLIENTE";
			joao.save();
		
		}
			
		

}
