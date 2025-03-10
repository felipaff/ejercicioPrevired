// -------------------------------
// Funciones de Utilidad
// -------------------------------
function mostrarError(elementoId, mensaje) {
    document.getElementById(elementoId).textContent = mensaje;
}

function limpiarErrores() {
    document.querySelectorAll('[id^="error"]').forEach(el => el.textContent = '');
}

function mostrarLoading(mostrar) {
    document.getElementById('loading').style.display = mostrar ? 'block' : 'none';
}

// -------------------------------
// Gestión de Empleados
// -------------------------------
// Variables globales de paginación
let currentPage = 1;
const pageSize = 15;

// Modificar función cargarEmpleados
async function cargarEmpleados(page = 1) {
    try {
        mostrarLoading(true);
        const response = await fetch(`/api/empleados?page=${page}&size=${pageSize}`);

        if (!response.ok) throw new Error('Error al cargar empleados');

        const data = await response.json();
        actualizarTablaEmpleados(data.data);
        actualizarPaginacion(data.totalPages);

    } catch (error) {
        alert('Error: ' + error.message);
    } finally {
        mostrarLoading(false);
    }
}

// Función para actualizar controles de paginación
function actualizarPaginacion(totalPages) {
    const paginationDiv = document.getElementById('pagination');
    paginationDiv.innerHTML = `
        <button ${currentPage === 1 ? 'disabled' : ''} onclick="cambiarPagina(-1)">Anterior</button>
        <span>Página ${currentPage} de ${totalPages}</span>
        <button ${currentPage === totalPages ? 'disabled' : ''} onclick="cambiarPagina(1)">Siguiente</button>
    `;
}

// Función para cambiar de página
function cambiarPagina(delta) {
    currentPage += delta;
    cargarEmpleados(currentPage);
}

// Agregar este div después de la tabla de empleados
<div id="pagination" class="pagination"></div>

function actualizarTablaEmpleados(empleados) {
    const tbody = document.getElementById('tablaBody');
    tbody.innerHTML = '';

    empleados.forEach(empleado => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${empleado.nombre}</td>
            <td>${empleado.apellido}</td>
            <td>${empleado.rut}</td>
            <td>${empleado.cargo}</td>
            <td>$${empleado.salarioBase.toLocaleString()}</td>
            <td>
                <button onclick="eliminarEmpleado(${empleado.id})">Eliminar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function eliminarEmpleado(id) {
    if (confirm('¿Eliminar empleado?')) {
        try {
            const response = await fetch(`/api/empleados/${id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error('Error al eliminar');
            cargarEmpleados();
        } catch (error) {
            alert('Error: ' + error.message);
        }
    }
}

// -------------------------------
// Formulario de Empleados
// -------------------------------
document.getElementById('formCSV').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData();
    formData.append('archivo', document.getElementById('archivo').files[0]);

    try {
        const response = await fetch('/api/nominas/calcular', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (!response.ok) {
            mostrarErroresCSV(data.errores);
            return;
        }

        actualizarResultadosCSV(data.resultados);
        cargarEmpleados(currentPage); // Actualizar tabla principal

    } catch (error) {
        alert('Error: ' + error.message);
    }
});

function validarFormulario(empleado) {
    let valido = true;

    if (!empleado.nombre) {
        mostrarError('errorNombre', 'Nombre requerido');
        valido = false;
    }

    if (!/^\d{7,8}-[\dKk]$/.test(empleado.rut)) {
        mostrarError('errorRut', 'RUT inválido (Ej: 12345678-9)');
        valido = false;
    }

    if (empleado.salarioBase < 400000) {
        mostrarError('errorSalario', 'Mínimo $400,000');
        valido = false;
    }

    return valido;
}

// -------------------------------
// Procesamiento de CSV
// -------------------------------
document.getElementById('formCSV').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData();
    formData.append('archivo', document.getElementById('archivo').files[0]);

    try {
        const response = await fetch('/api/nominas/calcular', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (!response.ok) {
            mostrarErroresCSV(data.errores);
            return;
        }

        actualizarResultadosCSV(data.resultados);
        cargarEmpleados(); // Actualizar lista

    } catch (error) {
        alert('Error: ' + error.message);
    }
});

function actualizarResultadosCSV(resultados) {
    const tbody = document.getElementById('cuerpoResultados');
    tbody.innerHTML = '';

    resultados.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.rut}</td>
            <td>${item.nombreCompleto}</td>
            <td>$${item.salarioFinal.toLocaleString()}</td>
        `;
        tbody.appendChild(tr);
    });
}

function mostrarErroresCSV(errores) {
    let mensaje = "Errores en el archivo:\n";
    errores.forEach(error => {
        mensaje += `Fila ${error.fila}: ${error.error}\n`;
    });
    alert(mensaje);
}

// -------------------------------
// Inicialización
// -------------------------------
document.addEventListener('DOMContentLoaded', cargarEmpleados);