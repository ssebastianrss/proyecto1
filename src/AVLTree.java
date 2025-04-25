import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Árbol AVL que indexa por clave String y almacena listas de IDs.
 * Mantiene equilibrio tras cada inserción para garantizar
 * alturas O(log n) y soporta serialización con marcadores null.
 */
public class AVLTree {
    // Nodo interno: guarda clave, lista de IDs, referencias a hijos y altura
    private static class Node {
        String key;
        List<Integer> ids;
        Node left, right;
        int height;

        // Crea un nodo con clave inicial y un ID en la lista
        Node(String key, int id) {
            this.key = key;
            this.ids = new ArrayList<>();
            this.ids.add(id);
            this.height = 1;  // altura inicial de un nodo hoja
        }
    }

    private Node root;  // raíz del árbol AVL

    // Constructor: árbol vacío al inicio
    public AVLTree() {
        this.root = null;
    }

    /**
     * Inserta la pareja (key, id) en el árbol.
     * Tras la inserción, recalcula alturas y aplica rotaciones si es necesario.
     */
    public void insert(String key, int id) {
        root = insert(root, key, id);
    }

    // Recursión principal de inserción y balanceo
    private Node insert(Node node, String key, int id) {
        if (node == null) {
            return new Node(key, id);  // inserta como hoja
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insert(node.left, key, id);
        } else if (cmp > 0) {
            node.right = insert(node.right, key, id);
        } else {
            // Mismo nodo: agregamos ID si no existe
            if (!node.ids.contains(id)) node.ids.add(id);
            return node;  // no cambiamos estructura
        }

        // Actualizar altura después de la inserción
        node.height = 1 + Math.max(height(node.left), height(node.right));
        int bf = getBalance(node);  // factor de balance

        // Rotaciones según el caso de desbalance
        if (bf > 1 && key.compareTo(node.left.key) < 0)        // LL
            return rightRotate(node);
        if (bf > 1 && key.compareTo(node.left.key) > 0) {      // LR
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        if (bf < -1 && key.compareTo(node.right.key) > 0)       // RR
            return leftRotate(node);
        if (bf < -1 && key.compareTo(node.right.key) < 0) {     // RL
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        return node;  // ya equilibrado
    }

    // Altura de un nodo, 0 si es null
    private int height(Node n) {
        return (n == null) ? 0 : n.height;
    }

    // Balance = altura(izq) - altura(der)
    private int getBalance(Node n) {
        return (n == null) ? 0 : height(n.left) - height(n.right);
    }

    // Rotación a la derecha (LL)
    private Node rightRotate(Node y) {
        Node x = y.left, T2 = x.right;
        x.right = y;  // pivote
        y.left = T2;
        // Actualizar alturas
        y.height = 1 + Math.max(height(y.left), height(y.right));
        x.height = 1 + Math.max(height(x.left), height(x.right));
        return x;  // nueva raíz de subárbol
    }

    // Rotación a la izquierda (RR)
    private Node leftRotate(Node x) {
        Node y = x.right, T2 = y.left;
        y.left = x;
        x.right = T2;
        // Actualizar alturas
        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));
        return y;  // nueva raíz de subárbol
    }

    /**
     * Recorre todo el árbol por niveles (BFS) y devuelve la lista
     * de IDs en el orden encontrado (cada nodo puede aportar varios IDs).
     */
    public List<Integer> getAllIds() {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Queue<Node> q = new LinkedList<>();
        q.add(root);
        while (!q.isEmpty()) {
            Node cur = q.poll();
            result.addAll(cur.ids);   // añadir todos los IDs del nodo actual
            if (cur.left != null)  q.add(cur.left);
            if (cur.right != null) q.add(cur.right);
        }
        return result;
    }

    /**
     * Serializa el árbol a un archivo usando recorrido por niveles:
     * - Incluye "null" para representar hijos ausentes.
     * - Recorta nulls sobrantes y deja un único marcador al final.
     */
    public void saveToFileWithNulls(String filename) {
        int h = treeHeight(root);  // altura total del árbol
        List<String> entries = new ArrayList<>();
        Queue<AbstractMap.SimpleEntry<Node,Integer>> q = new LinkedList<>();
        q.add(new AbstractMap.SimpleEntry<>(root,1));

        // Recorrido por niveles controlado por nivel máximo
        while (!q.isEmpty()) {
            AbstractMap.SimpleEntry<Node,Integer> en = q.poll();
            Node cur = en.getKey();
            int lvl = en.getValue();
            entries.add(cur == null ? "null" : String.valueOf(cur.ids.get(0)));
            if (lvl < h) {
                // Encolar hijos solo si el nodo existe
                if (cur != null) {
                    q.add(new AbstractMap.SimpleEntry<>(cur.left,  lvl+1));
                    q.add(new AbstractMap.SimpleEntry<>(cur.right, lvl+1));
                } else {
                    // Representar dos nulls como hijos de un null
                    q.add(new AbstractMap.SimpleEntry<>(null, lvl+1));
                    q.add(new AbstractMap.SimpleEntry<>(null, lvl+1));
                }
            }
        }

        // Detectar la posición del último valor real
        int lastReal = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (!entries.get(i).equals("null")) lastReal = i;
        }
        int limit = Math.min(entries.size(), lastReal + 2);  // +1 para un null de marcador

        // Escritura en el archivo hasta el límite calculado
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < limit; i++) {
                pw.print(entries.get(i));
                if (i < limit - 1) pw.print(",");
            }
        } catch (IOException e) {
            System.err.println("Error guardando AVL con nulls: " + e.getMessage());
        }
    }

    // Calcula la altura (niveles) de un subárbol dado
    private int treeHeight(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(treeHeight(node.left), treeHeight(node.right));
    }
}
