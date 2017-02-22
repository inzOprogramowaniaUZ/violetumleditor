package com.horstmann.violet.product.diagram.classes.node;

import com.horstmann.violet.product.diagram.abstracts.node.INamedNode;
import java.awt.*;
import java.util.*;
import java.util.List;

import com.horstmann.violet.framework.graphics.Separator;
import com.horstmann.violet.framework.graphics.content.*;
import com.horstmann.violet.framework.graphics.shape.ContentInsideRectangle;
import com.horstmann.violet.framework.dialog.IRevertableProperties;
import com.horstmann.violet.framework.util.MementoCaretaker;
import com.horstmann.violet.framework.util.ThreeStringMemento;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.product.diagram.abstracts.node.IRenameableNode;
import com.horstmann.violet.product.diagram.classes.ClassDiagramConstant;
import com.horstmann.violet.product.diagram.common.node.ColorableNodeWithMethodsInfo;
import com.horstmann.violet.product.diagram.property.text.LineText;
import com.horstmann.violet.product.diagram.abstracts.node.INamedNode;
import com.horstmann.violet.product.diagram.property.text.MultiLineText;
import com.horstmann.violet.product.diagram.property.text.SingleLineText;
import com.horstmann.violet.product.diagram.property.text.decorator.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class node in a class diagram.
 */
public class ClassNode extends ColorableNodeWithMethodsInfo implements INamedNode, IRevertableProperties, IRenameableNode
{

    public static boolean classNameChange = false;
    /**
     * Construct a class node with a default size
     */
    public ClassNode()
    {
        super();
        name = new SingleLineText(NAME_CONVERTER);
        stereotype = new SingleLineText(STEREOTYPE_CONVERTER);
        name.setAlignment(LineText.CENTER);
        attributes = new MultiLineText(PROPERTY_CONVERTER);
        methods = new MultiLineText(PROPERTY_CONVERTER);
        comment= new MultiLineText(PROPERTY_CONVERTER);
        createContentStructure();
    }

    protected ClassNode(ClassNode node) throws CloneNotSupportedException
    {
        super(node);
        stereotype = node.stereotype.clone();
        name = node.name.clone();
        attributes = node.attributes.clone();
        methods = node.methods.clone();
        comment=node.comment.clone();
        createContentStructure();
    }

    @Override
    protected void beforeReconstruction()
    {
        super.beforeReconstruction();

        if (null == stereotype) {
            stereotype = new SingleLineText();
        }
        if(null == name)
        {
            name = new SingleLineText();
        }
        if(null == attributes)
        {
            attributes = new MultiLineText();
        }
        if(null == methods)
        {
            methods = new MultiLineText();
        }
        if(null == comment)
        {
            comment = new MultiLineText();
        }
        stereotype.reconstruction(STEREOTYPE_CONVERTER);
        name.reconstruction(NAME_CONVERTER);
        attributes.reconstruction(PROPERTY_CONVERTER);
        methods.reconstruction(PROPERTY_CONVERTER);
        comment.reconstruction(PROPERTY_CONVERTER);
        name.setAlignment(LineText.CENTER);
    }

    @Override
    protected INode copy() throws CloneNotSupportedException
    {
        return new ClassNode(this);
    }

    @Override
    protected void createContentStructure()
    {
        TextContent stereotypeContent = new TextContent(stereotype);
        TextContent nameContent = new TextContent(name);
        nameContent.setMinHeight(MIN_NAME_HEIGHT);
        nameContent.setMinWidth(MIN_WIDTH);
        TextContent commentContent = new TextContent(comment);

        VerticalLayout verticalGroupContent = new VerticalLayout();
        verticalGroupContent.add(stereotypeContent);
        verticalGroupContent.add(nameContent);
		if (VISIBLE_METHODS_AND_ATRIBUTES == true) {
			TextContent attributesContent = new TextContent(attributes);
			TextContent methodsContent = new TextContent(methods);
			verticalGroupContent.add(attributesContent);
			verticalGroupContent.add(methodsContent);
            verticalGroupContent.add(commentContent);
		}
        separator = new Separator.LineSeparator(getBorderColor());
        verticalGroupContent.setSeparator(separator);

        ContentInsideShape contentInsideShape = new ContentInsideRectangle(verticalGroupContent);

        setBorder(new ContentBorder(contentInsideShape, getBorderColor()));
        setBackground(new ContentBackground(getBorder(), getBackgroundColor()));
        setContent(getBackground());
        setTextColor(super.getTextColor());
    }

