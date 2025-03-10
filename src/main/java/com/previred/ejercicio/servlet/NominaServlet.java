package com.previred.ejercicio.servlet;

import com.google.gson.Gson;
import com.previred.ejercicio.dao.EmpleadoDAO;
import com.previred.ejercicio.model.Empleado;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@WebServlet("/api/nominas/calcular")
@MultipartConfig
public class NominaServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(NominaServlet.class);
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final Gson gson = new Gson();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<String, Object> responseData = new HashMap<>();
        List<Map<String, Object>> resultados = new ArrayList<>();
        List<Map<String, String>> errores = new ArrayList<>();

        try {
            Part filePart = req.getPart("archivo");
            InputStream fileContent = filePart.getInputStream();

            try (Reader reader = new InputStreamReader(fileContent);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withIgnoreHeaderCase()
                         .withTrim())) {

                for (CSVRecord csvRecord : csvParser) {
                    Map<String, String> errorItem = new HashMap<>();
                    try {
                        Empleado empleado = parseEmpleadoFromCSV(csvRecord);
                        empleadoDAO.save(empleado);

                        Map<String, Object> resultado = new HashMap<>();
                        resultado.put("rut", empleado.getRut());
                        resultado.put("nombreCompleto", empleado.getNombre() + " " + empleado.getApellido());
                        resultado.put("salarioFinal", empleado.calcularSalarioFinal());
                        resultados.add(resultado);

                    } catch (IllegalArgumentException | SQLException e) {
                        errorItem.put("fila", String.valueOf(csvRecord.getRecordNumber()));
                        errorItem.put("error", e.getMessage());
                        errores.add(errorItem);
                    }
                }
            }

            if (!errores.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("errores", errores);
            }
            responseData.put("resultados", resultados);

        } catch (Exception e) {
            logger.error("Error procesando CSV: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseData.put("error", "Error procesando archivo: " + e.getMessage());
        }

        resp.getWriter().write(gson.toJson(responseData));
    }

    private Empleado parseEmpleadoFromCSV(CSVRecord csvRecord) {
        Empleado empleado = new Empleado();
        empleado.setNombre(csvRecord.get("nombre"));
        empleado.setApellido(csvRecord.get("apellido"));
        empleado.setRut(csvRecord.get("RUT/DNI"));
        empleado.setCargo(csvRecord.get("cargo"));
        empleado.setSalarioBase(new BigDecimal(csvRecord.get("salario base")));
        empleado.setBonos(new BigDecimal(csvRecord.get("bonos")));
        empleado.setDescuentos(new BigDecimal(csvRecord.get("descuentos")));
        return empleado;
    }
}

