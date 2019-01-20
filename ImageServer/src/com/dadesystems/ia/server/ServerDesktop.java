package com.dadesystems.ia.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.dadesystems.utils.base64.NDCoder;

public class ServerDesktop {
	
	private String imagePath = null;

	private JFrame serverFrame = new JFrame();

	private JPanel imgPanel = new JPanel();
	private JLabel imgViewer = new JLabel("");
	private ImageIcon rcvIcon = new ImageIcon();
	private Image sentImage = null;
	private Image scaleImage = null;
	private ImageIcon displayImage = new ImageIcon();
	
	private JPanel msgPanel = new JPanel();	
	private JTextArea msgArea = new JTextArea();
	
	private JPanel txtPanel = new JPanel();
	private JTextArea txtPane = new JTextArea();	
	
	private JPanel  cntlPanel = new JPanel();
	
	private final JButton btnStopServer = new JButton("Stop Server");		
	private final JButton btnStartServer = new JButton("Start Server");
	private final JButton btnNext = new JButton("Next");
	private final JButton btnPrevious = new JButton("Previous");
	
	private static final int PORT = 9090;
	
	private DataInputStream packetReader = null;
	private DataOutputStream packetSender = null;
	
	private String lineDelimiter = "\n";
	
	NDCoder dcoder;
	
	public ServerDesktop() {
		super();
	}	
	
	public class IAServer implements Runnable {
		
		ServerSocket svrSocket;	 
		Socket clientSocket;
		
		@Override
		public void run() {
			
			try {
				
				svrSocket = new ServerSocket(PORT);
	            
				while(true) {// Accept client
	                try {
	                		                    
	                	msgArea.append("Listening for client ..." + lineDelimiter);
	                    
	                    clientSocket = svrSocket.accept();
	                    
	                    packetReader = new DataInputStream(clientSocket.getInputStream());
	                    packetSender = new DataOutputStream(clientSocket.getOutputStream());
	                    
	    				Thread listener = new Thread(new ClientListener(clientSocket, packetSender));
	    				listener.start();
	    				msgArea.append("Got a connection." + lineDelimiter);
	    				
	                } catch (IOException ex) {
	                    ex.printStackTrace(System.err);
	                }
	            }
				
			}
           catch (Exception ex)
           {
        	   msgArea.setText("Error making a connection." + lineDelimiter);
           }
		   finally {
		        if (svrSocket != null) {
		            try {
		            	clientSocket.close();
		            	svrSocket.close();
		            } catch (IOException e) {
		                // log error just in case
		            }
		        }
		    }
		
		} 

	}
	
	   public class ClientListener implements Runnable	
	   {
	       BufferedReader reader;
	       
	       public ClientListener(Socket clientSocket, DataOutputStream client) 
	       {
	            try 
	            {
	                InputStreamReader isReader = new InputStreamReader(clientSocket.getInputStream());
	                reader = new BufferedReader(isReader);
	            }
	            catch (Exception ex) 
	            {
	            	msgArea.append("Unexpected error: " + ex.getMessage() + lineDelimiter);
	            }

	       }

