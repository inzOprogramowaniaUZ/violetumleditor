/*
 Violet - A program for editing UML diagrams.

 Copyright (C) 2007 Cay S. Horstmann (http://horstmann.com)
 Alexandre de Pellegrin (http://alexdp.free.fr);

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.horstmann.violet.product.diagram.activity.node;


import com.horstmann.violet.framework.dialog.IRevertableProperties;
import com.horstmann.violet.framework.graphics.content.ContentBackground;
import com.horstmann.violet.framework.graphics.content.ContentBorder;
import com.horstmann.violet.framework.graphics.content.ContentInsideShape;
import com.horstmann.violet.framework.graphics.content.TextContent;
import com.horstmann.violet.framework.graphics.shape.ContentInsideCustomShape;
import com.horstmann.violet.framework.util.MementoCaretaker;
import com.horstmann.violet.framework.util.OneStringMemento;
import com.horstmann.violet.product.diagram.abstracts.Direction;
import com.horstmann.violet.product.diagram.abstracts.edge.IEdge;
import com.horstmann.violet.product.diagram.activity.ActivityDiagramConstant;
import com.horstmann.violet.product.diagram.common.node.ColorableNode;
import com.horstmann.violet.product.diagram.property.text.LineText;
import com.horstmann.violet.product.diagram.property.text.SingleLineText;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * An send event node_old in an activity diagram.
 */
public class SignalSendingNode extends ColorableNode implements IRevertableProperties
{
    /**
     * Construct an send event node_old with a default size
     */
    public SignalSendingNode()
    {
        super();
        signal = new SingleLineText();
        signal.setPadding(1,10,1,20);
        createContentStructure();
    }

    protected SignalSendingNode(SignalSendingNode node) throws CloneNotSupportedException
    {
        super(node);
        signal = node.signal.clone();
        createContentStructure();
    }

    @Override
    protected void beforeReconstruction()
    {
        super.beforeReconstruction();
        if(null == signal)
        {
            signal = new SingleLineText();
        }
        signal.reconstruction();
        signal.setPadding(1,10,1,20);
    }

    @Override
    protected SignalSendingNode copy() throws CloneNotSupportedException
    {
        return new SignalSendingNode(this);
    }

    @Override
    protected void createContentStructure()
    {
        TextContent signalContent = new TextContent(signal);
        signalContent.setMinHeight(MIN_HEIGHT);
        signalContent.setMinWidth(MIN_WIDTH);

        ContentInsideShape contentInsideShape = new ContentInsideCustomShape(signalContent, new ContentInsideCustomShape.ShapeCreator() {
            @Override
            public Shape createShape(double contentWidth, double contentHeight) {
                GeneralPath path = new GeneralPath();
                path.moveTo(0, 0);
                path.lineTo(contentWidth - MIN_HEIGHT / 2, 0);
                path.lineTo(contentWidth, contentHeight / 2);
                path.lineTo(contentWidth - MIN_HEIGHT / 2, contentHeight);
                path.lineTo(0, contentHeight);
                path.lineTo(0, 0);
                return path;
            }
        });
        setBorder(new ContentBorder(contentInsideShape, getBorderColor()));
        setBackground(new ContentBackground(getBorder(), getBackgroundColor()));
        setContent(getBackground());

        setTextColor(super.getTextColor());
    }

    @Override
    public void setTextColor(Color textColor)
    {
        signal.setTextColor(textColor);
        super.setTextColor(textColor);
    }

    @Override
    public String getToolTip()
    {
        return ActivityDiagramConstant.ACTIVITY_DIAGRAM_RESOURCE.getString("tooltip.signal_sending_node");
    }

    @Override
    public LineText getName() {
        return signal;
    }

    @Override
    public LineText getAttributes() {
        return null;
    }

    @Override
    public LineText getMethods() {
        return null;
    }

    @Override
    public Point2D getConnectionPoint(IEdge edge)
    {
        Point2D connectionPoint = super.getConnectionPoint(edge);

        if (Direction.WEST.equals(edge.getDirection(this).getNearestCardinalDirection()))
        {
            double offset = Math.abs(connectionPoint.getY() - getLocation().getY() - MIN_HEIGHT / 2);

            return new Point2D.Double(
                    connectionPoint.getX() - offset,
                    connectionPoint.getY()
            );
        }

        return connectionPoint;
    }

    @Override
    public boolean addConnection(IEdge edge)
    {
        if (edge.getEndNode() != null && this != edge.getEndNode())
        {
            return true;
        }
        return false;
    }

    /**
     * Sets the signal property value.
     * 
     * @param newValue the new signal description
     */
    public void setSignal(LineText newValue)
    {
        signal.setText(newValue.toEdit());
    }

    /**
     * Gets the signal property value.
     */
    public LineText getSignal()
    {
        return signal;
    }

    private final MementoCaretaker<OneStringMemento> caretaker = new MementoCaretaker<OneStringMemento>();

    @Override
    public void beforeUpdate()
    {
        caretaker.save(new OneStringMemento(signal.toString()));
    }

    @Override
    public void revertUpdate()
    {
        signal.setText(caretaker.load().getValue());
    }

    private SingleLineText signal;

    private static int MIN_WIDTH = 80;
    private static int MIN_HEIGHT = 40;
}
