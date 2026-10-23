package com.example.bdget.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // Metodo auxiliar solo para construir un MethodParameter real
    @SuppressWarnings("unused")
    private void metodoAuxiliar(String valor) {
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRuntimeException_devuelve400ConMensaje() {
        ResponseEntity<Object> respuesta =
                handler.handleRuntimeException(new RuntimeException("Estudiante no encontrado"));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        Map<String, Object> cuerpo = (Map<String, Object>) respuesta.getBody();
        assertNotNull(cuerpo);
        assertEquals("Estudiante no encontrado", cuerpo.get("message"));
        assertEquals(400, cuerpo.get("status"));
        assertNotNull(cuerpo.get("timestamp"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleValidationException_devuelve400ConErroresPorCampo() throws Exception {
        Method metodo = GlobalExceptionHandlerTest.class
                .getDeclaredMethod("metodoAuxiliar", String.class);
        MethodParameter parametro = new MethodParameter(metodo, 0);

        BindingResult resultado = new BeanPropertyBindingResult(new Object(), "student");
        resultado.addError(new FieldError("student", "name", "no puede estar vacio"));

        MethodArgumentNotValidException excepcion =
                new MethodArgumentNotValidException(parametro, resultado);

        ResponseEntity<Object> respuesta = handler.handleValidationException(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        Map<String, Object> cuerpo = (Map<String, Object>) respuesta.getBody();
        assertNotNull(cuerpo);
        Map<String, String> errores = (Map<String, String>) cuerpo.get("errors");
        assertEquals("no puede estar vacio", errores.get("name"));
        assertEquals(400, cuerpo.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleGeneralException_devuelve500ConRuta() {
        WebRequest peticion = mock(WebRequest.class);
        when(peticion.getDescription(false)).thenReturn("uri=/students/99");

        ResponseEntity<Object> respuesta =
                handler.handleGeneralException(new Exception("fallo inesperado"), peticion);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        Map<String, Object> cuerpo = (Map<String, Object>) respuesta.getBody();
        assertNotNull(cuerpo);
        assertEquals("An unexpected error occurred", cuerpo.get("message"));
        assertEquals("fallo inesperado", cuerpo.get("details"));
        assertEquals("/students/99", cuerpo.get("path"));
        assertTrue(cuerpo.containsKey("timestamp"));
        assertEquals(500, cuerpo.get("status"));
    }
}
