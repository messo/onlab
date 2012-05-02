package hu.krivan.logic;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.*;

/**
 *
 * @author Balint
 */
public class Graph extends DirectedSparseMultigraph<Node, Edge> {

    private Map<String, Edge> edgesByName;
    private Map<String, Node> nodesByName = new HashMap<>();

    @Override
    public boolean addVertex(Node vertex) {
        boolean added = super.addVertex(vertex);
        if (added) {
            nodesByName.put(vertex.getName(), vertex);
        }
        return added;
    }

    public Node getNodeByName(String name) {
        return nodesByName.get(name);
    }

    public Edge getEdgeByName(String name) {
        return edgesByName.get(name);
    }

    /**
     * Útkeresés from-ból to-ba, szélességi bejárással
     *
     * @param from
     * @param to
     * @return
     */
    public Collection<Edge> findPath(Node from, Node to) {
        if (from.isDisabled() || to.isDisabled()) {
            return null; // tutira nincs út
        }

        //System.out.printf("## sima keresés: %s -> %s \n", from, to);
        Queue<Node> nodeQueue = new LinkedList<>();
        nodeQueue.add(from);

        // melyik csúcsba, melyik élen jutottunk el
        Map<Node, Edge> map = new HashMap<>();
        Map<Node, Integer> visited = new HashMap<>(nodeQueue.size());
        for (Node n : getVertices()) {
            visited.put(n, 0);
        }

        while (!nodeQueue.isEmpty()) {
            Node n = nodeQueue.poll();
            if (visited.get(n) == 2) {
                continue;
            } else {
                visited.put(n, 2);
            }

            if (n.equals(to)) {
                Node daddy = n;
                List<Edge> reversePath = new ArrayList<>();
                while (daddy != from) {
                    Edge e = map.get(daddy);
                    reversePath.add(e);
                    daddy = getSource(e);
                }

                // megfordítjuk
                List<Edge> path = new ArrayList<>(reversePath.size());
                for (int i = reversePath.size() - 1; i >= 0; i--) {
                    path.add(reversePath.get(i));
                }
                return path;
            }

            Collection<Edge> outEdges = getOutEdges(n);
            for (Edge e : outEdges) {
                if (e.isDisabled()) {
                    continue; // ha az él nem engedélyezett, akkor nem megyünk arra.
                }
                Node node = getDest(e);
                if (!node.isDisabled() && visited.get(node) == 0) {
                    map.put(node, e);
                    nodeQueue.add(node);
                    visited.put(node, 1);
                }
            }
        }

        return null;
    }

    /**
     * Út keresése v1-ből v2-be, adott élek érintésével
     *
     * @param v1 honnan
     * @param v2 hova
     * @param edges élek amiket érinteni kell
     */
    public Collection<Edge> findPath(Node v1, Node v2, Collection<String> edgeNamesToTraverse) {
        if (v1.isDisabled() || v2.isDisabled()) {
            return null; // tutira nincs út
        }

        System.out.printf("# Útkeresés [%s]-ből [%s]-be\n", v1, v2);

        if (edgeNamesToTraverse.isEmpty()) {
            // egyszerűsített esetre fallback
            return findPath(v1, v2);
        }

        if (edgesByName == null) {
            edgesByName = new HashMap<>();
            for (Node n : getVertices()) {
                for (Edge e : getOutEdges(n)) {
                    edgesByName.put(e.getName(), e);
                }
            }
        }

        System.out.println("# Élek amiket érinteni kell: ");
        List<Edge> edgesToTraverse = new ArrayList<>(edgeNamesToTraverse.size());
        for (String edgeName : edgeNamesToTraverse) {
            Edge e = edgesByName.get(edgeName);
            if (e.isDisabled()) {
                throw new IllegalArgumentException("Nem érinthetünk kikapcsolt élet!");
            }
            edgesToTraverse.add(e);
            System.out.println(e);
        }
        System.out.println("");

        boolean canStartFromV1 = false;
        for (int i = 0; i < edgesToTraverse.size(); i++) {
            Edge e = edgesToTraverse.get(i);
            if (getSource(e).equals(v1)) {
                Edge oldFirst = edgesToTraverse.get(0);
                edgesToTraverse.set(0, e);
                edgesToTraverse.set(i, oldFirst);
                canStartFromV1 = true;
                System.out.println("Indulhatunk v1-ből!");
                // szuper, akkor induljunk innen.
                break;
            }
        }

        List<Edge> path = new ArrayList<>();
        if (!canStartFromV1) {
            System.out.println("# Akkor keressünk utat v1-ből az első induló csúcsba...");
            path.addAll(findPath(v1, getSource(edgesToTraverse.get(0))));
            System.out.println("# done.");
        }

        // az edgesLeft sort kell elfogyasztani. Kiveszünk belőle egy élt
        // és megpróbálunk eljutni egy olyan élbe, ahonnan szintén megy ki még él.
        for (int i = 0; i < edgesToTraverse.size(); i++) {
            Edge e = edgesToTraverse.get(i); // ezen kell átmennünk!
            Edge next;
            if (edgesToTraverse.size() == i + 1) {
                next = null;
            } else {
                next = edgesToTraverse.get(i + 1);
            }
            Node to = (next != null) ? getSource(next) : v2;

            path.add(e);
            path.addAll(findPath(getDest(e), to));
        }

        return path;
    }

    public void reverseEdges() {
        List<Edge> copy = new ArrayList<>(getEdges()); // lemásoljuk a concurrent mod miatt
        for (Edge e : copy) {
            reverseEdge(e);
        }
    }

    public void reverseEdge(Edge edge) {
        Pair<Node> endpoints = getEndpoints(edge);
        Node from = endpoints.getSecond();
        Node to = endpoints.getFirst();
        removeEdge(edge);
        addEdge(edge, from, to);
    }

    public void list() {
        for (Node n : getVertices()) {
            for (Edge e : getOutEdges(n)) {
                System.out.println(e);
            }
        }
    }

    public void disableNode(Node node) {
        node.setDisabled(true);
        for (Edge e : getIncidentEdges(node)) {
            e.setDisabled(true);
        }
    }
}
