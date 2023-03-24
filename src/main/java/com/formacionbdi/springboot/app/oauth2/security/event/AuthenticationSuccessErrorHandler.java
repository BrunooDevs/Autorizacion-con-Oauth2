package com.formacionbdi.springboot.app.oauth2.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;
import com.formacionbdi.springboot.app.oauth2.services.IUsuarioService;

import brave.Tracer;
import feign.FeignException;


/*esta es una clase de evento se utiliza para manejar el exito y el fracaso en la autenticacion por ejemplo implementar
 * algun evento despues de que se aya iniciado sesion con exito o si falla la autenticacion realizar algun proceso */

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher{
	
	private static Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	/*obtenemos al usuario ´por el username utlizando la clase sevice  IUsuarioService */
	
	@Autowired
	private IUsuarioService usuarioService;
	
	@Autowired
	private Tracer tracer;
	
	

	/*public void publishAuthenticationSuccess(): es esta metodo manejamos el exito */
	
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		
		/*authentication.getDetails() instanceof WebAuthenticationDetails, indicamos que si el usuario no es nuestro
		 * sino en el cliet id es el frontedapp (ya que el evento muestra el mensage de logue del lado del cliente y 
		 * de lado de nuestro usuario de nuestra base de datos , pero solo queremos mostrar el mensaje cuando se logueo
		 * el de nuestro usuario no el cliente )*/
		if(authentication.getDetails() instanceof WebAuthenticationDetails) {
			return;
		}
		
		UserDetails user = (UserDetails) authentication.getPrincipal();
		System.out.println("Success Login" + user.getUsername());
		log .info("Success Login" + user.getUsername());
		
		/*aqui no necesitamos el try catch ya que hasta este punto el usuario ya esta autenticado*/
		
		/*con getName() o con getPrincipal obtenemos al usuario*/
		Usuario usuario = usuarioService.findByUsername(authentication.getName());
		
		if(usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
		}
		
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		
		String mensaje = "Error en el login: " + exception.getMessage();
		log.error("error en el login" + exception.getMessage());
		System.out.println("error en el login" + exception.getMessage());
		
		/*este try cath sirve para atrapar el error de que no  existe en usuario*/
		try {
			
			
			StringBuilder errors = new StringBuilder();
			errors.append(mensaje);
			
			/*con getName() o con getPrincipal obtenemos al usuario*/
			Usuario usuario = usuarioService.findByUsername(authentication.getName());
			
			/*preguntamos si intentos es igual a null, ya que si es null lanzara nullExceptionPointer, un error
			 * de que es nulo*/
			
			/*NOTA: CUNADO FALLA LA CONTRASEÑA SE EMPIEZAN A CONTAR LOS INTENTOS*/
			if(usuario.getIntentos() == null) {
				usuario.setIntentos(0);
			}
			
			
			log.info("Intentos actual es de " + usuario.getIntentos());
			usuario.setIntentos(usuario.getIntentos()+1);
			log.info("Intentos despues es de " + usuario.getIntentos());
			
			errors.append(" - Intentos del login: " + usuario.getIntentos());
			
			if(usuario.getIntentos() >=3 ) {
				String errorMaximoIntentos = String.format("El usuario %s des-habilitado por maximo intentos ",usuario.getUsername());
				log.error(errorMaximoIntentos );
				errors.append(" - " + errorMaximoIntentos);
				usuario.setEnabled(false);
			}
			
			usuarioService.update(usuario, usuario.getId());
			
			tracer.currentSpanCustomizer().tag("error.mensaje", errors.toString());
			
		} catch (FeignException e) {
			log.error(String.format("El usuario %s no existe en el sistema ",authentication.getName()));
		}
		
	}

}
