package com.trs.dzda;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.trs.components.metadata.center.MetaViewData;

public class MailServiceProvider {
	
	public void getMailInformation(String objid)throws Exception{
		MetaViewData obj = MetaViewData.findById(Integer.parseInt(objid));
		System.out.println("============="+obj.getPropertyAsString("WORDTITLE"));
	}

	public void sendMail()throws Exception{
		Properties properties = new Properties();
		// 开启debug调试 ，打印信息
		properties.setProperty("mail.debug", "true");
		// 发送服务器需要身份验证
		properties.setProperty("mail.smtp.auth", "true");
		// 发送服务器端口，可以不设置，默认是25 "mail.smtp.auth", "true"
		properties.setProperty("mail.smtp.port", "25");
		// 发送邮件协议名称
		// properties.setProperty("mail.transport.protocol", "smtp");
		// 设置邮件服务器主机名
//邮箱服务器地址
		properties.setProperty("mail.smtp.host", "smtp.163.com");
		properties.setProperty("mail.imap.partialfetch", "false");
		properties.setProperty("mail.mime.charset","UTF-8");
		
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// 在session中设置账户信息，Transport发送邮件时会使用
//服务器邮箱、授权码
				return new PasswordAuthentication("cgb2499404424", "qq1234");
			}
		});
		
		Message message = new MimeMessage(session);
//服务器邮箱
		message.setFrom(new InternetAddress("cgb2499404424@163.com"));
//邮件主题
		message.setSubject("邮件主题");
		//message.setText("邮件内容");
		message.setSentDate(new Date());
		
		/* 发送附件部分 */
//附件路径
		File file = new File(
				"C:\\Users\\Administrator.TRS-20140520WDQ\\Desktop\\38013中华人民共和国政府和印度共和国政府关于恢复边境贸易的备忘录F.pdf");
		
		Multipart mp = new MimeMultipart();
		//创建附件部分
		MimeBodyPart mbp = new MimeBodyPart();
		//获取文件
		DataSource ds = new FileDataSource(file);
		DataHandler dh = new DataHandler(ds);
		mbp.setDataHandler(dh); // 得到附件本身并至入BodyPart
		mbp.setFileName(MimeUtility.encodeText(file.getName())); // 得到文件名同样至入BodyPart
		mp.addBodyPart(mbp);
		
		//发送文本内容
		MimeBodyPart mbp2 = new MimeBodyPart();  
//邮件正文
		String cont = "中国中医科学院（China Academy of Chinese Medical Sciences）始建于1955年，前身为原卫生部中医研究院，1971年与北京中医学院合并，更名为中国中医研究院。2005年12月举行五十周年院庆时，更名为中国中医科学院."; 
        mbp2.setContent(cont, "text/html;charset=utf-8");  
        mp.addBodyPart(mbp2);  
		
		message.setContent(mp, "text/plain;charset=utf-8");
		message.saveChanges();
//发送邮箱
		Transport.send(message, InternetAddress.parse("2499404424@qq.com"));

		System.out.println("邮件发送完毕！");
	}
}
