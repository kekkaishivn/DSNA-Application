package com.dsna.desktop.client.ui;


import java.awt.Color;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.JTabbedPane;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import rice.pastry.JoinFailedException;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.entity.BaseEntity;
import com.dsna.entity.IpUpdateNotification;
import com.dsna.entity.Location;
import com.dsna.entity.Message;
import com.dsna.entity.Notification;
import com.dsna.entity.NotificationType;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.Status;
import com.dsna.entity.encrypted.EncryptedEntity;
import com.dsna.entity.encrypted.KeyInfo;
import com.dsna.entity.exception.UnsupportedNotificationTypeException;
import com.dsna.p2p.scribe.DSNAScribeClient;
import com.dsna.p2p.scribe.DSNAScribeContent;
import com.dsna.p2p.scribe.DSNAScribeFactory;
import com.dsna.service.IdBasedSecureSocialService;
import com.dsna.service.IdBasedSecureSocialServiceImpl;
import com.dsna.service.SocialService;
import com.dsna.service.SocialServiceFactory;
import com.dsna.service.SocialServiceImpl;
import com.dsna.service.SocialEventListener;
import com.dsna.storage.cloud.CloudStorageService;
import com.dsna.storage.cloud.GoogleCloudStorageServiceImpl;
import com.dsna.util.ASN1Util;
import com.dsna.util.FileUtil;
import com.dsna.util.NetworkUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientFrame extends JFrame implements SocialEventListener {
	
	public static final String publicKeyURL = "https://130.237.20.200:8080/DSNA_privatekeygenerator/SystemPublic.txt";
	public static final String secretKeyURL = "https://130.237.20.200:8080/DSNA_privatekeygenerator/KeyExtract.jsp";
	public static final String idParams = "clientid";	
	
	private JTextArea textArea_Message;
	private JTextField textField_BindPort;
	private JTextField textField_BootPort;
	private JTextField textField_BootAddress;
	private JTextPane textPane_Bulletin;
	private JList jlist_FriendList;
	private DefaultListModel listModel;
	private JButton btnLaunch;
	private JTabbedPane tabbedPane;
	private IdBasedSecureSocialService serviceHandler;
	private Set<String> subscribedTopic;
	private JTextField textField_FriendId;
	private JTextField textField_StatusId;
	private JTextArea textArea_Status;
	JTextPane textPane_StatusDisplay;
	private JTextField textField_Accinfo_Username;
	private JTextField textField_Accinfo_Displayname;
	private JTextField textField_Accinfo_Tomessage;
	private JTextField textField_Username_login;
	private JTextField textField_Accinfo_Newfeed;
	private JTextField textField_Accinfo_Ipupdate;
	private JTextField textField_Accinfo_Wallid;
	private JTextField textField_Accinfo_About;
	private JTextField textField_Contact_About;
	private JTextField textField_Contact_Wallid;
	private JTextField textField_Contact_Ipupdate;
	private JTextField textField_Contact_Newfeeds;
	private JTextField textField_Contact_Tomessage;
	private JTextField textField_Contact_Displayname;
	private JTextField textField_Contact_Username;
	
	private boolean isFindingFriend = false;
	private SocialProfile currentLookupFriend = null;
	private boolean isSynchronizingProfile;
	private HashMap<String,String> localFriendsContacts;
	CipherParameters[] publicKeys;
	private JTextArea textArea_CloudAuthentication;
	
	public ClientFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing window");
				if (ClientFrame.this.serviceHandler!=null)	{
					ClientFrame.this.saveUserProfile();
					ClientFrame.this.serviceHandler.logout();
				}
			}
		});
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
			textField_BootAddress.setText(NetworkUtil.getLocalAddress());
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
		    env.getParameters().setString("firewall_test_policy","always");
		    env.getParameters().setString("firewall_test_policy","always");
		    env.getParameters().setBoolean("probe_for_external_address", false);
		    env.getParameters().setBoolean("epost_nat_support", false);
		    //env.getParameters().setString("external_address", "83.180.236.252:12345");
		    //env.getParameters().remove("nat_handler_class");
				SocialServiceFactory factory = new SocialServiceFactory(env);
				try {
					String username = getUsername();
					SocialProfile user = ClientFrame.this.loadUserProfile(username+".dat");
					HashMap<String,Long> lastSeqs = ClientFrame.this.loadTopicsCache(username+"_lastseq.dat");
					if (user==null)
						serviceHandler = factory.newDSNAIdBasedSecureSocialService(getBindPort(), getBootPort(), getBootAddress(), ClientFrame.this, username);
					else
						serviceHandler = factory.newDSNAIdBasedSecureSocialService(getBindPort(), getBootPort(), getBootAddress(), ClientFrame.this, user, lastSeqs);
					System.out.println(serviceHandler);
					serviceHandler.setPreferEncrypted(false);
					updateAccountInfo();
					serviceHandler.initSubscribe();
					serviceHandler.pushProfileToDHT();
					ClientFrame.this.updateFriendList();
					ClientFrame.this.btnLaunch.setEnabled(false);
					ClientFrame.this.tabbedPane.setSelectedIndex(1);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				} catch (JoinFailedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//System.out.println(env.getParameters().getString("nat_search_policy"));
				//System.out.println(env.getParameters().getString("firewall_test_policy"));
				//System.out.println(env.getParameters().getString("external_address"));
				//System.out.println(env.getParameters().getBoolean("probe_for_external_address"));
				//System.out.println(env.getParameters().getString("nat_handler_class"));
			}
		});
		btnLaunch.setBounds(263, 155, 117, 29);
		panel.add(btnLaunch);
		
		textField_Username_login = new JTextField();
		textField_Username_login.setText("Anonymous");
		textField_Username_login.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Username_login.setColumns(10);
		textField_Username_login.setBounds(246, 122, 134, 28);
		panel.add(textField_Username_login);
		
		JLabel lblUsername_1 = new JLabel("Username:");
		lblUsername_1.setBounds(153, 129, 92, 16);
		panel.add(lblUsername_1);
		
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Account info", null, panel_3, null);
		panel_3.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username: ");
		lblUsername.setBounds(252, 27, 81, 16);
		panel_3.add(lblUsername);
		
		textField_Accinfo_Username = new JTextField();
		textField_Accinfo_Username.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_Username.setColumns(10);
		textField_Accinfo_Username.setBounds(345, 21, 192, 28);
		panel_3.add(textField_Accinfo_Username);
		
		JLabel lblDisplayName = new JLabel("Display name:");
		lblDisplayName.setBounds(252, 61, 92, 16);
		panel_3.add(lblDisplayName);
		
		textField_Accinfo_Displayname = new JTextField();
		textField_Accinfo_Displayname.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_Displayname.setColumns(10);
		textField_Accinfo_Displayname.setBounds(345, 54, 192, 28);
		panel_3.add(textField_Accinfo_Displayname);
		
		JLabel lblToMessage = new JLabel("To message:");
		lblToMessage.setBounds(252, 97, 92, 16);
		panel_3.add(lblToMessage);
		
		textField_Accinfo_Tomessage = new JTextField();
		textField_Accinfo_Tomessage.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_Tomessage.setColumns(10);
		textField_Accinfo_Tomessage.setBounds(345, 90, 192, 28);
		panel_3.add(textField_Accinfo_Tomessage);
		
		JButton btn_Accinfo_change = new JButton("Change");
		btn_Accinfo_change.setBounds(420, 273, 117, 29);
		panel_3.add(btn_Accinfo_change);
		
		JLabel lblNewfeeds = new JLabel("Newfeeds:");
		lblNewfeeds.setBounds(252, 137, 92, 16);
		panel_3.add(lblNewfeeds);
		
		textField_Accinfo_Newfeed = new JTextField();
		textField_Accinfo_Newfeed.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_Newfeed.setColumns(10);
		textField_Accinfo_Newfeed.setBounds(345, 130, 192, 28);
		panel_3.add(textField_Accinfo_Newfeed);
		
		JLabel lblIpUpdate = new JLabel("Ip update:");
		lblIpUpdate.setBounds(252, 176, 92, 16);
		panel_3.add(lblIpUpdate);
		
		textField_Accinfo_Ipupdate = new JTextField();
		textField_Accinfo_Ipupdate.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_Ipupdate.setColumns(10);
		textField_Accinfo_Ipupdate.setBounds(345, 169, 192, 28);
		panel_3.add(textField_Accinfo_Ipupdate);
		
		JLabel lblWallid = new JLabel("WallId:");
		lblWallid.setBounds(252, 216, 92, 16);
		panel_3.add(lblWallid);
		
		textField_Accinfo_Wallid = new JTextField();
		textField_Accinfo_Wallid.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_Wallid.setColumns(10);
		textField_Accinfo_Wallid.setBounds(345, 209, 192, 28);
		panel_3.add(textField_Accinfo_Wallid);
		
		JLabel lblAbout = new JLabel("About:");
		lblAbout.setBounds(252, 251, 92, 16);
		panel_3.add(lblAbout);
		
		textField_Accinfo_About = new JTextField();
		textField_Accinfo_About.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Accinfo_About.setColumns(10);
		textField_Accinfo_About.setBounds(345, 244, 192, 28);
		panel_3.add(textField_Accinfo_About);
		
		JPanel panel_4 = new JPanel();
		tabbedPane.addTab("Contact", null, panel_4, null);
		panel_4.setLayout(null);
		
		textField_Contact_About = new JTextField();
		textField_Contact_About.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_About.setColumns(10);
		textField_Contact_About.setBounds(115, 265, 192, 28);
		panel_4.add(textField_Contact_About);
		
		JLabel label_2 = new JLabel("About:");
		label_2.setBounds(22, 272, 92, 16);
		panel_4.add(label_2);
		
		JLabel label_6 = new JLabel("WallId:");
		label_6.setBounds(22, 237, 92, 16);
		panel_4.add(label_6);
		
		textField_Contact_Wallid = new JTextField();
		textField_Contact_Wallid.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_Wallid.setColumns(10);
		textField_Contact_Wallid.setBounds(115, 230, 192, 28);
		panel_4.add(textField_Contact_Wallid);
		
		textField_Contact_Ipupdate = new JTextField();
		textField_Contact_Ipupdate.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_Ipupdate.setColumns(10);
		textField_Contact_Ipupdate.setBounds(115, 190, 192, 28);
		panel_4.add(textField_Contact_Ipupdate);
		
		JLabel label_7 = new JLabel("Ip update:");
		label_7.setBounds(22, 197, 92, 16);
		panel_4.add(label_7);
		
		JLabel label_8 = new JLabel("Newfeeds:");
		label_8.setBounds(22, 158, 92, 16);
		panel_4.add(label_8);
		
		textField_Contact_Newfeeds = new JTextField();
		textField_Contact_Newfeeds.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_Newfeeds.setColumns(10);
		textField_Contact_Newfeeds.setBounds(115, 151, 192, 28);
		panel_4.add(textField_Contact_Newfeeds);
		
		textField_Contact_Tomessage = new JTextField();
		textField_Contact_Tomessage.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_Tomessage.setColumns(10);
		textField_Contact_Tomessage.setBounds(115, 111, 192, 28);
		panel_4.add(textField_Contact_Tomessage);
		
		JLabel label_9 = new JLabel("To message:");
		label_9.setBounds(22, 118, 92, 16);
		panel_4.add(label_9);
		
		JLabel label_10 = new JLabel("Display name:");
		label_10.setBounds(22, 82, 92, 16);
		panel_4.add(label_10);
		
		textField_Contact_Displayname = new JTextField();
		textField_Contact_Displayname.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_Displayname.setColumns(10);
		textField_Contact_Displayname.setBounds(115, 75, 192, 28);
		panel_4.add(textField_Contact_Displayname);
		
		textField_Contact_Username = new JTextField();
		textField_Contact_Username.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_Contact_Username.setColumns(10);
		textField_Contact_Username.setBounds(115, 42, 192, 28);
		panel_4.add(textField_Contact_Username);
		
		JLabel label_11 = new JLabel("Username: ");
		label_11.setBounds(22, 48, 81, 16);
		panel_4.add(label_11);
		
		JButton btnFind = new JButton("Find");
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClientFrame.this.findFriend();
			}
		});
		btnFind.setBounds(309, 43, 117, 29);
		panel_4.add(btnFind);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addFriend();
			}
		});
		btnAdd.setBounds(190, 302, 117, 29);
		panel_4.add(btnAdd);
		
		JButton button = new JButton("Remove");
		button.setBounds(616, 351, 113, 29);
		panel_4.add(button);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		tabbedPane.addTab("Messenger", null, panel_1, null);
		
		JLabel lblSubscribeTopics = new JLabel("List Friend");
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
		jlist_FriendList = new JList();
		jlist_FriendList.setBounds(6, 56, 264, 312);
		jlist_FriendList.setModel(listModel);
		jlist_FriendList.setBorder(lineBorder);
		panel_1.add(jlist_FriendList);
		
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
		
		JLabel lblFriendid = new JLabel("FriendId:");
		lblFriendid.setBounds(368, 6, 73, 16);
		panel_1.add(lblFriendid);
		
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
		
		textField_FriendId = new JTextField();
		textField_FriendId.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_FriendId.setColumns(10);
		textField_FriendId.setBounds(428, 0, 182, 28);
		panel_1.add(textField_FriendId);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Test storage", null, panel_2, null);
		panel_2.setLayout(null);
		
		JLabel lblString = new JLabel("Status:");
		lblString.setBounds(184, 6, 73, 16);
		panel_2.add(lblString);
		
		textArea_Status = new JTextArea();
		textArea_Status.setColumns(10);
		textArea_Status.setBounds(244, 6, 319, 158);
		panel_2.add(textArea_Status);
		
		JButton btnStore = new JButton("post");
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
				
				Runnable postStatusTask = new Runnable()	{
					public void run()	{
						postStatus();
					}
				};
				scheduler.scheduleAtFixedRate(postStatusTask, 10, 2, TimeUnit.SECONDS );
			}
		});
		btnStore.setBounds(446, 169, 117, 29);
		panel_2.add(btnStore);
		
		textPane_StatusDisplay = new JTextPane();
		textPane_StatusDisplay.setEditable(false);
		textPane_StatusDisplay.setBounds(246, 240, 317, 186);
		panel_2.add(textPane_StatusDisplay);
		
		JLabel label = new JLabel("Id:");
		label.setBounds(184, 206, 73, 16);
		panel_2.add(label);
		
		textField_StatusId = new JTextField();
		textField_StatusId.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_StatusId.setColumns(10);
		textField_StatusId.setBounds(244, 200, 182, 28);
		panel_2.add(textField_StatusId);
		
		JButton btnFetch = new JButton("fetch");
		btnFetch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fetchStatus();
			}
		});
		btnFetch.setBounds(446, 199, 117, 29);
		panel_2.add(btnFetch);
		
		JButton btnChangeSessionKey = new JButton("change sKey");
		btnChangeSessionKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//distributeSessionKey();
				final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
				
				Runnable postStatusTask = new Runnable()	{
					public void run()	{
						distributeSessionKey();
					}
				};
				scheduler.scheduleAtFixedRate(postStatusTask, 10, 2, TimeUnit.SECONDS );
				
			}
		});
		btnChangeSessionKey.setBounds(274, 169, 175, 29);
		panel_2.add(btnChangeSessionKey);
		
		JPanel panel_5 = new JPanel();
		panel_5.setLayout(null);
		tabbedPane.addTab("New tab", null, panel_5, null);
		
		textArea_CloudAuthentication = new JTextArea();
		textArea_CloudAuthentication.setColumns(10);
		textArea_CloudAuthentication.setBounds(428, 35, 319, 129);
		panel_5.add(textArea_CloudAuthentication);
		
		JLabel lblTokenId = new JLabel("Token Id:");
		lblTokenId.setBounds(357, 176, 73, 16);
		panel_5.add(lblTokenId);
		
		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClientFrame.this.confirmCloudToken();
			}
		});
		btnSubmit.setBounds(630, 169, 117, 29);
		panel_5.add(btnSubmit);
		
		JLabel label_13 = new JLabel("Bulletin:");
		label_13.setBounds(357, 35, 73, 16);
		panel_5.add(label_13);
		
		textField_cloudToken = new JTextField();
		textField_cloudToken.setHorizontalAlignment(SwingConstants.RIGHT);
		textField_cloudToken.setColumns(10);
		textField_cloudToken.setBounds(436, 170, 182, 28);
		panel_5.add(textField_cloudToken);
		
		JButton btnInitAuthenticate = new JButton("Init authenticate");
		btnInitAuthenticate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initGoogleCloudAuthenticate();
			}
		});
		btnInitAuthenticate.setBounds(594, 6, 153, 29);
		panel_5.add(btnInitAuthenticate);
	}
	
	public void addFriend(String friendId)	{
			listModel.addElement(friendId);
	}
	
	public void removeSubscribedTopic(String topic)	{
		if(this.subscribedTopic.contains(topic))	{
			this.subscribedTopic.remove(topic);
			listModel.removeElement(topic);
			this.serviceHandler.unsubscribe(topic);
		}
	}

	private static String CLIENT_ID = "74942622914-nlvoauooolp5dj9v3115krb0jma3susp.apps.googleusercontent.com";
  private static String CLIENT_SECRET = "YgO1hBnqY_7GX3oVNdjm7VeJ";
  private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";	
  GoogleAuthorizationCodeFlow flow;
  
	private void initGoogleCloudAuthenticate()	{
		HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new GsonFactory();
   
    flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
        .setAccessType("online")
        .setApprovalPrompt("auto").build();
    
    String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
    String txt = "Please open the following URL in your browser then type the authorization code:\n" + url;
    textArea_CloudAuthentication.setText(txt);	
	}
	
	private void confirmCloudToken()	{
		HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new GsonFactory();
    
		String code = textField_cloudToken.getText();
		GoogleTokenResponse response;
		try {
			System.out.println(code);
			response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
	    GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
	    
	    //Create a new authorized API client
	    Drive service = new Drive.Builder(httpTransport, jsonFactory, credential).build();
	    CloudStorageService cloudTest = new GoogleCloudStorageServiceImpl(service);
	    cloudTest.initializeDSNAFolders();
	    
	    this.serviceHandler.addCloudHandler(Location.GOOGLE_CLOUD.toString(), new GoogleCloudStorageServiceImpl(service));
		} catch (IOException e) {
			
			JOptionPane.showMessageDialog(this, "Create service for cloud failed!");
			e.printStackTrace();
		}
		
	}
	
	public int getBindPort()	{
		return Integer.parseInt(this.textField_BindPort.getText());
	}
	
	public int getBootPort()	{
		return Integer.parseInt(this.textField_BootPort.getText());
	}
	
	public String getBootAddress()	{
		return this.textField_BootAddress.getText();
	}
	
	public String getUsername()	{
		return textField_Username_login.getText();
	}
	
	private void publishMessage()	{
		String theId = this.textField_FriendId.getText().trim();
		String theMessage = this.textArea_Message.getText().trim();
		serviceHandler.sendMessage(theId, theMessage);
		this.textField_FriendId.setText("");
		this.textArea_Message.setText("");
		this.textField_FriendId.requestFocus();
	}
	
	private void removeSubscribedTopics()	{
		String theTopic = (String)jlist_FriendList.getSelectedValue();
		if(theTopic!=null)	{
			this.removeSubscribedTopic(theTopic);
		}
	}
	
	int count = 0;
	private void postStatus() {
		try {
			count++;
			serviceHandler.postStatus(textArea_Status.getText());
			if (count==1000)	{
				SwingUtilities.invokeLater(new Runnable()	{
					public void run()	{
						JOptionPane.showMessageDialog(ClientFrame.this, "Experiment done");
					}
				});
			}
		} catch (UserRecoverableAuthIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//textArea_Status.setText("");
	}
	
	private void fetchStatus()	{
		fetchStatus(textField_StatusId.getText());
	}
	
	private void fetchStatus(final String statusId)	{
		serviceHandler.lookupDHTById(statusId, new Continuation<PastContent, Exception>() {
      public void receiveResult(PastContent result) {
    		if (result instanceof DSNAPastContent)	{
    			BaseEntity entity = ((DSNAPastContent)result).getContent();
    			if (entity.getType()==Status.TYPE)	{
    				receiveStatus((Status)entity);
    			}
    			
    		}
      }

	    public void receiveException(Exception result) {
	      JOptionPane.showMessageDialog(ClientFrame.this, "Look up failed - "+statusId);
	    }
	  });
	}
	
	private void findFriend()	{
		
		serviceHandler.lookupDHTByName(textField_Contact_Username.getText(), new Continuation<PastContent, Exception>() {
      public void receiveResult(PastContent result) {
    		if (result instanceof DSNAPastContent)	{
    			BaseEntity entity = ((DSNAPastContent)result).getContent();
    			if (entity.getType()==SocialProfile.TYPE)	{
    				updateContactLookupTab((SocialProfile)entity);
    			}
    			
    		}
      }

	    public void receiveException(Exception result) {
	      JOptionPane.showMessageDialog(ClientFrame.this, "Look up failed - "+textField_StatusId.getText());
	    }
	  });
	}
	
	private void addFriend()	{
		if (serviceHandler.addFriend(currentLookupFriend))
			this.addFriend(currentLookupFriend.getUserId());
	}
	
	public void updateAccountInfo()	{
		SocialProfile user = ((SocialServiceImpl)serviceHandler).getUserProfile();
		if (user==null)	{
			
		} else	{
			this.textField_Accinfo_Username.setText(user.getOwnerUsername());
			this.textField_Accinfo_Displayname.setText(user.getOwnerDisplayName());
			this.textField_Accinfo_About.setText(user.getAbout());
			this.textField_Accinfo_Tomessage.setText(user.getToDeliverMessageTopic());
			this.textField_Accinfo_Newfeed.setText(user.getToFollowNotificationTopic());
			this.textField_Accinfo_Ipupdate.setText(user.getToFollowRealIpTopic());
			this.textField_Accinfo_Wallid.setText(user.getWallObjectId());
		}
	}
	
	public void updateFriendList()	{
		localFriendsContacts = serviceHandler.getFriendsContacts();
		for (String username:localFriendsContacts.keySet())	{
			this.addFriend(localFriendsContacts.get(username));
		}
	}
	
	public void updateContactLookupTab(SocialProfile profile)	{
		currentLookupFriend = profile;
		if (profile==null)	{
			this.textField_Contact_Displayname.setText("Cannot find this user's profile");
			this.textField_Contact_About.setText("");
			this.textField_Contact_Tomessage.setText("");
			this.textField_Contact_Newfeeds.setText("");
			this.textField_Contact_Ipupdate.setText("");
			this.textField_Contact_Wallid.setText("");			
		} else	{
			this.textField_Contact_Username.setText(profile.getOwnerUsername());
			this.textField_Contact_Displayname.setText(profile.getOwnerDisplayName());
			this.textField_Contact_About.setText(profile.getAbout());
			this.textField_Contact_Tomessage.setText(profile.getToDeliverMessageTopic());
			this.textField_Contact_Newfeeds.setText(profile.getToFollowNotificationTopic());
			this.textField_Contact_Ipupdate.setText(profile.getToFollowRealIpTopic());
			this.textField_Contact_Wallid.setText(profile.getWallObjectId());
		}
	}
	
	private SocialProfile loadUserProfile(String fileName)	{
		try	{
			FileInputStream fis = new FileInputStream(fileName);
			Object object = FileUtil.readObject(fis);
			SocialProfile profile;
			if (object!=null)	{
					profile = (SocialProfile) object;
					return profile;
			} 
			return null;
		} catch (Exception ex)	{
			return null;
		}
	}
	
	public void saveUserProfile()	{
		try	{
			SocialProfile user = serviceHandler.getUserProfile();
			String profileFileName = user.getOwnerUsername()+".dat";
			FileOutputStream fout = new FileOutputStream(profileFileName, false);
			System.out.println("Save file to "+profileFileName);
			FileUtil.writeObject(fout, user);
			
			String lastSeqFileName = user.getOwnerUsername()+"_lastseq.dat";
			System.out.println("Save last sequence to "+lastSeqFileName);
			fout = new FileOutputStream(lastSeqFileName, false);
			HashMap<String,Long> topicsLastSeq = ((SocialServiceImpl)serviceHandler).getTopicsLastSeq();
			FileUtil.writeObject(fout, topicsLastSeq);
		} catch (Exception ex)	{
			ex.printStackTrace();
		}
	}
	
	public HashMap<String,Long> loadTopicsCache(String fileName)	{
		try	{
			FileInputStream fis = new FileInputStream(fileName);
			Object object = FileUtil.readObject(fis);
			HashMap<String,Long> lastSeqs;
			if (object!=null)	{
					lastSeqs = (HashMap<String,Long>) object;
					return lastSeqs;
			} 
			return null;
		} catch (Exception ex)	{
			return null;
		}
	}

	@Override
	public void receiveLookupException(Exception result) {
		// TODO Auto-generated method stub
		isFindingFriend = false;
		result.printStackTrace();
	}
	
	@Override
	public void receiveLookupNull() {
		if (isFindingFriend)	{
			updateContactLookupTab(null);
			isFindingFriend = false;
		}
	}

	@Override
	public void receiveInsertException(Exception result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveMessage(Message msg) {
		JOptionPane.showMessageDialog(this, msg.toString());
	}

	@Override
	public void receiveNotification(Notification notification) {
		switch (notification.getNotificationType())	{
			case NEWFEEDS:
				//JOptionPane.showMessageDialog(this, notification.getArgument("objectId"));
				String googleId = notification.getFileId(Location.GOOGLE_CLOUD);
				if (googleId!=null)
					System.out.println("GoogleId:" + googleId);
				else 
					fetchStatus(notification.getFileId(Location.DHT));
				break;
			default:
		}
	}

	@Override
	public void receiveStatus(Status status) {
		JOptionPane.showMessageDialog(this, status.toString());
	}

	@Override
	public void receiveSocialProfile(SocialProfile profile) {
		if (isFindingFriend)	{
			updateContactLookupTab(profile);
			isFindingFriend = false;
		}		
		
		if (isSynchronizingProfile && serviceHandler.getUserProfile().isCompatibleProfile(profile))	{
			serviceHandler.updateProfile(profile);
			isSynchronizingProfile = false;
		}
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

	@Override
	public void receiveBaseEntity(BaseEntity entity) {
		// TODO Auto-generated method stub
		
	}
	
	private synchronized CipherParameters[] getPublicKeys() throws IOException, InvalidCertificateException, KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException	{
		if (publicKeys==null)	{
			KeyStore localTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			System.out.println(KeyStore.getDefaultType());
			InputStream in = new FileInputStream("dsnatrustkeystore.jks");
			localTrustStore.load(in, "kthdsna".toCharArray());  
      HttpsURLConnection urlConnection = NetworkUtil.establishHttpsConnection(publicKeyURL, localTrustStore);
    	String encodedSysPublicKey = FileUtil.readString(urlConnection.getInputStream());
    	urlConnection.disconnect();
    	publicKeys = ASN1Util.extractPublicKey(ASN1Util.decodeIBESysPublicParams(encodedSysPublicKey));	
		}
		return publicKeys;
	}
	
	private KeyInfo currentSessionKey;
	private JTextField textField_cloudToken;
	private void distributeSessionKey() {
		// TODO Auto-generated method stub
		try {
			count++;
			CipherParameters[] publicKeys = getPublicKeys();
			serviceHandler.changeAndDistributeSessionKey(IdBasedSecureSocialService.AESAlgorithm ,publicKeys[1], new Continuation<KeyInfo, Exception>() {
        public void receiveResult(KeyInfo result) {          
      		currentSessionKey = result;
      		System.out.println(currentSessionKey.getKeyId());
        }

        public void receiveException(Exception result) {
        	result.printStackTrace();
        }
			
			});
			
			if (count==1000)	{
				SwingUtilities.invokeLater(new Runnable()	{
					public void run()	{
						JOptionPane.showMessageDialog(ClientFrame.this, "Experiment session key done");
					}
				});
			}			
			
		} catch (IOException | InvalidCertificateException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void receiveBroadcastException(Exception e) {
		// TODO Auto-generated method stub
		
	}
}
