package com.horstmann.violet.application.autosave;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import com.horstmann.violet.application.gui.MainFrame;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.file.IGraphFile;
import com.horstmann.violet.workspace.IWorkspace;

/**
 * Violet's auto save
 *
 * @author Pawel Majka
 */
public class AutoSave implements ActionListener {

    private MainFrame mainFrame;
    private Timer saveTimer;
    private final int second = 100;
    private final int saveInterval = 60 * second;
    private final String autoSaveDirectory = System.getProperty("user.home") + File.separator + "VioletUML";

    /**
     * Constructor Autosave
     *
     * @param mainFrame where is attached this menu
     */
    public AutoSave(MainFrame mainFrame) {
        BeanInjector.getInjector().inject(this);

        if (mainFrame != null) {
            this.mainFrame = mainFrame;
            if (createVioletDirectory()) {
                openAutoSaveDirectory();
                initializeTimer();
            }
        }
    }

    public String getAutoSaveDirectory() {
        return autoSaveDirectory;
    }

    /**
     * Create Violet directory
     *
     * @return true if path was created
     */
    private boolean createVioletDirectory() {
        File directory = new File(autoSaveDirectory);
        return directory.isDirectory() || directory.mkdir();
    }

    /**
     * Get autosave file in direcotry, if exist initialize recovery frame
     */
    private void openAutoSaveDirectory() {
        File directory = new File(autoSaveDirectory);
        emptyFileRemove();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files.length == 0)
                return;

            new AutoSaveRecover(mainFrame);


        }
    }

    /**
     * Remove Violet empty saves
     */
    private void emptyFileRemove() {
        File directory = new File(autoSaveDirectory);

        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.length() == 0)
                file.delete();

        }
    }

    /**
     * Initialize timer
     */
    private void initializeTimer() {
        saveTimer = new Timer(saveInterval, (ActionListener) this);
        saveTimer.setInitialDelay(0);
        saveTimer.start();
    }

    /**
     * Action Performed
     *
     * @param e event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        for (IWorkspace workspace : mainFrame.getWorkspaceList()) {
            IGraphFile graphFile = workspace.getGraphFile();
            if (graphFile.isSaveRequired()) {
                graphFile.autoSave();
            }
        }
    }

}