package com.dsna.desktop.client.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.JTabbedPane;

import rice.environment.Environment;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;

import com.dsna.dht.scribe.DSNAScribeClient;
import com.dsna.dht.scribe.DSNAScribeFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ClientFrame extends JFrame implements UIUpdater {
	private JTextField textField_Subscribe;
	private JTextArea textArea_Message;
	private JTextField textField_BindPort;
	private JTextField textField_BootPort;
	private JTextField textField_BootAddress;
	private JTextPane textPane_Bulletin;
	private JList jlist_SubscribedTopic;
	private DefaultListModel listModel;
	private JButton btnLaunch;
	private JTabbedPane tabbedPane;
	private DSNAScribeClient messageHandler;
	private Set<String> subscribedTopic;
	private JTextField textField_PublishTopic;
	
	public ClientFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		subscribedTopic = new TreeSet<String>();
		
		//getContentPane().setLayout(null);
		getContentPane().setBounds(0, 0, 769, 460);
		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 769, 460);
		tabbedPane.setPreferredSize(new Dimension(769, 460));
		getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("DHT config", null, panel, null);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Bind port:");
		lblNewLabel.setBounds(153, 24, 61, 16);
		panel.add(lblNewLabel);
		
		textField_BindPort = new JTextField();
		textField_BindPort.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_BindPort.setBounds(246, 17, 134, 28);
		panel.add(textField_BindPort);
		textField_BindPort.setColumns(10);
		textField_BindPort.setText("9001");
		
		JLabel lblBootAddress = new JLabel("Boot port:");
		lblBootAddress.setBounds(153, 58, 81, 16);
		panel.add(lblBootAddress);
		
		textField_BootPort = new JTextField();
		textField_BootPort.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_BootPort.setColumns(10);
		textField_BootPort.setBounds(246, 51, 134, 28);
		textField_BootPort.setText("9001");
		panel.add(textField_BootPort);
		
		JLabel lblBootAddress_1 = new JLabel("Boot address:");
		lblBootAddress_1.setBounds(153, 94, 92, 16);
		panel.add(lblBootAddress_1);
		
		textField_BootAddress = new JTextField();
		textField_BootAddress.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_BootAddress.setColumns(10);
		textField_BootAddress.setBounds(246, 87, 134, 28);
		try {
			Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
            String myIp = "127.0.0.1";
			while(e.hasMoreElements())
            {
                NetworkInterface n=(NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while(ee.hasMoreElements())
                {
                    InetAddress i= (InetAddress) ee.nextElement();
                    System.out.println(i.getHostAddress());
                    if(!i.getHostAddress().contains(":")&&!i.getHostAddress().equalsIgnoreCase("127.0.0.1"))
                    {
                    	textField_BootAddress.setText(i.getHostAddress());
                    }
                }
            }
		} catch (SocketException e) {
			e.printStackTrace();
			textField_BootAddress.setText("127.0.0.1");
		}
		panel.add(textField_BootAddress);
		
		btnLaunch = new JButton("Launch");
		btnLaunch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    // Loads pastry configurations
			    Environment env = new Environment();

			    // disable the UPnP setting (in case you are testing this on a NATted LAN)
			    env.getParameters().setString("nat_search_policy","never");
				DSNAScribeFactory factory = new DSNAScribeFactory(env);
				try {
					messageHandler = factory.newClient(getBindPort(), getBootPort(), getBootAddress(), ClientFrame.this);
					ClientFrame.this.btnLaunch.setEnabled(false);
					ClientFrame.this.tabbedPane.setSelectedIndex(1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnLaunch.setBounds(263, 127, 117, 29);
		panel.add(btnLaunch);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		tabbedPane.addTab("Messenger", null, panel_1, null);
		
		JButton button_1 = new JButton("Add");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				subscribeToTopic();
			}
		});
		button_1.setBounds(209, 1, 61, 29);
		panel_1.add(button_1);
		
		textField_Subscribe = new JTextField();
		textField_Subscribe.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()=='\n')
					subscribeToTopic();
			}
		});
		textField_Subscribe.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Subscribe.setColumns(10);
		textField_Subscribe.setBounds(76, 0, 134, 28);
		panel_1.add(textField_Subscribe);
		
		JLabel label_1 = new JLabel("Subscribe:");
		label_1.setBounds(6, 6, 73, 16);
		panel_1.add(label_1);
		
		JLabel lblSubscribeTopics = new JLabel("Subscribe Topics");
		lblSubscribeTopics.setHorizontalAlignment(SwingConstants.CENTER);
		lblSubscribeTopics.setBounds(76, 40, 113, 16);
		panel_1.add(lblSubscribeTopics);
		
		JButton button_2 = new JButton("Remove");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSubscribedTopics();
			}
		});
		button_2.setBounds(157, 368, 113, 29);
		panel_1.add(button_2);
		
		listModel = new DefaultListModel();
		jlist_SubscribedTopic = new JList();
		jlist_SubscribedTopic.setBounds(6, 56, 264, 312);
		jlist_SubscribedTopic.setModel(listModel);
		jlist_SubscribedTopic.setBorder(lineBorder);
		panel_1.add(jlist_SubscribedTopic);
		
		textArea_Message = new JTextArea();
		textArea_Message.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()=='\n')
					publishMessage();
			}
		});
		textArea_Message.setColumns(10);
		textArea_Message.setBounds(428, 35, 319, 129);
		panel_1.add(textArea_Message);
		
		JLabel label_3 = new JLabel("Topic:");
		label_3.setBounds(368, 6, 73, 16);
		panel_1.add(label_3);
		
		JLabel label_4 = new JLabel("Message:");
		label_4.setBounds(368, 40, 73, 16);
		panel_1.add(label_4);
		
		JButton button_3 = new JButton("Publish");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				publishMessage();
			}
		});
		button_3.setBounds(630, 169, 117, 29);
		panel_1.add(button_3);
		
		textPane_Bulletin = new JTextPane();
		textPane_Bulletin.setEditable(false);
		//textPane_Bulletin.setColumns(10);
		textPane_Bulletin.setBounds(430, 208, 317, 218);
		textPane_Bulletin.setBorder(lineBorder);
		panel_1.add(textPane_Bulletin);
		
		JLabel label_5 = new JLabel("Bulletin:");
		label_5.setBounds(368, 213, 73, 16);
		panel_1.add(label_5);
		
		textField_PublishTopic = new JTextField();
		textField_PublishTopic.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_PublishTopic.setColumns(10);
		textField_PublishTopic.setBounds(428, 0, 182, 28);
		panel_1.add(textField_PublishTopic);
	}
	
	public void addSubscribedTopic(String topic)	{
		if(!this.subscribedTopic.contains(topic))	{
			this.subscribedTopic.add(topic);
			listModel.addElement(topic);
			this.messageHandler.subscribe(topic);
		}
	}
	
	public void removeSubscribedTopic(String topic)	{
		if(this.subscribedTopic.contains(topic))	{
			this.subscribedTopic.remove(topic);
			listModel.removeElement(topic);
			this.messageHandler.unsubscribe(topic);
		}
	}
	
	private int getBindPort()	{
		return Integer.parseInt(this.textField_BindPort.getText());
	}
	
	private int getBootPort()	{
		return Integer.parseInt(this.textField_BootPort.getText());
	}
	
	private String getBootAddress()	{
		return this.textField_BootAddress.getText();
	}
	
	private void publishMessage()	{
		String publishedTopic = this.textField_PublishTopic.getText().trim();
		String message = this.textArea_Message.getText().trim();
		this.messageHandler.sendMulticast(publishedTopic, message);
		this.textField_PublishTopic.setText("");
		this.textArea_Message.setText("");
		this.textField_PublishTopic.requestFocus();
	}
	
	private void removeSubscribedTopics()	{
		String theTopic = (String)jlist_SubscribedTopic.getSelectedValue();
		if(theTopic!=null)	{
			this.removeSubscribedTopic(theTopic);
		}
	}
	
	private void subscribeToTopic()	{
		String subscribedTopic = textField_Subscribe.getText().trim();
		if(!subscribedTopic.equalsIgnoreCase(""))	{
			ClientFrame.this.addSubscribedTopic(subscribedTopic);
		}
		textField_Subscribe.setText("");
	}

	@Override
	public boolean anycast(Topic topic, ScribeContent content) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deliver(Topic topic, ScribeContent content) {
		// TODO Auto-generated method stub
		String s = "Get new msg from topic "+topic+" : " + content.toString();
		this.textPane_Bulletin.setText(this.textPane_Bulletin.getText()+"\n"+s);
	}

	@Override
	public void childAdded(Topic topic, NodeHandle child) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void childRemoved(Topic topic, NodeHandle child) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeFailed(Topic topic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeFailed(Collection<Topic> topics) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeSuccess(Collection<Topic> topics) {
		// TODO Auto-generated method stub
		
	}

}
