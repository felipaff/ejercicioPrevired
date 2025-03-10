package com.previred.ejercicio.servlet;

import com.google.gson.Gson;
import com.previred.ejercicio.dao.EmpleadoDAO;
import com.previred.ejercicio.model.Empleado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/empleados/*")
public class EmpleadoServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(EmpleadoServlet.class);
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Obtener todos los empleados
                List<Empleado> empleados = empleadoDAO.getAll();
                sendJsonResponse(resp, HttpServletResponse.SC_OK, empleados);
            } else {
                // Obtener empleado por ID
                String[] splits = pathInfo.split("/");
                if (splits.length != 2) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Formato de URL inválido");
                    return;
                }

                Long id = Long.parseLong(splits[1]);
                Empleado empleado = empleadoDAO.getById(id);

                if (empleado != null) {
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, empleado);
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Empleado no encontrado");
                }
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        } catch (Exception e) {
            logger.error("Error en GET: {}", e.getMessage(), e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Empleado empleado = gson.fromJson(req.getReader(), Empleado.class);

            // prevenir null pointer
            if (empleado.getDescuentos() == null) {
                empleado.setDescuentos(new BigDecimal(0.0));
            }

            if (empleado.getBonos() == null) {
                empleado.setBonos(new BigDecimal(0.0));
            }

            // Validaciones
            if (empleado.getSalarioBase().compareTo(new BigDecimal("400000")) < 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Salario base menor a $400,000");
                return;
            }

            if (empleadoDAO.existeRut(empleado.getRut())) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "RUT ya registrado");
                return;
            }

            empleadoDAO.save(empleado);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Empleado creado exitosamente"));

        } catch (IllegalArgumentException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Error en POST: {}", e.getMessage(), e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear empleado");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Se requiere ID");
                return;
            }

            String[] splits = pathInfo.split("/");
            if (splits.length != 2) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Formato de URL inválido");
                return;
            }

            Long id = Long.parseLong(splits[1]);
            empleadoDAO.delete(id);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of("message", "Empleado eliminado"));

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID inválido");
        } catch (SQLException e) {
            logger.error("Error en DELETE: {}", e.getMessage(), e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al eliminar empleado");
        }
    }

    // Métodos auxiliares
    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        sendJsonResponse(resp, status, error);
    }
}