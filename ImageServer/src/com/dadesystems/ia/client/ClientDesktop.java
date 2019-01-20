package com.dadesystems.ia.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.dadesystems.utils.base64.NDCoder;
import com.dadesystems.utils.image.ImageChecker;

public class ClientDesktop {

	private JFrame clientFrame = new JFrame();
	
	private final JPanel msgPanel = new JPanel();
	private JTextArea msgArea = new JTextArea();
	
	private JPanel imgPanel = new JPanel();
	private ImageIcon rcvIcon = new ImageIcon();
	private Image selectedImage = null;
	private Image scaleImage = null;
	private JLabel imgViewer = new JLabel("");
	private ImageIcon displayImage = new ImageIcon();
	
	private final JButton btnSendImage = new JButton("Transmit");
	private final JButton btnSelectImage = new JButton("Select Image");
	
	private final JButton btnConnect = new JButton("CONNECT");
	private final JButton btnDisconnect = new JButton("DISCONNECT");
	
	private String clientAddress = "127.0.0.1";
	private int    clientPort = 9090;
	
    private Socket clientSocket;
    private BufferedReader packetReader;
    private PrintWriter packetWriter;	
    
    private Boolean isConnected = false;
    
	private String lineDelimiter = "\n";
	
	private JFileChooser imageChooser;
	
	private File imageFile;
	private String imagePath;

	public ClientDesktop() {
		super();
	}
	
