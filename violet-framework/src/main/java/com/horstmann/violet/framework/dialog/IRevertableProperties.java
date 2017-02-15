package com.horstmann.violet.framework.dialog;

public interface IRevertableProperties
{
    /**
     * Signalize implementing class that it should cache it's cache-able data at time of method call
     */
    void beforeUpdate();

    /**
     * Signalize implementing class that it should revert all changes to it's cache-able properties and assign them their
     * old cached values
     */
    //TODO: rename to something with more general meaning
    //TODO: introduce enum value to indicate what button user has clicked (rename method to afterUpdate)
    void revertUpdate();
}
