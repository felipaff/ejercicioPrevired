package com.previred.ejercicio.dao;

import com.previred.ejercicio.model.Empleado;
import com.previred.ejercicio.util.DatabaseUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmpleadoDAO {

    private static final Logger logger = LoggerFactory.getLogger(EmpleadoDAO.class);

    // SQL Queries
    private static final String SQL_INSERT = "INSERT INTO empleados "
            + "(nombre, apellido, rut, cargo, salario_base, bonos, descuentos) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_ALL = "SELECT * FROM empleados";

    private static final String SQL_DELETE = "DELETE FROM empleados WHERE id = ?";

    private static final String SQL_EXIST_RUT = "SELECT COUNT(*) FROM empleados WHERE rut = ?";

    public List<Empleado> getAll() throws SQLException {
        List<Empleado> empleados = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Empleado emp = mapResultSetToEmpleado(rs);
                empleados.add(emp);
            }

        } catch (SQLException ex) {
            logger.error("Error al obtener empleados: {}", ex.getMessage());
            throw ex;
        }
        return empleados;
    }

    public Empleado getById(Long id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM empleados WHERE id = ?")) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToEmpleado(rs) : null;
            }
        }
    }

    public void save(Empleado empleado) throws SQLException, IllegalArgumentException {
        validateEmpleado(empleado);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setStatementParameters(stmt, empleado);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    empleado.setId(generatedKeys.getLong(1));
                }
            }

        } catch (SQLException ex) {
            logger.error("Error al guardar empleado: {}", ex.getMessage());
            throw ex;
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            logger.error("Error al eliminar empleado ID {}: {}", id, ex.getMessage());
            throw ex;
        }
    }

    public boolean existeRut(String rut) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXIST_RUT)) {

            stmt.setString(1, rut);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException ex) {
            logger.error("Error verificando RUT {}: {}", rut, ex.getMessage());
            throw ex;
        }
    }

    // Métodos auxiliares
    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        Empleado emp = new Empleado();
        emp.setId(rs.getLong("id"));
        emp.setNombre(rs.getString("nombre"));
        emp.setApellido(rs.getString("apellido"));
        emp.setRut(rs.getString("rut"));
        emp.setCargo(rs.getString("cargo"));
        emp.setSalarioBase(rs.getBigDecimal("salario_base"));
        emp.setBonos(rs.getBigDecimal("bonos"));
        emp.setDescuentos(rs.getBigDecimal("descuentos"));
        return emp;
    }

    private void setStatementParameters(PreparedStatement stmt, Empleado emp) throws SQLException {
        stmt.setString(1, emp.getNombre());
        stmt.setString(2, emp.getApellido());
        stmt.setString(3, emp.getRut());
        stmt.setString(4, emp.getCargo());
        stmt.setBigDecimal(5, emp.getSalarioBase());
        stmt.setBigDecimal(6, emp.getBonos());
        stmt.setBigDecimal(7, emp.getDescuentos());
    }

    private void validateEmpleado(Empleado empleado) throws IllegalArgumentException {
        if (empleado.getSalarioBase().compareTo(new BigDecimal("400000")) < 0) {
            throw new IllegalArgumentException("Salario base menor a $400,000");
        }

        if (empleado.getBonos().compareTo(empleado.getSalarioBase().multiply(new BigDecimal("0.5"))) > 0) {
            throw new IllegalArgumentException("Bonos superan el 50% del salario base");
        }

        if(empleado.getDescuentos().equals(null)) {
            empleado.setDescuentos(new BigDecimal(0.0));
        }

        if (empleado.getDescuentos().compareTo(empleado.getSalarioBase()) > 0) {
            throw new IllegalArgumentException("Descuentos superan el salario base");
        }
    }

    public List<Empleado> getPaginated(int page, int size) throws SQLException {
        String sql = "SELECT * FROM empleados LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, (page - 1) * size);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Empleado> empleados = new ArrayList<>();
                while (rs.next()) {
                    empleados.add(mapResultSetToEmpleado(rs));
                }
                return empleados;
            }
        }
    }

    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleados";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
