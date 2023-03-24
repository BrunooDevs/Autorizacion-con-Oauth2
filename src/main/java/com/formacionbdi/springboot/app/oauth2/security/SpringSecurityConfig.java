package com.formacionbdi.springboot.app.oauth2.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter{
	
	/*UserDetailsService userDetailsService: aqui vamos a injectar a nuestra clase que se llama
	 * UsuarioService del paquete oauth2.service, como la anotamos con service spring va a ir a buscar
	 * ese componente que implemente UserDetailsService  y lo va ainjectar aqui en la variable 
	 * userDetailsService, este objeto obtendra nuestros usuarios y roles que obtenemos con Feig del 
	 * microservicio-usuarios mediante api rest*/
	
	@Autowired
	UserDetailsService userDetailsService;
					  
	@Autowired
	private AuthenticationEventPublisher authenticationEventPublisher;
	
	/*injectamos la clase AuthenticationSuccessErrorHandler del paquete security.event*/
	
	@Autowired
	private AuthenticationEventPublisher eventPublisher;
	
	/*AuthenticationManager authenticationManager: lo configuramos lo registramos como un compoenente de spring
	 * para que despues lo podamos utilizar injectar en el servidor de autorizacion de oauth2*/
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	/*con @Bean permite registrar objetos como componentes de spring, se va a guardar en el contenedor de 
	 * spring y lo podamos utilizar para encriptar nuestras * contraseñas
	 * NOTA: LO QUE ROTORNE EL METODO ES LO QUE SE VA A REGISTRAR COMO UN BEANS DE SPRING*/
	
	@Bean
	public static BCryptPasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
	}
	
	
	/*se pasa se injecta el objeto AuthenticationManagerBuilder por lo tanto se anota con @Autowired para que
	 * se pueda pasar e injectar en el metodo*/
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		/*registramos el userDetailsService que definimos arriba 
		 * a este metodo auth.authenticationEventPublisher y ademas encryptamos la contraseña para dale
		 * mas seguridad al password, cuando se inicie sesion se encryptara su contraseña@@*/
		auth.authenticationEventPublisher(authenticationEventPublisher).userDetailsService(userDetailsService)
		.passwordEncoder(passwordEncoder())
		.and()
		
		/*registramso nuestro evento eventPublisher que se definio arriba y posterior mente se creo en el paquete
		 * security.event*/
		.authenticationEventPublisher(eventPublisher)
		;
	}

}
