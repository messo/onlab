package hu.krivan.logic;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Balint
 */
public class Edge {

    private String[] inputs;
    private Predicate[] predicates;
    private Action[] actions;
    private String[] outputs;
    private String name;
    private Color color = Color.BLACK;
    private boolean disabled = false;
    // \\s*(\\s*(\\w)+\\s*,)
    private static final Pattern pattern = Pattern.compile("(?:\\{([^}]+)\\})?\\s*(?:\\[(.*?)\\])?\\s*/\\s*(?:([^{]+))?\\s*(?:\\{([^}]+)\\})?");
    private static final Pattern io = Pattern.compile("(?:\\s*([^,]+)\\s*,?)+");

    public Edge(String name) {
        Matcher m = pattern.matcher(name);
        if (m.matches()) {
            Matcher m2;
            String sInputs = m.group(1);
            if (sInputs != null) {
                inputs = sInputs.split("\\s*,\\s*");
            }
            String sPredicates = m.group(2);
            if (sPredicates != null) {
                String[] members = sPredicates.split("\\s*;\\s*");
                predicates = new Predicate[members.length];
                for (int i = 0; i < predicates.length; i++) {
                    predicates[i] = new Predicate(members[i]);
                }
            }
            String sActions = m.group(3);
            if (sActions != null) {
                String[] members = sActions.split("\\s*;\\s*");
                actions = new Action[members.length];
                for (int i = 0; i < actions.length; i++) {
                    String[] kv = members[i].split("\\s*=\\s*", 2);
                    actions[i] = new Action(kv[0], kv[1]);
                }
            }
            String sOutputs = m.group(4);
            if (sOutputs != null) {
                outputs = sOutputs.split("\\s*,\\s*");
            }
        } else {
            System.out.println(name);
            throw new RuntimeException("Nem jó az élek formátuma!");
        }

        this.name = name != null ? name : String.format("%s", hashCode());
    }

    public String getName() {
        return name;
    }

    public boolean hasInput() {
        return inputs != null && inputs.length > 0;
    }

    public String[] getInputs() {
        return inputs;
    }

    public boolean hasPredicate() {
        return predicates != null && predicates.length > 0;
    }

    public Predicate[] getPredicates() {
        return predicates;
    }

    public boolean hasAction() {
        return actions != null && actions.length > 0;
    }

    public Action[] getActions() {
        return actions;
    }

    public boolean hasOutput() {
        return outputs != null && outputs.length > 0;
    }

    public String[] getOutputs() {
        return outputs;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }
}
