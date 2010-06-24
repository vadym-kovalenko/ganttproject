/*
 * Created on 01.05.2005
 */
package net.sourceforge.ganttproject.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author bard
 */
public abstract class TextFieldAndFileChooserComponent {
    private JButton myChooserButton;

    private JTextField myTextField;

    private File myFile;

    private FileFilter myFileFilter;

    private String myDialogCaption;

    private JComponent myComponent;

    private Component myParentComponent;

    private int myFileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES;

    private boolean myProcessTextEventEnabled = true;

    public TextFieldAndFileChooserComponent(final Component parentComponent,
            String dialogCaption) {
        myDialogCaption = dialogCaption;
        myParentComponent = parentComponent;
        initComponents();
    }

    public TextFieldAndFileChooserComponent(String label, String dialogCaption) {
        Box innerBox = Box.createHorizontalBox();
//        innerBox.add(new JLabel(label));
//        innerBox.add(Box.createHorizontalStrut(5));
        myParentComponent = innerBox;
        myDialogCaption = dialogCaption;
        initComponents();
        innerBox.add(myComponent);
        myComponent = innerBox;
    }

    private void initComponents() {
        myChooserButton = new JButton(new AbstractAction(GanttLanguage.getInstance().getText("fileChooser.browse")) {
            public void actionPerformed(ActionEvent e) {
                showFileChooser();
            }
        });
        myTextField = new JTextField();
        //myTextField.setColumns(40);
        myTextField.getDocument().addDocumentListener(new DocumentListener() {
            private final Timer myTimer = new Timer();
            private TimerTask myTimerTask = null;

            public void removeUpdate(DocumentEvent e) {
                onChange();
            }
            public void insertUpdate(DocumentEvent e) {
                onChange();
            }
            public void changedUpdate(DocumentEvent arg0) {
                onChange();
            }
            private void onChange() {
                if (myTimerTask == null) {
                    myTimerTask = new TimerTask() {
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    tryFile();
                                }
                            });
                            myTimerTask = null;
                        }
                    };
                    myTimer.schedule(myTimerTask, 1000);
                }
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(myTextField);
        box.add(Box.createHorizontalStrut(3));
        box.add(myChooserButton);
        myComponent = box;
    }

    public JComponent getComponent() {
        return myComponent;
    }

    public File getFile() {
        return myFile;
    }

    public void setFile(File file) {
        myProcessTextEventEnabled = false;
        myFile = file;
        myTextField.setText(file == null ? "" : file.getAbsolutePath());
        myProcessTextEventEnabled = true;
    }

    public void setFileFilter(FileFilter filter) {
        myFileFilter = filter;
    }

    public void showFileChooser() {
        JFileChooser fc = new JFileChooser(new File(myTextField.getText()));
        fc.setDialogTitle(myDialogCaption);
        fc.setApproveButtonToolTipText(myDialogCaption);
        fc.setFileSelectionMode(myFileSelectionMode);
        // Remove the possibility to use a file filter for all files
        FileFilter[] filefilters = fc.getChoosableFileFilters();
        for (int i = 0; i < filefilters.length; i++) {
            fc.removeChoosableFileFilter(filefilters[i]);
        }

        fc.addChoosableFileFilter(myFileFilter);
        int returnVal = fc.showDialog(myParentComponent, GanttLanguage
                .getInstance().getText("ok"));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            myFile = fc.getSelectedFile();
            myTextField.setText(myFile.getAbsolutePath());
            onFileChosen(myFile);
        }
    }

    public void tryFile() {
        onFileChosen(new File(myTextField.getText()));
    }

    protected abstract void onFileChosen(File file);

    public void setFileSelectionMode(int mode) {
        myFileSelectionMode = mode;
    }

    public URL getSelectedURL() {
        try {
            return new URL("file://" + myTextField.getText());
        } catch (MalformedURLException e) {
            GPLogger.log(e);
            return null;
        }
    }

    private void examineFile(File f) {
        if (!f.exists()) {
            showFileStatus(new Status(IStatus.ERROR, "foo", IStatus.ERROR, "File does not exist", null));
            return;
        }
        if (!f.canRead()) {
            showFileStatus(new Status(IStatus.ERROR, "foo", IStatus.ERROR, "File read error", null));
            return;
        }
        showFileStatus(new Status(IStatus.OK, "foo", IStatus.OK, "  ", null));
    }
    protected void showFileStatus(IStatus status) {

    }

}