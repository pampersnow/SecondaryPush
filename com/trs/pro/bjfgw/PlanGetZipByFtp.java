package com.trs.pro.bjfgw;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.infra.common.WCMException;

public class PlanGetZipByFtp extends BaseStatefulScheduleWorker {

	private static Logger s_logger = Logger
			.getLogger(com.trs.pro.bjfgw.PlanGetZipByFtp.class);
	private FtpClient ftpClient;
	private static String encoding = System.getProperty("file.encoding");

	public void start() throws WCMException {
		String PARA1 = "172.26.56.73,8021,web_ftp,!qaz2wsx";
		String PARA2 = "/";
		String PARA3 = "/wcm3/wcm/zfxxgk";

		String[] para = PARA1.split(",");
		boolean flag = connectServer(para[0], Integer.parseInt(para[1]),
				para[2], para[3], PARA2);
		s_logger.info("ftp连接成功,并指到指定目录判断条件flag:" + flag);

		if (flag) {
			try {
				download(PARA2, PARA3);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeConnect();
			}
		}
	}

	@Override
	protected void execute() throws WCMException {
		/*
		 * String PARA1 = "172.26.56.73,8021,web_ftp,!qaz2wsx"; String PARA2 =
		 * "/"; String PARA3 = "/wcm3/wcm/zfxxgk";
		 */

		String PARA1 = getArgAsString("PARA1");
		String PARA2 = getArgAsString("PARA2");
		String PARA3 = getArgAsString("PARA3");

		String[] para = PARA1.split(",");
		boolean flag = connectServer(para[0], Integer.parseInt(para[1]),
				para[2], para[3], PARA2);
		s_logger.info("ftp连接成功,并指到指定目录判断条件flag:" + flag);
		if (flag) {
			try {
				download(PARA2, PARA3);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				closeConnect();
			}
		}
	}

	public void download(String remotePath,String localPath) {
		TelnetInputStream is = null;
		FileOutputStream os = null;
		try {
			DataInputStream dis = new DataInputStream(ftpClient.nameList(remotePath));
			String fileone = "";
			while ((fileone = dis.readLine()) != null) {
				s_logger.info("外网服务器/up下文件名:" + fileone);
				boolean isdir = isDirExist(fileone);
				if(!isdir){
					is = ftpClient.get(fileone);
					os = new FileOutputStream(localPath);
					byte[] bytes = new byte[1024];
					int c;
					while ((c = is.read(bytes)) != -1) {
						os.write(bytes, 0, c);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public boolean isDirExist(String dir) {
		try {
			ftpClient.cd(dir);
		} catch (Exception e) {

			return false;
		}
		return true;
	}

	public boolean connectServer(String ip, int port, String user,
			String password, String path) {
		boolean flag = false;
		try {
			/* ******连接服务器的两种方法****** */
			// 第一种方法
			ftpClient = new FtpClient();
			ftpClient.openServer(ip, port);
			// 第二种方法
			// ftpClient = new FtpClient(ip);

			ftpClient.login(user, password);
			// 设置成2进制传输
			ftpClient.binary();
			s_logger.info(":::::::::FTP服务器连接成功");
			if (path.length() != 0) {
				// 把远程系统上的目录切换到参数path所指定的目录
				ftpClient.cd(path);
			}
			ftpClient.binary();
			flag = true;
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return flag;
	}

	public void closeConnect() {
		try {
			ftpClient.closeServer();
			s_logger.info(":::::::::FTP服务器连接关闭成功");
		} catch (IOException ex) {
			s_logger.info(":::::::::FTP服务器连接关闭失败");
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