	       @Override
	       public void run() 
	       {
            
	            String[] dataPackage;
	            
	            String ctrlPayload = "";
	            String msgPayload = "";
	            String typPayload = "";

	            String msgStream = null;
	            
	            String CONNECT = "Connected";
	            String DISCONNECT = "Disconnected";
	            String SENDIMG  = "SendImagePayLoad" ;
	            
	            try 
	            {
	                while ((msgStream = reader.readLine()) != null) 
	                {
	                	//msgArea.append("Received -  " + msgStream + lineDelimiter);
	                	
	                	dataPackage = msgStream.split(":");
	                	
	                     ctrlPayload = dataPackage[0];
	                     typPayload = dataPackage[1];
	                     msgPayload = dataPackage[2];	                	
	                   
	                    if (ctrlPayload.equals(CONNECT)) 
	                    {
	                    	msgArea.append("Client Connected - " + lineDelimiter);
	                    } 
	                    else if (ctrlPayload.equals(DISCONNECT)) 
	                    {
	                    	msgArea.append("Client Disconnected - " + lineDelimiter);
	                    } 
	                    else if (ctrlPayload.equals(SENDIMG)) 
	                    {
	                    	msgArea.append("Image Processing - " + lineDelimiter);
	                    	System.out.println(msgPayload);
	                    	txtPane.setText(msgPayload);
	                    	
	                    	dcoder = new NDCoder();
	                    	
	                    	String result = dcoder.dcode(msgPayload);
	                    	
	                    	if (result.equals("SUCCESS")){
	                    		System.out.println("Ready to build Image");
	                    		
	                    		imagePath = "C:\\tmp\\ds-image." + typPayload;
	                    		System.out.println("Building file: " + imagePath);
	                    		
	                    		File imageFile = new File(imagePath);
	                    		
	                    		if (imageFile.exists()){
	                    			
		                    		try (FileOutputStream imageOutFile = new FileOutputStream(imagePath)) {
	
		                    			imageOutFile.write(dcoder.getDecodeArray());
		                    			
		                    			rcvIcon = new ImageIcon(imagePath);
		                    			sentImage = rcvIcon.getImage();
		                    			scaleImage = sentImage.getScaledInstance(imgViewer.getWidth(), imgViewer.getHeight(), Image.SCALE_SMOOTH);
		                    			displayImage = new ImageIcon(scaleImage);		
		                    			imgViewer.setIcon(displayImage);
		                    			send("SendImagePayLoad:Received");
		                    			
		                    		} catch (FileNotFoundException e) {
		                    			System.out.println("Image not found" + e);
		                    		} catch (IOException ioe) {
		                    			System.out.println("Exception while converting the Image " + ioe);
		                    		}
		                    		
		                    	}
	                    		else {
	                    			msgArea.append("Error: Unable to create file - " + imageFile);
	                    		}
	                    		
	                    	}
	                    	else
	                    	{
	                    		msgArea.append(result);
	                    	}
	                    } 
	                    else 
	                    {
	                    	msgArea.append("No Conditions were met." + lineDelimiter);
	                    }
	                } 
	             } 
	            catch (SocketException iex){
	            	msgArea.setText(iex.getMessage());
	            }	            
	             catch (Exception ex) 
	             {
	            	 msgArea.setText("Lost a connection" + lineDelimiter);
	                ex.printStackTrace();
	             } 
		} 
	    }	
		
	/*
	 * Configure Server Display
	 */	
	private void ServerConsole() {
		
		serverFrame.setTitle("ImageApp - Server");
		serverFrame.getContentPane().setLayout(null);

		serverFrame.setBounds(100, 100, 800, 600);
		serverFrame.getContentPane().setMinimumSize(new Dimension(800, 600));
		serverFrame.getContentPane().setMaximumSize(new Dimension(800, 600));

		serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				
		
		//IAServer iaServer = new IAServer();
		
		/*
		 * Image Panel
		 */		
		
		imgViewer.setBounds(20, 11, 403, 274);
		rcvIcon = new ImageIcon(getClass().getClassLoader().getResource("splash.jpg"));
		sentImage = rcvIcon.getImage();
		scaleImage = sentImage.getScaledInstance(imgViewer.getWidth(), imgViewer.getHeight(), Image.SCALE_SMOOTH);
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
				
		/*
		 * Control Panel
		 */				
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnPrevious.setBounds(10, 45, 89, 23);
		

		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNext.setBounds(10, 11, 89, 23);
		
		cntlPanel.setLayout(null);
		cntlPanel.setBounds(433, 421, 341, 129);
		cntlPanel.add(btnPrevious);
		cntlPanel.add(btnNext);
		
		serverFrame.getContentPane().add(imgPanel);		
		serverFrame.getContentPane().add(msgPanel);
		serverFrame.getContentPane().add(cntlPanel);
		
		btnStartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		        Thread starter = new Thread(new IAServer());
		        starter.start();		        
		        msgArea.setText("Server started...");
		        btnStopServer.setEnabled(true);
			}
		});
		btnStartServer.setBounds(223, 11, 108, 23);
		cntlPanel.add(btnStartServer);
		
		btnStopServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        try 
		        {
		            Thread.sleep(3000); 
		        } 
		        catch(InterruptedException ex) {Thread.currentThread().interrupt();}
		        
		        msgArea.append("Server stopping... " + lineDelimiter);
		        btnStopServer.setEnabled(false);
			}
		});
		btnStopServer.setBounds(223, 45, 108, 23);
		cntlPanel.add(btnStopServer);
		
		txtPanel.setLayout(null);
		txtPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		txtPanel.setBounds(435, 11, 339, 399);
		serverFrame.getContentPane().add(txtPanel);
		txtPane.setEditable(false);
		txtPane.setLineWrap(true);
		txtPane.setBounds(10, 5, 319, 383);
		txtPanel.add(txtPane);

	}
	
    public String receive() throws IOException {
        return packetReader.readUTF();
    }
    
    public void send(String str) throws IOException {
        packetSender.writeUTF(str);
    }	
		
	/**
	 * Launch the Server Application.
	 */
	public static void main(String[] args) throws Exception {
			
			  SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		            	ServerDesktop app = new ServerDesktop();		
		    			app.ServerConsole();				
		    			app.serverFrame.setVisible(true);
		            }
		          });
		
	}	
}