    @Override
    public void setBorderColor(Color borderColor)
    {
        if(null != separator)
        {
            separator.setColor(borderColor);
        }
        super.setBorderColor(borderColor);
    }

    @Override
    public void setTextColor(Color textColor)
    {
        stereotype.setTextColor(textColor);
        name.setTextColor(textColor);
        attributes.setTextColor(textColor);
        methods.setTextColor(textColor);
        comment.setTextColor(textColor);
        super.setTextColor(textColor);
    }

    @Override
    public String getToolTip()
    {
        return ClassDiagramConstant.CLASS_DIAGRAM_RESOURCE.getString("tooltip.class_node");
    }


    private final MementoCaretaker<ThreeStringMemento> caretaker = new MementoCaretaker<ThreeStringMemento>();

    @Override
    public void beforeUpdate() {
        caretaker.save(new ThreeStringMemento(name.toString(), attributes.toString(), methods.toString()));
    }

    @Override
    public void revertUpdate()
    {
        ThreeStringMemento memento = caretaker.load();

        name.setText(memento.getFirstValue());
        attributes.setText(memento.getSecondValue());
        methods.setText(memento.getThirdValue());
    }
    
	/**
	 * Edit visible boolean parameter to opposite value.
	 * And refers structure.
	 */
	@Override
	public void switchVisible() {
		VISIBLE_METHODS_AND_ATRIBUTES = !VISIBLE_METHODS_AND_ATRIBUTES;
		createContentStructure();
	}

    @Override
    public void replaceNodeOccurrences(String oldValue, String newValue) {
        super.replaceNodeOccurrences(oldValue, newValue);
        replaceNodeOccurrencesInAttributes(oldValue, newValue);
    }

    /**
     * Sets the stereotype property value.
     *
     * @param newValue the class name
     */
    public void setStereotype(LineText newValue) {
        stereotype.setText(newValue);
    }

    /**
     * Gets the stereotype property value.
     *
     * @return the class name
     */
    public LineText getStereotype() {
        return stereotype;
    }

    /**
     * Sets the name property value.
     *
     * @param newValue the class name
     */
    public void setName(LineText newValue)
    {
        if (classNameChange == true)
        {
            toBigLetter(getName());
        }
        else
        {
            name.setText(newValue);
        }
    }

    /**
     * Sets the name from big letter.
     *
     * @param newValue the class name
     */
    public void toBigLetter(LineText newValue)
    {
        String newName = newValue.toString().substring(0, 1).toUpperCase()
                         + getName().toString().substring(1);
        name.setText(newName);
    }

    /**
     * Sets the attributes property value.
     *
     * @param newValue the attributes of this class
     */
    public void setAttributes(LineText newValue)
    {
        attributes.setText(newValue);
    }

    /**
     * Gets the attributes property value.
     *
     * @return the attributes of this class
     */
    public LineText getAttributes()
    {
        return attributes;
    }

    /**
     * Sets the methods property value.
     *
     * @param newValue the methods of this class
     */
    public void setMethods(LineText newValue)
    {
        methods.setText(newValue);
    }

    /**
     * Gets the methods property value.
     *
     * @return the methods of this class
     */
    public LineText getMethods()
    {
        return methods;
    }

    /**
     * Replaces class name occurrences in attributes
     * @param oldValue old class name
     * @param newValue new class name
     */
    private void replaceNodeOccurrencesInAttributes(String oldValue, String newValue)
    {
        if (!getAttributes().toString().isEmpty()) {
            MultiLineText renamedAttributes = new MultiLineText();
            renamedAttributes.setText(renameAttributes(oldValue, newValue));
            setAttributes(renamedAttributes);
        }
    }

