package com.horstmann.violet.product.diagram.abstracts.node;

import com.horstmann.violet.product.diagram.abstracts.IColorable;
import com.horstmann.violet.product.diagram.property.text.LineText;

/**
 * @author Sebastian Podgorski
 */
public interface INodeName extends INode, IColorable {

    LineText getName();
}