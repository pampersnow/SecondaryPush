package com.trs.pro.bjfgw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.infra.common.WCMException;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyDateTime;
import com.ucap.restful.client.ResClientUtils;
import com.ucap.utils.TimeUtil;

public class PlanGetfileByFtp2 extends BaseStatefulScheduleWorker {

	private static Logger s_logger = Logger
			.getLogger(com.trs.pro.bjfgw.PlanGetfileByFtp2.class);

	//策略编码 (对接接口时需要管理员分配部门对应策略编码)
	private static String strategyCode = "gkxtCode";
	//密钥(对接接口时需要管理员分配部门对应密钥)
	private static String key = "gkxtjmcl";	
	//添加稿件
	private static String addManuscriptUrl="http://172.26.66.11:8888/website-webapp/rest/manuscript/addManuscript";
	//获取栏目
	private static String getChannelsByWebsiteCodeUrl="http://172.26.66.11:8888/website-webapp/rest/channel/getChannelsByWebsiteCode";
	
	private FtpClient ftpClient;

	private static int BUFFERSIZE = 2048;
	
	public void start()throws WCMException{
		//String PARA1 = "202.127.160.56,21,sjzfxxgk,qwer1234";
		//String PARA2 = "/wcm/sjzfxxgk/up";
		//String PARA3 = "/wcm/sjzfxxgk/up";
		String PARA1 = "172.26.56.73,8021,web_ftp,!qaz2wsx";
		String PARA2 = "/";
		String PARA3 = "/";
		String PARA4 = "/trs/webservice/govopen/xml";
		String PARA5 = "/trs/webservice/govopen/up";
		String PARA6 = "GovopenWebservice";
		String PARA7 = "http://172.31.3.64:9080/services/GovopenWebservice?wsdl";
		String PARA8 = "/trs/webservice/govopen/value";

		String[] para = PARA1.split(",");
		boolean flag = connectServer(para[0], Integer.parseInt(para[1]),
				para[2], para[3], PARA2);
		s_logger.info("ftp连接成功,并指到指定目录判断条件flag:" + flag);
		if (flag) {
			// 下载
			try {
				//copy(PARA5,PARA4);
				download(PARA2, PARA3, PARA4, PARA5, PARA6, PARA7, PARA8);
				//ArrayList result = getXmlData(PARA4);
				//pushData(result);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				closeConnect();
			}
			
		}
	}
	protected void execute() throws WCMException {
		// TODO Auto-generated method stub
		s_logger.info("PlanGetfileByFtp.line :::::::::::::::::::::::::::定时获取外网xml文件开始,执行时间:"
				+ CMyDateTime.now());
		String PARA1 = getArgAsString("PARA1");// 外网WCM的IP地址:202.127.166.122,21,Viwe,199301180209
		String PARA2 = getArgAsString("PARA2");// 登录远程系统上时切换到的指定目录:/up
		String PARA3 = getArgAsString("PARA3");// 外网WCM生成的XML文件所在路径:f:/wcm/sjzfxxgk/up
		String PARA4 = getArgAsString("PARA4");// XML文件复制到内网的存储路径:e:/xml
		String PARA5 = getArgAsString("PARA5");// 内网打压缩zip包所在路径：e:/wcm
		String PARA6 = getArgAsString("PARA6");// web服务名：GovopenWebservice
		String PARA7 = getArgAsString("PARA7");// web服务的URL地址：http://172.31.3.64:9080/services/GovopenWebservice?wsdl
		String PARA8 = getArgAsString("PARA8");// 返回XML存放地址

		String[] para = PARA1.split(",");
		boolean flag = connectServer(para[0], Integer.parseInt(para[1]),
				para[2], para[3], PARA2);
		s_logger.info("ftp连接成功,并指到指定目录判断条件flag:" + flag);
		if (flag) {
			// 下载
			download(PARA2, PARA3, PARA4, PARA5, PARA6, PARA7, PARA8);
			closeConnect();
		}
		s_logger.info("PlanGetfileByFtp.line :::::::::::::::::::::::::::定时获取外网xml文件结束,执行时间:"
				+ CMyDateTime.now());
	}
	
	
	
	/**
	 * 服务器连接
	 * 
	 * @param ip
	 *            服务器IP
	 * @param port
	 *            服务器端口
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * @param path
	 *            服务器路径
	 */
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

	/**
	 * 关闭连接
	 */
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

