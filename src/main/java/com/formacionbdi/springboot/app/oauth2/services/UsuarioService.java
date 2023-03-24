package com.formacionbdi.springboot.app.oauth2.services;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;
import com.formacionbdi.springboot.app.oauth2.clients.UsuarioFeignClient;

import brave.Tracer;
import feign.FeignException;

/*esta clase de servicio es propio de spring security, esta clase se configura en spring securtty para indicar
 * que el proceso de autenticacion se va a realizar de esta forma como esta en la clase*/


@Service
public class UsuarioService implements IUsuarioService,UserDetailsService{
	
	@Autowired
	private UsuarioFeignClient client;
	
	private final Logger logger = LoggerFactory.getLogger( UsuarioService.class);
	
	/*Tracer: agregamos un nuevo tag a la traza de zipkin*/
	
	@Autowired
	private Tracer tracer;
	

	/*public UserDetails loadUserByUsername: se encarga de autenticar de obtener al usuario por username
	 * NOTA: es independiente si estamos usando jpa, , jdbc o microservicios  */

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		/*el try lo usamos para que enves de que aparesca el mensaje 404 de feing aparece bad credentials cuando el
		 * username no existe o la contraseña*/
		try {
			
	/*mediante client, capturamos al usuario, cabe mencionar que aqui se comunica con el microservicio-usuarios
	 * se comunica mediante balanceo de carga enviando el username y obtenemos al usuario autenticado*/	
		Usuario usuario = client.findByUsername(username);

	
		/*obtenemos los roles pero del tipo de spring security son del tipo generico GrantedAuthority */
		List<GrantedAuthority> authorities = usuario.getRoles()
				.stream()
				/*convertimos los roles a los roles que usa spring security por cada rol lo convertimos un 
				 * tipo  SimpleGrantedAuthority(role.getNombre()*/
				.map(role -> new SimpleGrantedAuthority(role.getNombre()))
				
				/*mostramos por consola (logger) el rol del usuario*/
				.peek(authority -> logger.info("Role: " + authority.getAuthority()))
				/*volvemos a convertir a un tipo list el stream ya que seguia siendo uns tream al usar
				 * .stream*/
				.collect(Collectors.toList());
				
		logger.info("Usuario autenticado" + username);
				
		
		/*si existe el usuario retornamos la implementacion concreta
		 * NOTA: debemos de retornar un UserDetails es un tipo de interfaz que representa un usuario de spring
		 * -security un usuario autenticado pero retornamos la implementacion concreta new User*/
		return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, 
				true, true, authorities);
		
		} catch (FeignException e) {
			
			/*si no existe el usuario lanzamos una excepcion de Feign*/
			String error = "error en el login no existe el usuario" + username + "en el sistema";
			
			logger.error(error);
			
			/*agregamos al span actual un nuevo atributo un nuevo atributo de informacion que queramos exportar a zipkin*/
			tracer.currentSpan().tag("error.mensaje", error + ": " + e.getMessage());
			
			/*cuando no se encuentra um usuario lanzamos una exception*/
			throw new UsernameNotFoundException(error);
			
		}
		
	}

	
	
	
	@Override
	public Usuario findByUsername(String username) {
		return client.findByUsername(username);
		 
	}




	@Override
	public Usuario update(Usuario usuario, Long id) {
		return client.update(usuario, id);
		
	}
	
	
	
}
