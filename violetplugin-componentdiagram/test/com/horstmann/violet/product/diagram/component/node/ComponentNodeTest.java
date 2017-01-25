package com.horstmann.violet.product.diagram.component.node;

import com.horstmann.violet.product.diagram.property.text.LineText;
import com.horstmann.violet.product.diagram.property.text.SingleLineText;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComponentNodeTest {
    private ComponentNode componentNode;


    @Before
    public void setUp() throws Exception {
         componentNode = new ComponentNode();
         componentNode.setText(new SingleLineText(LineText.DEFAULT_CONVERTER));
    }
    @Test
    public void beforeReconstruction() throws Exception {

    }

    @Test
    public void copy() throws Exception {

    }

    @Test
    public void createContentStructure() throws Exception {

    }

    @Test
    public void setTextColor() throws Exception {

    }

    @Test
    public void getToolTip() throws Exception {

    }

    @Test
    public void getConnectionPoint() throws Exception {

    }

    @Test
    public void addConnection() throws Exception {

    }

    @Test
    public void setText() throws Exception {

    }

    @Test
    public void getText() throws Exception {

    }

}