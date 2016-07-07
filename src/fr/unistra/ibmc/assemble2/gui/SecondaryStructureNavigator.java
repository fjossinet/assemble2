package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.model.*;
import org.jdesktop.swingx.JXPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import fr.unistra.ibmc.assemble2.event.SelectionTransmitter;

import javax.swing.border.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.*;

public class SecondaryStructureNavigator extends JXPanel implements  SelectionTransmitter {

    private Mediator mediator;
    private Explorer explorer;
    private JPopupMenu popupMenu;

    public SecondaryStructureNavigator(final Mediator mediator) {
        this.setBackground(Color.WHITE);
        this.mediator = mediator;
        this.mediator.setSecondaryStructureNavigator(this);
        this.popupMenu = new JPopupMenu();
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.popupMenu.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }

    public void update() {
        explorer.updateUI();
    }

    public void reconstructTree() {
        this.removeAll();
        if (mediator.getSecondaryCanvas().getSecondaryStructure() != null) {
            this.explorer = new Explorer(new ExplorerModel(mediator.getSecondaryCanvas().getSecondaryStructure()));
            this.add(this.explorer);
            this.explorer.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent event) {
                }

                public void mousePressed(MouseEvent event) {
                    if (explorer.selectedNodes != null) {
                        popupMenu.removeAll();
                        if (explorer.selectedNodes.length == 1 && StructuralDomain.class.isInstance(((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject)) {
                            JMenuItem item = new JMenuItem("Rename");
                            popupMenu.add(item);
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent actionEvent) {
                                    String name = JOptionPane.showInputDialog(null,"New name",((StructuralDomain)((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject).getName());
                                    if (name != null && name.length() != 0) {
                                        ((StructuralDomain)((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject).setName(name);
                                        updateNode(((ExplorerNode)explorer.selectedNodes[0].getParentPath().getLastPathComponent()).wrappedObject,((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject);
                                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                                    }
                                }
                            });
                            popupMenu.add(item);
                        }
                        if (explorer.selectedNodes.length == 1 && Molecule.class.isInstance(((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject)) {
                            JMenuItem item = new JMenuItem("Rename");
                            popupMenu.add(item);
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent actionEvent) {
                                    String name = JOptionPane.showInputDialog(null,"New name",((Molecule)((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject).getName());
                                    if (name != null && name.length() != 0) {
                                        ((Molecule)((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject).setName(name);
                                        updateNode(((ExplorerNode)explorer.selectedNodes[0].getParentPath().getLastPathComponent()).wrappedObject,((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject);
                                        ((DefaultTableModel)mediator.getMoleculesList().getModel()).fireTableRowsUpdated(0,0); //we update the first item (the reference sequence) in the molecular list
                                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                                    }
                                }
                            });
                            popupMenu.add(item);
                        }

                        if (SecondaryStructure.class.isInstance(((ExplorerNode)explorer.selectedNodes[0].getLastPathComponent()).wrappedObject)) {





                        }
                        JMenuItem item = new JMenuItem("Clear Color");
                        popupMenu.add(item);
                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent actionEvent) {
                                for (TreePath path: explorer.selectedNodes)  {
                                    if (StructuralDomain.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject)) {
                                        StructuralDomain sd = (StructuralDomain) ((ExplorerNode) path.getLastPathComponent()).wrappedObject;
                                        sd.setCustomColor(null);
                                    } else if (BaseBaseInteraction.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject)) {
                                        BaseBaseInteraction bbi = (BaseBaseInteraction) ((ExplorerNode) path.getLastPathComponent()).wrappedObject;
                                        bbi.setCustomColor(null);
                                    }
                                }
                                mediator.getSecondaryCanvas().repaint();
                            }
                        });
                        popupMenu.add(item);
                        popupMenu.add(new ColorMenu("Set Color", explorer.selectedNodes));
                        maybeShowPopup(event);
                    }
                }

                public void mouseReleased(MouseEvent event) {
                    maybeShowPopup(event);
                }

                public void mouseEntered(MouseEvent event) {
                }

                public void mouseExited(MouseEvent event) {
                }
            });
            this.revalidate();
            this.doLayout();
        }
    }

    public void insertNode(Object parentContent, Object newContent) {
        if (this.explorer != null)
            this.explorer.insertNode(parentContent, newContent);
    }

    public void removeNode(Object parentContent, Object newContent) {
        if (this.explorer != null)
            this.explorer.removeNode(parentContent, newContent);
    }

    public void updateNode(Object parentContent, Object newContent) {
        if (this.explorer != null)
            this.explorer.updateNode(parentContent, newContent);
    }

    public void selectNode(Object content) {
        ExplorerNode hit = this.explorer.search(content,(ExplorerNode)this.explorer.getModel().getRoot());
        if (hit != null) {
            this.explorer.setSelectionPath(hit.getTreePath());
        }
    }

    public void addNode(Object content) {
        ExplorerNode hit = this.explorer.search(content,(ExplorerNode)this.explorer.getModel().getRoot());
        if (hit != null)
            this.explorer.addSelectionPath(hit.getTreePath());
    }

    public void clearSelection() {
        if (this.explorer != null)
            this.explorer.clearSelection();
    }

    public void interactionSelected(BaseBaseInteraction interaction, boolean isShiftDown) {
        if (!isShiftDown) {
            this.clearSelection();
            this.selectNode(interaction);
        }
        else
            this.addNode(interaction);
    }

    public void structuralDomainSelected(StructuralDomain structuralDomain, boolean isShiftDown) {
        if (!isShiftDown) {
            this.clearSelection();
            this.selectNode(structuralDomain);
        }
        else
            this.addNode(structuralDomain);
    }

    public void clear() {
        this.removeAll();
    }

    private class Explorer extends javax.swing.JTree implements javax.swing.event.TreeSelectionListener {

        private TreePath[] selectedNodes;

        Explorer(final ExplorerModel treeModel) {
            super(treeModel);
            putClientProperty("JTree.lineStyle", "Angled");
            setEditable(false);
            getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            this.addTreeSelectionListener(this);
            this.setCellRenderer(new ExplorerRenderer());
            this.setExpandsSelectedPaths(true);
        }

        public void valueChanged(final javax.swing.event.TreeSelectionEvent e) {
            this.selectedNodes = this.getSelectionPaths();
            if (this.selectedNodes != null) {
                mediator.getSecondaryCanvas().clearSelection();
                for (TreePath path: explorer.selectedNodes)  {
                    if (Helix.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject))
                        mediator.getSecondaryCanvas().selectHelix((Helix)((ExplorerNode)path.getLastPathComponent()).wrappedObject);
                    else if (SingleStrand.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject))
                        mediator.getSecondaryCanvas().selectSingleStrand((SingleStrand)((ExplorerNode)path.getLastPathComponent()).wrappedObject);
                    else if (BaseBaseInteraction.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject))
                        mediator.getSecondaryCanvas().selectBaseBaseInteraction((BaseBaseInteraction)((ExplorerNode)path.getLastPathComponent()).wrappedObject);
                }
                mediator.getSecondaryCanvas().repaint();
            }
        }

