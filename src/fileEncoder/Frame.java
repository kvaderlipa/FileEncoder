package fileEncoder;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.awt.event.ActionEvent;

public class Frame extends JFrame {

	private static final long serialVersionUID = 4432572861809787818L;
	private JPanel contentPane;
	private JComboBox<String> comboBox;
	private JTextArea txtConsole;
	private JTextField txtInputFile;
	private JTextField txtOutputFile;
	private JTextField txtFileSize;
	private JTextField txtRNDSeed;
	private JTextField txtThreads;
	private JTextField txtKeyFile;
	private JButton btnKeyFile;
	private JButton btnOutputFile;
	private JButton btnInputFile;
	private JButton btnRun;
	private JLabel lblInputFile;
	private JLabel lblOutputFile;
	private JLabel lblFileSize;
	private JLabel lblBytes;
	private JLabel lblRndSeed;
	private JLabel lblThreads;
	private JLabel lblKeyFile;
	
	
	Frame frame;
	Color btnForegroudColor = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
					frame.frame = frame;
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Frame() {
		setTitle("FileEncoder");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 390, 367);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		
		lblInputFile = new JLabel("Input File:");
		
		lblOutputFile = new JLabel("Output file:");
		
		txtInputFile = new JTextField();
		txtInputFile.setEnabled(false);
		txtInputFile.setColumns(10);
		
		txtOutputFile = new JTextField();
		txtOutputFile.setEnabled(false);
		txtOutputFile.setColumns(10);
		
		txtKeyFile = new JTextField();
		txtKeyFile.setEnabled(false);
		txtKeyFile.setColumns(10);
		