	/*
	 * Configure Client Display
	 */
	public void clientConsole() {

		clientFrame.setTitle("ImageApp - Client");
		clientFrame.getContentPane().setLayout(null);
		clientFrame.setBounds(100, 100, 800, 600);
		clientFrame.getContentPane().setMinimumSize(new Dimension(800, 600));
		clientFrame.getContentPane().setMaximumSize(new Dimension(800, 600));
		clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		imageChooser = new JFileChooser();
												
		imgViewer.setBounds(20, 11, 403, 274);
		rcvIcon = new ImageIcon(getClass().getClassLoader().getResource("splash.jpg"));
		selectedImage = rcvIcon.getImage();		
		scaleImage = selectedImage.getScaledInstance(imgViewer.getWidth(), imgViewer.getHeight(), Image.SCALE_SMOOTH);
		displayImage = new ImageIcon(scaleImage);		
		imgViewer.setIcon(displayImage);
		
		imgPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		imgPanel.setLayout(new BorderLayout(0, 0));
		imgPanel.setBounds(20, 11, 403, 274);
		imgPanel.add(imgViewer);
		msgArea.setEditable(false);
		msgArea.setLineWrap(true);
		msgArea.setBounds(10, 5, 383, 238);
		msgArea.setText("");

		msgPanel.setLayout(null);
		msgPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		msgPanel.setBounds(20, 296, 403, 254);
		msgPanel.add(msgArea);		

		btnSendImage.setBounds(659, 527, 89, 23);
		btnSendImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				msgArea.append("Sending Image" + imagePath + lineDelimiter);
				
				try {
					// Pause to display message before attempting to send image
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
						       
		        if (imagePath.equals("")) {
		        	msgArea.setText("");
		        	msgArea.requestFocus();
		        } else {
		            try {
		            	
		            	NDCoder ndCoder = new NDCoder();
		            	String imgPayload = "";
		            	String imgExtension = "";
		            	
		            	imgPayload = ndCoder.ncode(imagePath);
		            	//imgPayload = "SUCCESS:THIS IS A TEST";
		            	//System.out.println(imgPayload.replaceFirst("SUCCESS:",""));	
		            	//System.out.println(imgPayload.substring(8));	
		            	
		            	imgExtension = imagePath.substring(imagePath.lastIndexOf('.')+1);
		            	
		            	if (imgPayload.startsWith("SUCCESS:")){
			            	packetWriter.println("SendImagePayLoad:"+ imgExtension + ":" + imgPayload.replaceFirst("SUCCESS:","") );
			            	packetWriter.flush();
				            msgArea.append("Image Transmitted." + lineDelimiter);
		            	}
		            	else{
		            		msgArea.append("Error in converting image for transmission."+ lineDelimiter);   
				            msgArea.append("Image NOT Transmitted."+ lineDelimiter);          		
		            	}
		            } catch (Exception ex) {
		            	msgArea.append("Message was not sent." + lineDelimiter);
		            }
		            msgArea.requestFocus();
		            btnSendImage.setEnabled(false);
		        }

		        msgArea.requestFocus();				

			}
		});
		btnSendImage.setEnabled(false);

		btnConnect.setBounds(659, 11, 115, 23);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connectServer();
			}
		});
		
		btnDisconnect.setBounds(659, 45, 115, 23);	
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeConnection();
			}
		});		

		btnSelectImage.setBounds(456, 527, 175, 23);
		btnSelectImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				selectImage();
			}
			
			private void selectImage() {
				
				ImageChecker imageChecker = new ImageChecker();
				
		        int returnVal = imageChooser.showOpenDialog(clientFrame);
		        
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            imageFile = imageChooser.getSelectedFile();
		            
		            if (imageChecker.isGoodFile(imageFile)){
		            	msgArea.append("Retrieving Image: " + imageFile.getName() + "." + lineDelimiter);
		            	msgArea.append("Full File Path:" + imageFile.getPath() + "." + lineDelimiter);

		            	imagePath = imageFile.getPath();
		            	
		        		rcvIcon = new ImageIcon(imagePath);
		        		selectedImage = rcvIcon.getImage();

		        		scaleImage = selectedImage.getScaledInstance(imgViewer.getWidth(), imgViewer.getHeight(), Image.SCALE_SMOOTH);
		        		displayImage = new ImageIcon(scaleImage);		
		        		imgViewer.setIcon(displayImage);
		        		
		            	btnSendImage.setEnabled(true);
		            }
		            else {
		            	msgArea.append("Warning: Invalid File. Accepted Formats are: jpg, jpeg, png, gif" + lineDelimiter);

		        		rcvIcon = new ImageIcon(getClass().getClassLoader().getResource("splash.jpg"));
		        		selectedImage = rcvIcon.getImage();	
		        		
		        		scaleImage = selectedImage.getScaledInstance(imgViewer.getWidth(), imgViewer.getHeight(), Image.SCALE_SMOOTH);
		        		displayImage = new ImageIcon(scaleImage);		
		        		imgViewer.setIcon(displayImage);
		        		
		            	btnSendImage.setEnabled(false);
		            }
		            
		        } else {
		        	msgArea.append("Open command cancelled by user." + lineDelimiter);
		        }
		        msgArea.setCaretPosition(msgArea.getDocument().getLength());
				
			}   			
		});
				
		clientFrame.getContentPane().add(imgPanel);		
		clientFrame.getContentPane().add(msgPanel);
		clientFrame.getContentPane().add(btnSendImage);
		clientFrame.getContentPane().add(btnConnect);
		clientFrame.getContentPane().add(btnDisconnect);
		clientFrame.getContentPane().add(btnSelectImage);
				
	}

    public void connectServer() {
    	
        if (!isConnected) 
        {

            try 
            {
            	clientSocket = new Socket(clientAddress, clientPort);
            	
                InputStreamReader streamreader = new InputStreamReader(clientSocket.getInputStream());
                
                packetReader = new BufferedReader(streamreader);
                packetWriter = new PrintWriter(clientSocket.getOutputStream());
                packetWriter.println("Connected:Msg:Client On");
                packetWriter.flush(); 
                isConnected = true; 
                msgArea.append("Connected." + lineDelimiter);
            } 
            catch (Exception ex) 
            {
            	msgArea.append("Cannot Connect! Try Again."+ lineDelimiter);

            }
            
            initListener();
            
        } else if (isConnected) 
        {
        	msgArea.append("You are already connected."+ lineDelimiter);
        }
    }
    
    public class Receiver implements Runnable
    {
        @Override
        public void run() 
        {
            String[] dataPackage;
            
            String ctrlPayload = "";
            String msgPayload = "";
            
            String msgStream = null;
            
            String CONNECT = "Connected";
            String DISCONNECT = "Disconnected";
            String SENDIMG  = "SendImagePayLoad" ;

            try 
            {
                while ((msgStream = packetReader.readLine()) != null) 
                {
                     dataPackage = msgStream.split(":");
                     
                     ctrlPayload = dataPackage[0];
                     msgPayload = dataPackage[1];

                     if (ctrlPayload.equals(CONNECT))
                     {
                    	 msgArea.append("Ready to receive images." + lineDelimiter);
                     } 
                     else if (ctrlPayload.equals(DISCONNECT)) 
                     {
                    	 msgArea.append("Exit Application - Server not available." + lineDelimiter);
                    	 msgArea.removeAll();
                     } 
                     else if (ctrlPayload.equals(SENDIMG)) 
                     {
                    	 msgArea.append(msgPayload + lineDelimiter);
                    	 msgArea.setCaretPosition(msgArea.getDocument().getLength());
                     } 
                    else 
                    {
                    	msgArea.append("No Conditions were met." + lineDelimiter);
                    }
                }
           }catch(Exception ex) { }
        }
    }
    
    public void initListener() 
    {
         Thread Receiver = new Thread(new Receiver());
         Receiver.start();
    }
    
    public void closeConnection() {
    	
        try
        {
        	packetWriter.println("Disconnected:Msg:Client Left"); 
        	packetWriter.flush(); 
        } catch (Exception e) 
        {
        	msgArea.append("Could not send Disconnect message." + lineDelimiter);
        }
        
        try 
        {
        	msgArea.append("Disconnected." + lineDelimiter);
        	clientSocket.close();
        } catch(Exception ex) {
        	msgArea.append("Failed to disconnect." + lineDelimiter);
        }
        isConnected = false;       
    }    
     	
	/**
	 * Launch the application.
	 **/

	public static void main(String[] args) throws Exception {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ClientDesktop app = new ClientDesktop();
				app.clientConsole();
				app.clientFrame.setVisible(true);	
				
			}
		});
				
		
	}
}
