package com.horstmann.violet.application.autosave;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import javax.swing.*;
import com.horstmann.violet.application.gui.MainFrame;
import com.horstmann.violet.framework.file.GraphFile;
import com.horstmann.violet.framework.file.IFile;
import com.horstmann.violet.framework.file.LocalFile;
import com.horstmann.violet.framework.file.persistence.IFileReader;
import com.horstmann.violet.framework.file.persistence.JFileReader;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.file.IGraphFile;
import com.horstmann.violet.workspace.IWorkspace;
import com.horstmann.violet.workspace.Workspace;

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

	public AutoSave(MainFrame mainFrame) {
		BeanInjector.getInjector().inject(this);

		if (mainFrame != null) {
			this.mainFrame = mainFrame;
			if (createVioletDirectory()) {
				openAutoSaveProjects();
				initializeTimer();
			}
		}
	}

	private boolean createVioletDirectory() {
		File directory = new File(autoSaveDirectory);
		if (directory.isDirectory()) {
			return true;
		} else {
			return directory.mkdir();
		}
	}


	private void createSaverecoverDialog() {


		final JFrame autosaveFrame = new JFrame("Autosave recover");
		autosaveFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		//autosaveFrame.setSize(500, 300);
		//autosaveFrame.setLocation(dim.width / 2 - autosaveFrame.getSize().width / 2, dim.height / 2 - autosaveFrame.getSize().height / 2);

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());
		autosaveFrame.add(jPanel);
		String name = "Przywróć nie zapisany proejkt";
		String name2 = "Rozpocznij nowy projekt";
		JButton LoadAutoBtn = new JButton(name);
		LoadAutoBtn.setSize(100, 50);
		LoadAutoBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)


			{

				Loadsavetoworkscpae();

				autosaveFrame.dispose();
			}
		});
		JButton StartNewBtn = new JButton(name2);
		LoadAutoBtn.setSize(100, 50);
		StartNewBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

			removeautosavefile();
					autosaveFrame.dispose();

			}
		});
		jPanel.add(LoadAutoBtn);
		jPanel.add(StartNewBtn);
		autosaveFrame.setResizable(false);
		autosaveFrame.setAlwaysOnTop(true);
		autosaveFrame.setVisible(true);
	}

	private void Loadsavetoworkscpae()
	{
		File directory = new File(autoSaveDirectory);
		File[] files = directory.listFiles();
		for (File file : files) {

			try {

				IFile autoSaveFile = new LocalFile(file);
				IFileReader readFile = new JFileReader(file);
				InputStream in = readFile.getInputStream();
				if (in != null) {
					// IGraph graph = this.filePersistenceService.read(in);
					IGraphFile graphFile = new GraphFile(autoSaveFile);

					IWorkspace workspace = new Workspace(graphFile);

					mainFrame.addWorkspace(workspace);

					in.close();

				}
			} catch (IOException e) {
				file.delete();
			} catch (Exception e) {
				file.delete();
			}
		}
	}
	private void removeautosavefile()
	{
		File directory = new File(autoSaveDirectory);
		File[] files = directory.listFiles();
		for (File file : files) {


			file.delete();
		}
	}
	private void openAutoSaveProjects() {
		File directory = new File(autoSaveDirectory);
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files.length == 0)
				return;
			createSaverecoverDialog();

		}
	}

	private void initializeTimer() {
		saveTimer = new Timer(saveInterval, (ActionListener) this);
		saveTimer.setInitialDelay(0);
		saveTimer.start();
	}

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
