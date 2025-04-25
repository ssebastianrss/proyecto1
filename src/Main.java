import java.util.*;
import java.io.File;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // Mostrar ruta de trabajo actual
        System.out.println("Directorio actual: " + new File(".").getAbsolutePath());
        GestorContactos gestor = new GestorContactos();
        // Permitir al usuario cargar un CSV existente o iniciar vacío
        gestor.inicializar();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Menú principal
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Agregar contacto");
            System.out.println("2. Eliminar contacto");
            System.out.println("3. Actualizar contacto");
            System.out.println("4. Buscar contactos");
            System.out.println("5. Crear índice");
            System.out.println("6. Ver todos los contactos");
            System.out.println("7. Exportar archivo CSV actualizado");
            System.out.println("8. Importar archivo CSV desde una ruta");
            System.out.println("9. Cambiar el directorio actual");
            System.out.println("10. Salir");
            System.out.print("Opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();  // Consumir salto de línea

            // Despachar según la opción seleccionada
            switch (opcion) {
                case 1 -> agregarContacto(scanner, gestor);
                case 2 -> eliminarContacto(scanner, gestor);
                case 3 -> actualizarContacto(scanner, gestor);
                case 4 -> buscarContactos(scanner, gestor);
                case 5 -> crearIndice(scanner, gestor);
                case 6 -> verTodosLosContactos(gestor, scanner);
                case 7 -> exportarCSV(scanner, gestor);
                case 8 -> importarCSVDesdeRuta(scanner, gestor);
                case 9 -> cambiarDirectorioActual(scanner, gestor);
                case 10 -> {
                    System.out.println("Saliendo del programa.");
                    return;  // Terminar ejecución
                }
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    /**
     * Solicita al usuario todos los campos de un nuevo contacto,
     * valida con expresiones regulares y delega la creación al gestor.
     */
    private static void agregarContacto(Scanner scanner, GestorContactos gestor) {
        System.out.println("Ingrese los datos del contacto:");

        System.out.print("Nombre: ");
        String nombre = solicitarEntrada(scanner, "^.+$", "Nombre inválido.");

        System.out.print("Apellido: ");
        String apellido = solicitarEntrada(scanner, "^.+$", "Apellido inválido.");

        System.out.print("Apodo: ");
        String apodo = solicitarEntrada(scanner, "^.+$", "Apodo inválido.");

        System.out.print("Teléfono: ");
        String telefono = solicitarEntrada(scanner, "^\\d{8}$", "Teléfono inválido.");

        System.out.print("Email: ");
        String email = solicitarEntrada(scanner, "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", "Email inválido.");

        System.out.print("Dirección: ");
        String direccion = solicitarEntrada(scanner, "^.+$", "Dirección inválida.");

        System.out.print("Fecha de Nacimiento: ");
        String fechaNacimiento = solicitarEntrada(scanner, "^\\d{2}/\\d{2}/\\d{4}$", "Fecha inválida.");

        // Crear y guardar el contacto
        Contacto contacto = new Contacto(nombre, apellido, apodo, telefono, email, direccion, fechaNacimiento);
        gestor.agregarContacto(contacto);
    }

    /**
     * Muestra lista de contactos, pide un ID y confirma antes de eliminar.
     */
    private static void eliminarContacto(Scanner scanner, GestorContactos gestor) {
        if (gestor.getCantidadContactos() == 0) {
            System.out.println("No hay contactos registrados.");
            return;
        }
        gestor.listarContactosOrdenados("id");
        System.out.print("Ingrese el ID del contacto a eliminar: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("¿Seguro? (s/n): ");
        if (scanner.nextLine().equalsIgnoreCase("s")) {
            gestor.eliminarContacto(id);
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    /**
     * Permite seleccionar y actualizar múltiples campos de un contacto existente.
     */
    private static void actualizarContacto(Scanner scanner, GestorContactos gestor) {
        if (gestor.getCantidadContactos() == 0) {
            System.out.println("No hay contactos registrados.");
            return;
        }
        gestor.listarContactosOrdenados("id");
        System.out.print("ID del contacto a actualizar: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Contacto contacto = gestor.getContactoPorId(id);
        if (contacto == null) {
            System.out.println("ID no encontrado.");
            return;
        }
        System.out.println("Contacto seleccionado: " + contacto);

        // Leer los números de campos a modificar separados por comas
        System.out.println("Campos: 1.Nombre 2.Apellido 3.Apodo 4.Teléfono 5.Email 6.Dirección 7.Fecha");
        System.out.print("Seleccione campos (ej. 1,3,7): ");
        List<Integer> opciones = Arrays.stream(scanner.nextLine().split(","))
                .map(String::trim).map(Integer::parseInt)
                .filter(n -> n >= 1 && n <= 7).distinct().toList();

        // Para cada campo, solicitar valor y actualizar el objeto
        for (int campo : opciones) {
            switch (campo) {
                case 1 -> {
                    System.out.print("Nuevo nombre: ");
                    contacto.setNombre(solicitarEntrada(scanner, "^.+$", "Nombre inválido."));
                }
                case 2 -> {
                    System.out.print("Nuevo apellido: ");
                    contacto.setApellido(solicitarEntrada(scanner, "^.+$", "Apellido inválido."));
                }
                case 3 -> {
                    System.out.print("Nuevo apodo: ");
                    contacto.setApodo(solicitarEntrada(scanner, "^.+$", "Apodo inválido."));
                }
                case 4 -> {
                    System.out.print("Nuevo teléfono: ");
                    contacto.setTelefono(solicitarEntrada(scanner, "^\\d{8}$", "Teléfono inválido."));
                }
                case 5 -> {
                    System.out.print("Nuevo email: ");
                    contacto.setEmail(solicitarEntrada(scanner, "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                            "Email inválido."));
                }
                case 6 -> {
                    System.out.print("Nueva dirección: ");
                    contacto.setDireccion(solicitarEntrada(scanner, "^.+$", "Dirección inválida."));
                }
                case 7 -> {
                    System.out.print("Nueva fecha: ");
                    contacto.setFechaNacimiento(solicitarEntrada(scanner, "^\\d{2}/\\d{2}/\\d{4}$", "Fecha inválida."));
                }
            }
        }
        gestor.actualizarContacto(contacto);
        System.out.println("Contacto actualizado.");
    }

    /**
     * Solicita un campo y un valor para buscar contactos que cumplan esa condición.
     */
    private static void buscarContactos(Scanner scanner, GestorContactos gestor) {
        if (gestor.getCantidadContactos() == 0) {
            System.out.println("No hay contactos.");
            return;
        }
        System.out.print("Campo (nombre, apellido, email, telefono): ");
        String campo = scanner.nextLine();
        System.out.print("Valor: ");
        String valor = scanner.nextLine();
        List<Contacto> lista = gestor.buscarContactos(campo, valor);
        if (lista.isEmpty()) {
            System.out.println("No se encontraron resultados.");
        } else {
            lista.forEach(System.out::println);
        }
    }

    /** Pide campo y tipo para crear un índice, y delega en el gestor. */
    private static void crearIndice(Scanner scanner, GestorContactos gestor) {
        System.out.println("Campos para índice: id, nombre, apellido, apodo, telefono, email, direccion, fecha_nacimiento");
        System.out.print("Campo: ");
        String campo = scanner.nextLine();
        System.out.println("Tipos: bst, avl");
        System.out.print("Tipo: ");
        String tipo = scanner.nextLine();
        gestor.crearIndice(campo, tipo);
    }

    /** Muestra las opciones de orden y llama a listarContactosOrdenados. */
    private static void verTodosLosContactos(GestorContactos gestor, Scanner scanner) {
        if (gestor.getCantidadContactos() == 0) {
            System.out.println("No hay contactos.");
            return;
        }
        System.out.println("Ordenar por: 1.id 2.nombre 3.apellido 4.apodo 5.telefono 6.email 7.direccion 8.fecha_nacimiento");
        int opc = scanner.nextInt();
        scanner.nextLine();
        String campo = switch (opc) {
            case 2 -> "nombre";
            case 3 -> "apellido";
            case 4 -> "apodo";
            case 5 -> "telefono";
            case 6 -> "email";
            case 7 -> "direccion";
            case 8 -> "fecha_nacimiento";
            default -> "id";
        };
        gestor.listarContactosOrdenados(campo);
    }

    /** Gestiona opciones de exportación de CSV. */
    private static void exportarCSV(Scanner scanner, GestorContactos gestor) {
        System.out.println("1.Directorio actual  2.Otra ruta");
        int opc = scanner.nextInt();
        scanner.nextLine();
        String ruta = opc == 1
                ? gestor.getDirectorioActual() + File.separator + "contacts.csv"
                : solicitarEntrada(scanner, "^.+$", "Ruta inválida.") + File.separator + "contacts.csv";
        gestor.exportarContactos(ruta);
    }

    /** Lee ruta de un CSV externo e importa contactos. */
    private static void importarCSVDesdeRuta(Scanner scanner, GestorContactos gestor) {
        System.out.print("Ruta completa del CSV: ");
        String ruta = scanner.nextLine();
        gestor.importarCSVDesdeRuta(ruta);
    }

    /** Cambia el directorio de trabajo y ofrece cargar CSV nuevo. */
    private static void cambiarDirectorioActual(Scanner scanner, GestorContactos gestor) {
        System.out.print("Nueva ruta de directorio: ");
        String ruta = scanner.nextLine();
        gestor.cambiarDirectorioActual(ruta);
    }

    /**
     * Valida la entrada del usuario contra una expresión regular;
     * repite la solicitud hasta que coincida.
     */
    private static String solicitarEntrada(Scanner scanner, String regex, String mensajeError) {
        while (true) {
            String entrada = scanner.nextLine().trim();
            if (entrada.matches(regex)) {
                return entrada;
            }
            System.out.println(mensajeError);
        }
    }
}
