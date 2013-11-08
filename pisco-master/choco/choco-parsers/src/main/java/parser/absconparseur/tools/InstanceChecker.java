/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.absconparseur.tools;

import choco.kernel.common.logging.ChocoLogging;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceChecker extends JFrame {

	protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	private static final long serialVersionUID = -3961722178499026262L;

	private InstanceCheckerEngine coder;

	private final MyPanel content = new MyPanel();

	private final Indicator indicator;

	public interface Indicator {
		public void write(String text);
	}

	static class IndicatorGUI implements Indicator {
		InstanceChecker.MyPanel content;

		public IndicatorGUI(InstanceChecker.MyPanel content) {
			this.content = content;
		}

		public void write(String text) {
			content.textArea.append(text);
			content.textArea.setAutoscrolls(true);
			JScrollBar scrollBar = content.scrollPane.getVerticalScrollBar();
			scrollBar.setValue(scrollBar.getMaximum());
		}
	}

	enum CHECKING_MODE {
		VALIDATION, CANONICAL, EXTENSIONAL;

		public static CHECKING_MODE getOperatorFor(final int mode) {
			if (mode == 1 || mode == 2)
				return VALIDATION;
			if (mode == 3)
				return CANONICAL;
			if (mode == 4)
				return EXTENSIONAL;
			LOGGER.severe("ERROR \t the mode is not correct");
			System.exit(1);
			return null;
		}
	}

	class MyPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		JTextField srcField = new JTextField(20);

		JTextField dstField = new JTextField(20);

		JButton srcButton = new JButton("Src");

		JButton dstButton = new JButton("Dst");

		JRadioButton validation = new JRadioButton("check validity of instances");

		JRadioButton validationPlus = new JRadioButton("check validity of instances (including additional rules of the 2008 competition of CSP solvers)");

		JRadioButton convert = new JRadioButton("check (not including additional 2008 competition rules) and convert instances into canonical form");

		JRadioButton toExtension = new JRadioButton("check (not including additional 2008 competition rules) and convert instances into canonical extensional form");

		ButtonGroup group = new ButtonGroup();

		JCheckBox defaultBox = new JCheckBox("Default file names");

		// JCheckBox fullControl = new JCheckBox("Control order");

		// JCheckBox resDir = new JCheckBox("Respect directories");

		JButton startButton = new JButton("Start");

		JButton stopButton = new JButton("Stop");

		JTextArea textArea = new JTextArea(18, 50);

		JScrollPane scrollPane = new JScrollPane(textArea);

		JTextField counter1Field = new JTextField("0", 3);

		JTextField counter2Field = new JTextField("0", 3);

		JTextField counter3Field = new JTextField("0", 3);

		File srcDirectory;

		File dstDirectory;

		private void manageStartButton() {
			startButton.setEnabled(srcDirectory != null && (validation.isSelected() || validationPlus.isSelected() || dstDirectory != null));
		}

		MyPanel() {
			final JPanel p1 = new JPanel();
			p1.add(srcButton);
			p1.add(srcField);
			srcField.setEnabled(false);
			srcField.setToolTipText("The source directory, if selected, is displayed in this field");
			srcButton.setToolTipText("Push this button in order to select the source directory, i.e. the directory which contains files that represent CSP instances.");
			final JPanel p2 = new JPanel();
			p2.add(dstButton);
			p2.add(dstField);
			dstField.setToolTipText("The destination directory, if selected, is displayed in this field");
			dstButton.setToolTipText("Push this button in order to select the destination directory, i.e. the directory which must contain files to be generated.");

			dstField.setEnabled(false);

			final JPanel p2b = new JPanel();
			p2b.setLayout(new GridLayout(4, 2, 5, 5));
			p2b.add(validation);
			validation.setToolTipText("Select this radio button in order to check files, with suffix xml, that are located in the source directory.");
			p2b.add(validationPlus);
			validationPlus.setToolTipText("Select this radio button in order to check files, with suffix xml, that are located in the source directory.");
			p2b.add(convert);
			convert.setToolTipText("Select this radio button in order to convert instances into canonical form.");
			p2b.add(toExtension);
			toExtension.setToolTipText("Select this radio button in order to convert instances into extensional form.");

			final JPanel p2t = new JPanel();
			p2t.setLayout(new BoxLayout(p2t, BoxLayout.X_AXIS));
			p2t.add(Box.createHorizontalGlue());
			p2t.add(p2b);
			p2t.add(Box.createHorizontalGlue());

			group.add(validation);
			group.add(validationPlus);
			group.add(convert);
			group.add(toExtension);
			validation.setSelected(true);
			final Border line = BorderFactory.createTitledBorder("Mode");
			final Border empty = new EmptyBorder(10, 10, 10, 10);
			p2b.setBorder(BorderFactory.createCompoundBorder(empty, line));

			final JPanel p3 = new JPanel();
			p3.add(defaultBox);
			defaultBox.setToolTipText("Select this box in order to generate files with a default file name as a prefix.");
			p3.add(new JLabel("    "));
			// p3.add(fullControl);
			// fullControl.setToolTipText("Select this box in order to control the asending order of values in domains and the lexicographic order of tuples in relations.");
			// fullControl.setSelected(true);
			// JPanel p3b = new JPanel();
			// p3b.add(resDir);
			final JPanel p4 = new JPanel();
			p4.add(startButton);
			startButton.setEnabled(false);
			startButton.setToolTipText("Push this button in order to start checking (and generation).");
			p4.add(stopButton);
			stopButton.setEnabled(false);
			stopButton.setToolTipText("Push this button in order to stop checking (and generation).");
			final JPanel p5 = new JPanel();
			textArea.setEditable(false);
			textArea.setFont(new Font("Serif", Font.PLAIN, 12));
			p5.add(scrollPane);
			textArea.setToolTipText("Area that gives information about checking and generation");

			final JPanel p6 = new JPanel();
			p6.setLayout(new BoxLayout(p6, BoxLayout.X_AXIS));
			p6.add(Box.createHorizontalGlue());
			p6.add(Box.createRigidArea(new Dimension(5, 0)));
			p6.add(new JLabel("Valid / Invalid / Ignored "));
			p6.add(Box.createRigidArea(new Dimension(5, 0)));
			p6.add(counter1Field);
			counter1Field.setToolTipText("Counter that gives the number of files that have been succesfully checked");
			counter1Field.setMaximumSize(new Dimension(15, 25));
			p6.add(Box.createRigidArea(new Dimension(5, 0)));
			p6.add(counter2Field);
			counter2Field.setToolTipText("Counter that gives the number of files that have been unsuccesfully checked");
			counter2Field.setMaximumSize(new Dimension(15, 25));
			p6.add(Box.createRigidArea(new Dimension(5, 0)));
			p6.add(counter3Field);
			counter3Field.setToolTipText("Counter that gives the number of files that have been ignored (suffix different from xml)");
			counter3Field.setMaximumSize(new Dimension(15, 25));
			p6.add(Box.createRigidArea(new Dimension(5, 0)));
			p6.add(Box.createHorizontalGlue());

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(p1);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(p2);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(p2t);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(p3);
			// add(Box.createRigidArea(new Dimension(0, 5)));
			// add(p3b);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(p4);
			add(Box.createRigidArea(new Dimension(0, 10)));
			add(p5);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(p6);
			add(Box.createRigidArea(new Dimension(0, 10)));

			validation.addChangeListener(new ChangeListener() {
				public void stateChanged(final ChangeEvent e) {
					if (validation.isSelected()) {
						defaultBox.setEnabled(false);
						defaultBox.setSelected(false);
						dstButton.setEnabled(false);
						dstField.setText("");
						dstDirectory = null;
						manageStartButton();
					} else if (!validationPlus.isSelected()) {
						defaultBox.setEnabled(true);
						dstButton.setEnabled(true);
						manageStartButton();
					}
				}

			});

			validationPlus.addChangeListener(new ChangeListener() {
				public void stateChanged(final ChangeEvent e) {
					if (validationPlus.isSelected()) {
						defaultBox.setEnabled(false);
						defaultBox.setSelected(false);
						dstButton.setEnabled(false);
						dstField.setText("");
						dstDirectory = null;
						manageStartButton();
					} else if (!validation.isSelected()) {
						defaultBox.setEnabled(true);
						dstButton.setEnabled(true);
						manageStartButton();
					}
				}

			});

			srcButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (chooser.showOpenDialog(InstanceChecker.this) == JFileChooser.APPROVE_OPTION) {
						final File f = chooser.getSelectedFile();
						if (!f.exists()) {
							srcDirectory = null;
							JOptionPane.showMessageDialog(InstanceChecker.this, "directory not found");
						} else {
							if (f.equals(dstDirectory) && defaultBox.isSelected())
								JOptionPane.showMessageDialog(null, "Please, with default file names, select a source directory different from the destination directory.");
							else {
								srcDirectory = f;
								srcField.setText(chooser.getSelectedFile().getPath());
							}
						}
						manageStartButton();
					}
				}
			});

			dstButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (chooser.showOpenDialog(InstanceChecker.this) == JFileChooser.APPROVE_OPTION) {
						final File f = chooser.getSelectedFile();
						if (f.equals(srcDirectory) && defaultBox.isSelected())
							JOptionPane.showMessageDialog(null, "Please, with default file names, select a destination directory different from the source directory.");
						else {
							dstDirectory = f;
							dstField.setText(chooser.getSelectedFile().getPath());
						}
					}
					manageStartButton();
				}
			});

			startButton.addActionListener(new ActionListener() {

				private CHECKING_MODE getMode() {
					if (validation.isSelected() || validationPlus.isSelected())
						return CHECKING_MODE.VALIDATION;
					if (convert.isSelected())
						return CHECKING_MODE.CANONICAL;
					return CHECKING_MODE.EXTENSIONAL;
				}

				public void actionPerformed(final ActionEvent e) {
					startButton.setEnabled(false);
					textArea.setText("");
					updateCounters(0, 0, 0);

					coder = new InstanceCheckerEngine(InstanceChecker.this, indicator, srcDirectory, dstDirectory, defaultBox.isSelected(), validationPlus.isSelected(), getMode());
					coder.start();
					// statutField.setText("running...");
					stopButton.setEnabled(true);
				}
			});

			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (coder == null)
						return;
					coder.setFinished(true);
					stopButton.setEnabled(false);
					// statutField.setText("finished");
				}
			});
		}
	}

	public void updateCounters(final int nb1, final int nb2, final int nb3) {
		content.counter1Field.setText(String.valueOf(nb1));
		content.counter2Field.setText(String.valueOf(nb2));
		content.counter3Field.setText(String.valueOf(nb3));
	}

	public void endOfCoder(final int nb1, final int nb2, final int nb3) {
		updateCounters(nb1, nb2, nb3);
		// content.statutField.setText("Finished in " + duration + " milliseconds");
		content.stopButton.setEnabled(false);
		content.startButton.setEnabled(true);
	}

	public InstanceChecker() {
		final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

		setTitle("InstanceChecker");
		setContentPane(content);
		indicator = new IndicatorGUI(content);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);
		setVisible(true);
	}

	public static void main(final String[] args) throws Exception {
		if (args.length == 2 || args.length == 3) {
			try {
				File file = new File(args[0]);
				if (file.isDirectory()) {
					LOGGER.severe("PROBLEM \t you must give the name of a file (and not the name of a directory)");
					System.exit(2);
				}
				if (!file.exists()) {
					LOGGER.severe("PROBLEM \t the file has not been found");
					System.exit(2);
				}

				file = file.getAbsoluteFile();
				final File srcDirectory = new File(file.getPath());

				final int arg = Integer.parseInt(args[1]);
				final boolean competitionControl = (arg == 2);

				final InstanceChecker.CHECKING_MODE mode = InstanceChecker.CHECKING_MODE.getOperatorFor(arg);

				Indicator indicator = new  Indicator() {
					public void write(String text) {
					}
				};
				final InstanceCheckerEngine coder = new InstanceCheckerEngine(null, indicator, srcDirectory, srcDirectory, false, competitionControl, mode);
				coder.setOverwriteDecided(true);
				coder.setOverwrite(args.length == 3 && (arg == 3 || arg == 4) && args[2].charAt(0) == 'y');
				try {
					coder.treat(file);
				} catch (final Exception e) {
					LOGGER.log(Level.SEVERE,"\t the instance is not valid: ", e);
					// e.printStackTrace();
					System.exit(1);
				}
				LOGGER.log(Level.INFO, "OK \t The instance is valid {0} including additional rules of the 2008 competition of CSP solvers)", (arg != 3 ? "- not " : "- " ));
				System.exit(0);
			} catch (final Throwable e) {
				LOGGER.severe("PROBLEM \t " + e.getMessage());
				e.printStackTrace();
				System.exit(2);
			}
		}
		if (args.length == 1 && args[0].toLowerCase().equals("gui"))
			new InstanceChecker();
		else {
			if(LOGGER.isLoggable(Level.INFO)) {
				final StringBuilder b = new StringBuilder(512);
				b.append("InstanceChecker ").append(InstanceParser.VERSION);
				b.append("Usage 1 : java ... InstanceChecker gui");
				b.append("Usage 2 : java ... InstanceChecker <instanceFileName> <mode> {<overwrite>}");
				b.append("With Usage 1, InstanceChecker displays a graphical user interface (gui)");
				b.append("With Usage 2, you run a simple command line:");
				b.append("  with mode = 1, you can check the validity of the given instance");
				b.append("  with mode = 2, you can check the validity of the given instance (including additional rules of the 2008 competition of CSP solvers)");
				b.append("  with mode = 3, you can check (not including additional 2008 competition rules) and convert the given instance into canonical form");
				b.append("  with mode = 4, you can check (not including additional 2008 competition rules) and convert the given instance into canonical extensional form");
				b.append("NB: with mode = 3 and mode = 4, you can indicate (set 'y' to overwrite) that you want to overwrite the given instance\n");
				b.append("Exit code of instanceChecker (with usage 2) is as follows:");
				b.append("  0 : no problem occurs and the instance is valid");
				b.append("  1 : the instance is not valid");
				b.append("  2 : a problem occurs (file not found, ...)");
				LOGGER.info( new String(b));
				System.exit(0);
			}

		}
	}
}
