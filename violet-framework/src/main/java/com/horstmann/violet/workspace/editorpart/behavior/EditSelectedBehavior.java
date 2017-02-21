package com.horstmann.violet.workspace.editorpart.behavior;

import com.horstmann.violet.framework.dialog.DialogFactory;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.InjectedBean;
import com.horstmann.violet.framework.injection.resources.ResourceBundleInjector;
import com.horstmann.violet.framework.injection.resources.annotation.ResourceBundleBean;
import com.horstmann.violet.product.diagram.abstracts.IGraph;
import com.horstmann.violet.product.diagram.abstracts.edge.IEdge;
import com.horstmann.violet.product.diagram.abstracts.node.INamedNode;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.framework.dialog.IRevertableProperties;
import com.horstmann.violet.product.diagram.common.node.DiagramLinkNode;
import com.horstmann.violet.product.diagram.propertyeditor.CustomPropertyEditor;
import com.horstmann.violet.product.diagram.propertyeditor.ICustomPropertyEditor;
import com.horstmann.violet.workspace.editorpart.IEditorPart;
import com.horstmann.violet.workspace.editorpart.IEditorPartBehaviorManager;
import com.horstmann.violet.workspace.editorpart.IEditorPartSelectionHandler;
import com.horstmann.violet.workspace.spellchecker.SpellChecker;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class EditSelectedBehavior extends AbstractEditorPartBehavior
{
    public EditSelectedBehavior(IEditorPart editorPart)
    {
        BeanInjector.getInjector().inject(this);
        ResourceBundleInjector.getInjector().inject(this);
        this.editorPart = editorPart;
        this.graph = editorPart.getGraph();
        this.selectionHandler = editorPart.getSelectionHandler();
        this.behaviorManager = editorPart.getBehaviorManager();
        duplicateBehavior = new EditSelectedDuplicateBehavior();
    }

    @Override
    public void onMouseClicked(MouseEvent event)
    {
        boolean isButton1Clicked = (event.getButton() == MouseEvent.BUTTON1);
        if (event.getClickCount() > 1 && isButton1Clicked)
        {
            double zoom = editorPart.getZoomFactor();
            Point2D mouseLocation = new Point2D.Double(event.getX() / zoom, event.getY() / zoom);
            processSelection(mouseLocation);
            createSelectedItemEditMenu();
        }
    }

    /**
     * Selects double clicked element (inspired from SelectByClickBehavior class)
     * 
     * @param mouseLocation
     */
    private void processSelection(Point2D mouseLocation)
    {
        this.selectionHandler.clearSelection();
        INode node = this.graph.findNode(mouseLocation);
        IEdge edge = this.graph.findEdge(mouseLocation);
        if (edge != null)
        {
            processEdgeSelection(edge);
            return;
        }
        if (node != null)
        {
            processNodeSelection(node);
        }
    }

    private void processNodeSelection(INode node)
    {
        this.selectionHandler.addSelectedElement(node);
        if (this.selectionHandler.getSelectedNodes().size() == 1)
        {
            this.behaviorManager.fireOnNodeSelected(node);
        }
    }

    private void processEdgeSelection(IEdge edge)
    {
        this.selectionHandler.addSelectedElement(edge);
        if (this.selectionHandler.getSelectedEdges().size() == 1)
        {
            this.behaviorManager.fireOnEdgeSelected(edge);
        }
    }

    public void createSelectedItemEditMenu()
    {
        final Object edited;
        edited = getSelected();
        if (edited != null)
        {
            final ICustomPropertyEditor sheet = new CustomPropertyEditor(edited);
            addPropertyListener(edited, sheet);
            JOptionPane optionPane = createOptionPane(edited, sheet);
            String tooltip = handleSheetEdit(edited, sheet, optionPane);
            if(edited instanceof IRevertableProperties)
            {
               ((IRevertableProperties)edited).beforeUpdate();
            }
            this.dialogFactory.showDialog(optionPane, tooltip + this.dialogTitle, false);
        }
    }

    private Object getSelected()
    {
        final Object edited;
        if (selectionHandler.isNodeSelectedAtLeast())
        {
            edited = selectionHandler.getLastSelectedNode();
        }
        else
        {
            edited = selectionHandler.getLastSelectedEdge();
        }
        return edited;
    }

    private String handleSheetEdit(final Object edited, final ICustomPropertyEditor sheet, JOptionPane optionPane)
    {
        String tooltip = "";
        if (sheet.isEditable())
        {
            if (edited instanceof INode)
            {
                tooltip = ((INode) edited).getToolTip() + ": ";
                this.behaviorManager.fireBeforeEditingNode((INode) edited);
            }
            if (edited instanceof IEdge)
            {
                tooltip = ((IEdge) edited).getToolTip() + ": ";
                this.behaviorManager.fireBeforeEditingEdge((IEdge) edited);
            }
            optionPane.setMessage(sheet.getAWTComponent());
        }
        else
        {
            JLabel label = new JLabel(this.uneditableBeanMessage);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            optionPane.setMessage(label);
        }
        return tooltip;
    }

    private JOptionPane createOptionPane(final Object edited, final ICustomPropertyEditor sheet)
    {
        JOptionPane optionPane = new JOptionPane("",JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION);

        optionPane.setOpaque(true);

        optionPane.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if ((event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) && isInitialized(event.getNewValue()))
                {
                    if (sheet.isEditable())
                    {
                        if (edited instanceof INode)
                        {
                            behaviorManager.fireAfterEditingNode((INode) edited);
                        }
                        if (edited instanceof IEdge)
                        {
                            behaviorManager.fireAfterEditingEdge((IEdge) edited);
                        }
                        editorPart.getSwingComponent().invalidate();


                        if (edited instanceof IRevertableProperties)
                        {
                            if(event.getNewValue().equals(JOptionPane.CANCEL_OPTION))
                            {
                                ((IRevertableProperties)edited).revertUpdate();
                            }
                        }

                    }
                }
            }

            private boolean isInitialized(Object eventValue) {
                return eventValue != null && eventValue != JOptionPane.UNINITIALIZED_VALUE;
            }
        });
        return optionPane;
    }

    private void addPropertyListener(final Object edited, final ICustomPropertyEditor sheet)
    {
        sheet.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(final PropertyChangeEvent event)
            {
                if (edited instanceof INode)
                {
                    behaviorManager.fireWhileEditingNode((INode) edited, event);
                    checkCorrectnessOfString((INamedNode) edited);
                }
                if (edited instanceof IEdge)
                {
                    behaviorManager.fireWhileEditingEdge((IEdge) edited, event);
                }
                if (edited instanceof INamedNode)
                {
                    duplicateBehavior.detectDuplicatedNode((INamedNode) edited, graph.getAllNodes().iterator(),
                            event.getPropertyName());
                }
                editorPart.getSwingComponent().invalidate();
            }
        });
    }

    private void checkCorrectnessOfString(final INamedNode edited) {
        try {
            String nodeName = edited.getName().getText();
            String[] splitNameByUpperCase = getClearNodeName(nodeName);
            for(String word : splitNameByUpperCase) {
                boolean correctWord = SpellChecker.isCorrectWord(word);
                if (!correctWord){
                    edited.getName().setTextColor(Color.RED);
                }else{
                    edited.getName().setTextColor(Color.BLACK);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * Its returns node name without html tags
     *
     * @param nodeName with html tags
     * @return clear node name without html tags
     */
    @NotNull
    private String[] getClearNodeName(String nodeName) {

        final String regexSplitByUpperCase = "(?=\\p{Lu})";
        nodeName = nodeName.replaceAll("<html><font size=\\+1>", "").replaceAll("</font><html>", "");
        nodeName = nodeName.replaceAll("<html><center>\u00ABinterface\u00BB</center> <font size=\\+1>", "");
        nodeName = nodeName.replaceAll("<html><center>\u00ABenumeration\u00BB</center> <font size=\\+1>", "");

        return nodeName.split(regexSplitByUpperCase);
    }

    private IEditorPartSelectionHandler selectionHandler;
    private IEditorPart editorPart;
    private IGraph graph;
    private IEditorPartBehaviorManager behaviorManager;
    private EditSelectedDuplicateBehavior duplicateBehavior;
    @InjectedBean
    private DialogFactory dialogFactory;

    @ResourceBundleBean(key = "edit.properties.title")
    private String dialogTitle;

    @ResourceBundleBean(key = "edit.properties.empty_bean_message")
    private String uneditableBeanMessage;
}
