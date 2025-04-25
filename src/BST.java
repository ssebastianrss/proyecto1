// BST.java
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Árbol Binario de Búsqueda que indexa por clave String
 * y almacena listas de IDs para cada clave.
 */
public class BST {

    // Nodo interno que guarda una clave, lista de IDs y referencias a hijos
    private static class Node {
        String key;             // Clave de indexación (p. ej. apodo, nombre)
        List<Integer> ids;      // Lista de IDs asociados a esta clave
        Node left, right;       // Hijos izquierdo y derecho

        // Constructor de nodo: inicializa la clave y agrega el primer ID
        Node(String key, int id) {
            this.key = key;
            this.ids = new ArrayList<>();
            this.ids.add(id);
        }
    }

    private Node root;  // Raíz del árbol

    // Constructor: crea un BST vacío
    public BST() {
        this.root = null;
    }

    /**
     * Inserta un par (key, id) en el BST.
     * Si la clave ya existe, añade el ID a la lista (sin duplicados).
     */
    public void insert(String key, int id) {
        root = insertRec(root, key, id);
    }

    // Recursión real de inserción: busca la posición y crea/actualiza el nodo
    private Node insertRec(Node node, String key, int id) {
        if (node == null) {
            return new Node(key, id);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insertRec(node.left, key, id);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, key, id);
        } else {
            // Clave ya existe: agregar ID si no está presente
            if (!node.ids.contains(id)) {
                node.ids.add(id);
            }
        }
        return node;
    }

    /**
     * Devuelve la lista de todos los IDs en recorrido por niveles (BFS).
     * Cada nodo contribuye con todos sus IDs.
     */
    public List<Integer> getAllIds() {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Queue<Node> q = new LinkedList<>();
        q.add(root);
        while (!q.isEmpty()) {
            Node cur = q.poll();
            result.addAll(cur.ids);
            if (cur.left  != null) q.add(cur.left);
            if (cur.right != null) q.add(cur.right);
        }
        return result;
    }

    /**
     * Serializa el árbol por niveles a un archivo:
     * - Incluye "null" solo para hijos directos de nodos reales.
     * - Recorta los null finales y deja un único marcador al final.
     * Ejemplo de salida: 1,2,3,null,4,6,5,7,null,null,null,9,null,10,null,8,null
     */
    public void saveToFileWithNulls(String filename) {
        int h = treeHeight(root);                           // Altura máxima del árbol
        List<String> entries = new ArrayList<>();
        Queue<AbstractMap.SimpleEntry<Node,Integer>> q = new LinkedList<>();
        q.add(new AbstractMap.SimpleEntry<>(root, 1));

        while (!q.isEmpty()) {
            var en = q.poll();
            Node cur = en.getKey();
            int lvl = en.getValue();
            // Añadir ID o marcador null
            entries.add(cur == null ? "null" : String.valueOf(cur.ids.get(0)));

            // Solo expandir hijos si el nodo es real y no se alcanza la altura máxima
            if (cur != null && lvl < h) {
                q.add(new AbstractMap.SimpleEntry<>(cur.left,  lvl + 1));
                q.add(new AbstractMap.SimpleEntry<>(cur.right, lvl + 1));
            }
        }

        // Detectar índice del último valor real
        int lastReal = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (!entries.get(i).equals("null")) {
                lastReal = i;
            }
        }
        // Conservar hasta el último real + un null de marcador
        int limit = Math.min(entries.size(), lastReal + 2);

        // Escribir en archivo hasta el límite calculado
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < limit; i++) {
                pw.print(entries.get(i));
                if (i < limit - 1) {
                    pw.print(",");
                }
            }
        } catch (IOException e) {
            System.err.println("Error guardando BST con nulls: " + e.getMessage());
        }
    }

    // Calcula recursivamente la altura (número de niveles) del árbol
    private int treeHeight(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(treeHeight(node.left), treeHeight(node.right));
    }
}
