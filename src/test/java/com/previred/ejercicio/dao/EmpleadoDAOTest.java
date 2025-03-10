package com.previred.ejercicio.dao;

import com.previred.ejercicio.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class EmpleadoDAOTest {

    @BeforeAll
    static void setup() throws SQLException {
        DatabaseUtil.getConnection();
    }

    @Test
    void testExisteRut() throws SQLException {
        EmpleadoDAO dao = new EmpleadoDAO();
        assertFalse(dao.existeRut("12345678-9"));
    }
}