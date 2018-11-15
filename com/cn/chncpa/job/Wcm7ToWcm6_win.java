package com.cn.chncpa.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.wcm.content.domain.DocumentMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.ChnlDoc;
import com.trs.components.wcm.resource.Status;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;

public class Wcm7ToWcm6_win extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.cn.chncpa.job.Wcm7ToWcm6_win.class);
	@Override
	protected void execute() throws WCMException {
		String sSfqy = CMyString.showNull(getArgAsString("sfqy"), "0");
		String RWMS = CMyString.showNull(getArgAsString("RWMS"), "定时任务");
		if (!("1".equals(sSfqy))) {
			s_logger.info(RWMS + " 未启用该策略！");
			return;
		}
		s_logger.info(RWMS + " 执行开始" + new CMyDateTime().now().toString());
		
		String howtime = CMyString.showNull(getArgAsString("howtime"), "0");
		s_logger.info("得到参数：要获取"+howtime+"分钟之前数据");
		
		String fromChannel = CMyString.showNull(getArgAsString("fromChannel"), "0");
		String toChannel = CMyString.showNull(getArgAsString("toChannel"), "0");
		
		s_logger.info("来源栏目ID:" + fromChannel);
		s_logger.info("目标栏目ID:" + toChannel);
		
		if("".equals(fromChannel)){
			return;
		}
		
		if("".equals(toChannel)){
			return;
		}
		
		ArrayList<HashMap<String,String>> result = this.queryWcm7Data(Integer.parseInt(howtime),fromChannel);
		s_logger.info("获取到"+result.size()+"条符合条件的数据");
		
		HashMap<String,String> toChannelMap = null;
		if(result.size() != 0){
			try {
				toChannelMap = this.stringToMap(fromChannel,toChannel);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String filePath = this.createXml(result,toChannelMap);
			String sourcePath = filePath.substring(0, filePath.lastIndexOf("\\"));
			//String sourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
			String zipPath = this.toZip(sourcePath);
			if(!"".equals(zipPath)){
				s_logger.info("待推送数据压缩包路径：" + zipPath);
				String sResult = this.pushZipData(zipPath);
				String isSuccess = this.getPushResult(sResult, "SUCCESS");
				if("true".equals(isSuccess)){
					String docid = this.getPushResult(sResult, "IDS");
					s_logger.info("得到WCM6数据ID：" + docid);
					this.updateWcm7Data(result,docid);
				}
				try{
					s_logger.info("删除功能未开启，定时任务结束！！！");
					//this.delZipFile("/trs/TRS/TRSWCMV7/WCMData/xmls_temp");
					//this.delZipFile("D:\\TRS\\TRSWCMV7\\WCMData\\xmls_temp");
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				s_logger.info("数据压缩失败，未找到zip类型数据文件");
			}
		}
	}
	
	private void updateWcm7Data(ArrayList<HashMap<String,String>> dataList,String docid)throws WCMException{
		String[] ids = docid.split(",");
		if(dataList.size() != ids.length){
			s_logger.info("更新数据的两个集合数量不一致");
		}else{
			s_logger.info("开始更新。。。。");
		}
	}
	
	private String pushZipData(String zipPath)throws WCMException{
		String sResult = "";
		String sTargetEndpointAddress = "http://10.2.5.41/wcm/services/trswcm:ImportService";
		Service service = new Service();
		try {
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(sTargetEndpointAddress));
			call.setOperationName(new QName("http://service.wcm.trs.com","importDocuments"));
			s_logger.info("开始推送数据");
			sResult = (String) call.invoke(new Object[] {CMyFile.readBytesFromFile(zipPath), "zip" });
			s_logger.info("推送数据返回值：" + sResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sResult;
	}
	
	private String toZip(String srcPath)throws WCMException{
		String zip_Path = "";
		File sourceFile = new File(srcPath);
		if(sourceFile.exists() == false){  
			s_logger.info("待压缩的文件目录："+sourceFile+"不存在.");  
		}else{
			FileInputStream fis = null;  
	        BufferedInputStream bis = null;  
	        FileOutputStream fos = null;  
	        ZipOutputStream zos = null; 
			try {
				String zipName = srcPath.substring(srcPath.lastIndexOf("\\"), srcPath.length());
				String zipPath = srcPath.substring(0,srcPath.lastIndexOf("\\"));
				//String zipName = srcPath.substring(srcPath.lastIndexOf("/"), srcPath.length());
				//String zipPath = srcPath.substring(0,srcPath.lastIndexOf("/"));
				File zipFile = new File(zipPath + zipName + ".zip"); 
				if(zipFile.exists()){
					s_logger.info("目录下存在同名文件"); 
					zip_Path = zipFile.getPath();
				}else{
					File[] sourceFiles = sourceFile.listFiles(); 
					if(null == sourceFiles || sourceFiles.length < 1){ 
						s_logger.info("目录下不存在文件"); 
						zip_Path = "";
					}else{
						fos = new FileOutputStream(zipFile);  
                        zos = new ZipOutputStream(new BufferedOutputStream(fos)); 
                        byte[] bufs = new byte[1024 * 10];
                        for(int i=0;i<sourceFiles.length;i++){
                        	ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                        	zos.putNextEntry(zipEntry);
                        	fis = new FileInputStream(sourceFiles[i]);  
                            bis = new BufferedInputStream(fis, 1024*10); 
                            int read = 0;  
                            while((read=bis.read(bufs, 0, 1024*10)) != -1){  
                                zos.write(bufs,0,read);  
                            }
                        }
                        zip_Path = zipFile.getPath();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				try {
					if(null != bis) bis.close();  
                    if(null != zos) zos.close(); 
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		return zip_Path;
	}
	
	private String createXml(ArrayList<HashMap<String,String>> dataList,HashMap<String,String> toChannelMap)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		//String xmlPath = "/trs/TRS/TRSWCMV7/WCMData/xmls_temp/";
		String xmlPath = "D:\\TRS\\TRSWCMV7\\WCMData\\xmls_temp\\";
		String xmlName = this.getXmlName(xmlPath);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String now = format.format(new Date());
		//String[] toChannels = toChannel.split(",");
		try {
			Element root = new Element("WCMDOCUMENTS");												
			Document xml = new Document(root);
			//for (int a = 0; a < toChannels.length; a++) {
			for (int i = 0; i < dataList.size(); i++) {
				Element wcmdocument = new Element("WCMDOCUMENT").setAttribute("Version", "6.0");	
				Element properties = new Element("PROPERTIES");										
				
				HashMap<String,String> map = dataList.get(i);
				properties.addContent(new Element("DOCID").addContent(CMyString.showEmpty(CMyString.showNull(map.get("DOCID"), ""), "")));
				properties.addContent(new Element("DOCVERSION").addContent("0"));
				properties.addContent(new Element("DOCTYPE").addContent("20"));
				properties.addContent(new Element("DOCPUBTIME"));
				properties.addContent(new Element("CRUSER").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("CRUSER"), ""), ""))));
				properties.addContent(new Element("CRTIME").addContent(now));
				properties.addContent(new Element("DOCFLAG").addContent("0"));
				properties.addContent(new Element("ATTRIBUTE"));
				
				properties.addContent(new Element("ATTACHPIC").addContent(""));
				properties.addContent(new Element("DOCLINK").addContent(new CDATA("")));
				properties.addContent(new Element("DOCFILENAME").addContent(new CDATA("")));
				
				//指定推送栏目
				properties.addContent(new Element("DOCCHANNEL").addContent(toChannelMap.get(map.get("CHNLID"))));
				//properties.addContent(new Element("DOCCHANNEL").addContent(CMyString.showEmpty(CMyString.showNull("7048", ""), "")));
				
				properties.addContent(new Element("DOCTITLE").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCTITLE"), ""), ""))));
				properties.addContent(new Element("DOCPEOPLE").addContent(new CDATA("")));
				properties.addContent(new Element("DOCSOURCENAME").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCSOURCENAME"), ""), ""))));
				properties.addContent(new Element("DOCSTATUS").addContent("10"));
				properties.addContent(new Element("DOCCONTENT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCCONTENT"), ""), ""))));
				
				String content = CMyString.showEmpty(CMyString.showNull(map.get("DOCHTMLCON"), ""), "");
				if(!"".equals(content)){
					if(content.indexOf("src=\"") != -1 || content.indexOf("SRC=\"") != -1){
						this.copyPicToXmlFolder(content,xmlName);
					}
				}
				properties.addContent(new Element("DOCHTMLCON").addContent(new CDATA(content)));
				properties.addContent(new Element("DOCWORDSCOUNT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCWORDSCOUNT"), ""), ""))));
				properties.addContent(new Element("DOCABSTRACT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCABSTRACT"), ""), ""))));
				properties.addContent(new Element("DOCKEYWORDS").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCKEYWORDS"), ""), ""))));
				properties.addContent(new Element("DOCAUTHOR").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCAUTHOR"), ""), ""))));
				properties.addContent(new Element("DOCRELTIME").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCRELTIME"), ""), ""))));
				properties.addContent(new Element("TITLECOLOR").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("TITLECOLOR"), ""), ""))));
				properties.addContent(new Element("SUBDOCTITLE").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("SUBDOCTITLE"), ""), ""))));
				properties.addContent(new Element("DOCEDITOR").addContent(CMyString.showEmpty(CMyString.showNull(map.get("DOCEDITOR"), ""), "")));
				properties.addContent(new Element("PRONAME").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("PRONAME"), ""), ""))));
				properties.addContent(new Element("WH").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull("1", ""), ""))));
				properties.addContent(new Element("CHNLNAME").addContent(new CDATA("")));

				wcmdocument.addContent(properties);
				Appendixes appendixes = Appendixes.findAppendixesByObj(com.trs.components.wcm.content.persistent.Document.findById(Integer.parseInt(map.get("DOCID"))));
				s_logger.info("获取到"+appendixes.size()+"个附件");
				
				if(appendixes.size() != 0){
					Element wcmappendixs = new Element("WCMAPPENDIXS");	
					for (int j = 0; j < appendixes.size(); j++) {
						Appendix appendix = (Appendix) appendixes.getAt(j);
						if(appendix == null)continue;
						Element wcmappendix = new Element("WCMAPPENDIX").setAttribute("Version", "6.0");
						Element properties_a = new Element("PROPERTIES");
						
						s_logger.info("附件ID："+appendix.getId());
						
						properties_a.addContent(new Element("APPSERN").addContent(appendix.getSerialNo() + ""));
						properties_a.addContent(new Element("APPDESC").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(appendix.getDesc(), ""), ""))));
						properties_a.addContent(new Element("APPENDIXID").addContent(appendix.getId() + ""));
						properties_a.addContent(new Element("APPFILETYPE").addContent(CMyString.showEmpty(CMyString.showNull(appendix.getFileTypeId() + "", ""), "")));
						properties_a.addContent(new Element("APPFLAG").addContent(CMyString.showEmpty(CMyString.showNull(appendix.getFlag() + "", ""), "")));
						properties_a.addContent(new Element("FILEEXT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(appendix.getFileExt(), ""), ""))));
						properties_a.addContent(new Element("APPLINKALT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(appendix.getAlt(), ""), ""))));
						properties_a.addContent(new Element("CRTIME").addContent(CMyString.showEmpty(CMyString.showNull(appendix.getCrTime().getDateTimeAsString("yyyy-MM-dd HH:mm:ss"), ""), "")));
						properties_a.addContent(new Element("ATTRIBUTE").addContent(new CDATA("")));
						properties_a.addContent(new Element("SRCFILE").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(appendix.getSrcFile(), ""), ""))));
						properties_a.addContent(new Element("CRUSER").addContent(CMyString.showEmpty(CMyString.showNull(appendix.getCrUserName(), ""), "")));
						properties_a.addContent(new Element("USEDVERSIONS").addContent(CMyString.showEmpty(CMyString.showNull(appendix.getUsedVersions().getValue() + "", ""), "")));
						properties_a.addContent(new Element("ISIMPORTTOIMAGELIB").addContent("0"));
						properties_a.addContent(new Element("APPDOCID").addContent(appendix.getDocId() + ""));
						properties_a.addContent(new Element("APPTIME"));
						properties_a.addContent(new Element("APPFILE").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(appendix.getFile(), ""), ""))));
						
						s_logger.info("开始复制附件");
						this.copyFileToTemp(appendix.getFile(),xmlName);
						
						wcmappendix.addContent(properties_a);
						wcmappendixs.addContent(wcmappendix);
					}
					wcmdocument.addContent(wcmappendixs);
				}else{
					s_logger.info("该数据没有附件");
				}
				root.addContent(wcmdocument);
				
				//修改推送标识
				this.updateAndPublish(map.get("DOCID"),map.get("CHNLID"));
			}
			//}
			XMLOutputter XMLOut = new XMLOutputter("	",true,"UTF-8");  
			FileOutputStream fileOutputStream = new FileOutputStream(xmlName);
			XMLOut.output(xml, fileOutputStream);
			fileOutputStream.close();
			XMLOut = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlName;
	}
	
	private void copyFileToTemp(String srcFile,String filePath)throws WCMException{
		FileInputStream ins = null;
        FileOutputStream out = null;
		try {
			if(!"".equals(srcFile)){
				String srcFilePath = this.getSrcFilePath(srcFile);
				//String newFileName = filePath.substring(0, filePath.lastIndexOf("/") + 1) + srcFile;
				String newFileName = filePath.substring(0, filePath.lastIndexOf("\\") + 1) + srcFile;
				s_logger.info("附件原路径："+srcFilePath);
				s_logger.info("附件目标路径："+newFileName);
				ins = new FileInputStream(srcFilePath);
				out = new FileOutputStream(newFileName);
				byte[] b = new byte[1024];
			    int n = 0;
			    while((n=ins.read(b))!=-1){
		            out.write(b, 0, n);
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				ins.close();
		        out.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private String getSrcFilePath(String srcFile)throws WCMException{
		//String rootPath = "/trs/TRS/TRSWCMV7/WCMData/";
		String rootPath = "D:\\TRS\\TRSWCMV7\\WCMData\\";
		String srcFilePath = "";
		String suffixes = srcFile.substring(srcFile.lastIndexOf(".") + 1,srcFile.length());
		String one = srcFile.substring(0, 8);
		String two = srcFile.substring(0, 10);
		if ("jpeg".equals(suffixes.toLowerCase()) || "jpg".equals(suffixes.toLowerCase()) || "gif".equals(suffixes.toLowerCase()) || "png".equals(suffixes.toLowerCase()) || "bmp".equals(suffixes.toLowerCase())) {
			//srcFilePath = rootPath + "webpic/" + one + "/" + two + "/" + srcFile;
			srcFilePath = rootPath + "webpic\\" + one + "\\" + two + "\\" + srcFile;
		} else {
			//srcFilePath = rootPath + "protect/" + one + "/" + two + "/" + srcFile;
			srcFilePath = rootPath + "protect\\" + one + "\\" + two + "\\" + srcFile;
		}
		return srcFilePath;
	}
	
	private ArrayList<HashMap<String,String>> queryWcm7Data(int howtime,String fromChannel)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		String startTime = this.getTimeByMin(howtime);
		String endTime = this.getNowTime();
		String threeTime = this.getTimeByMin(4320);
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT WC.DOCID,WC.CHNLID,WD.DOCCHANNEL,WD.DOCTITLE,WD.DOCCONTENT,WD.DOCHTMLCON,WD.DOCABSTRACT,WD.DOCKEYWORDS,WD.DOCRELWORDS,WD.DOCAUTHOR,WD.DOCEDITOR,WD.DOCRELTIME,WD.CRUSER,WD.DOCWORDSCOUNT,WD.OPERTIME,WD.OPERUSER,WD.DOCSOURCENAME,WD.PRONAME,WD.BUTTONSTATU,WD.ISPUSH ");
		sql.append("FROM WCMDOCUMENT WD INNER JOIN WCMCHNLDOC WC ON WD.DOCID = WC.DOCID ");
		sql.append("WHERE WD.DOCCHANNEL IN (" + fromChannel + ") AND WC.CHNLID IN ("+fromChannel+")");
		sql.append("AND WC.DOCSTATUS = 10 AND WD.ISPUSH = '' ");
		//sql.append("AND WD.DOCPUBTIME BETWEEN TO_DATE('"+startTime+"','yyyy-mm-dd hh24:mi:ss') AND TO_DATE('"+endTime+"','yyyy-mm-dd hh24:mi:ss') ");
		//sql.append("AND WD.DOCRELTIME BETWEEN TO_DATE('"+threeTime+"','yyyy-mm-dd hh24:mi:ss') AND TO_DATE('"+endTime+"','yyyy-mm-dd hh24:mi:ss') ");
		sql.append("AND WD.DOCPUBTIME BETWEEN '"+startTime+"' AND '"+endTime+"' ");
		sql.append("AND WD.DOCRELTIME BETWEEN '"+threeTime+"' AND '"+endTime+"' ");
		sql.append("ORDER BY WC.DOCID ");
		try{
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			s_logger.info("执行sql:"+sql.toString());
			resultSet = stmt.executeQuery(sql.toString());
			if (resultSet != null) {
				while(resultSet.next()){
					HashMap<String,String> map = new HashMap<String,String>();
					map.put("DOCID", resultSet.getString("DOCID") == null?"":resultSet.getString("DOCID"));
					map.put("CHNLID", resultSet.getString("CHNLID") == null?"":resultSet.getString("CHNLID"));
					map.put("DOCTITLE", resultSet.getString("DOCTITLE") == null?"":resultSet.getString("DOCTITLE"));
					map.put("DOCCONTENT", resultSet.getString("DOCCONTENT") == null?"":resultSet.getString("DOCCONTENT"));
					map.put("DOCHTMLCON", resultSet.getString("DOCHTMLCON") == null?"":resultSet.getString("DOCHTMLCON"));
					map.put("DOCABSTRACT", resultSet.getString("DOCABSTRACT") == null?"":resultSet.getString("DOCABSTRACT"));
					map.put("DOCKEYWORDS", resultSet.getString("DOCKEYWORDS") == null?"":resultSet.getString("DOCKEYWORDS"));
					map.put("DOCRELWORDS", resultSet.getString("DOCRELWORDS") == null?"":resultSet.getString("DOCRELWORDS"));
					map.put("DOCAUTHOR", resultSet.getString("DOCAUTHOR") == null?"":resultSet.getString("DOCAUTHOR"));
					map.put("DOCEDITOR", resultSet.getString("DOCEDITOR") == null?"":resultSet.getString("DOCEDITOR"));
					map.put("DOCRELTIME", resultSet.getString("DOCRELTIME") == null?"":resultSet.getString("DOCRELTIME"));
					map.put("CRUSER", resultSet.getString("CRUSER") == null?"":resultSet.getString("CRUSER"));
					map.put("DOCWORDSCOUNT", resultSet.getString("DOCWORDSCOUNT") == null?"":resultSet.getString("DOCWORDSCOUNT"));
					map.put("OPERTIME", resultSet.getString("OPERTIME") == null?"":resultSet.getString("OPERTIME"));
					map.put("OPERUSER", resultSet.getString("OPERUSER") == null?"":resultSet.getString("OPERUSER"));
					map.put("DOCSOURCENAME", resultSet.getString("DOCSOURCENAME") == null?"":resultSet.getString("DOCSOURCENAME"));
					map.put("PRONAME", resultSet.getString("PRONAME") == null?"":resultSet.getString("PRONAME"));
					map.put("BUTTONSTATU", resultSet.getString("BUTTONSTATU") == null?"":resultSet.getString("BUTTONSTATU"));
					map.put("ISPUSH", resultSet.getString("ISPUSH") == null?"":resultSet.getString("ISPUSH"));
					result.add(map);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/*
	 * 工具方法：获取时间点
	 * */
	private String getTimeByMin(int time)throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long hm = time * 60000;
		long dqhm = new Date().getTime();
		return format.format(new Date(dqhm - hm));
	}
	
	/*
	 * 工具方法：获取当前时间点
	 * */
	private String getNowTime()throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}
	
	private String getXmlName(String xmlPath)throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String xmlName = "ST";
		Date date = new Date();
		//String newXmlPath = xmlPath + "DOCUMENT" + format.format(date) + + date.getTime() + "/";
		String newXmlPath = xmlPath + "DOCUMENT" + format.format(date) + + date.getTime() + "\\";
		File file = new File(newXmlPath);
		file.mkdir();
		xmlName = newXmlPath + xmlName + format.format(date) + date.getTime() + ".xml";
		return xmlName;
	}
	
	/*
	 * 工具方法：获取正文中图片路径
	 * */
	private String contentSrc2NewPath(String content) throws Exception {
		String img = "";
		String temp = "";
		Pattern p_image;
		Matcher m_image;
		String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(content);
		HashMap<String,String> map = new HashMap<String,String>();
		while (m_image.find()) {
			img = img + "," + m_image.group();
			Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)")
					.matcher(img);
			while (m.find()) {
				String oldpath = m.group(1);
				if(oldpath.indexOf("W0") != -1){
					String newpath = this.o2npath(oldpath);
					map.put(oldpath, newpath);
				}
			}
		}
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = map.get(key);
			content = content.replace(key, value);
		}
		return content;
	}
	
	private String contentSrc2NewPaths(String content)throws Exception {
		String img = "";
		String temp = "";
		Pattern p_image;
		Matcher m_image;
		String regEx_img = "<img.*SRC\\s*=\\s*(.*?)[^>]*?>";
		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(content);
		HashMap<String,String> map = new HashMap<String,String>();
		while (m_image.find()) {
			img = img + "," + m_image.group();
			Matcher m = Pattern.compile("SRC\\s*=\\s*\"?(.*?)(\"|>|\\s+)")
					.matcher(img);
			while (m.find()) {
				String oldpath = m.group(1);
				if(oldpath.indexOf("W0") != -1){
					String newpath = this.o2npath(oldpath);
					map.put(oldpath, newpath);
				}
			}
		}
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = map.get(key);
			content = content.replace(key, value);
		}
		return content;
	}
	
	/*
	 * 工具方法：将正文中图片路径替换
	 * */
	private String o2npath(String oldpath)throws Exception{
		String one = "http://10.2.5.43/webpic/";
		String two = oldpath.substring(0, 8) + "/";
		String three = oldpath.substring(0, 10) + "/";
		String newpath = one + two + three + oldpath;
		return newpath;
	}
	
	private void delZipFile(String dataPath)throws Exception{
		s_logger.info("开始删除:"+dataPath+"下的数据");
		File file = new File(dataPath);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (dataPath.endsWith(File.separator)) {
				temp = new File(dataPath + tempList[i]);
			} else {
				temp = new File(dataPath + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if(temp.isDirectory()){
				delZipFile(dataPath + "/" + tempList[i]);
				delFolder(dataPath + "/" + tempList[i]);
			}
		}
	}
	
	private void delFolder(String folderPath)throws Exception{
		try {
			delZipFile(folderPath); 
	        String filePath = folderPath;
	        filePath = filePath.toString();
	        java.io.File myFilePath = new java.io.File(filePath);
	        myFilePath.delete(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getPushResult(String xmlDoc,String field)throws WCMException{
		String result = "";
		StringReader read = new StringReader(xmlDoc);
		InputSource source = new InputSource(read);
		SAXBuilder sb = new SAXBuilder();
		try {
			Document doc = sb.build(source);
			Element root = doc.getRootElement();
			List jiedian = root.getChildren();
			Element et = null;
			Element et2 = null;
			if("SUCCESS".equals(field)){
            	et = (Element) jiedian.get(0);
            	result = et.getText();
            	System.out.println(result);
            }
			if("IDS".equals(field)){
				for (int i = 2;i < jiedian.size();i++) {
					et = (Element)jiedian.get(i);
					List zjiedian = et.getChildren();
					for (int j = 0; j < zjiedian.size(); j++) {
						et2 = (Element)zjiedian.get(j);
						if("TITLE".equals(et2.getName())){
							String doctitle = this.getZkhContent(et2.getText());
							String docid = doctitle.substring(doctitle.indexOf("-")+1, doctitle.length());
							result += docid + ",";
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String getZkhContent(String msg)throws WCMException{
		Pattern p = Pattern.compile("(\\[[^\\]]*\\])");  
        Matcher m = p.matcher(msg);
        String result = "";
        while(m.find()){  
            result = m.group().substring(1, m.group().length()-1);  
        } 
        return result;
	}
	
	private HashMap<String,String> stringToMap(String fromChannel,String toChannel)throws Exception{
		String[] fs = fromChannel.split(",");
		String[] ts = toChannel.split(",");
		HashMap<String,String> map = new HashMap<String,String>();
		if(fs.length == ts.length){
			for (int i = 0; i < fs.length; i++) {
				map.put(fs[i], ts[i]);
			}
		}
		return map;
	}
	
	private void updateAndPublish(String docid,String chnlid)throws WCMException{
		s_logger.info("修改文档扩展字段,文档ID:"+docid+",栏目ID："+chnlid);
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		String sql = "UPDATE WCMDOCUMENT SET ISPUSH = 1 WHERE DOCID = " + docid;
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void copyPicToXmlFolder(String content,String xmlName)throws Exception{
		String img = "";
		String temp = "";
		Pattern p_image;
		Matcher m_image;
		String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(content);
		while (m_image.find()) {
			img = img + "," + m_image.group();
			Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)")
					.matcher(img);
			while (m.find()) {
				String oldpath = m.group(1);
				if(oldpath.indexOf("W0") != -1){
					temp += oldpath + ",";
				}
			}
		}
		String[] arr = temp.split(",");
		for(int i = 0;i < arr.length;i++){
			this.copyFileToTemp(arr[i], xmlName);
		}
	}
	
	public void start(String howtime)throws Exception{
		String fromChannel = "138";
		String toChannel = "140";
		ArrayList<HashMap<String,String>> result = this.queryWcm7Data(Integer.parseInt(howtime),fromChannel);
		s_logger.info("获取到"+result.size()+"条符合条件的数据");
		HashMap<String,String> toChannelMap = null;
		if(result.size() != 0){
			try {
				toChannelMap = this.stringToMap(fromChannel,toChannel);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String filePath = this.createXml(result,toChannelMap);
			s_logger.info("filePath：" + filePath);
			String sourcePath = filePath.substring(0, filePath.lastIndexOf("\\"));
			String zipPath = this.toZip(sourcePath);
			if(!"".equals(zipPath)){
				s_logger.info("待推送数据压缩包路径：" + zipPath);
				/*String sResult = this.pushZipData(zipPath);
				String isSuccess = this.getPushResult(sResult, "SUCCESS");
				if("true".equals(isSuccess)){
					String docid = this.getPushResult(sResult, "IDS");
					s_logger.info("得到WCM6数据ID：" + docid);
					this.updateWcm7Data(result,docid);
				}
				try{
					s_logger.info("删除功能未开启，定时任务结束！！！");
				}catch(Exception e){
					e.printStackTrace();
				}*/
			}else{
				s_logger.info("数据压缩失败，未找到zip类型数据文件");
			}
		}
	}
}