		txtInputFile.setDropTarget(new DropTarget() {
		    public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<File> droppedFiles = (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            for (File file : droppedFiles) {
		                if(txtInputFile.isEnabled())
		                	txtInputFile.setText(file.getAbsolutePath());
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});
		txtOutputFile.setDropTarget(new DropTarget() {
		    public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<File> droppedFiles = (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            for (File file : droppedFiles) {
		            	if(txtOutputFile.isEnabled())
		            		txtOutputFile.setText(file.getAbsolutePath());
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});
		txtKeyFile.setDropTarget(new DropTarget() {
		    public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<File> droppedFiles = (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            for (File file : droppedFiles) {
		            	if(txtKeyFile.isEnabled())
		            		txtKeyFile.setText(file.getAbsolutePath());
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});
		
		final JProgressBar progressBar = new JProgressBar();
		
		final JFileChooser jFileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
		
		btnInputFile = new JButton("...");
		btnInputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				jFileChooser.setCurrentDirectory(new File(txtInputFile.getText()));
				if(jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					txtInputFile.setText(jFileChooser.getSelectedFile().toString());				
				}
			}
		});
		btnInputFile.setEnabled(false);
		
		btnRun = new JButton("RUN");
		btnForegroudColor = btnRun.getForeground();
		
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(btnRun.getText().equals("RUN")) {
					progressBar.setValue(0);
					
					btnRun.setText("CANCEL");
					btnRun.setForeground(Color.RED);
					
					FileEncoder.inProgress = true;
					FileEncoder.cancel = false;
					disableAll();
					// execute
					new Thread(new Runnable() {
					      public void run() {
					    	  try {
						    	  switch(comboBox.getSelectedIndex()) {
									case 0: //"--- CHOOSE ACTION ---"				
										break;
									case 1://BASE 2/16/64 ENCODE
									case 3:
									case 5:
											int base = 2;
											if(comboBox.getSelectedIndex()==3)
												base = 16;
											if(comboBox.getSelectedIndex()==5)
												base = 64;
											addConsole("BASE "+base+" Encryption started - "+txtInputFile.getText()+" - >"+txtOutputFile.getText());
											if(comboBox.getSelectedIndex()==1)	
												FileEncoder.BASE2enc(txtInputFile.getText(), txtOutputFile.getText());
											if(comboBox.getSelectedIndex()==3)
												FileEncoder.BASE16enc(txtInputFile.getText(), txtOutputFile.getText());
											if(comboBox.getSelectedIndex()==5)
												FileEncoder.BASE64enc(txtInputFile.getText(), txtOutputFile.getText());
											if(!FileEncoder.cancel) {
												addConsole("BASE "+base+" Encryption successfully finished");
												JOptionPane.showMessageDialog(frame, "BASE "+base+" Encryption successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
											}else {
												addConsole("BASE 2 Encryption CANCELLED by user");
												JOptionPane.showMessageDialog(frame, "BASE "+base+" Encryption CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
											}											
										break;										
									case 2://BASE 2/16/64 DECODE
									case 4:
									case 6:
											base = 2;
											if(comboBox.getSelectedIndex()==4)
												base = 16;
											if(comboBox.getSelectedIndex()==6)
												base = 64;
											addConsole("BASE "+base+" Decryption started - "+txtInputFile.getText()+" - >"+txtOutputFile.getText());
											if(comboBox.getSelectedIndex()==2)	
												FileEncoder.BASE2dec(txtInputFile.getText(), txtOutputFile.getText());
											if(comboBox.getSelectedIndex()==4)
												FileEncoder.BASE16dec(txtInputFile.getText(), txtOutputFile.getText());
											if(comboBox.getSelectedIndex()==6)
												FileEncoder.BASE64dec(txtInputFile.getText(), txtOutputFile.getText());
											if(!FileEncoder.cancel) {
												addConsole("BASE "+base+" Decryption successfully finished");
												JOptionPane.showMessageDialog(frame, "BASE "+base+" Decryption successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
											}else {
												addConsole("BASE "+base+" Decryption finished with ERROR");
												JOptionPane.showMessageDialog(frame, "BASE "+base+" Decryption CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
											}
										break;									
									case 7:	//"FILE ANALYSIS"	
										addConsole("File Analysis started ("+txtFileSize.getText()+" bit tuples) - analyzing "+txtInputFile.getText()+" - >"+txtOutputFile.getText());
										PrintStream p = new PrintStream(txtOutputFile.getText());
										FileEncoder.GET_FILE_FREQUENCY(txtInputFile.getText(), p, Integer.parseInt(txtFileSize.getText()));
										p.close();
										if(!FileEncoder.cancel) {
											addConsole("File analyzing sussfully finished");
											JOptionPane.showMessageDialog(frame, "File analyzing sussfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("File analyzing finished with ERROR");
											JOptionPane.showMessageDialog(frame, "File analyzing CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;
									case 8:	//"GENERATE RANDOM DATA"
										addConsole("Generate random DATA started (SEED "+txtRNDSeed.getText()+") - "+txtFileSize.getText()+" Bytes - >"+txtOutputFile.getText());
										FileEncoder.GEN_RANDOM_DATA_FILE(txtOutputFile.getText(), Integer.parseInt(txtFileSize.getText()), Integer.parseInt(txtRNDSeed.getText()));
										if(!FileEncoder.cancel) {
											addConsole("Generating random DATA successfully finished");
											JOptionPane.showMessageDialog(frame, "Generating random DATA successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("Generating random DATA finished with ERROR");
											JOptionPane.showMessageDialog(frame, "Generating random DATA CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;
									case 9:	//"GENERATE SECURE RANDOM DATA"
										addConsole("Generate SECURE random DATA started (THREAD COUNT "+txtThreads.getText()+") - "+txtFileSize.getText()+" Bytes - >"+txtOutputFile.getText());
										FileEncoder.GEN_SECURE_RANDOM_DATA_FILE(txtOutputFile.getText(), Integer.parseInt(txtFileSize.getText()), Integer.parseInt(txtThreads.getText()));
										if(!FileEncoder.cancel) {
											addConsole("Generating SECURE random DATA successfully finished");
											JOptionPane.showMessageDialog(frame, "Generating SECURE random DATA successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("Generating SECURE random DATA finished with ERROR");
											JOptionPane.showMessageDialog(frame, "Generating SECURE random DATA CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;
									case 10://"ONE TIME PAD - FILE KEY"
										addConsole("ONE TIME PAD File encryption (KEY "+txtKeyFile.getText()+") - "+txtInputFile.getText()+" - >"+txtOutputFile.getText());
										FileEncoder.ONE_TIME_PAD_FILE_KEY(txtInputFile.getText(), txtOutputFile.getText(), txtKeyFile.getText());
										if(!FileEncoder.cancel) {
											addConsole("ONE TIME PAD ecryption successfully finished");
											JOptionPane.showMessageDialog(frame, "ONE TIME PAD ecryption successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("ONE TIME PAD ecryption finished with ERROR");
											JOptionPane.showMessageDialog(frame, "ONE TIME PAD ecryption CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;
									case 11://"ONE TIME PAD - RND STREAM KEY"
										addConsole("ONE TIME PAD File encryption (SEED "+txtRNDSeed.getText()+") - "+txtInputFile.getText()+" - >"+txtOutputFile.getText());
										FileEncoder.ONE_TIME_PAD(txtInputFile.getText(), txtOutputFile.getText(), Integer.parseInt(txtRNDSeed.getText()));
										if(!FileEncoder.cancel) {
											addConsole("ONE TIME PAD ecryption successfully finished");
											JOptionPane.showMessageDialog(frame, "ONE TIME PAD ecryption successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("ONE TIME PAD ecryption finished with ERROR");
											JOptionPane.showMessageDialog(frame, "ONE TIME PAD ecryption CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;	
									case 12://"STEG - ENCODE INTO IMAGE
										addConsole("STEG - ENCODE INTO IMAGE (SEED "+txtRNDSeed.getText()+") - "+txtInputFile.getText()+" + "+txtKeyFile.getText()+" - >"+txtOutputFile.getText());
										int bitCount = FileEncoder.ENCODE_FILE_TO_IMAGE(txtKeyFile.getText(), txtInputFile.getText(), txtOutputFile.getText(), Long.parseLong(txtRNDSeed.getText()));										
										if(!FileEncoder.cancel) {
											addConsole("STEG - ENCODE INTO IMAGE successfully finished ("+bitCount+" bit used)");
											JOptionPane.showMessageDialog(frame, "STEG - ENCODE INTO IMAGE successfully finished ("+bitCount+" bit used)", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("STEG - ENCODE INTO IMAGE finished with ERROR");
											JOptionPane.showMessageDialog(frame, "STEG - ENCODE INTO IMAGE CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;
									case 13://"STEG - DECODE FROM IMAGE
										addConsole("STEG - DECODE FROM IMAGE (SEED "+txtRNDSeed.getText()+") - "+txtInputFile.getText()+" - >"+txtOutputFile.getText());
										FileEncoder.DECODE_FILE_FROM_IMAGE(txtInputFile.getText(), txtOutputFile.getText(), Long.parseLong(txtRNDSeed.getText()));										
										if(!FileEncoder.cancel) {
											addConsole("STEG - DECODE FROM IMAGE successfully finished");
											JOptionPane.showMessageDialog(frame, "STEG - DECODE FROM IMAGE successfully finished", "OK - success", JOptionPane.INFORMATION_MESSAGE);
										}else {
											addConsole("STEG - DECODE FROM IMAGE finished with ERROR");
											JOptionPane.showMessageDialog(frame, "STEG - DECODE FROM IMAGE CANCELLED by user", "ERROR", JOptionPane.ERROR_MESSAGE);
										}
										break;
								}
					    	  }catch(Exception ex) {
									addConsole(ex.toString());
									JOptionPane.showMessageDialog(frame, "Error during execution: "+ex.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
									FileEncoder.inProgress = false;
					    	  }
					      }
					    }).start();
					
					new Thread(new Runnable() {
					      public void run() {
					        while(true) {

					          // Runs inside of the Swing UI thread
					          SwingUtilities.invokeLater(new Runnable() {
					            public void run() {					            	
					            	progressBar.setValue(FileEncoder.progress);
					            	if(!FileEncoder.inProgress) {
					            		progressBar.setValue(0);
					            		btnRun.setText("RUN");
					            		btnRun.setForeground(btnForegroudColor);
					            		enableSelected(comboBox.getSelectedIndex());
					            	}
					            }
					          });

					          if(!FileEncoder.inProgress)
					        	  break;
					          
					          try {
					            java.lang.Thread.sleep(333);
					          }catch(Exception e) { }
					        }
					      }
					    }).start();
					
				}else {//cancel
					if(JOptionPane.showConfirmDialog(frame, "Do you really want to cancel process? (Outputs will be not consistent).")==0) {						
						FileEncoder.cancel = true;
						btnRun.setEnabled(false);
						btnRun.setText("CANCELLING...");
					}
				}
				progressBar.setValue(0);
			}
		});
		
		btnRun.setEnabled(false);
		
		btnOutputFile = new JButton("...");
		btnOutputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				jFileChooser.setCurrentDirectory(new File(txtOutputFile.getText()));
				if(jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					txtOutputFile.setText(jFileChooser.getSelectedFile().toString());				
				}
			}
		});
		btnOutputFile.setEnabled(false);
		
		JLabel lblAction = new JLabel("Action:");
		
		btnKeyFile = new JButton("...");
		btnKeyFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				jFileChooser.setCurrentDirectory(new File(txtKeyFile.getText()));
				if(jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					txtKeyFile.setText(jFileChooser.getSelectedFile().toString());				
				}
			}
		});
		btnKeyFile.setEnabled(false);
		
		lblFileSize = new JLabel("File Size:");
		lblBytes = new JLabel("Bytes");
		lblRndSeed = new JLabel("RND Seed:");
		lblThreads = new JLabel("Threads:");
		lblKeyFile = new JLabel("Key File:");
		lblInputFile.setEnabled(false);
		lblOutputFile.setEnabled(false);
		lblFileSize.setEnabled(false);
		lblBytes.setEnabled(false);
		lblRndSeed.setEnabled(false);
		lblThreads.setEnabled(false);
		progressBar.setEnabled(false);
		lblKeyFile.setEnabled(false);
		
		comboBox = new JComboBox<String>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtInputFile.setEnabled(false);
				txtInputFile.setEnabled(false);
				txtOutputFile.setEnabled(false);
				txtKeyFile.setEnabled(false);
				btnKeyFile.setEnabled(false);
				btnInputFile.setEnabled(false);
				btnOutputFile.setEnabled(false);						
				txtFileSize.setEnabled(false);
				txtRNDSeed.setEnabled(false);
				txtThreads.setEnabled(false);
				btnRun.setEnabled(false);
				lblInputFile.setEnabled(false);
				lblOutputFile.setEnabled(false);
				lblFileSize.setEnabled(false);
				lblBytes.setEnabled(false);
				lblRndSeed.setEnabled(false);
				lblThreads.setEnabled(false);
				progressBar.setEnabled(false);
				lblKeyFile.setEnabled(false);
				
				enableSelected(comboBox.getSelectedIndex());				
			}
		});
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"--- CHOOSE ACTION ---", "BASE2 ENCODE", "BASE2 DECODE", "BASE16 ENCODE", "BASE16 DECODE", "BASE64 ENCODE", "BASE64 DECODE", "FILE ANALYSIS", 
																		 "GENERATE RANDOM DATA", "GENERATE SECURE RANDOM DATA", "ONE TIME PAD - FILE KEY", "ONE TIME PAD - RND STREAM KEY",
																		 "STEG - ENCODE INTO IMAGE", "STEG - DECODE FROM IMAGE"}));
		
		txtFileSize = new JTextField();
		txtFileSize.setEnabled(false);
		txtFileSize.setColumns(10);
		
		txtRNDSeed = new JTextField();
		txtRNDSeed.setText("4963");
		txtRNDSeed.setEnabled(false);
		txtRNDSeed.setColumns(10);
		
		
		
		txtThreads = new JTextField();
		txtThreads.setText("256");
		txtThreads.setEnabled(false);
		txtThreads.setColumns(10);
				
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(0)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblRndSeed)
								.addComponent(lblThreads)
								.addComponent(lblFileSize))
							.addGap(7)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(txtThreads)
								.addComponent(txtRNDSeed)
								.addComponent(txtFileSize))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
									.addComponent(btnRun)
									.addContainerGap())
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(lblBytes)
									.addContainerGap())))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblAction)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(lblInputFile)
										.addComponent(lblOutputFile)
										.addComponent(lblKeyFile))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(comboBox, Alignment.TRAILING, 0, 305, Short.MAX_VALUE)
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(txtOutputFile, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
												.addComponent(txtInputFile, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
												.addComponent(txtKeyFile, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
											.addPreferredGap(ComponentPlacement.RELATED)
											.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(btnInputFile)
												.addComponent(btnOutputFile)
												.addComponent(btnKeyFile, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))))
									.addPreferredGap(ComponentPlacement.RELATED)))
							.addGap(0))
						.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)))
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAction)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblInputFile)
						.addComponent(txtInputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnInputFile))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOutputFile)
						.addComponent(txtOutputFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnOutputFile))
					.addGap(7)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblKeyFile)
						.addComponent(txtKeyFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnKeyFile))
					.addGap(7)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFileSize)
						.addComponent(lblBytes)
						.addComponent(txtFileSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRndSeed)
						.addComponent(txtRNDSeed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblThreads)
						.addComponent(txtThreads, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRun))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
		);
		
		txtConsole = new JTextArea();
		txtConsole.setEditable(false);		
		scrollPane.setViewportView(txtConsole);
		contentPane.setLayout(gl_contentPane);
		 DefaultCaret caret = (DefaultCaret) txtConsole.getCaret();
		 caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
	
	private String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = new Date(System.currentTimeMillis());
	    String strDate = sdfDate.format(now);
	    return strDate;
	}
	
	private void addConsole(String line) {		
		txtConsole.append(getCurrentTimeStamp()+": "+line+"\n");
	}
	
	private void disableAll() {
		txtInputFile.setEnabled(false);
		txtInputFile.setEnabled(false);
		txtOutputFile.setEnabled(false);
		txtKeyFile.setEnabled(false);
		btnKeyFile.setEnabled(false);
		btnInputFile.setEnabled(false);
		btnOutputFile.setEnabled(false);						
		txtFileSize.setEnabled(false);
		txtRNDSeed.setEnabled(false);
		txtThreads.setEnabled(false);
		comboBox.setEnabled(false);
	}
	
	private void enableSelected(int choice) {
		comboBox.setEnabled(true);
		switch(choice) {
			case 0: //"--- CHOOSE ACTION ---"				
			break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6://"BASE2/16/64 ENCODE/DECODE"
				lblInputFile.setEnabled(true);
				txtInputFile.setEnabled(true);						
				lblOutputFile.setEnabled(true);
				txtOutputFile.setEnabled(true);				
				btnInputFile.setEnabled(true);
				btnOutputFile.setEnabled(true);						
				btnRun.setEnabled(true);
				break;		
			case 7:	//"FILE ANALYSIS"
				txtInputFile.setEnabled(true);
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(false);
				btnKeyFile.setEnabled(false);
				btnInputFile.setEnabled(true);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(true);
				lblFileSize.setText("Bit Size:");
				lblBytes.setText("bits");
				txtRNDSeed.setEnabled(false);
				txtThreads.setEnabled(false);
				btnRun.setEnabled(true);
				lblInputFile.setEnabled(true);
				lblOutputFile.setEnabled(true);
				lblBytes.setEnabled(true);
				lblFileSize.setEnabled(true);
				break;
			case 8:	//"GENERATE RANDOM DATA"
				txtInputFile.setEnabled(false);
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(false);
				btnKeyFile.setEnabled(false);
				btnInputFile.setEnabled(false);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(true);
				lblFileSize.setText("File Size:");
				lblBytes.setText("Bytes");
				txtRNDSeed.setEnabled(true);				
				txtThreads.setEnabled(false);
				lblFileSize.setEnabled(true);
				lblRndSeed.setEnabled(true);
				lblBytes.setEnabled(true);								
				lblOutputFile.setEnabled(true);
				btnRun.setEnabled(true);
				break;
			case 9:	//"GENERATE SECURE RANDOM DATA"
				txtInputFile.setEnabled(false);
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(false);
				btnKeyFile.setEnabled(false);
				btnInputFile.setEnabled(false);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(true);
				lblFileSize.setText("File Size:");
				lblBytes.setText("Bytes");
				txtRNDSeed.setEnabled(false);				
				txtThreads.setEnabled(true);				
				lblThreads.setEnabled(true);
				lblFileSize.setEnabled(true);
				lblRndSeed.setEnabled(false);
				lblBytes.setEnabled(true);								
				lblOutputFile.setEnabled(true);
				btnRun.setEnabled(true);
				break;
			case 10://"ONE TIME PAD - FILE KEY"
				txtInputFile.setEnabled(true);
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(true);
				btnKeyFile.setEnabled(true);
				btnInputFile.setEnabled(true);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(false);
				txtRNDSeed.setEnabled(false);				
				txtThreads.setEnabled(false);
				lblKeyFile.setEnabled(true);
				lblInputFile.setEnabled(true);
				lblOutputFile.setEnabled(true);
				btnRun.setEnabled(true);
				break;
			case 11://"ONE TIME PAD - RND STREAM KEY"
				txtInputFile.setEnabled(true);
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(false);
				btnKeyFile.setEnabled(false);
				btnInputFile.setEnabled(true);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(false);
				txtRNDSeed.setEnabled(true);				
				txtThreads.setEnabled(false);
				lblRndSeed.setEnabled(true);
				lblInputFile.setEnabled(true);
				lblOutputFile.setEnabled(true);
				btnRun.setEnabled(true);
				break;		
			case 12://"STEG ENCODE
				txtInputFile.setEnabled(true);				
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(true);				
				btnKeyFile.setEnabled(true);
				btnInputFile.setEnabled(true);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(false);
				txtRNDSeed.setEnabled(true);				
				txtThreads.setEnabled(false);
				lblRndSeed.setEnabled(true);
				lblKeyFile.setEnabled(true);
				lblInputFile.setEnabled(true);
				lblOutputFile.setEnabled(true);
				btnRun.setEnabled(true);
				break;	
			case 13://"STEG DECODE
				txtInputFile.setEnabled(true);
				txtOutputFile.setEnabled(true);
				txtKeyFile.setEnabled(false);
				btnKeyFile.setEnabled(false);
				btnInputFile.setEnabled(true);
				btnOutputFile.setEnabled(true);
				txtFileSize.setEnabled(false);
				txtRNDSeed.setEnabled(true);				
				txtThreads.setEnabled(false);
				lblRndSeed.setEnabled(true);
				lblInputFile.setEnabled(true);
				lblOutputFile.setEnabled(true);
				btnRun.setEnabled(true);
				break;
		}
	}
	
}
