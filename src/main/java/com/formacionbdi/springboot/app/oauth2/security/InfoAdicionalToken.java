package com.formacionbdi.springboot.app.oauth2.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;
import com.formacionbdi.springboot.app.oauth2.services.IUsuarioService;

/*esta clase lo que hace es que agregamos informacion adicional a muestro token (agregamos los cleims)*/
/*lo anotamos como component para que lo podamos injectar en el AuthorizationServerConfig*/

@Component
public class InfoAdicionalToken implements TokenEnhancer{

	@Autowired
	private IUsuarioService usuarioService;
	
	
	/**OAuth2AccessToken enhance():potencia el token para agregarle nueva informacin*/
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

		Map<String, Object> info = new HashMap<String, Object>();
		
		/**el nomrbe de usuario lo obtenemos de  OAuth2Authentication authentication*/
		Usuario usuario = usuarioService.findByUsername(authentication.getName());
		info.put("nombre", usuario.getNombre());
		info.put("apellido", usuario.getApellido());
		info.put("correo", usuario.getEmail());
		
		/*aignamos esta info al accessToken, no podemos agregar directamente la informacion a ese objeto
		 * por la clase clase es muy generica para agregarlo utilizamos una clase concreta la clase
		 * */
		( (DefaultOAuth2AccessToken) accessToken ) .setAdditionalInformation(info);
				
		return accessToken;
	}
	
	

}
