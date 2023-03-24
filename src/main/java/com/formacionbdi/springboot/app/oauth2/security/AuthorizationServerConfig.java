package com.formacionbdi.springboot.app.oauth2.security;

import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**/

/*@EnableAuthorizationServer: habilitamos esta clase como un servidor de autenticacion que se encarga de todo
 * el proceso de login por el lado de oauth2*/

/*@RefreshScope: nos permite actualizar los componentes como contorladores, clases que le estamos injectando
 * con la anotacion @Value tambien el Environment cuando hacemos un cambio desde el servidor de configuracion o desde
 * el repositorio, actualiza se refresca el contexto vuelve a injectar 
 * y se vuelve a inicializar el componente con los cambios reflejados en tiempo real y sin tener que reiniciar
 * la aplicacion y todo esto mediante una ruta url un edpoint de actuator
 * 
 * NOTA: SE DEBE AGREGAR LA DEPENCIA Ops/Spring boot actuator */


@RefreshScope
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{
	
	/*injectamos dos atributos de clase SpringSecurityConfig*/
	
	/*BCryptPasswordEncoder esta en al clase SpringSecurityConfig anotada como un bean por lo tanto se puede
	 * injectar aqui*/
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	/*AuthenticationManager esta en al clase SpringSecurityConfig anotada como un bean por lo tanto se puede
	 * injectar aqui*/
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private InfoAdicionalToken infoAdicionalToken;
	
	@Autowired
	private Environment env;
	
	
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		
		/*configuramos ClientDetailsServiceConfigurer es el permiso que va a tener nuestros endpoints
		   * del servidor de autorizacion de oauth2 para generar el token y tambien para validar el token*/
		
		/*
		 * tokenKeyAccess: justamente es el endpoint para generar el token para autenticarnos con la ruta
		   /oauth/token del tipo POST, enviamos las credenciales del usuario y tambien las credenciales 
		    del cliente de la aplicacion se encargar de validar y autenticarnos
		    
		 *permitrAll(): es un permiso de spring security para permitir a todo, la idea que cualquier persona
		 *pueda acceder a esta ruta para generar el token
		 */
		security.tokenKeyAccess("permitAll()")
		/*checkTokenAccess: validamos nuestro token, esta ruta para validar nuestro token requiere 
		 * autenticacion para eso ponemos isAuthenticated() es un metodo de spring security que nos permite
		 * que el cliente este autenticado */
		.checkTokenAccess("isAuthenticated()");
		
		/*NOTA ESTOS DOS endpoint estan protegidos por Authentication via HTTP Basic utlizando las 
		 * credenciales del cliente es decir de la aplicacion se envian el cliente_id con su secreto 
		 * cliente_secret  */
		
	}

	
	/*public void configure(ClientDetailsServiceConfigurer clients): configuramos a nuestros clientes que
	 * van acceder a nuestros microservicios, puede ser una aplicacion con angular o con react o
	 * en android, si tubieramos varios clientes varias aplicaciones clientes que consumen nuestros
	 * servicios tenemos que registrar uno a uno estos clientes con sus id y su contraseña */
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		
		/*frontedapp: representa el identificador de nuestro cliente
		 * secret: representa una contraseña
		 * passwordEncoder.encode: encriptamos nuestra contraseña
		 
		 *env.getProperty vamos a obtener la configuracion que se encuestra en nuestro servidor de configuracion 
		 *desde git  
		 *NOTA: EN GIT TENEMOS UN ARCHIVO LLAMADO application.properties HAY ESTAN ESTAS CONFIGURACION CON NOMBRE 
		 *config.security.oauth.client.id Y config.security.oauth.client.secret CON SUS RESPECTIVOS VALORES*/
		clients.inMemory().withClient(env.getProperty("config.security.oauth.client.id"))
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		
		/*scopes:son permisos que se les da a los clientes o aplicaciones,, 
		 * configuramos el alcanze de la aplicacion (es el permiso de la aplicacion cliente)
		 * por eemplo puede ser de lectura con read, write, */
		.scopes("read","write")
		
		/*authorizedGrantTypes: basicamente como va hacer el tipo de autenticacion, como vamos a obtener el
		   token en nuestro caso con password, utilizamos password cuando es con credenciales, es cuando
		   nuestros usuarios existen en nuestro sistema de backend 
		 * refresh_token: es un token que nos permite obtener un nuevo token un token de acceso completamente
		   renovado , tipicamente se utiliza para obtener un nuevo token justo antes de que caduque el token
		   actual
		 * 
		  */
		.authorizedGrantTypes("password","refresh_token")
		
		/*accessTokenValiditySeconds: configuramos el tiempo de validez del token antes de que caduque
		 * indicamos un entero en segundos*/
		.accessTokenValiditySeconds(3600)
		
		/*refreshTokenValiditySeconds: tiempo del refresh_token*/
		  .refreshTokenValiditySeconds(3600)
		  
		  
	       /*NOTA: and() lo usariamos si queremos a gregar otro cliente, por ejemplo un cliente desde android
	         que quieran infresar a nuestro sistema
	         
		    .and()
		    .withClient("androidapp")
			.secret(passwordEncoder.encode("android123"))
			.scopes("read","write")
			.authorizedGrantTypes("password","refresh_token")
			.accessTokenValiditySeconds(3600)
			.refreshTokenValiditySeconds(3600)
		  
		    */
		  
		  
		;
	}

	
	/*el authenticationManager lo debemos de registrar en el AuthorizationServer,esta configuracion que 
	 *  recibe AuthorizationServerEndpointsConfigurer esta relacionado al endpoint de oauth2 del servidor 
	 *  de autorizacion que se encarga de generar el token el endpoint es /oauth/token y la peticion es de
	 *  tipo POST, que recibe el username el password 
	  */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		/*unimos los datos de AccessTokenConverter() con los que tenemos en la clase InfoAdicionalToken
		 * en esta clase la injectamos la clase InfoAdicionalToken ya que la registramos como un componente
		 *  por que la utiliza el metodo tokenEnhancerChain.setTokenEnhancers()
		 
		 * TokenEnhancerChain : unimos los datos del token
		 * el parametro AccessTokenConverter() son los datos por defecto*/
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(infoAdicionalToken,AccessTokenConverter()));
		
		
		/*registramos el autenticationmanager que emos injectado*/
		endpoints.authenticationManager(authenticationManager)
		
		/*.tokenStore(TokenStore()
		 * se encarga de guardar el token de generar el token con los datos del AccessTokenConverter()*/
		.tokenStore( TokenStore() )
		
		/*.accessTokenConverter: configuramos el toquen para que sea de tipo jwt, llamamos a AccessTokenConverter()*/
		.accessTokenConverter(AccessTokenConverter() )
		
		/*agregamos el  tokenEnhancerChain que definimos en este metodocon la informacion adicional a 
		 * nuestro token*/
		.tokenEnhancer(tokenEnhancerChain)
		
		;
	}

	/*JWTAccessTokenConverter es del tipo concreto y no AccessTokenConverter*/
	@Bean
	public JwtAccessTokenConverter AccessTokenConverter() {
		
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		/*tokenConverter.setSigningKey
		 * agregamos un codigo secreto para firmar el token tiene que ser unico despues este mismo lo vamos
		 * a utilizar en el servidor de recursos para validar el token que sea el correcto con la misma firma 
		 * y asi dar accesoa los clientes */
		
		/*convertimos el llaveJwt primero a base64 (la llave se validara en base64)
		 * setSigningKey recibe un string y no bytes, por eso invocamos el metodo encodeToString, y asu vez el metodo
		 *encodeToString recibe bytes, entonces por eso convertimos  env.getProperty("config.security.oauth.jwt.key").getBytes() 
		 *a bytes*/
		tokenConverter.setSigningKey(Base64.getEncoder().encodeToString(env.getProperty("config.security.oauth.jwt.key").getBytes()));
		
		return tokenConverter;
	}
	
	
	@Bean
	public JwtTokenStore TokenStore() {
	
		/*JwtTokenStore(): recibe por argumento el tokenConverter del metodo AccessTokenConverter()
		  el TokenStore para poder crear el token y almacenarlo necesitamos el componente que se encarga
		  de convertir el token con todos los datos con toda la informacion como puede ser
		  username, roles y con cualquier informacion que queramos almacenar*/
		return new JwtTokenStore( AccessTokenConverter() );
	}

	
	
	
	
	
	
	
	
	
	
	
	

}
