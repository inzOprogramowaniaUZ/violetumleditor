package com.horstmann.violet.workspace.editorpart.behavior;

import com.horstmann.violet.product.diagram.abstracts.IGraph;
import com.horstmann.violet.product.diagram.abstracts.IGridSticker;
import com.horstmann.violet.product.diagram.abstracts.Id;
import com.horstmann.violet.product.diagram.abstracts.edge.IEdge;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.workspace.editorpart.IEditorPart;
import com.horstmann.violet.workspace.editorpart.IEditorPartBehaviorManager;
import com.horstmann.violet.workspace.editorpart.IEditorPartSelectionHandler;
import com.horstmann.violet.workspace.sidebar.graphtools.GraphTool;
import com.horstmann.violet.workspace.sidebar.graphtools.IGraphToolsBar;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AddEdgeBehavior extends AbstractEditorPartBehavior
{

    public AddEdgeBehavior(IEditorPart editorPart, IGraphToolsBar graphToolsBar)
    {
        this.editorPart = editorPart;
        this.graph = editorPart.getGraph();
        this.grid = editorPart.getGraph().getGridSticker();
        this.selectionHandler = editorPart.getSelectionHandler();
        this.behaviorManager = editorPart.getBehaviorManager();
        this.graphToolsBar = graphToolsBar;
    }

    @Override
    public void onMousePressed(MouseEvent event)
    {
        if (!isConditionOK(event))
        {
            cancel();
            return;
        }
        if (!this.isLinkingInProgress)
        {
            startAction(event);
            return;
        }
        if (this.isLinkingInProgress && this.isLinkBySeparatedClicks)
        {
            if (isRecognizedAsTransitionAction())
            {
                transitionAction(event);
                return;
            }
            endAction(event);
            return;
        }
    }

    @Override
    public void onMouseDragged(MouseEvent event)
    {
        if (!this.isLinkingInProgress)
        {
            return;
        }
        repaintOnMouseMoved(event);
    }

    @Override
    public void onMouseMoved(MouseEvent event)
    {
        if (!this.isLinkingInProgress)
        {
            return;
        }
        this.isLinkBySeparatedClicks = true;
        repaintOnMouseMoved(event);
    }

    @Override
    public void onMouseReleased(MouseEvent event)
    {
        this.editorPart.getSwingComponent().invalidate();
        if (this.isLinkBySeparatedClicks)
        {
            return;
        }
        if (this.isLinkingInProgress)
        {
            endAction(event);
            return;
        }
    }

    private void repaintOnMouseMoved(MouseEvent event)
    {
        double zoom = this.editorPart.getZoomFactor();
        Point2D mousePoint = new Point2D.Double(event.getX() / zoom, event.getY() / zoom);
        Point2D snappedMousePoint = grid.snap(mousePoint);
        if (!snappedMousePoint.equals(lastMousePoint)) {
            this.editorPart.getSwingComponent().invalidate();
            this.editorPart.getSwingComponent().repaint();
        }
        this.lastMousePoint = snappedMousePoint;
    }

    private boolean isConditionOK(MouseEvent event)
    {
        if (event.getClickCount() > 1)
        {
            return false;
        }
        if (event.getButton() != MouseEvent.BUTTON1)
        {
            return false;
        }
        if (GraphTool.SELECTION_TOOL.equals(this.graphToolsBar.getSelectedTool()))
        {
            return false;
        }
        GraphTool selectedTool = this.selectionHandler.getSelectedTool();
        if (!IEdge.class.isInstance(selectedTool.getNodeOrEdge()))
        {
            return false;
        }
        return true;
    }

    private boolean isRecognizedAsTransitionAction()
    {
        if (this.newEdge == null)
        {
            return false;
        }
        if (!this.newEdge.isTransitionPointsSupported())
        {
            return false;
        }
        INode aNode = graph.findNode(this.lastMousePoint);
        if (aNode == null)
        {
            return true;
        }
        return false;
    }

    private void startAction(MouseEvent event)
    {
        double zoom = editorPart.getZoomFactor();
        final Point2D mousePoint = new Point2D.Double(event.getX() / zoom, event.getY() / zoom);
        INode targetNode = graph.findNode(mousePoint);
        this.isLinkingInProgress = (targetNode != null);
        this.firstMousePoint = grid.snap(mousePoint);
        this.lastMousePoint = grid.snap(mousePoint);
        GraphTool selectedTool = this.selectionHandler.getSelectedTool();
        IEdge prototype = (IEdge) selectedTool.getNodeOrEdge();
        this.newEdge = (IEdge) prototype.clone();
        this.newEdge.setId(new Id());
    }

    private void transitionAction(MouseEvent event)
    {
        this.transitionPoints.add(this.lastMousePoint);
    }

    private void endAction(MouseEvent event)
    {
        boolean added = addEdgeAtPoints(this.newEdge, firstMousePoint, lastMousePoint);
        if (added)
        {
            this.selectionHandler.setSelectedElement(this.newEdge);
        }
        this.isLinkingInProgress = false;
        this.isLinkBySeparatedClicks = false;
        this.transitionPoints.clear();
        this.newEdge = null;
        
    }

    private void cancel()
    {
        this.isLinkingInProgress = false;
        this.isLinkBySeparatedClicks = false;
        this.transitionPoints.clear();
        this.newEdge = null;
    }

    /**
     * Adds an edge at a specific location
     * 
     * @param newEdge
     * @param startPoint
     * @param endPoint
     * @return true id the edge has been added
     */
    public boolean addEdgeAtPoints(IEdge newEdge, Point2D startPoint, Point2D endPoint)
    {
        boolean isAdded = false;
        if (startPoint.distance(endPoint) > CONNECT_THRESHOLD)
        {
            this.behaviorManager.fireBeforeAddingEdgeAtPoints(newEdge, startPoint, endPoint);
            try
            {
                INode startNode = graph.findNode(startPoint);
                INode endNode = graph.findNode(endPoint);
                Point2D relativeStartPoint = null;
                Point2D relativeEndPoint = null;
                Point2D[] transitionPointsAsArray = this.transitionPoints.toArray(new Point2D[this.transitionPoints.size()]);
                if (startNode != null)
                {
                    Point2D startNodeLocationOnGraph = startNode.getLocationOnGraph();
                    double relativeStartX = startPoint.getX() - startNodeLocationOnGraph.getX();
                    double relativeStartY = startPoint.getY() - startNodeLocationOnGraph.getY();
                    relativeStartPoint = new Point2D.Double(relativeStartX, relativeStartY);
                }
                if (endNode != null)
                {
                    Point2D endNodeLocationOnGraph = endNode.getLocationOnGraph();
                    double relativeEndX = endPoint.getX() - endNodeLocationOnGraph.getX();
                    double relativeEndY = endPoint.getY() - endNodeLocationOnGraph.getY();
                    relativeEndPoint = new Point2D.Double(relativeEndX, relativeEndY);
                }
                if (graph.connect(newEdge, startNode, relativeStartPoint, endNode, relativeEndPoint, transitionPointsAsArray))
                {
                    newEdge.incrementRevision();
                    isAdded = true;
                }
            }
            finally
            {
                this.behaviorManager.fireAfterAddingEdgeAtPoints(newEdge, startPoint, endPoint);
            }
        }
        return isAdded;
    }

    @Override
    public void onPaint(Graphics2D g2)
    {
        if (!isLinkingInProgress)
        {
            return;
        }
        Color oldColor = g2.getColor();
        g2.setColor(PURPLE);
        GeneralPath path = new GeneralPath();
        path.moveTo(this.firstMousePoint.getX(), this.firstMousePoint.getY());
        for (Point2D aTransitionPoint : this.transitionPoints)
        {
            path.lineTo(aTransitionPoint.getX(), aTransitionPoint.getY());
        }
        path.lineTo(this.lastMousePoint.getX(), this.lastMousePoint.getY());
        g2.draw(path);
        g2.setColor(oldColor);
    }

    private static final Color PURPLE = new Color(0.7f, 0.4f, 0.7f);

    private static final int CONNECT_THRESHOLD = 8;

    private Point2D firstMousePoint = null;

    private Point2D lastMousePoint = null;
    
    private IEditorPart editorPart;

    private IGraph graph;
    
    private IGridSticker grid;

    private IEditorPartSelectionHandler selectionHandler;

    private IEditorPartBehaviorManager behaviorManager;

    private IGraphToolsBar graphToolsBar;

    private boolean isLinkingInProgress = false;

    private boolean isLinkBySeparatedClicks = false;

    private List<Point2D> transitionPoints = new ArrayList<Point2D>();

    private IEdge newEdge = null;

}