	/**
	 * 下载文件
	 * 
	 * @param remoteFile
	 *            远程文件路径(服务器端)
	 * @param localFile
	 *            本地文件路径(客户端)
	 * @param para5
	 *            zip包所在路径
	 * @param para6
	 *            web的服务名推送
	 * @param para7
	 *            web服务的url地址
	 * @param para8
	 *            返回value的存储路径
	 */
	/*String PARA2 = "/wcm/sjzfxxgk/up";
	String PARA3 = "/wcm/sjzfxxgk/up";
	String PARA4 = "/trs/webservice/govopen/xml";
	String PARA5 = "/trs/webservice/govopen/up";
	String PARA6 = "GovopenWebservice";
	String PARA7 = "http://172.31.3.64:9080/services/GovopenWebservice?wsdl";
	String PARA8 = "/trs/webservice/govopen/value";*/
	public void download(String para2, String remoteFile, String localFile,
			String para5, String para6, String para7, String para8) {
		TelnetInputStream is = null;
		FileOutputStream os = null;
		try {
			// 获取远程机器上的文件filename，借助TelnetInputStream把该文件传送到本地。
			DataInputStream dis = new DataInputStream(
					ftpClient.nameList(remoteFile));
			String wjj_name = "";
			while ((wjj_name = dis.readLine()) != null) {
				s_logger.info("外网服务器/up下文件夹名" + wjj_name);
				boolean isdir = isDirExist(wjj_name);
				if (isdir) {
					// 外网wcm服务器上param时间戳文件夹，window系统的路径显示为\,所以写成\\
					String localfile = String.valueOf(wjj_name).substring(
							String.valueOf(wjj_name).lastIndexOf("/"));
					File file_in = new File(localFile + "/" + localfile);
					if (file_in.exists()) {
						s_logger.info("内网服务器已经存在文件夹" + file_in);
					} else {
						file_in.mkdir();// 本地创建同名的文件夹
						DataInputStream dism = new DataInputStream(
								ftpClient.nameList(wjj_name));
						String a_xmlname = "";
						while ((a_xmlname = dism.readLine()) != null) {
							String filename = String.valueOf(a_xmlname);// param时间戳文件夹下的文件：xml，dat，jpg等
							/********** 新增修改部分 ************/
							String xmlname = filename.substring(filename
									.lastIndexOf(".") + 1);
							/********** 新增修改部分 ************/
							if (!xmlname.equals("exe")
									&& !xmlname.equals("jspx")
									&& !xmlname.equals("bat")
									&& !xmlname.equals("dll")
									&& !xmlname.equals("so")
									&& !xmlname.equals("acm")
									&& !xmlname.equals("com")
									&& !xmlname.equals("cpl")
									&& !xmlname.equals("drv")
									&& !xmlname.equals("scr")
									&& !xmlname.equals("sys")) {

								s_logger.info("xml或附件的文件名:" + filename);
								System.out.println(":::"
										+ filename.substring(remoteFile
												.length() - para2.length()));
								is = ftpClient.get(filename
										.substring(remoteFile.length()
												- para2.length()));
								// 复制到本地(内网)的文件路径以及文件名,window系统的路径显示为\,所以写成\\
								String lfilename = file_in
										+ "/"
										+ filename.substring(filename
												.lastIndexOf("/"));
								os = new FileOutputStream(lfilename);
								byte[] bytes = new byte[1024];
								int c;
								while ((c = is.read(bytes)) != -1) {
									os.write(bytes, 0, c);
								}
								/********** 新增修改部分 ************/
							}

						}
						/*String zippath = fileToZip(file_in, para5);
						if (!zippath.equals("")) {
							s_logger.info("成功生成zip包,正在调取接口推送");
							updateNewActiveInfo(zippath, para6, para7, para8);
						}*/
					}
				}
			}
			try {
				ArrayList result = this.getXmlData(localFile);
				if(result != null && result.size() != 0){
					this.pushData(result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException ex) {
			s_logger.error("FTP下载文件失败");
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
	 * 
	 * @param sourceFilePath
	 *            :待压缩的文件路径
	 * @param zipFilePath
	 *            :压缩后存放路径
	 * @return zip包文件的绝对路径
	 */
	public String fileToZip(File sourceFile, String zipFilePath) {
		String zipfilename = "";
		// File sourceFile = new File(sourceFilePath);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new Date());
		try {
			Date date = format.parse(time);
			if (sourceFile.exists() == false) {
				System.out.println("待压缩的文件目录:" + sourceFile + "不存在.");
			} else {
				try {
					double random = Math.random() * 1000000;
					String code = String.valueOf(random).substring(0, 4);
					zipfilename = zipFilePath + "/param" + +date.getTime()
							+ code + ".zip";
					File zipFile = new File(zipfilename);
					if (zipFile.exists()) {
						System.out.println(zipFilePath + "目录下存在名字为"
								+ zipfilename + "的zip包");
					} else {
						File[] sourceFiles = sourceFile.listFiles();
						if (null == sourceFiles || sourceFiles.length < 1) {
							System.out.println("待压缩的文件目录:" + sourceFile
									+ "里面不存在文件,无需压缩.");
						} else {
							fos = new FileOutputStream(zipFile);
							zos = new ZipOutputStream(new BufferedOutputStream(
									fos));
							byte[] bufs = new byte[1024 * 10];
							for (int i = 0; i < sourceFiles.length; i++) {
								// 创建ZIP实体，并添加进压缩包
								ZipEntry zipEntry = new ZipEntry(
										sourceFiles[i].getName());
								zos.putNextEntry(zipEntry);
								// 读取待压缩的文件并写进压缩包里
								fis = new FileInputStream(sourceFiles[i]);
								bis = new BufferedInputStream(fis, 1024 * 10);
								int read = 0;
								while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
									zos.write(bufs, 0, read);
								}
							}

						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					// 关闭流
					try {
						if (null != bis)
							bis.close();
						if (null != zos)
							zos.close();
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return zipfilename;
	}

	/**
	 * 更新主动公开新信息
	 * 
	 * @param zippath
	 *            推送的zip包所在路径
	 * @param para6
	 *            web的服务名
	 * @param para7
	 *            web服务的url地址
	 * @param para8
	 *            推送返回value的存储路径
	 * */
	public void updateNewActiveInfo(String zippath, String para6, String para7,
			String para8) {
		String methodName = "updateNewActiveInfo";
		// 输入文件
		String inputFilePath = zippath;
		// 输出文件
		String outputFilePath = para8 + "/" + System.currentTimeMillis();
		// 调用交换信息的函数
		this.exchangeInfo(methodName, inputFilePath, outputFilePath, para6,
				para7);
		// 到相应的目录进行结果观测
	}

	/**
	 * 交换信息
	 * 
	 * @param methodName
	 *            操作的服务
	 * @param inputFilePath
	 *            测试用的输入文件路径
	 * @param outputFilePath
	 *            测试用的输出文件路径
	 * @param para6
	 *            web的服务名
	 * @param para7
	 *            web服务的url地址
	 */
	private void exchangeInfo(String methodName, String inputFilePath,
			String outputFilePath, String para6, String para7) {

		try {
			// 读取测试文件到字节数组中
			FileInputStream fileInputStream = new FileInputStream(inputFilePath);
			byte[] inByteStream = null;
			byte[] inputBuffer = new byte[2048];
			while (true) {
				int cnt = fileInputStream.read(inputBuffer);
				if (cnt < 0)
					break;
				inByteStream = joinBytes(inByteStream, inputBuffer);
			}

			// 调用远程方法
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(para7));
			// 设置操作的名称
			call.setOperationName(new QName(para6, methodName));
			call.setReturnType(Constants.XSD_BASE64);

			// 设置参数

			call.addParameter("byteStream", Constants.XSD_BASE64,
					ParameterMode.IN);
			byte[] retValue = (byte[]) call
					.invoke(new Object[] { inByteStream });
			unzipFile(retValue, outputFilePath);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			System.err.println("交换数据失败:" + ex.getMessage());
		}

	}

	/**
	 * 拼接字符数组
	 * 
	 * @param input1
	 *            输入值1
	 * @param input2
	 *            输入值2
	 * @return 拼接后的字符数组
	 */
	private byte[] joinBytes(byte[] input1, byte[] input2) {
		// 构造新的字符数组
		int length = 0;
		if (input1 != null)
			length = length + input1.length;
		if (input2 != null)
			length = length + input2.length;
		byte[] retValue = new byte[length];
		int index = 0;

		// 读入第一个字符数组
		if (input1 != null) {
			for (int i = 0; i < input1.length; i++) {
				retValue[index] = input1[i];
				index++;
			}
		}
		// 拼接第二个字符数组
		if (input2 != null) {
			for (int i = 0; i < input2.length; i++) {
				retValue[index] = input2[i];
				index++;
			}
		}
		return retValue;
	}

	/**
	 * 解压缩文件到指定目录下
	 * 
	 * @param byteStream
	 *            压缩文件二进制流
	 * @param dirPath
	 *            目录路径
	 * @return 解压成功返回true，否则返回false
	 */
	private static boolean unzipFile(byte[] byteStream, String dirPath) {
		// 返回值
		boolean retValue = false;

		File fileDir = new File(dirPath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		// 解压缩ZIP文件
		ByteArrayInputStream bais = new ByteArrayInputStream(byteStream);
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry entry;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[BUFFERSIZE];
				// 将文件写入磁盘
				String filePath = dirPath + File.separator + entry.getName();
				File file = new File(filePath);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos,
						BUFFERSIZE);
				while ((count = zis.read(data, 0, BUFFERSIZE)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				fos.flush();
				fos.close();
			}
			zis.close();
			retValue = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		return retValue;
	}

	/**
	 * 检查文件夹是否存在
	 * 
	 * @param dir
	 * @param ftpClient
	 * @return
	 */
	public boolean isDirExist(String dir) {
		try {
			ftpClient.cd(dir);
		} catch (Exception e) {

			return false;
		}
		return true;
	}
	
	/*
	 * Method:解析生成的数据中的xml
	 * author:Li Si
	 * arg1:新数据存放的路径
	 * */
	private ArrayList getXmlData(String newPath)throws Exception{
		ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		File file = new File(newPath);
		String[] filePath = file.list();
		for (int i = 0; i < filePath.length; i++) {
			File dirFile = new File(newPath + file.separator + filePath[i]);
			if(dirFile.isDirectory()){
				File file2 = new File(dirFile.getAbsolutePath());
				String[] filePath2 = file2.list();
				//HashMap<String,Object> map = new HashMap<String,Object>();
				for (int j = 0; j < filePath2.length; j++) {
					File xfile = new File(dirFile + file2.separator + filePath2[j]);
					if(xfile.isFile()){
						String filename = xfile.getName();
						String kzm = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
						if("xml".equals(kzm)){
							HashMap<String,Object> map = new HashMap<String,Object>();
							SAXBuilder builder = new SAXBuilder(false);
							Document document = builder.build(xfile);
							Element root = document.getRootElement();
							List nodes = root.getChildren();
							for (int k = 0; k < nodes.size(); k++) {
								Element element = (Element) nodes.get(k);
								if("ATTACHMENTS".equals(element.getName())){
									continue;
								}else{
									List nodes_s = element.getChildren();
									for (int l = 0; l < nodes_s.size(); l++) {
										Element end = (Element) nodes_s.get(l);
										if("INFO_CONT".equals(end.getName())){
											/*String content = "";
											Object obj = map.get("content");
											if(obj != null){
												content += obj.toString() + "<br/>" + end.getText();
												map.put("content", content);
											}else{*/
												map.put("content", end.getText());
											//}
										}else{
											map.put(end.getName().toLowerCase(), end.getText());
										}
										
									}
								}
							}
							list.add(map);
						}else{
							/*Object obj = map.get("content");
							String content = "";
							if(obj == null){
								map.put("content", String.valueOf("<a href='http://10.11.100.106"+xfile.getAbsolutePath()+"'>"+xfile.getName()+"</a>"));
							}else{
								content += map.get("content").toString() + "<br/>" + "<a href='http://10.11.100.106"+xfile.getAbsolutePath()+"'>"+xfile.getName()+"</a>";
								map.put("content",content);
							}*/
							continue;
						}
					}else{
						continue;
					}
				}
				
			}
		}
		return list;
	}
	
	private void copy(String oldPath,String newPath)throws Exception{
		File file = new File(oldPath);
		String[] filePath = file.list();
		if(!(new File(newPath)).exists()){
			(new File(newPath)).mkdir();
		}
		for (int i = 0; i < filePath.length; i++) {
			if((new File(oldPath + file.separator + filePath[i])).isDirectory()){
				this.copy(oldPath + file.separator + filePath[i],newPath + file.separator + filePath[i]);
			}
			if((new File(oldPath + file.separator + filePath[i])).isFile()){
				this.copyFile(oldPath + file.separator + filePath[i],newPath + file.separator + filePath[i]);
			}
		}
	}
	
	private void copyFile(String oldPath,String newPath)throws Exception{
		File oldFile = new File(oldPath);
		File file = new File(newPath);
		FileInputStream in = new FileInputStream(oldFile);
		FileOutputStream out = new FileOutputStream(file);
		try {
			byte[] bytes = new byte[1024];
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(in != null){
				in.close();
			}
			if(out != null){
				out.close();
			}
		}
	}
	
	private void pushData(ArrayList list)throws Exception{
		/***非必填项**/
		if(list != null && list.size() != 0){
			for (int i = 0; i < list.size(); i++) {
				HashMap map = (HashMap) list.get(i);
				Map<String, String> params = new HashMap<String, String>();
				//目标栏目ID
				String catalog_id = map.get("catalog_id").toString();
				//数据来源类型
				String info_type_id = map.get("info_type_id").toString();
				System.out.println("开始向市信息公开平台推送数据...................................."+map);				 
				/*if("1".equals(info_type_id)){
					this.addManuscript_ywdt(map,catalog_id);
					continue;
				}else if("2".equals(info_type_id)){
					this.addManuscript_fgwj(map,catalog_id);
					continue;
				}else if("3".equals(info_type_id)){
					this.addManuscript_ghjh(map,catalog_id);
					continue;
				}else if("4".equals(info_type_id)){
					this.addManuscript_xzzz(map,catalog_id);
					continue;
				}else if("5".equals(info_type_id)){
					this.addManuscript_ywdt(map,catalog_id);
					continue;
				}*/
			}
		}
	}
	
	/*
	 * 1.机构职责
	 * */
	public void addManuscript_jgzz(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
		
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String editmt_jgzz="<p>机构职责</p>";
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")){
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")){
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")){
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")){
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")){
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")){
					params.put("meta_ztlx",meta_ztlx);
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")){
					params.put("meta_gkxs",meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")){
					params.put("meta_gklb",meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")){
					params.put("meta_ggzrbm",meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")){
					 params.put("meta_gkrq",meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")){
					params.put("meta_beizhu",meta_beizhu);
				}
				if(editmt_jgzz!=null && !editmt_jgzz.equals("")){
					params.put("editmt_jgzz",editmt_jgzz);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")){
					params.put("meta_ztfll",meta_ztfll);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 2.机构信息
	 * */
	public void addManuscript_jgxx(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
		
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String meta_wangzhi  = map.get("meta_wangzhi") == null?"":map.get("meta_wangzhi").toString();
				String meta_yzbm     =map.get("meta_yzbm") == null?"":map.get("meta_yzbm").toString();
				String meta_bgdh     =map.get("meta_bgdh") == null?"":map.get("meta_bgdh").toString();
				String meta_chuanzhen=map.get("meta_chuanzhen") == null?"":map.get("meta_chuanzhen").toString();
				String meta_dzyx     =map.get("meta_dzyx") == null?"":map.get("meta_dzyx").toString();
				String meta_bgdz     =map.get("meta_bgdz") == null?"":map.get("meta_bgdz").toString();
				String editmt_zxfw   =map.get("editmt_zxfw") == null?"":map.get("editmt_zxfw").toString();
				String editmt_jdts   =map.get("editmt_jdts") == null?"":map.get("editmt_jdts").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_wangzhi!=null && !meta_wangzhi.equals("")) {
					params.put("meta_wangzhi", meta_wangzhi);
				}
				if(meta_yzbm!=null && !meta_yzbm.equals("")) {
					params.put("meta_yzbm", meta_yzbm);
				}
				if(meta_bgdh!=null && !meta_bgdh.equals("")) {
					params.put("meta_bgdh", meta_bgdh);
				}
				if(meta_chuanzhen!=null && !meta_chuanzhen.equals("")) {
					params.put("meta_chuanzhen", meta_chuanzhen);
				}
				if(meta_dzyx!=null && !meta_dzyx.equals("")) {
					params.put("meta_dzyx", meta_dzyx);
				}
				if(meta_bgdz!=null && !meta_bgdz.equals("")) {
					params.put("meta_bgdz", meta_bgdz);
				}
				if(editmt_zxfw!=null && !editmt_zxfw.equals("")) {
					params.put("editmt_zxfw", editmt_zxfw);
				}
				if(editmt_jdts!=null && !editmt_jdts.equals("")) {
					params.put("editmt_jdts", editmt_jdts);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 3.领导介绍
	 * */
	public void addManuscript_ldjs(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
		
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				String meta_gzll=map.get("meta_gzll") == null?"":map.get("meta_gzll").toString();
				String meta_grjbxx=map.get("meta_grjbxx") == null?"":map.get("meta_grjbxx").toString();
				String meta_gzfg=map.get("meta_gzfg") == null?"":map.get("meta_gzfg").toString();
				String meta_zhiwu=map.get("meta_zhiwu") == null?"":map.get("meta_zhiwu").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_zhiwu!=null && !meta_zhiwu.equals("")) {
					params.put("meta_zhiwu",meta_zhiwu);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);
				}
				if(meta_grjbxx!=null && !meta_grjbxx.equals("")) {
					params.put("meta_grjbxx", meta_grjbxx);
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gzfg!=null && !meta_gzfg.equals("")) {
					params.put("meta_gzfg", meta_gzfg);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gzll!=null && !meta_gzll.equals("")) {
					params.put("meta_gzll", meta_gzll);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
		
	}
	
	/*
	 * 4.内设机构
	 * */
	public void addManuscript_nsjg(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
		
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String meta_zyfzr=map.get("meta_zyfzr") == null?"":map.get("meta_zyfzr").toString();
				String meta_zyzz=map.get("meta_zyzz") == null?"":map.get("meta_zyzz").toString();
				String meta_bgdh     =map.get("meta_bgdh") == null?"":map.get("meta_bgdh").toString();
				String meta_chuanzhen=map.get("meta_chuanzhen") == null?"":map.get("meta_chuanzhen").toString();
				String meta_dzyx     =map.get("meta_dzyx") == null?"":map.get("meta_dzyx").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_zyzz!=null && !meta_zyzz.equals("")) {
					params.put("meta_zyzz", meta_zyzz);
				}
				if(meta_bgdh!=null && !meta_bgdh.equals("")) {
					params.put("meta_bgdh", meta_bgdh);
				}
				if(meta_zyfzr!=null && !meta_zyfzr.equals("")) {
					params.put("meta_zyfzr", meta_zyfzr);
				}
				if(meta_chuanzhen!=null && !meta_chuanzhen.equals("")) {
					params.put("meta_chuanzhen", meta_chuanzhen);
				}
				if(meta_dzyx!=null && !meta_dzyx.equals("")) {
					params.put("meta_dzyx", meta_dzyx);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 5.直属单位
	 * */
	public void addManuscript_zsdw(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String meta_zyfzr=map.get("meta_zyfzr") == null?"":map.get("meta_zyfzr").toString();
				String meta_zyzz=map.get("meta_zyzz") == null?"":map.get("meta_zyzz").toString();
				String meta_bgdh     =map.get("meta_bgdh") == null?"":map.get("meta_bgdh").toString();
				String meta_chuanzhen=map.get("meta_chuanzhen") == null?"":map.get("meta_chuanzhen").toString();
				String meta_dzyx     =map.get("meta_dzyx") == null?"":map.get("meta_dzyx").toString();
				String meta_wangzhi  =map.get("meta_wangzhi") == null?"":map.get("meta_wangzhi").toString();
				String meta_yzbm     =map.get("meta_yzbm") == null?"":map.get("meta_yzbm").toString();
				String meta_bgdz     =map.get("meta_bgdz") == null?"":map.get("meta_bgdz").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_zyzz!=null && !meta_zyzz.equals("")) {
					params.put("meta_zyzz", meta_zyzz);
				}
				if(meta_bgdh!=null && !meta_bgdh.equals("")) {
					params.put("meta_bgdh", meta_bgdh);
				}
				if(meta_zyfzr!=null && !meta_zyfzr.equals("")) {
					params.put("meta_zyfzr", meta_zyfzr);
				}
				if(meta_chuanzhen!=null && !meta_chuanzhen.equals("")) {
					params.put("meta_chuanzhen", meta_chuanzhen);
				}
				
				if(meta_wangzhi!=null && !meta_wangzhi.equals("")) {
					params.put("meta_wangzhi", meta_wangzhi);
				}
				
				if(meta_bgdz!=null && !meta_bgdz.equals("")) {
					params.put("meta_bgdz", meta_bgdz);
				}
				if(meta_yzbm!=null && !meta_yzbm.equals("")) {
					params.put("meta_yzbm", meta_yzbm);
				}
				if(meta_dzyx!=null && !meta_dzyx.equals("")) {
					params.put("meta_dzyx", meta_dzyx);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 6.区县机构
	 * */
	public void addManuscript_qxjg(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String meta_zyfzr=map.get("meta_zyfzr") == null?"":map.get("meta_zyfzr").toString();
				String meta_bgdh     =map.get("meta_bgdh") == null?"":map.get("meta_bgdh").toString();
				String meta_chuanzhen=map.get("meta_chuanzhen") == null?"":map.get("meta_chuanzhen").toString();
				String meta_dzyx     =map.get("meta_dzyx") == null?"":map.get("meta_dzyx").toString();
				String meta_wangzhi  =map.get("meta_wangzhi") == null?"":map.get("meta_wangzhi").toString();
				String meta_yzbm     =map.get("meta_yzbm") == null?"":map.get("meta_yzbm").toString();
				String meta_bgdz     =map.get("meta_bgdz") == null?"":map.get("meta_bgdz").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_bgdh!=null && !meta_bgdh.equals("")) {
					params.put("meta_bgdh", meta_bgdh);
				}
				if(meta_zyfzr!=null && !meta_zyfzr.equals("")) {
					params.put("meta_zyfzr", meta_zyfzr);
				}
				if(meta_chuanzhen!=null && !meta_chuanzhen.equals("")) {
					params.put("meta_chuanzhen", meta_chuanzhen);
				}
				
				if(meta_wangzhi!=null && !meta_wangzhi.equals("")) {
					params.put("meta_wangzhi", meta_wangzhi);
				}
				
				if(meta_bgdz!=null && !meta_bgdz.equals("")) {
					params.put("meta_bgdz", meta_bgdz);
				}
				if(meta_yzbm!=null && !meta_yzbm.equals("")) {
					params.put("meta_yzbm", meta_yzbm);
				}
				if(meta_dzyx!=null && !meta_dzyx.equals("")) {
					params.put("meta_dzyx", meta_dzyx);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 7.内设机构其他栏目
	 * */
	public void addManuscript_nsjgqtlm(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				//String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String meta_zyfzr=map.get("meta_zyfzr") == null?"":map.get("meta_zyfzr").toString();
				String meta_bgdh     =map.get("meta_bgdh") == null?"":map.get("meta_bgdh").toString();
				String meta_chuanzhen=map.get("meta_chuanzhen") == null?"":map.get("meta_chuanzhen").toString();
				String meta_dzyx     =map.get("meta_dzyx") == null?"":map.get("meta_dzyx").toString();
				String meta_wangzhi  =map.get("meta_wangzhi") == null?"":map.get("meta_wangzhi").toString();
				String meta_yzbm     =map.get("meta_yzbm") == null?"":map.get("meta_yzbm").toString();
				String meta_bgdz     =map.get("meta_bgdz") == null?"":map.get("meta_bgdz").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_bgdh!=null && !meta_bgdh.equals("")) {
					params.put("meta_bgdh", meta_bgdh);
				}
				if(meta_zyfzr!=null && !meta_zyfzr.equals("")) {
					params.put("meta_zyfzr", meta_zyfzr);
				}
				if(meta_chuanzhen!=null && !meta_chuanzhen.equals("")) {
					params.put("meta_chuanzhen", meta_chuanzhen);
				}
				
				if(meta_wangzhi!=null && !meta_wangzhi.equals("")) {
					params.put("meta_wangzhi", meta_wangzhi);
				}
				
				if(meta_bgdz!=null && !meta_bgdz.equals("")) {
					params.put("meta_bgdz", meta_bgdz);
				}
				if(meta_yzbm!=null && !meta_yzbm.equals("")) {
					params.put("meta_yzbm", meta_yzbm);
				}
				if(meta_dzyx!=null && !meta_dzyx.equals("")) {
					params.put("meta_dzyx", meta_dzyx);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 8.其他栏目
	 * */
	public void addManuscript_qtlm(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(content!=null && !content.equals("")) {
					params.put("content", content);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 9.法规文件
	 * */
	public void addManuscript_fgwj(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				String meta_wtfl  ="95a3d907b7124434856765353ce83100;";
				String meta_xlfl  ="1ec9fb98b8eb4c3bb64553bee6befb67;";
				String meta_lhfwdw=map.get("meta_lhfwdw") == null?"":map.get("meta_lhfwdw").toString();
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(content!=null && !content.equals("")) {
					params.put("content", content);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_wtfl!=null && !meta_wtfl.equals("")) {
					params.put("meta_wtfl", meta_wtfl);
				}
				if(meta_xlfl!=null && !meta_xlfl.equals("")) {
					params.put("meta_xlfl", meta_xlfl);
				}
				if(meta_lhfwdw!=null && !meta_lhfwdw.equals("")) {
					params.put("meta_lhfwdw", meta_lhfwdw);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 10.规划计划
	 * */
	public void addManuscript_ghjh(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(content!=null && !content.equals("")) {
					params.put("content", content);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 11.行政职责
	 * */
	public void addManuscript_xzzz(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ss
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				String meta_xxlb="f3af1da1cd654e0e9978eac815d44916;";
				
				String catalog_name = map.get("catalog_name").toString();
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(content!=null && !content.equals("")) {
					params.put("content", content);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_xxlb!=null && !meta_xxlb.equals("")) {
					params.put("meta_xxlb", meta_xxlb);
				}
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
	}
	
	/*
	 * 12.业务动态
	 * */
	public void addManuscript_ywdt(HashMap map,String channelId)throws Exception{
		String clbm = ConfigServer.getServer().getSysConfigValue("CLBM", "0");
		String my = ConfigServer.getServer().getSysConfigValue("MY", "0");
		String tjgj = ConfigServer.getServer().getSysConfigValue("TJGJ", "0");
		/***必填项**/
		String websiteId = ConfigServer.getServer().getSysConfigValue("WEBSITEID", "0");
		String siteCode = ConfigServer.getServer().getSysConfigValue("SITECODE", "0");
		String userId=ConfigServer.getServer().getSysConfigValue("USERID", "0");
		String title="测试接口稿件";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		// TimeUtil.dateStrToGMTLong() 此方法为计算GMT+8:00时区并返回计算后的毫秒数
		//只需要把日期参数改为需要写入的日期，其他两个参数不需要修改
		//格式为:yyyy-MM-dd HH:mm:ssst
		String meta_scrq  = TimeUtil.dateStrToGMTLong(format.format(new Date()),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
		String meta_xxyxx ="8866f682d54841f78d13c602153f5fa2;";
				Map<String, String> params = new HashMap<String, String>();
				//String subTitle = map.get("info_name") == null?"":map.get("info_name").toString();
				title = map.get("info_name") == null?"":map.get("info_name").toString();
				String keyWord = map.get("keyword") == null?"":map.get("keyword").toString();
				String memo = map.get("memo") == null?"":map.get("memo").toString();
				String publishTime = System.currentTimeMillis()+"";
				String content = map.get("content") == null?"":map.get("content").toString();
				String meta_wenhao = map.get("doc_number") == null?"":map.get("doc_number").toString();
				String meta_jlxs = map.get("record_from") == null?"":map.get("record_from").toString();
				String meta_ztlx = map.get("carrier_type") == null?"":map.get("carrier_type").toString();
				String meta_gkxs = map.get("pub_method") == null?"":map.get("pub_method").toString();
				String meta_gklb = map.get("pub_type") == null?"":map.get("pub_type").toString();
				String meta_ggzrbm = map.get("pub_duty_dept") == null?"":map.get("pub_duty_dept").toString();
				String meta_gkrq = TimeUtil.dateStrToGMTLong(map.get("pub_date").toString(),"GMT+8:00","yyyy-MM-dd HH:mm:ss")+"";
				String meta_beizhu = map.get("rmrk") == null?"":map.get("rmrk").toString();
				String meta_ztfll = "b3d78cea61934e888d5b98af324e5f32;";
				String meta_bssx="87a9ef9dc42c43c3948f812ab0942ecf;";
				
				params.put("channelId", channelId);
				params.put("websiteId", websiteId);
				params.put("userId", userId);
				params.put("title", title);
				params.put("meta_scrq", meta_scrq);
				params.put("meta_xxyxx", meta_xxyxx);
				//params.put("syh_syh","");//索引号为后台自动生成，此字段无需改动
				
				if(title!=null && !title.equals("")) {
					params.put("subTitle", title);
				}
				if(keyWord!=null && !keyWord.equals("")) {
					params.put("keyword", keyWord);
				}
				if(memo!=null && !memo.equals("")) {
					params.put("memo", memo);
				}
				if(publishTime!=null && !publishTime.equals("")) {
					params.put("publishTime",publishTime);
				}
				if(content!=null && !content.equals("")) {
					params.put("content", content);
				}
				if(meta_wenhao!=null && !meta_wenhao.equals("")) {
					params.put("meta_wenhao",meta_wenhao);
				}
				if(meta_jlxs!=null && !meta_jlxs.equals("")) {
					params.put("meta_jlxs", meta_jlxs);
				}
				if(meta_ztlx!=null && !meta_ztlx.equals("")) {
					params.put("meta_ztlx", meta_ztlx);	
				}
				if(meta_gkxs!=null && !meta_gkxs.equals("")) {
					params.put("meta_gkxs", meta_gkxs);
				}
				if(meta_gklb!=null && !meta_gklb.equals("")) {
					params.put("meta_gklb", meta_gklb);
				}
				if(meta_ggzrbm!=null && !meta_ggzrbm.equals("")) {
					params.put("meta_ggzrbm", meta_ggzrbm);
				}
				if(meta_gkrq!=null && !meta_gkrq.equals("")) {
					params.put("meta_gkrq", meta_gkrq);
				}
				if(meta_beizhu!=null && !meta_beizhu.equals("")) {
					params.put("meta_beizhu", meta_beizhu);
				}
				if(meta_ztfll!=null && !meta_ztfll.equals("")) {
					params.put("meta_ztfll", meta_ztfll);
				}
				if(meta_bssx!=null && !meta_bssx.equals("")) {
					params.put("meta_bssx", meta_bssx);
				}
				System.out.println("...............................................");
				System.out.println(params);
				System.out.println("--------------------------------------------------");
				String msg = ResClientUtils.sendPostManuscriptUrl(tjgj , clbm, my, params);
				System.out.println(msg);
				System.out.println("===================================================");
	}
	
	
	
	private static HashMap<String,String> mapingMap = new HashMap<String,String>();
	static{
		mapingMap.put("文本", "36decd92e590447f84a5b3d7eced1294;");
		mapingMap.put("图表", "376cc424aab645398c0ab980f6f435f8;");
		mapingMap.put("照片", "c7ee38ff48d640deb840d12714e4e37b;");
		mapingMap.put("影音", "fd29d66b5f764b9989180844a5125bd7;");
		mapingMap.put("其他记录形式", "31603486d5d8429f9a036456194f5484;");
		
		mapingMap.put("纸质", "2057afe3e9e249369d8a12d9ab8c72eb;");
		mapingMap.put("胶卷", "5f77a15438ca4f51acd99758c074a2f5;");
		mapingMap.put("磁带", "9afcd2d2d30a4f47828facc3aec5880b;");
		mapingMap.put("磁盘", "499c142670524b7e9e72203c0173ef06;");
		mapingMap.put("光盘", "6f12d9462ee44e9b8619fe01da0971ab;");
		mapingMap.put("其他载体类型", "9758b60bb1164696b5ada900487bf3f7;");
		
		mapingMap.put("网站公开", "9f6449199ef94ed19b489af08a19b622;");
		mapingMap.put("政府信息公开大厅", "ed8d85e5eb974fb7a5e09febf7da790f;");
		mapingMap.put("政府公报", "f02eba8726fa4011854eaa2afd630a16;");
		mapingMap.put("政府信息公开栏", "854c413000e94601b0ec9bbc60f00544;");
		mapingMap.put("电子触摸屏", "1c0d41634d89411983780aa370801f43;");
		mapingMap.put("便民手册", "d496a99960df45b9b68a5195c0a03b2e;");
		mapingMap.put("新闻发布会", "aae404937e99416888aaf4d2a4adeae5;");
		mapingMap.put("档案馆文件查阅中心", "be02df89927b4d4ab31e75e0abfd3e7f;");
		mapingMap.put("报纸", "60b25941eb004877b2e54e477d035d54;");
		mapingMap.put("广播", "4478f8d6282640f6a626fe75ff7af4d2;");
		mapingMap.put("电视", "b65a81139d374ccfa98b065142760786;");
		mapingMap.put("其他公开形式", "722650f1ad874dfdb49b2aedab036070;");
		mapingMap.put("文件查阅中心", "b2b748a445b8409b828cd153d7ba6fb7;");
		
		mapingMap.put("依申请公开", "358f53c8db674cf792d32d70410702f7;");
		mapingMap.put("主动公开", "cc0a2c7e0e0441248c731dd8588bf5da;");
		
		mapingMap.put("人口社会", "042e316eccb7481ca98e2dfa50c53706;");
		mapingMap.put("经济投资", "b7250c0a007d41dd87a72808afd4dc4d;");
		mapingMap.put("农村农民", "dce898c3272d4d5f9e4fbd4e50da7314;");
		
		mapingMap.put("目录", "a6d319000cbc4e749f0e1bff3737c6f6;");
		mapingMap.put("目录与链接", "f3af1da1cd654e0e9978eac815d44916;");
		mapingMap.put("目录与信息", "8cc7e52c09bf417cb38bc6a0653f5736;");
		
		mapingMap.put("非办理类", "87a9ef9dc42c43c3948f812ab0942ecf;");
		mapingMap.put("办理类", "6d5342d516a7418782505f25c4f8efac;");
	}
	
	public void xgyxx(String id,Map map)throws Exception{
		String strategyCode = "sfgwCode";
		String key = "Sfgw1109";	
		String updateManuscriptUrl="http://172.26.66.67:8080/website-webapp/rest/manuscript/updateManuscript";
		String userId="2338";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("manuscriptId", id);
		params.put("userId", userId);
		
		Set keys = map.keySet();
    	Iterator it = keys.iterator();
    	while(it.hasNext()){
    		String str = (String) it.next();
    		Object obj = map.get(str);
    		if(obj != null){
    			params.put(str, obj.toString());
    		}
    	}
    	params.put("meta_xxyxx", "8866f682d54841f78d13c602153f5fa2;");
		System.out.println("params----->"+params);	
		String msg = ResClientUtils.sendPostManuscriptUrl(updateManuscriptUrl , strategyCode, key, params);
		System.out.println("update===="+msg);
	}
	
	public Map getDataById(String manuscriptId)throws Exception{
		String strategyCode = "sfgwCode";
		String key = "Sfgw1109";	
		String getManuscriptByIdUrl="http://172.26.66.67:8080/website-webapp/rest/manuscript/getManuscriptById";
		
		String url=getManuscriptByIdUrl+"?manuscriptId="+manuscriptId;
		String msg = ResClientUtils.sendGetUrl(url, strategyCode, key);
		
		JSONObject obj = JSONObject.fromObject(msg); 
		Map map = this.jsonToMap(obj);  
		
		return map;
	}
	
	public Map filteMap(Map map)throws Exception{
		Map param = new HashMap();
		Map dataMap = (Map)map.get("data");
		param.put("memo", dataMap.get("memo") == null?"":dataMap.get("memo").toString());
		param.put("keyword", dataMap.get("keyword") == null?"":dataMap.get("keyword").toString());
		param.put("content", dataMap.get("content") == null?"":dataMap.get("content").toString());
		param.put("subTitle", dataMap.get("subTitle") == null?"":dataMap.get("subTitle").toString());
		param.put("title", dataMap.get("title") == null?"":dataMap.get("title").toString());
		param.put("publishTime", dataMap.get("publishedTime") == null?"":dataMap.get("publishedTime").toString());
		
		List metadataList1 = (List) dataMap.get("metadata");
		Map metadataMap = (Map) metadataList1.get(0);
		List metadataList2 = (List) metadataMap.get("metadata");
		if(metadataList2 != null && metadataList2.size() != 0){
			for (int i = 0; i < metadataList2.size(); i++) {
				Map lastMap = (Map) metadataList2.get(i);
				String name = lastMap.get("metadataName").toString();
				String value = lastMap.get("metadataValue") == null?"":lastMap.get("metadataValue").toString();
				String key = lastMap.get("metadataShortName") == null?"":lastMap.get("metadataShortName").toString();
				String type = lastMap.get("metadataType").toString();
				
				if("其他".equals(value)){
					param.put("meta_" + key, mapingMap.get(value + name));
				}else if("wenhao".equals(key) || "ggzrbm".equals(key) || "gkrq".equals(key) || "scrq".equals(key) || "beizhu".equals(key)){
					param.put("meta_" + key, value);
				}else if("xxyxx".equals(key)){
					param.put("meta_" + key, "8866f682d54841f78d13c602153f5fa2;");
				}else if("syh".equals(key)){
					param.put("syh_syh", "");
				}else{
					param.put("meta_" + key, mapingMap.get(value));
				}
			}
		}
		
		return param;
	}
	
	public Map<String, Object> jsonToMap(JSONObject obj) {  
        Set<?> set = obj.keySet();  
        Map<String, Object> map = new HashMap<String, Object>(set.size());  
        for (Object key : obj.keySet()) {  
            Object value = obj.get(key);  
            if (value instanceof JSONArray) {  
                map.put(key.toString(), jsonToList((JSONArray) value));  
            } else if (value instanceof JSONObject) {  
                map.put(key.toString(), jsonToMap((JSONObject) value));  
            } else {  
                map.put(key.toString(), obj.get(key));  
            }  
  
        }  
        return map;  
    } 
	public List<Object> jsonToList(JSONArray jsonArr) {  
        List<Object> list = new ArrayList<Object>();  
        for (Object obj : jsonArr) {  
            if (obj instanceof JSONArray) {  
                list.add(jsonToList((JSONArray) obj));  
            } else if (obj instanceof JSONObject) {  
                list.add(jsonToMap((JSONObject) obj));  
            } else {  
                list.add(obj);  
            }  
        }  
        return list;  
    }  
	
	public void publishData(String manuscriptIds)throws Exception{
		String strategyCode = "sfgwCode";
		String key = "Sfgw1109";	
		String publishManuscriptByIdUrl="http://172.26.66.67:8080/website-webapp/rest/manuscript/publishManuscriptById";
		String userId="2338";
		String url=publishManuscriptByIdUrl+"?manuscriptIds="+manuscriptIds+"&userId="+userId;
		String msg = ResClientUtils.sendGetUrl(url, strategyCode, key);
		System.out.println(msg);
	}
}
