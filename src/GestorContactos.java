import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GestorContactos {

    // Lista que almacena todos los contactos en memoria
    private List<Contacto> contactos;
    // Map que asocia un campo (nombre, apellido, etc.) con su índice
    private Map<String, Indice> indices;
    // Siguiente ID disponible para asignar a un nuevo contacto
    private int nextId;
    // Directorio de trabajo donde se buscan o exportan archivos CSV
    private String directorioActual = ".";
    // Ruta del CSV cargado inicialmente o último exportado
    private String loadedCSVPath;
    // Indica si existen cambios en memoria que aún no han sido exportados
    private boolean dirty;

    public GestorContactos() {
        contactos     = new ArrayList<>();
        indices       = new HashMap<>();
        nextId        = 1;
        loadedCSVPath = null;
        dirty         = false;
        // No se carga ningún archivo aquí; Main invoca inicializar()
    }

    /**
     * Pregunta al usuario si desea cargar un CSV al iniciar.
     * Si no, empieza con cero contactos y sólo guardará al exportar.
     */
    public void inicializar() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("¿Desea cargar un archivo CSV existente al iniciar? (s/n): ");
        String resp = scanner.nextLine().trim();
        if (resp.equalsIgnoreCase("s")) {
            cargarContactosDesdeCSV();
        } else {
            System.out.println("Iniciando con cero contactos (guardar sólo al exportar).");
        }
    }

    public int getCantidadContactos() {
        return contactos.size();
    }

    public String getDirectorioActual() {
        return directorioActual;
    }

    /**
     * Busca archivos .csv en directorioActual, muestra una lista numerada,
     * lee la selección y carga en memoria el archivo elegido.
     */
    public void cargarContactosDesdeCSV() {
        File carpeta      = new File(directorioActual);
        File[] archivos   = carpeta.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
        if (archivos == null || archivos.length == 0) {
            System.out.println("No se encontraron archivos CSV en: " + directorioActual);
            return;
        }
        System.out.println("Archivos CSV disponibles:");
        for (int i = 0; i < archivos.length; i++) {
            System.out.println((i + 1) + ". " + archivos[i].getName());
        }
        System.out.print("Seleccione archivo: ");
        Scanner scanner = new Scanner(System.in);
        int opcion = scanner.nextInt(); scanner.nextLine();
        if (opcion < 1 || opcion > archivos.length) {
            System.out.println("Opción inválida.");
            return;
        }
        cargarContactosDesdeArchivo(archivos[opcion - 1]);
    }

    /**
     * Lee el CSV línea por línea, crea objetos Contacto en memoria,
     * asigna IDs, actualiza nextId y restablece dirty = false.
     */
    private void cargarContactosDesdeArchivo(File archivoCSV) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivoCSV))) {
            String line;
            boolean primera = true;
            contactos.clear();
            while ((line = br.readLine()) != null) {
                if (primera) {
                    primera = false;
                    if (line.toLowerCase().contains("id,nombre,apellido")) continue;
                }
                String[] v = line.split(",\\s*");
                if (v.length == 8 && v[0].matches("\\d+")) {
                    Contacto c = new Contacto(v[1], v[2], v[3],
                            v[4], v[5], v[6], v[7]);
                    c.setId(Integer.parseInt(v[0]));
                    contactos.add(c);
                    nextId = Math.max(nextId, c.getId() + 1);
                }
            }
            loadedCSVPath = archivoCSV.getAbsolutePath();
            dirty = false;  // Los cambios se sincronizan al cargar
            System.out.println("Cargados desde: " + loadedCSVPath);
        } catch (IOException e) {
            System.out.println("Error lectura: " + e.getMessage());
        }
    }
    /**
     * Importa un CSV desde la ruta indicada en memoria, evita duplicados,
     * actualiza índices y marca dirty = true.
     */
    public void importarCSVDesdeRuta(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String line;
            boolean primera = true;
            int maxId = contactos.stream()
                    .mapToInt(Contacto::getId)
                    .max()
                    .orElse(0);
            while ((line = br.readLine()) != null) {
                if (primera) {
                    primera = false;
                    if (line.toLowerCase().contains("id,nombre,apellido")) continue;
                }
                String[] v = line.split(",\\s*");
                if (v.length == 8 &&
                        !existeContacto(v[1], v[2], v[3], v[5], v[4])) {
                    Contacto c = new Contacto(
                            v[1].trim(), v[2].trim(), v[3].trim(),
                            v[4].trim(), v[5].trim(), v[6].trim(), v[7].trim()
                    );
                    maxId++;
                    c.setId(maxId);
                    contactos.add(c);
                }
            }
            actualizarIndices();
            dirty = true;  // Hay cambios pendientes de exportación
            System.out.println("Importación completada en memoria.");
        } catch (IOException e) {
            System.out.println("Error al importar CSV: " + e.getMessage());
        }
    }
    /**
     * Cambia el directorio de trabajo y automáticamente ofrece
     * cargar un CSV desde el nuevo directorio.
     */
    public void cambiarDirectorioActual(String nuevoDirectorio) {
        File carpeta = new File(nuevoDirectorio);
        if (carpeta.exists() && carpeta.isDirectory()) {
            directorioActual = nuevoDirectorio;
            System.out.println("Directorio cambiado a: " + directorioActual);
            cargarContactosDesdeCSV();
        } else {
            System.out.println("Ruta inválida o no es carpeta.");
        }
    }

    /**
     * Exporta los contactos actuales a la ruta especificada.
     * Al finalizar, dirty pasa a false y loadedCSVPath se actualiza.
     */
    public void exportarContactos(String rutaArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaArchivo))) {
            pw.println("id,nombre,apellido,apodo,telefono,email,direccion,fecha_nacimiento");
            for (Contacto c : contactos) {
                pw.println(c.getId() + "," +
                        c.getNombre() + "," +
                        c.getApellido() + "," +
                        c.getApodo() + "," +
                        c.getTelefono() + "," +
                        c.getEmail() + "," +
                        c.getDireccion() + "," +
                        c.getFechaNacimiento());
            }
            System.out.println("CSV exportado en: " + rutaArchivo);
            loadedCSVPath = rutaArchivo;
            dirty = false;  // Ya no hay cambios pendientes
        } catch (IOException e) {
            System.out.println("Error al exportar CSV: " + e.getMessage());
        }
    }

    /**
     * Agrega un nuevo Contacto en memoria, actualiza índices,
     * marca dirty = true y NO escribe en disco.
     */
    public void agregarContacto(Contacto contacto) {
        if (!esEmailValido(contacto.getEmail())) {
            System.out.println("Formato de email inválido.");
            return;
        }
        if (!esTelefonoValido(contacto.getTelefono())) {
            System.out.println("Teléfono inválido.");
            return;
        }
        if (existeContacto(contacto.getNombre(), contacto.getApodo())) {
            System.out.println("Este contacto ya existe.");
            return;
        }
        contacto.setId(nextId++);
        contactos.add(contacto);
        actualizarIndices(contacto);
        dirty = true;  // Indica cambios pendientes de exportar
        System.out.println("Contacto agregado (en memoria).");
    }

    /**
     * Elimina un Contacto por ID en memoria, actualiza índices,
     * marca dirty = true y NO escribe en disco.
     */
    public void eliminarContacto(int id) {
        if (contactos.isEmpty()) {
            System.out.println("No hay contactos registrados.");
            return;
        }
        boolean eliminado = contactos.removeIf(c -> c.getId() == id);
        if (!eliminado) {
            System.out.println("No se encontró contacto con ID especificado.");
            return;
        }
        actualizarIndices();
        dirty = true;
        System.out.println("Contacto eliminado (en memoria).");
    }

    /**
     * Actualiza los datos de un Contacto existente en memoria,
     * marca dirty = true y NO escribe en disco.
     */
    public void actualizarContacto(Contacto contacto) {
        if (contactos.isEmpty()) {
            System.out.println("No hay contactos registrados.");
            return;
        }
        for (Contacto c : contactos) {
            if (c.getId() == contacto.getId()) {
                c.setNombre(contacto.getNombre());
                c.setApellido(contacto.getApellido());
                c.setApodo(contacto.getApodo());
                c.setTelefono(contacto.getTelefono());
                c.setEmail(contacto.getEmail());
                c.setDireccion(contacto.getDireccion());
                c.setFechaNacimiento(contacto.getFechaNacimiento());
                actualizarIndices();
                dirty = true;
                System.out.println("Contacto actualizado (en memoria).");
                return;
            }
        }
        System.out.println("No se encontró contacto con ID especificado.");
    }

    public List<Contacto> buscarContactos(String criterio, String valor) {
        if (contactos.isEmpty()) {
            System.out.println("No hay contactos registrados.");
            return Collections.emptyList();
        }
        return contactos.stream()
                .filter(c ->
                        criterio.equalsIgnoreCase("nombre")   ? c.getNombre().equalsIgnoreCase(valor)   :
                                criterio.equalsIgnoreCase("apellido") ? c.getApellido().equalsIgnoreCase(valor) :
                                        criterio.equalsIgnoreCase("email")    ? c.getEmail().equalsIgnoreCase(valor)     :
                                                criterio.equalsIgnoreCase("telefono") ? c.getTelefono().equals(valor)            :
                                                        false
                )
                .collect(Collectors.toList());
    }

    public void crearIndice(String campo, String tipo) {
        Indice indice = new Indice(campo, tipo, this);
        for (Contacto c : contactos) {
            indice.insertar(c);
        }
        indices.put(campo, indice);
        indice.flush();  // Guarda el archivo del índice si hubo cambios
    }

    public void listarContactosOrdenados(String campo) {
        Comparator<Contacto> comp = switch (campo) {
            case "nombre"           -> Comparator.comparing(Contacto::getNombre);
            case "apellido"         -> Comparator.comparing(Contacto::getApellido);
            case "fecha_nacimiento" -> Comparator.comparing(Contacto::getFechaNacimiento);
            default                 -> Comparator.comparingInt(Contacto::getId);
        };
        contactos.stream().sorted(comp).forEach(System.out::println);
    }

    // Actualiza todos los índices con un nuevo contacto
    private void actualizarIndices(Contacto contacto) {
        for (Indice idx : indices.values()) {
            idx.insertar(contacto);
        }
    }
    // Reconstruye todos los índices completos (en casos de eliminación o edición masiva)
    private void actualizarIndices() {
        for (Indice idx : indices.values()) {
            idx.reconstruir();
        }
    }

    // Validación básica de formato de email
    private boolean esEmailValido(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    // Validación de teléfono de 8 dígitos
    private boolean esTelefonoValido(String telefono) {
        return telefono.matches("^\\d{8}$");
    }
    // Comprueba duplicado basado en nombre y apodo
    private boolean existeContacto(String nombre, String apodo) {
        return contactos.stream()
                .anyMatch(c ->
                        c.getNombre().equalsIgnoreCase(nombre) &&
                                c.getApodo().equalsIgnoreCase(apodo)
                );
    }
    // Comprueba duplicado avanzado: combina varios campos
    private boolean existeContacto(String nombre,
                                   String apellido,
                                   String apodo,
                                   String email,
                                   String telefono) {
        return contactos.stream().anyMatch(c ->
                (c.getNombre().equalsIgnoreCase(nombre)
                        && c.getApellido().equalsIgnoreCase(apellido)
                        && c.getApodo().equalsIgnoreCase(apodo))
                        || c.getEmail().equalsIgnoreCase(email)
                        || c.getTelefono().equals(telefono)
        );
    }

    public Contacto getContactoPorId(int id) {
        return contactos.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