    /**
     * Finds all of oldValue class occurrences in attributes and replaces it with newValue
     * @param oldValue old class name
     * @param newValue new class name
     * @return attributes with renamed classes
     */
    private String renameAttributes(String oldValue, String newValue) {
        ArrayList<String> attributes = new ArrayList<String>(Arrays.asList(getAttributes().toEdit().split("\n")));
        StringBuilder renamedAttributes = new StringBuilder();
        Pattern pattern = Pattern.compile(".*:\\s*(" + oldValue + ")\\s*$");

        Iterator<String> iterator = attributes.iterator();
        while(iterator.hasNext()) {
            String attribute = iterator.next();
            StringBuffer attributeToRename = new StringBuffer(attribute);
            Matcher matcher = pattern.matcher(attribute);
            renamedAttributes.append(
                    (matcher.matches()
                            ? attributeToRename.replace(matcher.start(1), matcher.end(1), newValue)
                            : attribute)
            );

            if(iterator.hasNext()) {
                renamedAttributes.append("\n");
            }
        }

        return renamedAttributes.toString();
    }

    /**
     * Sets the methods property value.
     *
     * @param newValue the methods of this class
     */
    public void setComment(LineText newValue)
    {
        comment.setText(newValue);
    }

    /**
     * Gets the comment property value.
     *
     * @return the attributes of this class
     */
    public LineText getComment()
    {
        return comment;
    }

    private SingleLineText stereotype;
    private SingleLineText name;
    private MultiLineText attributes;
    private MultiLineText comment;

    private transient Separator separator;

    private static final int MIN_NAME_HEIGHT = 45;
    private static final int MIN_WIDTH = 100;
    private boolean VISIBLE_METHODS_AND_ATRIBUTES = true;
    private static final String STATIC = "\u00ABstatic\u00BB";
    private static final String ABSTRACT = "\u00ABabstract\u00BB";
    private static final String HIDE= "hide ";

    private static final String[][] SIGNATURE_REPLACE_KEYS = {
            { "public ", "+ " },
            { "package ", "~ " },
            { "protected ", "# " },
            { "private ", "- " },
            { "property ", "/ " },
            { "hide ", ""}
    };

    private static final List<String> STEREOTYPES = Arrays.asList(
            "«Utility»",
            "«Type»",
            "«Metaclass»",
            "«ImplementationClass»",
            "«Focus»",
            "«Entity»",
            "«Control»",
            "«Boundary»",
            "«Auxiliary»",
            ABSTRACT,
            HIDE
    );

    private static boolean containsLettersOnly(String text) {
        char[] chars = text.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * converts the stereotype from plain text to one that may contain decorators
     * @param text class stereotype
     */
    private static final LineText.Converter STEREOTYPE_CONVERTER = new LineText.Converter() {
        @Override
        public OneLineText toLineString(String text) {
            OneLineText controlText = null;
            char[] textCharTable = text.toCharArray();

            if (!containsLettersOnly(text)) {

            }
            else if (textCharTable[0]==u/00AB || text.equals("")) {
                controlText = new OneLineText(text);
            } else {
                String withBrackets = new String(u/00AB+ text + u/00BB);
                controlText = new OneLineText(withBrackets);
            }

            OneLineText lineString=new SmallSizeDecorator(controlText);

            return lineString;
        }
    };

    /**
     * converts class name from plain text to one that may contain decorators
     * @param text class name
     */
    private static final LineText.Converter NAME_CONVERTER = new LineText.Converter() {
        @Override
        public OneLineText toLineString(String text) {
            OneLineText controlText = new OneLineText(text);
            OneLineText lineString = new LargeSizeDecorator(controlText);

            if (controlText.contains(ABSTRACT)) {
                lineString = new ItalicsDecorator(lineString);
            }

            return lineString;
        }
    };

    private static final LineText.Converter PROPERTY_CONVERTER = new LineText.Converter()
    {
        @Override
        public OneLineText toLineString(String text)
        {
            OneLineText lineString = new OneLineText(text);

            if(lineString.contains(HIDE))
            {
                lineString = new HideDecorator(lineString);
            }

            if(lineString.contains(STATIC))
            {
                lineString = new UnderlineDecorator(new RemoveSentenceDecorator(lineString, STATIC));
            }
            for(String[] signature : SIGNATURE_REPLACE_KEYS)
            {
                lineString = new ReplaceSentenceDecorator(lineString, signature[0], signature[1]);
            }

            return lineString;
        }
    };
}
