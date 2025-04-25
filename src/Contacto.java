public class Contacto {
    // Identificador único del contacto
    private int id;
    // Nombre de pila
    private String nombre;
    // Apellido paterno y/o materno
    private String apellido;
    // Nombre informal o mote
    private String apodo;
    // Número de teléfono (sin espacios ni guiones)
    private String telefono;
    // Dirección de correo electrónico válida
    private String email;
    // Dirección física
    private String direccion;
    // Fecha de nacimiento en formato DD/MM/AAAA
    private String fechaNacimiento;

    public Contacto(String nombre, String apellido, String apodo,
                    String telefono, String email,
                    String direccion, String fechaNacimiento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.apodo = apodo;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
    }

    // --- Getters y setters para acceder y modificar cada campo ---

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getApodo() {
        return apodo;
    }
    public void setApodo(String apodo) {
        this.apodo = apodo;
    }

    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }
    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Representación en texto de todos los datos del contacto,
     * útil para imprimir en consola o registros.
     */
    @Override
    public String toString() {
        return "Contacto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", apodo='" + apodo + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", direccion='" + direccion + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                '}';
    }
}
