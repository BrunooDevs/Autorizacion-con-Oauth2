package com.formacionbdi.springboot.app.oauth2.clients;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.formacionbdi.springboot.app.commons.usuarios.models.entity.Usuario;

/*@FeignClient: con esta anotacion se define se declara que esta interface es un cliente Feign 
 * name = "servicio-productos": anotamos el nombre del servicio que nos queremos conectar , en este caso a 
   nuestro servicio servicio-productos que tenemos en el otro producto y ese nombre lo tenemos registrado en
   el aplication.properties del proyecto servicio-productos
  
  *url = "localhost:8001": ponemos la url de nuestro servicio en este caso es localhost y el puesto 8001
  
  @FeignClient: al anotarla con esa anotacion pasa a ser un componente de spring por lo tanto se puede injectar
  
  */

@FeignClient(name="servicio-usuarios")
public interface UsuarioFeignClient {
	
	/*@GetMapping: cuando se trabajo con Feing, con @GetMapping indicamos las ruta para consumir  el servicio
	 * el API REST , y obtenemos los datos del JSON pero convertirlos a nuestros objetos en este caso 
	 * al producto
	 
	 *NOTA: TENEMOS QUE MAPEAR LOS METODOS A LAS RUTAS ORIGINALES DE NUESTRO MICROSERVICIO DE servicio-productos */
	
	/*NOTA IMPORTANTE: /usuarios debe de empesar igual como el que tiene  el microservicio servicios-
	 * usuarios ( @RepositoryRestResource(path="usuarios") ) de lo contrario dara error
	 * */
	
	@GetMapping("/usuarios/search/buscar-username")
	public Usuario findByUsername(@RequestParam String username);
	
	
	/*mandamos un request body ya que en el microservicios usuario lo va a capturar como un json y lo convierte al 
	 * entity objeto usuario*/
	@PutMapping("/usuarios/{id}")
	public Usuario update(@RequestBody Usuario usuario,@PathVariable Long id);

}
