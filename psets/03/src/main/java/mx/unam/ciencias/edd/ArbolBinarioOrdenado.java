package mx.unam.ciencias.edd;

import java.util.Iterator;

/**
 * <p>Clase para árboles binarios ordenados. Los árboles son genéricos, pero
 * acotados a la interfaz {@link Comparable}.</p>
 *
 * <p>Un árbol instancia de esta clase siempre cumple que:</p>
 * <ul>
 *   <li>Cualquier elemento en el árbol es mayor o igual que todos sus
 *       descendientes por la izquierda.</li>
 *   <li>Cualquier elemento en el árbol es menor o igual que todos sus
 *       descendientes por la derecha.</li>
 * </ul>
 */
public class ArbolBinarioOrdenado<T extends Comparable<T>>
    extends ArbolBinario<T> {

    /* Clase interna privada para iteradores. */
    private class Iterador implements Iterator<T> {

        /* Pila para recorrer los vértices en DFS in-order. */
        private Pila<Vertice> pila;

        /* Construye un iterador con el vértice recibido. */
        public Iterador() {
            pila = new Pila<ArbolBinario<T>.Vertice>();
            if (esVacia()) { return; }
            Vertice v = raiz;
            while(v != null) {
                pila.mete(v);
                v = v.izquierdo;
            }
        }

        /* Nos dice si hay un elemento siguiente. */
        @Override public boolean hasNext() {
            return !pila.esVacia();
        }

        /* Regresa el siguiente elemento en orden DFS in-order. */
        @Override public T next() {
            Vertice v = pila.saca();
            Vertice vi;
            if(v.hayDerecho()) {
                vi = v.derecho;
                while(vi != null) {
                    pila.mete(vi);
                    vi = vi.izquierdo;
                }
            }
            return v.elemento;
        }
    }

    /**
     * El vértice del último elemento agegado. Este vértice sólo se puede
     * garantizar que existe <em>inmediatamente</em> después de haber agregado
     * un elemento al árbol. Si cualquier operación distinta a agregar sobre el
     * árbol se ejecuta después de haber agregado un elemento, el estado de esta
     * variable es indefinido.
     */
    protected Vertice ultimoAgregado;

    /**
     * Constructor sin parámetros. Para no perder el constructor sin parámetros
     * de {@link ArbolBinario}.
     */
    public ArbolBinarioOrdenado() { super(); }

    /**
     * Construye un árbol binario ordenado a partir de una colección. El árbol
     * binario ordenado tiene los mismos elementos que la colección recibida.
     * @param coleccion la colección a partir de la cual creamos el árbol
     *        binario ordenado.
     */
    public ArbolBinarioOrdenado(Coleccion<T> coleccion) {
        super(coleccion);
    }

    /**
     * Agrega un nuevo elemento al árbol. El árbol conserva su orden in-order.
     * @param elemento el elemento a agregar.
     */
    @Override public void agrega(T elemento) {
        if (elemento == null) { throw new IllegalArgumentException(); }
        if(this.esVacia()) {
            this.raiz = this.ultimoAgregado = new Vertice(elemento);
            this.elementos = 1;
        } else {
            this.agrega(this.raiz, elemento);
        }
    }

    /**
     * Método auxiliar recursivo que recibe un vértice actual
     * distinto de <code>null</code> y el nuevo vértice. Agrega
     * el nuevo elemento en orden.
     * @param vertice usado en la comparación.
     * @param elemento por agregar
     */
    private void agrega(Vertice vertice, T elemento) {
        if(elemento.compareTo(vertice.elemento) <= 0) {
            if(!vertice.hayIzquierdo()) {
                vertice.izquierdo = new Vertice(elemento);
                vertice.izquierdo.padre = vertice;
                this.ultimoAgregado = vertice.izquierdo;
                this.elementos += 1;
                return;
            }
            this.agrega(vertice.izquierdo, elemento);
        } else {
            if(!vertice.hayDerecho()) {
                vertice.derecho = new Vertice(elemento);
                vertice.derecho.padre = vertice;
                this.ultimoAgregado = vertice.derecho;
                this.elementos += 1;
                return;
            }
            this.agrega(vertice.derecho, elemento);
        }
    }

    /**
     * Elimina un elemento. Si el elemento no está en el árbol, no hace nada; si
     * está varias veces, elimina el primero que encuentre (in-order). El árbol
     * conserva su orden in-order.
     * @param elemento el elemento a eliminar.
     */
    @Override public void elimina(T elemento) {
        Vertice v = (ArbolBinario<T>.Vertice) this.busca(elemento);
        if(v == null) { return; }

        // Si es hoja
        if(!v.hayIzquierdo() && !v.hayDerecho()) {
            this.elementos -= 1;
            if(esHijoIzquierdo(v)) {
                v.padre.izquierdo = null;
            } else {
                v.padre.derecho = null;
            }
        }
        // Si sólo tiene un hijo
        else if(v.hayIzquierdo() ^ v.hayDerecho()) {
            eliminaVertice(v);
        }
        // Si ambos hijos existen
        else {
            Vertice vElim = intercambiaEliminable(v.izquierdo);
            vElim.padre.derecho = null;
            vElim.padre = null;
        }

    }

    /**
     * Regresa vértice máximo en árbol.
     * @param vertice inicial del árbol
     * @return vertice máximo.
     */
    private Vertice maxInSubtree(Vertice vertice) {
        while(vertice.hayDerecho()) {
            vertice = vertice.derecho;
        }
        return vertice;
    }

    /**
     * Intercambia el elemento de un vértice con dos hijos distintos de
     * <code>null</code> con el elemento de un descendiente que tenga a lo más
     * un hijo.
     * @param vertice un vértice con dos hijos distintos de <code>null</code>.
     * @return el vértice descendiente con el que vértice recibido se
     *         intercambió. El vértice regresado tiene a lo más un hijo distinto
     *         de <code>null</code>.
     */
    protected Vertice intercambiaEliminable(Vertice vertice) {
        Vertice u = maxInSubtree(vertice);
        Vertice aux = new Vertice(vertice.elemento);
        vertice.elemento = u.elemento;
        u.elemento = aux.elemento;
        return u;
    }

    /**
     * Elimina un vértice que a lo más tiene un hijo distinto de
     * <code>null</code> subiendo ese hijo (si existe).
     * @param vertice el vértice a eliminar; debe tener a lo más un hijo
     *                distinto de <code>null</code>.
     */
    protected void eliminaVertice(Vertice vertice) {
        if(!vertice.hayIzquierdo() && ! vertice.hayDerecho()) { return; }

        Vertice p = vertice.padre;
        Vertice u = vertice.hayIzquierdo() ? vertice.izquierdo : vertice.derecho;

        this.elementos -= 1;

        if(p == null) {
            this.raiz = u;
            return;
        }

        if(esHijoIzquierdo(vertice)) {
            p.izquierdo = u;
        } else {
            p.derecho = u;
        }
        u.padre = p;
    }

    /**
     * Busca un elemento en el árbol recorriéndolo in-order. Si lo encuentra,
     * regresa el vértice que lo contiene; si no, regresa <tt>null</tt>.
     * @param elemento el elemento a buscar.
     * @return un vértice que contiene al elemento buscado si lo
     *         encuentra; <tt>null</tt> en otro caso.
     */
    @Override public VerticeArbolBinario<T> busca(T elemento) {
        return busca(this.raiz, elemento);
    }

    /**
     * Método auxiliar que realiza la búsqueda de manera recursiva
     * @param vertice usado en la comparación
     * @param elemento buscado
     * @return vertice si existe, null si no.
     */
    private VerticeArbolBinario<T> busca(Vertice vertice, T elemento) {
        if(vertice == null) { return null; }
        if(vertice.elemento.equals(elemento)) { return vertice; }

        if(vertice.elemento.compareTo(elemento) < 0) {
            return busca(vertice.izquierdo, elemento);
        } else {
            return busca(vertice.derecho, elemento);
        }
    }

    /**
     * Regresa el vértice que contiene el último elemento agregado al
     * árbol. Este método sólo se puede garantizar que funcione
     * <em>inmediatamente</em> después de haber invocado al método {@link
     * agrega}. Si cualquier operación distinta a agregar sobre el árbol se
     * ejecuta después de haber agregado un elemento, el comportamiento de este
     * método es indefinido.
     * @return el vértice que contiene el último elemento agregado al árbol, si
     *         el método es invocado inmediatamente después de agregar un
     *         elemento al árbol.
     */
    public VerticeArbolBinario<T> getUltimoVerticeAgregado() {
        return ultimoAgregado;
    }

    /**
     * Gira el árbol a la derecha sobre el vértice recibido. Si el vértice no
     * tiene hijo izquierdo, el método no hace nada.
     * @param vertice el vértice sobre el que vamos a girar.
     */
    public void giraDerecha(VerticeArbolBinario<T> vertice) {
        // Aquí va su código.
    }

    /**
     * Gira el árbol a la izquierda sobre el vértice recibido. Si el vértice no
     * tiene hijo derecho, el método no hace nada.
     * @param vertice el vértice sobre el que vamos a girar.
     */
    public void giraIzquierda(VerticeArbolBinario<T> vertice) {
        // Aquí va su código.
    }

    /**
     * Realiza un recorrido DFS <em>pre-order</em> en el árbol, ejecutando la
     * acción recibida en cada elemento del árbol.
     * @param accion la acción a realizar en cada elemento del árbol.
     */
    public void dfsPreOrder(AccionVerticeArbolBinario<T> accion) {
        // Aquí va su código.
    }

    /**
     * Realiza un recorrido DFS <em>in-order</em> en el árbol, ejecutando la
     * acción recibida en cada elemento del árbol.
     * @param accion la acción a realizar en cada elemento del árbol.
     */
    public void dfsInOrder(AccionVerticeArbolBinario<T> accion) {
        // Aquí va su código.
    }

    /**
     * Realiza un recorrido DFS <em>post-order</em> en el árbol, ejecutando la
     * acción recibida en cada elemento del árbol.
     * @param accion la acción a realizar en cada elemento del árbol.
     */
    public void dfsPostOrder(AccionVerticeArbolBinario<T> accion) {
        // Aquí va su código.
    }

    /**
     * Regresa un iterador para iterar el árbol. El árbol se itera en orden.
     * @return un iterador para iterar el árbol.
     */
    @Override public Iterator<T> iterator() {
        return new Iterador();
    }
}
