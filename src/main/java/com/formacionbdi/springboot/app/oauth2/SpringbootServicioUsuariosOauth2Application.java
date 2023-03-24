package com.formacionbdi.springboot.app.oauth2;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*Para la ultima versión Spring boot versión 3.0.1 se debe usar la versión de Spring cloud 2022.0.0 y jdk 17
no admite versiones anteriores, ademas se deben quitar estos dos anotaciones en la clase del main //@EnableCircuitBreaker

//@EnableEurekaClient*/

/*@EnableFeignClients: esta anotacion habilita nuestros cliente feign que tengamos implementado en nuestro
 * proyecto y ademas nos permite injectar estos clientes en nuestros controladores u otros componentes de spring
 * por ejemplo en una clase service habilita la injeccion de dependencias (Autowired)
 * NOTA SE DEBE DE AGREGAR LA DEPENDENDIA EN EL POW DE FEING*/

/*la ruta para la autenticacion de neustro segvidor sera /oauth/token para generar el token y podernos 
 * autenticarnos
 * 
  ademas se tiene que configurar la autenticacion del cliente id con la palabra secreta eso se configura
  en el Authorization en type le ponemos Basic Auth, y ponemos las credenciales que pusimos en la configuracion
  AuthorizationServerConfig que fueron frontedapp y contraseña 12345 desde postman
  
  y en el body de la peticion tenemos que selecionar x-www-form-urlencode y debemos de poner el key de
  username:  = usuario
  password: = contraseña
  grant_type = password: tipo de consecion de nuestra autenticacion seria del tipo password ya que cambiamos las
           credenciales del usuario y su contraseña por el token
  */

@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
public class SpringbootServicioUsuariosOauth2Application implements CommandLineRunner{

	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	
	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioUsuariosOauth2Application.class, args);
	}

	/*dentro de este metodo vamos a encryptar las contraseñas, para eso necesitamos el password encode*/
	
	@Override
	public void run(String... args) throws Exception {
		
		String password = "12345";
		
		for (int i = 0; i< 4;i++) {
			
			/*passwordBCryp: es la contraseña pero ya cifrada, lo que hace potente a bcryp es que podemos
			 * crear varias contraseñas apartir de un solo string o contraseña*/
			String passwordBCryp = passwordEncode.encode(password);
			System.out.println(passwordBCryp);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
