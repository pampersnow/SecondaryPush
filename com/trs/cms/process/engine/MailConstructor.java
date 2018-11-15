package com.trs.cms.process.engine;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MailConstructor {

	//private String EnvelopeFrom;		//�ʼ����͵�ַ
	//private String MailHost;			//�ʼ�������
	private String UserName;			//�û���
	private String PassWord;			//�û�����
	private MimeMessage mimeMsg; 		//MIME�ʼ�����
	private Session session; 			//�ʼ��Ự����
	private Properties props; 			//ϵͳ����
	//private boolean needAuth = false; 	//smtp�Ƿ���Ҫ��֤
	private Multipart mp; 				//Multipart����,�ʼ�����,����,���������ݾ���ӵ����к�������MimeMessage����
	
	public MailConstructor () {
		
	}
	/**
	public void sendMail() {
	setSmtpHost(getConfig.mailHost);//���û��ָ���ʼ�������,�ʹ�getConfig���л�ȡ
	createMimeMessage();
	}
	**/
	public MailConstructor(String smtp){
		setSmtpHost(smtp);
		createMimeMessage();
	}
	/**
	* @param hostName String
	*/
	public void setSmtpHost(String hostName) {
		
		System.out.println("����ϵͳ���ԣ�mail.smtp.host = "+hostName);
		if(props == null)props = System.getProperties(); 		//���ϵͳ���Զ���
		props.put("mail.smtp.host",hostName); 					//����SMTP����
	}
	
	/**
	* @return boolean
	*/
	public boolean createMimeMessage() {
		try {
			System.out.println("׼����ȡ�ʼ��Ự����");
			session = Session.getDefaultInstance(props,null); 	//����ʼ��Ự����
		} catch(Exception e){
			System.err.println("��ȡ�ʼ��Ự����ʱ��������"+e);
			return false;
		}
	
		System.out.println("׼������MIME�ʼ�����");
		try{
			mimeMsg = new MimeMessage(session); 				//����MIME�ʼ�����
			mp = new MimeMultipart();
			return true;
		} catch(Exception e){
			System.err.println("����MIME�ʼ�����ʧ�ܣ�"+e);
			return false;
		}
	}
	/**
	* @param need boolean
	*/
	public void setNeedAuth(boolean need) {
		System.out.println("����smtp�����֤��mail.smtp.auth = "+need);
		if( props == null ) props = System.getProperties();
	
		if ( need ) {
			props.put("mail.smtp.auth","true");
		} else {
			props.put("mail.smtp.auth","false");
		}
	}
	/**
	* @param port int
	*/
	public void setPort(int port) {
		System.out.println("����smtp�˿ڣ�mail.smtp.port = " + port);
		if( props == null ) props = System.getProperties();
		props.put("mail.smtp.port",Integer.toString(port));
	}
	/**
	* @param name String
	* @param pass String
	*/
	public void setNamePass(String name,String pass) {
		UserName = name;
		PassWord = pass;
	}
	/**
	* @param mailSubject String
	* @return boolean
	*/
	public boolean setSubject(String mailSubject) {
		System.out.println("�����ʼ����⣡");
		try{
			mimeMsg.setSubject(mailSubject, "GB2312");
			return true;
		} catch(Exception e) {
			System.err.println("�����ʼ����ⷢ������");
			return false;
		}
	}
	/**
	* @param mailBody String
	*/
	public boolean setBody(String mailBody) {
		try{
			BodyPart bp = new MimeBodyPart();
			bp.setContent(mailBody,"text/html;charset=GB2312");
			mp.addBodyPart(bp);
			return true;
		} catch(Exception e){
			System.err.println("�����ʼ�����ʱ��������"+e);
			return false;
		}
	}
	/**
	* @param name String
	* @param pass String
	*/
	public boolean addFileAffix(String filename) {

		System.out.println("�����ʼ�������"+filename);
		try{
			BodyPart bp = new MimeBodyPart();
			FileDataSource fileds = new FileDataSource(filename);
			bp.setDataHandler(new DataHandler(fileds));
			bp.setFileName(fileds.getName());
			mp.addBodyPart(bp);
			return true;
		} catch(Exception e){
			System.err.println("�����ʼ�������"+filename+"��������"+e);
			return false;
		}
	}
	/**
	* @param name String
	* @param pass String
	*/
	public boolean setFrom(String from) {
		System.out.println("���÷����ˣ�");
		try{
			mimeMsg.setFrom(new InternetAddress(from)); //���÷�����
			return true;
		} catch(Exception e) {
			return false; 
		}
	}
	/**
	* @param name String
	* @param pass String
	*/
	public boolean setTo(String to){
		if(to == null)return false;
	
		try {
			mimeMsg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	* @param name String
	* @param pass String
	*/
	public boolean setCopyTo(String copyto) {
		if(copyto == null)return false;
		try{
			mimeMsg.setRecipients(Message.RecipientType.CC,(Address[])InternetAddress.parse(copyto));
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	* @param name String
	* @param pass String
	*/
	public boolean sendout() {
		try{
			mimeMsg.setContent(mp);
			mimeMsg.saveChanges();
			System.out.println("���ڷ����ʼ�....");
		
			Session mailSession = Session.getInstance(props,null);
			int port = Integer.parseInt((String)props.get("mail.smtp.port"));
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String)props.get("mail.smtp.host"),port,UserName,PassWord);
			transport.sendMessage(mimeMsg,mimeMsg.getRecipients(Message.RecipientType.TO));
			//transport.send(mimeMsg);
		
			System.out.println("�����ʼ��ɹ���");
			transport.close();
		
			return true;
		} catch(Exception e) {
			System.err.println("�ʼ�����ʧ�ܣ�"+e);
			return false;
		}
	}

	/**
	* Just do it as this
	*/
	public static void main(String[] args) {

		String mailbody = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>"+
		"<div align=center><a href=http://www.csdn.net> csdn </a></div>";
	
		MailConstructor themail = new MailConstructor("smtp.163.com");
		themail.setNeedAuth(true);
		themail.setPort(25);
	
		if(themail.setSubject("����") == false) return;
		if(themail.setBody(mailbody) == false) return;
		if(themail.setTo("zhang.yanan@trs.com.cn,zhangyanan2008@gmail.com,zhangyananqq@126.com") == false) return;
		if(themail.setFrom("hdbbsmail@163.com") == false) return;
		//if(themail.addFileAffix("c:\\boot.ini") == false) return;
		themail.setNamePass("hdbbsmail","zhangyanan");
	
		if(themail.sendout() == false) return; 
	}
	
	/**
	public static  void MailConstructors (TempUser tempUser) throws Exception {

		//------------------------------------------------------------
		//-------------------- �������Ͷ������ --------------------
		//------------------------------------------------------------
		String from = "";		//���͵�ַ
		String mailhost = "";	//�ʼ�������
		String username = "";	//�˺�
		String password = "";	//����
		//------------------------------------------------------------
		//-------------------- ����Ŀ�������� --------------------
		//------------------------------------------------------------
		String to = "";			//���ܵ�ַ
		String cc = "";			//���͵�ַ
		String bcc = "";		//���͵�ַ
		//------------------------------------------------------------
		//-------------------- �������ݶ������ --------------------
		//------------------------------------------------------------
		String subject = "";	//�ʼ�����
		String content = "";	//�ʼ�����
		//FileDataSource fds = new FileDataSource(_fds);	//�ʼ�����
		/**
		String to = "cheugu@163.com",
			subject = "test",
			from = "solobutterfly@163.com",
			cc = null,
			bcc = null;
		String mailhost = "smtp.163.com";
		String username = "solobutterfly";
		String password = "1302243355";
		
		boolean debug = false;
		Properties props = System.getProperties();
		props.put("mail.smtp.auth", "true");
		SMTPAuth auth = new SMTPAuth(username, password);

		if (mailhost != null) {
			props.put("mail.smtp.host", mailhost);
		}
		// Get a Session object
		Session session = Session.getDefaultInstance(props, auth);
		if (debug) {
			session.setDebug(true);
		}
		// construct the message
		Message msg = new MimeMessage(session);
		if (from != null) {
			msg.setFrom(new InternetAddress(from));
		} else {
			msg.setFrom();
		}
		msg.setRecipients( Message.RecipientType.TO,InternetAddress.parse(to, false));
		if (cc != null) {
			msg.setRecipients(
				Message.RecipientType.CC,
				InternetAddress.parse(cc, false));
		}
		if (bcc != null) {
			msg.setRecipients(
				Message.RecipientType.BCC,
				InternetAddress.parse(bcc, false));
		}
		subject = new Date().toLocaleString();
		msg.setSubject(subject);

		MimeBodyPart mbp1 = new MimeBodyPart();
		
		mbp1.setContent(content, "text/html");
		MimeMultipart mp = new MimeMultipart("related"); //alternative
		mp.addBodyPart(mbp1);
		//mp.addBodyPart(mbp2);
		msg.setContent(mp);

		msg.setSentDate(new Date());
		Transport.send(msg);
		System.out.println(mp.getCount());
		System.out.println("\nMail was sent successfully.");
		//return true;
	}
	**/
}
/**
 * 
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class SMTPAuth extends javax.mail.Authenticator {
	private String user, password;
	
	public SMTPAuth(String u, String p) {
		user = u;
		password = p;
	}
	public void getuserinfo(String getuser, String getpassword) {
		user = getuser;
		password = getpassword;
	}
	protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
		return new javax.mail.PasswordAuthentication(user, password);
	}
}
