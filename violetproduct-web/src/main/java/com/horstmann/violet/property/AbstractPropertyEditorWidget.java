package com.horstmann.violet.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import com.horstmann.violet.framework.util.SerializableEnumeration;
import com.vaadin.sass.internal.util.Clonable;

import eu.webtoolkit.jwt.WCompositeWidget;
import eu.webtoolkit.jwt.WWidget;

public abstract class AbstractPropertyEditorWidget<T> extends WCompositeWidget {
	
	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
	
	private T oldValue;
	
	private T newValue;
	
	private Object bean;
	
	private PropertyDescriptor propertyDescriptor;
	
	public AbstractPropertyEditorWidget(Object bean, PropertyDescriptor propertyDescriptor) {
		this.bean = bean;
		this.propertyDescriptor = propertyDescriptor;
		setImplementation(getCustomEditor());
	}
	
    /**
     * A PropertyEditor may choose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A widget Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *      not supported.
     */

    protected abstract WWidget getCustomEditor();
    
    /**
     * Called when setValue() is invoked to refresh editor content
     */
    protected abstract void updateCustomEditor();
	
	/**
     * Set (or change) the object that is to be edited.  Primitive types such
     * as "int" must be wrapped as the corresponding object type such as
     * "java.lang.Integer".
     *
     * @param value The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     */
    public void setValue(T value) {
    	this.newValue = value;
    	if (!isKnownImmutable(type))
        {
            try
            {
                value = (T) value.getClass().getMethod("clone").invoke(value);
            }
            catch (Throwable t)
            {
                // we tried
            }
        }
    	this.oldValue = value;
    	updateCustomEditor();
    }
    
    private boolean isKnownImmutable(Class<?> type)
    {
        if (type.isPrimitive()) return true;
        if (SerializableEnumeration.class.isAssignableFrom(type)) return true;
        if (Clonable.class.isAssignableFrom(type)) return true;
        return false;
    }

    /**
     * Gets the property value.
     *
     * @return The value of the property.  Primitive types such as "int" will
     * be wrapped as the corresponding object type such as "java.lang.Integer".
     */

    public T getValue() {
    	return this.newValue;
    }
	
	
	/**
     * Adds a listener for the value change.
     * When the property editor changes its value
     * it should fire a {@link PropertyChangeEvent}
     * on all registered {@link PropertyChangeListener}s,
     * specifying the {@code null} value for the property name
     * and itself as the source.
     *
     * @param listener  the {@link PropertyChangeListener} to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    	this.listeners.add(listener);
    }

    /**
     * Removes a listener for the value change.
     *
     * @param listener  the {@link PropertyChangeListener} to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    	this.listeners.remove(listener);
    }
    
    protected void firePropertyChanged(T newValue) {
    	new PropertyChangeEvent(this.bean, this.propertyDescriptor.getName(), , newValue)
    }
    
    

}