        private void insertNode(Object parentContent, Object newContent) {
            ExplorerNode parentNode=this.search(parentContent,(ExplorerNode)treeModel.getRoot());
            if (parentNode != null) {
                ExplorerNode _newNode = new ExplorerNode(newContent);
                ((ExplorerModel)treeModel).addNode(parentNode,_newNode);
                this.setSelectionPath(_newNode.getTreePath());
            }
        }

        private void removeNode(Object parentContent, Object content) {
            ExplorerNode _parentNode=this.search(parentContent,(ExplorerNode)treeModel.getRoot()),
                    _childNode=this.search(content,(ExplorerNode)treeModel.getRoot())  ;
            if (_parentNode != null && _childNode != null)
                ((ExplorerModel)treeModel).removeNode(_parentNode,_childNode);
        }

        private void updateNode(Object parentContent, Object content) {
            ExplorerNode _parentNode=this.search(parentContent,(ExplorerNode)treeModel.getRoot()),
                    _childNode=this.search(content,(ExplorerNode)treeModel.getRoot())  ;
            if (_parentNode != null && _childNode != null)
                ((ExplorerModel)treeModel).updateNode(_parentNode,_childNode);
        }

        private ExplorerNode search(Object content,ExplorerNode node) {
            ExplorerNode hit = null;
            if (node.wrappedObject == content)
                hit = node;
            else
                for (int i=0;i<node.children.size() && hit == null ;i++)
                    hit = this.search(content,node.getChildAt(i));
            return hit;
        }

    }

    private class ExplorerModel extends DefaultTreeModel {

        private ExplorerNode userSelections;
        private ExplorerNode secondaryStructure;
        private java.util.List<TreeModelListener> listeners;

        private ExplorerModel(final SecondaryStructure secondaryStructure) {
            super(new ExplorerNode("Assemble Model"));
            this.userSelections = new ExplorerNode("User Selections");
            ((ExplorerNode)this.getRoot()).addChild(this.userSelections);
            this.secondaryStructure = new ExplorerNode(secondaryStructure);
            ((ExplorerNode)this.getRoot()).addChild(this.secondaryStructure);
            this.listeners = new ArrayList<TreeModelListener>();
            Molecule m = secondaryStructure.getMolecule();
            ExplorerNode moleculeNode = new ExplorerNode(m);
            this.secondaryStructure.addChild(moleculeNode);
            List<StructuralDomain> sds = new ArrayList<StructuralDomain>();
            sds.addAll(secondaryStructure.getHelices());
            sds.addAll(secondaryStructure.getSingleStrands());
            Collections.sort(sds, new Comparator<StructuralDomain>() {
                @Override
                public int compare(StructuralDomain structuralDomain, StructuralDomain structuralDomain1) {
                    return structuralDomain.getLocation().getStart()-structuralDomain1.getLocation().getStart();
                }
            });
            for (StructuralDomain sd:sds)  {
                if (Helix.class.isInstance(sd)) {
                    ExplorerNode helixNode = new ExplorerNode(sd);
                    moleculeNode.addChild(helixNode);
                    for (BaseBaseInteraction interaction:((Helix)sd).getSecondaryInteractions())
                        helixNode.addChild(new ExplorerNode(interaction));
                } else
                    moleculeNode.addChild(new ExplorerNode(sd));
            }
            for (BaseBaseInteraction interaction:secondaryStructure.getTertiaryInteractions())
                moleculeNode.addChild(new ExplorerNode(interaction));
        }

        public Object getChild(final Object parent, final int index) {
            return ((ExplorerNode) parent).getChildAt(index);
        }

        public int getChildCount(final Object parent) {
            return ((ExplorerNode) parent).getChildCount();
        }

        public boolean isLeaf(final Object node) {
            return ((ExplorerNode) node).isLeaf();
        }

        public void valueForPathChanged(final javax.swing.tree.TreePath path, final Object newValue) {
        }

        public int getIndexOfChild(final Object parent, final Object child) {
            return ((TreeNode) parent).getIndex((TreeNode)child);
        }

        public void addTreeModelListener(TreeModelListener treeModelListener) {
            this.listeners.add(treeModelListener);
        }

        public void removeTreeModelListener(TreeModelListener treeModelListener) {
            this.listeners.remove(treeModelListener);
        }

        private void addNode(ExplorerNode parent, ExplorerNode child) {
            parent.addChild(child);
            for (TreeModelListener l:listeners)
                l.treeNodesInserted(new TreeModelEvent(this,parent.getTreePath(),new int[]{parent.children.indexOf(child)},new Object[]{child}));
        }

        private void removeNode(ExplorerNode parent, ExplorerNode child) {
            int index = parent.children.indexOf(child);
            parent.removeChild(child);
            for (TreeModelListener l:listeners)
                l.treeNodesRemoved(new TreeModelEvent(this,parent.getTreePath(),new int[]{index},new Object[]{child}));
        }

        private void updateNode(ExplorerNode parent, ExplorerNode child) {
            for (TreeModelListener l:listeners)
                l.treeNodesChanged(new TreeModelEvent(this,parent.getTreePath(),new int[]{parent.children.indexOf(child)},new Object[]{child}));
        }


    }

    private class ExplorerRenderer extends DefaultTreeCellRenderer {

        private ExplorerRenderer() {
            setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
            setForeground(Color.black);
            setVisible(true);
        }

        public java.awt.Component getTreeCellRendererComponent(final javax.swing.JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
            if (selected) {
                this.setOpaque(true);
                this.setBackground(Color.ORANGE);
            } else
                this.setOpaque(false);
            this.setText(value.toString());
            /*if (((ExplorerNode) value).wrappedObject instanceof SingleStrand) {
                this.setIcon(new PhotoIcon());
            } else if (((ExplorerNode) value).wrappedObject instanceof Helix) {
                this.setIcon(new ImageIcon(RessourcesUtils.getImage("helix-node.png")));
            } else if (((ExplorerNode) value).wrappedObject instanceof BaseBaseInteraction) {
                this.setIcon(new ImageIcon(RessourcesUtils.getImage("interaction-node.png")));
            } else if (((ExplorerNode) value).wrappedObject instanceof Molecule) {
                this.setIcon(new ImageIcon(RessourcesUtils.getImage("molecule-node.png")));
            } else if (((ExplorerNode) value).wrappedObject instanceof SecondaryStructure) {
                this.setIcon(new ImageIcon(RessourcesUtils.getImage("secondary-structure-node.png")));
            }
            else if (((ExplorerNode) value).wrappedObject instanceof String && (((ExplorerNode) value).wrappedObject).equals("User Selections")) {
                this.setIcon(new ImageIcon(RessourcesUtils.getImage("user.gif")));
            }
            else {
                this.setIcon(new ImageIcon(RessourcesUtils.getImage("node.png")));
            } */
            return this;
        }
    }

    private class ExplorerNode implements TreeNode, Comparable {
        private ExplorerNode parent;
        private java.util.List<ExplorerNode> children;
        private Object wrappedObject;

        private ExplorerNode(Object o) {
            this.children = new ArrayList<ExplorerNode>();
            this.wrappedObject = o;
        }

        public int getIndex(TreeNode child) {
            return this.children.indexOf(child);
        }

        public boolean isLeaf() {
            return this.children.size() == 0;
        }

        public int getChildCount() {
            return this.children.size();
        }

        public ExplorerNode getChildAt(int index) {
            return this.children.get(index);
        }

        private void addChild(ExplorerNode explorerNode) {
            this.children.add(explorerNode);
            explorerNode.parent = this;
        }

        private void removeChild(ExplorerNode explorerNode) {
            this.children.remove(explorerNode);
            explorerNode.parent = null;
        }

        public TreeNode getParent() {
            return this.parent;
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public Enumeration children() {
            return Collections.enumeration(this.children);
        }

        public String toString() {
            if (SecondaryStructure.class.isInstance(wrappedObject))
                return "2D";
            if (SingleHBond.class.isInstance(wrappedObject))
                return  "SingleHBond "+((BaseBaseInteraction)wrappedObject).getLocation().toString();
            else if (BaseBaseInteraction.class.isInstance(wrappedObject))
                return ((BaseBaseInteraction)wrappedObject).getOrientation()+""+((BaseBaseInteraction)wrappedObject).getEdge(((BaseBaseInteraction) wrappedObject).getResidue())+""+((BaseBaseInteraction)wrappedObject).getEdge(((BaseBaseInteraction) wrappedObject).getPartnerResidue())+" "+((BaseBaseInteraction)wrappedObject).getLocation().toString();
            else if (StructuralDomain.class.isInstance(wrappedObject))
                return wrappedObject.getClass().getSimpleName()+" "+((StructuralDomain)wrappedObject).getName()+" "+((StructuralDomain)wrappedObject).getLocation().toString();
            else if (Molecule.class.isInstance(wrappedObject))
                return "Molecule "+((Molecule)wrappedObject).getName();
            else if (String.class.isInstance(wrappedObject))
                return (String)wrappedObject;
            else
                return "";
        }

        public int compareTo(Object o) {
            if (Helix.class.isInstance(this.wrappedObject) && SingleStrand.class.isInstance(((ExplorerNode)o).wrappedObject))
                return -1;
            else if (SingleStrand.class.isInstance(this.wrappedObject) && Helix.class.isInstance(((ExplorerNode)o).wrappedObject))
                return 1;
            else if (Helix.class.isInstance(this.wrappedObject) && BaseBaseInteraction.class.isInstance(((ExplorerNode)o).wrappedObject))
                return -1;
            else if (BaseBaseInteraction.class.isInstance(this.wrappedObject) && Helix.class.isInstance(((ExplorerNode)o).wrappedObject))
                return 1;
            else if (BaseBaseInteraction.class.isInstance(this.wrappedObject) && SingleStrand.class.isInstance(((ExplorerNode)o).wrappedObject))
                return 1;
            else if (SingleStrand.class.isInstance(this.wrappedObject) && BaseBaseInteraction.class.isInstance(((ExplorerNode)o).wrappedObject))
                return -1;
            else if (Helix.class.isInstance(this.wrappedObject) && Helix.class.isInstance(((ExplorerNode)o).wrappedObject)) {
                Helix h1 = (Helix)this.wrappedObject, h2 = (Helix)((ExplorerNode)o).wrappedObject;
                int diff = h1.getLocation().getStart() - h2.getLocation().getStart();
                if (diff < 0)
                    return -1;
                else
                    return 1;
            }
            else if (BaseBaseInteraction.class.isInstance(this.wrappedObject) && BaseBaseInteraction.class.isInstance(((ExplorerNode)o).wrappedObject)) {
                BaseBaseInteraction bb1 = (BaseBaseInteraction)this.wrappedObject, bb2 = (BaseBaseInteraction)((ExplorerNode)o).wrappedObject;
                int diff = bb1.getResidue().getAbsolutePosition() - bb2.getResidue().getAbsolutePosition();
                if (diff < 0)
                    return -1;
                else
                    return 1;
            }
            else if (SingleStrand.class.isInstance(this.wrappedObject) && SingleStrand.class.isInstance(((ExplorerNode)o).wrappedObject)) {
                SingleStrand ss1 = (SingleStrand)this.wrappedObject, ss2 = (SingleStrand)((ExplorerNode)o).wrappedObject;
                int diff = ss1.getLocation().getStart() - ss2.getLocation().getStart();
                if (diff < 0)
                    return -1;
                else
                    return 1;
            }
            else return 0;

        }

        private TreePath getTreePath() {
            java.util.List<ExplorerNode> parents = new ArrayList<ExplorerNode>();
            parents.add(this);
            ExplorerNode _parent = this.parent;
            while (_parent != null) {
                parents.add(0,_parent);
                _parent = _parent.parent;
            }
            return new TreePath(parents.toArray(new ExplorerNode[]{}));
        }
    }

    private  class ColorMenu extends JMenu {
        protected Border unselectedBorder;

        protected Border selectedBorder;

        protected Border activeBorder;

        protected Hashtable paneTable;

        protected ColorPane colorPane;

        protected TreePath[] paths;

        public ColorMenu(String name, TreePath[] paths) {
            super(name);
            this.paths = paths;
            unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                    getBackground()), new BevelBorder(BevelBorder.LOWERED,
                    Color.white, Color.gray));
            selectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                    Color.red), new MatteBorder(1, 1, 1, 1, getBackground()));
            activeBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                    Color.blue), new MatteBorder(1, 1, 1, 1, getBackground()));

            JPanel p = new JPanel();
            p.setBorder(new EmptyBorder(5, 5, 5, 5));
            p.setLayout(new GridLayout(8, 8));
            paneTable = new Hashtable();

            int[] values = new int[] { 0 ,128, 192, 255 };

            for (int r = 0; r < values.length; r++) {
                for (int g = 0; g < values.length; g++) {
                    for (int b = 0; b < values.length; b++) {
                        Color c = new Color(values[r], values[g], values[b]);
                        ColorPane pn = new ColorPane(c);
                        p.add(pn);
                        paneTable.put(c, pn);
                    }
                }
            }
            add(p);
        }

        public void setColor(Color c) {
            Object obj = paneTable.get(c);
            if (obj == null)
                return;
            if (colorPane != null)
                colorPane.setSelected(false);
            colorPane = (ColorPane) obj;
            colorPane.setSelected(true);
        }

        public Color getColor() {
            if (colorPane == null)
                return null;
            return colorPane.getColor();
        }

        public void doSelection() {
            fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                    getActionCommand()));
        }

        private class ColorPane extends JPanel implements MouseListener {
            protected Color color;

            protected boolean isSelected;

            public ColorPane(Color c) {
                color = c;
                setBackground(c);
                setBorder(unselectedBorder);
                String msg = "R " + c.getRed() + ", G " + c.getGreen() + ", B "
                        + c.getBlue();
                setToolTipText(msg);
                addMouseListener(this);
            }

            public Color getColor() {
                return color;
            }

            public Dimension getPreferredSize() {
                return new Dimension(15, 15);
            }

            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            public void setSelected(boolean selected) {
                isSelected = selected;
                if (isSelected)
                    setBorder(selectedBorder);
                else
                    setBorder(unselectedBorder);
            }

            public boolean isSelected() {
                return isSelected;
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {
                setColor(color);
                MenuSelectionManager.defaultManager().clearSelectedPath();
                doSelection();
                for (TreePath path: paths)  {
                    if (StructuralDomain.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject)) {
                        StructuralDomain sd = (StructuralDomain) ((ExplorerNode) path.getLastPathComponent()).wrappedObject;
                        sd.setCustomColor(color);
                    }  else if (BaseBaseInteraction.class.isInstance(((ExplorerNode)path.getLastPathComponent()).wrappedObject)) {
                        BaseBaseInteraction bbi = (BaseBaseInteraction) ((ExplorerNode) path.getLastPathComponent()).wrappedObject;
                        bbi.setCustomColor(color);
                    }
                }
                mediator.getSecondaryCanvas().repaint();
                mediator.getAlignmentCanvas().repaint();
            }

            public void mouseEntered(MouseEvent e) {
                setBorder(activeBorder);
            }

            public void mouseExited(MouseEvent e) {
                setBorder(isSelected ? selectedBorder : unselectedBorder);
            }
        }
    }

}
