package hu.krivan;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import hu.krivan.logic.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
import org.apache.commons.collections15.functors.ConstantTransformer;

/**
 *
 * @author Balint
 */
public class Application extends javax.swing.JFrame {

    private final hu.krivan.logic.Graph graph;
    private final Collection<Edge> path;
    private final VisualizationViewer<Node, Edge> vv;
    private Iterator<Edge> pathIt;

    /**
     * Creates new form GraphFrameWithJUNG
     */
    public Application() {
        initComponents();
        Parser p = Parser.parse("graph.txt");
        graph = p.getGraph();

        Collection<String> mustVisit = new ArrayList<>();
        mustVisit.add("tettunk bele valamit");
        mustVisit.add("kivettunk belole valamit");
        mustVisit.add("van meg hely");
        mustVisit.add("van meg benne");

        //graph.reverseEdges();
        path = null;
        //path = graph.findPath(graph.getNodeByName("EMPTY/FULL"), graph.getNodeByName("INIT"), mustVisit);
        //graph.reverseEdges();

        // élek bejelölése
        for (String edgeName : mustVisit) {
            //Edge e = graph.getEdgeByName(edgeName);
            //e.setColor(Color.BLUE);
        }

        FRLayout<Node, Edge> l = new FRLayout<>(graph);
        vv = new VisualizationViewer<>(l, new Dimension(1028, 768));

        // graph.visualize(visualGraph, vv);

        vv.setGraphLayout(new StaticLayout<>(graph,
                TransformerUtils.mapTransformer(p.getLocations())));

        final DefaultModalGraphMouse<Node, Edge> gm = new DefaultModalGraphMouse<>();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(gm);

        vv.getRenderContext().setVertexLabelTransformer(new Transformer<Node, String>() {

            @Override
            public String transform(Node n) {
                return n.getName();
            }
        });
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge, String>() {

            @Override
            public String transform(Edge e) {
                String result = "<html>";
                if (e.hasInput()) {
                    result += "<font color=\"blue\"";
                    if (vv.getPickedEdgeState().getPicked().contains(e)) {
                        result += " bgcolor=\"orange\"";
                    }
                    result += ">";
                    StringBuilder sb = new StringBuilder();
                    for (String i : e.getInputs()) {
                        sb.append(i).append(", ");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);

                    result += sb.toString();
                    result += "</font> ";
                }

                if (e.hasPredicate()) {
                    String color = "<font color=\"red\">";
                    if (vv.getPickedEdgeState().getPicked().contains(e)) {
                        color = "<font color=\"green\">";
                    }
                    StringBuilder sb = new StringBuilder();
                    for (Predicate p : e.getPredicates()) {
                        sb.append(p.toHTML()).append(", ");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);
                    result += color + "[" + sb.toString() + "]";
                }

                if (e.hasOutput()) {
                    result += " <font color=\"green\"";
                    if (vv.getPickedEdgeState().getPicked().contains(e)) {
                        result += " bgcolor=\"orange\"";
                    }
                    result += ">";
                    StringBuilder sb = new StringBuilder();
                    for (String o : e.getOutputs()) {
                        sb.append(o).append(", ");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.deleteCharAt(sb.length() - 1);

                    result += sb.toString();
                    result += "</font> ";
                }

                return result;
            }
        });
        vv.setEdgeToolTipTransformer(new Transformer<Edge, String>() {

            @Override
            public String transform(Edge e) {
                StringBuilder sb = new StringBuilder("<html>");
                if (e.hasInput()) {
                    sb.append("<b>INPUT:</b><br>");
                    for (String input : e.getInputs()) {
                        sb.append("&middot; ").append(input).append("<br>");
                    }
                    sb.append("<br>");
                }
                if (e.hasPredicate()) {
                    sb.append("<b>PREDICATES:</b><br>");
                    for (Predicate pred : e.getPredicates()) {
                        sb.append("&middot; ").append(pred.toHTML()).append("<br>");
                    }
                    sb.append("<br>");
                }
                if (e.hasAction()) {
                    sb.append("<b>ACTIONS:</b><br>");
                    for (Action action : e.getActions()) {
                        sb.append("&middot; ").append(action).append("<br>");
                    }
                    sb.append("<br>");
                }
                if (e.hasOutput()) {
                    sb.append("<b>OUTPUTS:</b><br>");
                    for (String output : e.getOutputs()) {
                        sb.append("&middot; ").append(output).append("<br>");
                    }
                    sb.append("<br>");
                }
                return sb.toString();
            }
        });

        vv.getRenderContext().setLabelOffset(20);
        vv.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer(.5, .5));
        vv.getRenderContext().setVertexShapeTransformer(new VertexShapeTransformer(vv));
        vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<Node, Paint>() {

            @Override
            public Paint transform(Node n) {
                if (n.isDisabled()) {
                    return Color.LIGHT_GRAY;
                } else {
                    return Color.BLACK;
                }
            }
        });
        vv.getRenderContext().setVertexFillPaintTransformer(new ConstantTransformer(Color.WHITE));
        vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<Edge, Paint>() {

            @Override
            public Paint transform(Edge e) {
                if (vv.getPickedEdgeState().getPicked().contains(e)) {
                    return Color.GREEN;
                }
                if (e.isDisabled()) {
                    return Color.LIGHT_GRAY;
                } else {
                    return e.getColor();
                }
            }
        });
        vv.getRenderContext().setArrowFillPaintTransformer(new Transformer<Edge, Paint>() {

            @Override
            public Paint transform(Edge e) {
                if (e.isDisabled()) {
                    return Color.LIGHT_GRAY;
                } else {
                    if (vv.getPickedEdgeState().getPicked().contains(e)) {
                        return Color.GREEN;
                    } else {
                        return e.getColor();
                    }
                }
            }
        });
        vv.getRenderContext().setArrowDrawPaintTransformer(new Transformer<Edge, Paint>() {

            @Override
            public Paint transform(Edge e) {
                if (e.isDisabled()) {
                    return Color.GRAY;
                } else {
                    return Color.BLACK;
                }
            }
        });
        vv.getRenderer().setVertexLabelRenderer(new VertexLabelRenderer());
        vv.setBackground(Color.white);

        graphPanel.add(vv);
        
        new TTCNOutput(graph).output();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        graphPanel = new javax.swing.JPanel();
        getLocations = new javax.swing.JButton();
        findPathBtn = new javax.swing.JButton();
        saveBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(600, 600));

        getLocations.setText("Pozíciók kinyerése");
        getLocations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getLocationsActionPerformed(evt);
            }
        });

        findPathBtn.setText("Útkeresés");
        findPathBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findPathBtnActionPerformed(evt);
            }
        });

        saveBtn.setText("Mentés képként");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(getLocations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findPathBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveBtn)
                .addContainerGap(69, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getLocations)
                    .addComponent(findPathBtn)
                    .addComponent(saveBtn))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void getLocationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getLocationsActionPerformed
        for (Node n : graph.getVertices()) {
            Point2D loc = vv.getGraphLayout().transform(n);
            if (n.getType() == Node.Type.CONTROL_STATE) {
                System.out.printf("%s{{%.0f,%.0f}}\n", n, loc.getX(), loc.getY());
            } else {
                System.out.printf("%s{%.0f,%.0f}\n", n, loc.getX(), loc.getY());
            }
        }
    }//GEN-LAST:event_getLocationsActionPerformed

    private void findPathBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findPathBtnActionPerformed
        pathIt = path.iterator();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                Edge e = pathIt.next();

//                if (e.getColor() == Color.BLACK || e.getColor() == Color.BLUE) {
//                    e.setColor(Color.RED);
//                } else if (e.getColor() == Color.RED) {
//                    e.setColor(Color.ORANGE);
//                } else if (e.getColor() == Color.ORANGE) {
//                    e.setColor(Color.YELLOW);
//                } else if (e.getColor() == Color.YELLOW) {
//                    e.setColor(Color.MAGENTA);
//                }

                if (e.getColor() == Color.BLACK) {
                    e.setColor(Color.RED);
                }
                if (e.getColor() == Color.BLUE) {
                    e.setColor(Color.YELLOW);
                }
                vv.repaint();

                if (!pathIt.hasNext()) {
                    cancel();
                }
            }
        }, 0, 1000);
    }//GEN-LAST:event_findPathBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        int width = vv.getWidth();
        int height = vv.getHeight();

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        vv.paint(graphics);
        graphics.dispose();

        try {
            Calendar cal = Calendar.getInstance();
            ImageIO.write(bi, "png", new File(String.format("graph-%1$ts.png", cal)));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Application().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton findPathBtn;
    private javax.swing.JButton getLocations;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JButton saveBtn;
    // End of variables declaration//GEN-END:variables
}
