// Cargar empleados al iniciar
fetch("/api/empleados")
    .then(response => response.json())
    .then(data => renderTable(data));

// Agregar empleado
document.getElementById("formEmpleado").addEventListener("submit", (e) => {
    e.preventDefault();
    const data = {
        nombre: e.target.nombre.value,
        rut: e.target.rut.value,
        // ...otros campos
    };

    fetch("/api/empleados", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) throw new Error("Error en validación");
        return response.json();
    })
    .then(() => location.reload()) // Recargar datos
    .catch(error => mostrarError(error));
});