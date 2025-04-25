// Indice.java
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona un índice para un campo específico de Contacto,
 * usando internamente un BST o un AVL para acelerar búsquedas.
 */
public class Indice {
    // Nombre del campo de Contacto que se indexa (e.g., "nombre", "apodo")
    private final String campo;
    // Tipo de estructura: "bst" o "avl"
    private final String tipo;
    // Mapa que asocia cada valor de campo con la lista de IDs de Contactos
    private final Map<String, List<Integer>> indice;
    // Estructuras de árbol para mantener el índice en memoria
    private final BST bst;
    private final AVLTree avl;
    // Referencia al gestor para recuperar objetos Contacto por ID
    private final GestorContactos gestor;
    // Logger para reportar errores en I/O
    private static final Logger logger = Logger.getLogger(Indice.class.getName());
    // Indica si hubo inserciones o cambios que aún no se han volcado a disco
    private boolean modificado;

    /**
     * Constructor: inicializa el índice en memoria y borra el archivo previo.
     * @param campo   nombre del campo a indexar
     * @param tipo    tipo de árbol ("bst" o "avl")
     * @param gestor  gestor para obtener Contacto por ID
     */
    public Indice(String campo, String tipo, GestorContactos gestor) {
        this.campo      = campo;
        this.tipo       = tipo.toLowerCase();
        this.gestor     = gestor;
        this.indice     = new HashMap<>();
        this.modificado = false;
        // Crear la estructura de árbol correspondiente
        if (this.tipo.equals("bst")) {
            bst = new BST();
            avl = null;
        } else {
            avl = new AVLTree();
            bst = null;
        }
        // Borrar fichero de índice existente para empezar limpio
        File f = new File(campo + "-" + tipo + ".txt");
        if (f.exists()) f.delete();
    }

    /**
     * Inserta o actualiza un Contacto en el índice.
     * - Elimina la entrada antigua si el valor cambió.
     * - Añade el ID al nuevo valor en el mapa y en el árbol.
     */
    public void insertar(Contacto contacto) {
        String valor = obtenerValorCampo(contacto);
        // Eliminar ID de cualquier lista previa donde aún estuviera
        for (Iterator<Map.Entry<String, List<Integer>>> it = indice.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String,List<Integer>> e = it.next();
            if (e.getValue().remove((Integer) contacto.getId()) && e.getValue().isEmpty()) {
                it.remove();
            }
        }
        // Añadir el ID bajo el valor actual
        indice.computeIfAbsent(valor, k -> new ArrayList<>()).add(contacto.getId());
        // Insertar en la estructura de árbol elegida
        if (tipo.equals("bst")) bst.insert(valor, contacto.getId());
        else                   avl.insert(valor, contacto.getId());
        // Marcar como modificado para que flush() lo vuelque a disco
        modificado = true;
    }

    /**
     * Reconstruye completamente el mapa de valor→IDs a partir del árbol.
     * Útil si se modificó la lista de IDs en bloque.
     */
    public void reconstruir() {
        indice.clear();
        List<Integer> ids = (tipo.equals("bst") ? bst.getAllIds() : avl.getAllIds());
        // Volver a poblar el mapa usando el recorrido por niveles del árbol
        for (Integer id : ids) {
            Contacto c = gestor.getContactoPorId(id);
            if (c != null) {
                String v = obtenerValorCampo(c);
                indice.computeIfAbsent(v, k -> new ArrayList<>()).add(id);
            }
        }
    }

    /**
     * Busca contactos cuyo campo coincide exactamente con 'valor'.
     * @param valor  cadena a buscar en el índice
     * @return lista de objetos Contacto que coinciden
     */
    public List<Contacto> buscar(String valor) {
        List<Contacto> res = new ArrayList<>();
        for (Integer id : indice.getOrDefault(valor.trim(), Collections.emptyList())) {
            Contacto c = gestor.getContactoPorId(id);
            if (c != null) res.add(c);
        }
        return res;
    }

    /**
     * Si hubo cambios (modificado == true), vuelca el índice completo a disco:
     * - Llama a saveToFileWithNulls de la estructura de árbol.
     * - Imprime un único mensaje de éxito o error.
     * - Resetea la bandera 'modificado'.
     */
    public void flush() {
        if (!modificado) return;
        String filename = campo + "-" + tipo + ".txt";
        try {
            if (tipo.equals("bst")) bst.saveToFileWithNulls(filename);
            else                   avl.saveToFileWithNulls(filename);
            System.out.println("indice creado");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error guardando índice en " + filename, e);
            System.out.println("no se pudo crear el indice");
        }
        modificado = false;
    }

    /**
     * Obtiene el valor del campo configurado desde un Contacto.
     * @param c  objeto Contacto
     * @return valor de la propiedad indicada
     */
    private String obtenerValorCampo(Contacto c) {
        return switch(campo) {
            case "id"               -> String.valueOf(c.getId());
            case "nombre"           -> c.getNombre();
            case "apellido"         -> c.getApellido();
            case "apodo"            -> c.getApodo();
            case "telefono"         -> c.getTelefono();
            case "email"            -> c.getEmail();
            case "direccion"        -> c.getDireccion();
            case "fecha_nacimiento" -> c.getFechaNacimiento();
            default -> throw new IllegalArgumentException("Campo no soportado: " + campo);
        };
    }
}
