/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.krivan.logic;

import java.io.PrintStream;
import java.util.Collection;

/**
 *
 * @author Balint
 */
public class TTCNOutput {

    private final Graph graph;
    private int indent = 0;
    private PrintStream out;

    public TTCNOutput(Graph g) {
        this.graph = g;
        this.out = System.out;
    }

    public void output() {
        for (Node n : graph.getVertices()) {
            if (n.getType() == Node.Type.CONTROL_STATE) {
                writeStateBlock(n);
            }
        }
    }

    private void writeStateBlock(Node n) {
        println(String.format("label %s {", n.getName()));
        indent += 2;
        writePorts(n);
        writeAlt(n);
        indent -= 2;
        println(String.format("}"));
        println("");
    }

    private void writePorts(Node n) {
    }

    private void writeAlt(Node n) {
        println("alt {");
        indent += 2;

        for (Edge e : graph.getOutEdges(n)) {
            println(String.format("[%s] receive(%s) {", e.hasPredicate() ? implode(e.getPredicates()) : "", implode(e.getInputs())));
            indent += 2;
            if (e.hasOutput()) {
                for (String o : e.getOutputs()) {
                    if (o.startsWith("CONN_") || o.startsWith("DISC_") || o.startsWith("DATA_")) {
                        println(String.format("portU.send(%s)", o));
                    } else {
                        println(String.format("portL.send(%s)", o));
                    }
                }
            }
            Node to = graph.getDest(e);
            println(String.format("goto %s", to.getType() == Node.Type.CONTROL_STATE ? graph.getDest(e).getName() : n.getName()));
            indent -= 2;
            println("}");
        }

        indent -= 2;
        println("}");
    }

    private void println(String s) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.println(s);
    }

    private String implode(String[] ary) {
        String out = "";
        for (int i = 0; i < ary.length; i++) {
            if (i != 0) {
                out += ", ";
            }
            out += ary[i];
        }
        return out;
    }

    private String implode(Predicate[] ary) {
        String out = "";
        for (int i = 0; i < ary.length; i++) {
            if (i != 0) {
                out += ", ";
            }
            out += ary[i].toString();
        }
        return out;
    }
}
