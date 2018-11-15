package com.cn.chncpa.job;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.jdom.output.XMLOutputter;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.common.publish.PublishConstants;
import com.trs.components.common.publish.domain.PublishServer;
import com.trs.components.common.publish.persistent.element.IPublishContent;
import com.trs.components.common.publish.persistent.element.PublishElementFactory;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.definition.ClassInfo;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;

public class W72W6 extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.cn.chncpa.job.W72W6.class);
	
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
		
		HashMap<String,String> toChannelMap = null;
		if(result.size() != 0){
			try {
				toChannelMap = this.stringToMap(fromChannel,toChannel);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String filePath = this.createXml(result,toChannelMap);
			//String sourcePath = filePath.substring(0, filePath.lastIndexOf("\\"));
			String sourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
			s_logger.info("全路径--->："+filePath);
			s_logger.info("即将压缩的路径--->："+sourcePath);
			String zipPath = this.toZip(sourcePath);
			if(!"".equals(zipPath)){
				this.pushZipData(zipPath);
				this.updateAndPublish(result);
				s_logger.info("定时任务结束！！！");
			}
		}
	}
	
	public ArrayList<HashMap<String,String>> start(String docid,String fromChannel,String toChannel)throws WCMException{
		ArrayList<HashMap<String,String>> data = this.queryListById(docid, fromChannel);
		HashMap<String,String> toChannelMap = null;
		if(data.size() != 0){
			try {
				toChannelMap = this.stringToMap(fromChannel,toChannel);
				String filePath = this.createXml(data,toChannelMap);
				String sourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
				String zipPath = this.toZip(sourcePath);
				this.pushZipData(zipPath);
				this.updateAndPublish(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	/*
	 * 推送zip到wcm6.5
	 * */
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
	
	/*
	 * 添加压缩ZIP
	 * */
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
				//String zipName = srcPath.substring(srcPath.lastIndexOf("\\"), srcPath.length());
				//String zipPath = srcPath.substring(0,srcPath.lastIndexOf("\\"));
				String zipName = srcPath.substring(srcPath.lastIndexOf("/"), srcPath.length());
				String zipPath = srcPath.substring(0,srcPath.lastIndexOf("/"));
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
	
	/*
	 * 生成XML文件
	 * */
	private String createXml(ArrayList<HashMap<String,String>> dataList,HashMap<String,String> toChannelMap)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		String xmlPath = "/trs/TRS/TRSWCMV7/WCMData/xmls_temp/";
		//String xmlPath = "D:\\TRS\\TRSWCMV7\\WCMData\\xmls_temp\\";
		String xmlName = this.getXmlName(xmlPath);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String now = format.format(new Date());
		try {
			Element root = new Element("WCMDOCUMENTS");												
			Document xml = new Document(root);
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
			
				properties.addContent(new Element("DOCCHANNEL").addContent(toChannelMap.get(map.get("CHNLID"))));
			
				properties.addContent(new Element("DOCTITLE").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCTITLE"), ""), ""))));
				properties.addContent(new Element("DOCPEOPLE").addContent(new CDATA("")));
				properties.addContent(new Element("DOCSOURCENAME").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCSOURCENAME"), ""), ""))));
				properties.addContent(new Element("DOCSTATUS").addContent("2"));
				properties.addContent(new Element("DOCCONTENT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCCONTENT"), ""), ""))));
				
				String content = CMyString.showEmpty(CMyString.showNull(map.get("DOCHTMLCON"), ""), "");
				if(!"".equals(content)){
					if(content.indexOf("src=\"") != -1 || content.indexOf("SRC=\"") != -1){
						this.copyPicToXmlFolder(content,xmlName);
					}
				}
				properties.addContent(new Element("DOCHTMLCON").addContent(new CDATA(content)));
				properties.addContent(new Element("DOCABSTRACT").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCABSTRACT"), ""), ""))));
				properties.addContent(new Element("DOCKEYWORDS").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCKEYWORDS"), ""), ""))));
				properties.addContent(new Element("DOCAUTHOR").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCAUTHOR"), ""), ""))));
				properties.addContent(new Element("DOCRELTIME").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull(map.get("DOCRELTIME"), ""), ""))));
				properties.addContent(new Element("ZZ").addContent(new CDATA(CMyString.showEmpty(CMyString.showNull("1", ""), ""))));
				properties.addContent(new Element("CHNLNAME").addContent(new CDATA("")));
				
				wcmdocument.addContent(properties);
				Appendixes appendixes = Appendixes.findAppendixesByObj(com.trs.components.wcm.content.persistent.Document.findById(Integer.parseInt(map.get("DOCID"))));
				if(appendixes.size() != 0){
					Element wcmappendixs = new Element("WCMAPPENDIXS");	
					for (int j = 0; j < appendixes.size(); j++) {
						Appendix appendix = (Appendix) appendixes.getAt(j);
						if(appendix == null)continue;
						Element wcmappendix = new Element("WCMAPPENDIX").setAttribute("Version", "6.0");
						Element properties_a = new Element("PROPERTIES");
						
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
			}
			XMLOutputter XMLOut = new XMLOutputter("	",true,"UTF-8");  
			FileOutputStream fileOutputStream = new FileOutputStream(xmlName);
			XMLOut.output(xml, fileOutputStream);
			fileOutputStream.close();
			XMLOut = null;
		}catch(Exception e){
			
		}
		return xmlName;
	}
	
	/*
	 * 更新推送标识
	 * */
	private void updateAndPublish(ArrayList<HashMap<String,String>> data)throws WCMException{
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		if(data.size() != 0){
			for (int i = 0; i < data.size(); i++) {
				HashMap<String,String> map = data.get(i);
				String docid = map.get("DOCID");
				s_logger.info("开始更新数据推送标识:ID;"+docid);
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
		}
	}
	
	/*
	 * 更新后发布
	 * */
	private void publishToWcm(String docid)throws Exception{
		ContextHelper.initContext(User.findByName("admin"));
		com.trs.components.wcm.content.persistent.Document oDocument = com.trs.components.wcm.content.persistent.Document.findById(Integer.parseInt(docid));
		s_logger.info("发布数据 docid："+docid);
		IPublishContent content = PublishElementFactory.makeContentFrom(oDocument, null);
		PublishServer publishServer = PublishServer.getInstance();
		publishServer.publishContent(content,PublishConstants.PUBLISH_CONTENT);
	}
	
	/*
	 * 将正文中的图片拷贝到临时文件夹
	 * */
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
	
	/*
	 * 拷贝图片
	 * */
	private void copyFileToTemp(String srcFile,String filePath)throws WCMException{
		FileInputStream ins = null;
        FileOutputStream out = null;
		try {
			if(!"".equals(srcFile)){
				String srcFilePath = this.getSrcFilePath(srcFile);
				String newFileName = filePath.substring(0, filePath.lastIndexOf("/") + 1) + srcFile;
				//String newFileName = filePath.substring(0, filePath.lastIndexOf("\\") + 1) + srcFile;
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
	
	/*
	 * 获取源图片
	 * */
	private String getSrcFilePath(String srcFile)throws WCMException{
		String rootPath = "/WCMData/";
		//String rootPath = "D:\\TRS\\TRSWCMV7\\WCMData\\";
		String srcFilePath = "";
		String suffixes = srcFile.substring(srcFile.lastIndexOf(".") + 1,srcFile.length());
		String one = srcFile.substring(0, 8);
		String two = srcFile.substring(0, 10);
		if ("jpeg".equals(suffixes.toLowerCase()) || "jpg".equals(suffixes.toLowerCase()) || "gif".equals(suffixes.toLowerCase()) || "png".equals(suffixes.toLowerCase()) || "bmp".equals(suffixes.toLowerCase())) {
			srcFilePath = rootPath + "webpic/" + one + "/" + two + "/" + srcFile;
			//srcFilePath = rootPath + "webpic\\" + one + "\\" + two + "\\" + srcFile;
		} else {
			srcFilePath = rootPath + "protect/" + one + "/" + two + "/" + srcFile;
			//srcFilePath = rootPath + "protect\\" + one + "\\" + two + "\\" + srcFile;
		}
		return srcFilePath;
	}
	
	/*
	 * 获取XML
	 * */
	private String getXmlName(String xmlPath)throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String xmlName = "ST";
		Date date = new Date();
		String newXmlPath = xmlPath + "DOCUMENT" + format.format(date) + + date.getTime() + "/";
		//String newXmlPath = xmlPath + "DOCUMENT" + format.format(date) + + date.getTime() + "\\";
		File file = new File(newXmlPath);
		file.mkdir();
		xmlName = newXmlPath + xmlName + format.format(date) + date.getTime() + ".xml";
		return xmlName;
	}
	
	/*
	 * 获取数据
	 * */
	public ArrayList<HashMap<String,String>> queryWcm7Data(int howtime,String fromChannel)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		String startTime = this.getTimeByMin(howtime);
		String endTime = this.getNowTime();
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT WC.CHNLID,WC.MODAL,WD.DOCID,WD.DOCTITLE,WD.DOCCONTENT,WD.DOCHTMLCON,WD.DOCABSTRACT,WD.DOCRELTIME,WD.DOCKEYWORDS,WD.DOCAUTHOR,WD.DOCSOURCENAME,WD.ISPUSH FROM WCMCHNLDOC WC LEFT JOIN WCMDOCUMENT WD ON WC.DOCID = WD.DOCID "); 
		sql.append("WHERE WC.CHNLID IN("+fromChannel+") AND WC.DOCSTATUS = 10 AND (WD.ISPUSH is NULL OR WD.ISPUSH = '') AND WC.MODAL IN(1,3) ");
		sql.append("AND WC.DOCPUBTIME BETWEEN TO_DATE('"+startTime+"','yyyy-mm-dd hh24:mi:ss') AND TO_DATE('"+endTime+"','yyyy-mm-dd hh24:mi:ss') ");
		//sql.append("AND WC.DOCPUBTIME BETWEEN '"+startTime+"' AND '"+endTime+"' ");
		sql.append("ORDER BY WC.DOCID");
		try {
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
					map.put("DOCRELTIME", resultSet.getString("DOCRELTIME") == null?"":resultSet.getString("DOCRELTIME"));
					
					String modal = resultSet.getString("MODAL");
					if("3".equals(modal)){
						map = this.getDataByMetaDataId(resultSet.getString("DOCID"),map);
					}else{
						map.put("DOCABSTRACT", resultSet.getString("DOCABSTRACT") == null?"":resultSet.getString("DOCABSTRACT"));
						map.put("DOCKEYWORDS", resultSet.getString("DOCKEYWORDS") == null?"":resultSet.getString("DOCKEYWORDS"));
						map.put("DOCAUTHOR", resultSet.getString("DOCAUTHOR") == null?"":resultSet.getString("DOCAUTHOR"));
						map.put("DOCSOURCENAME", resultSet.getString("DOCSOURCENAME") == null?"":resultSet.getString("DOCSOURCENAME"));
					}
					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
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
	 * 将各栏目以MAP形式
	 * */
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
	
	/*
	 * 获取分类法名称
	 * */
	private HashMap<String,String> getDataByMetaDataId(String docid,HashMap<String,String> map)throws WCMException{
		MetaViewData metaViewData = MetaViewData.findById(Integer.parseInt(docid));
		//摘要
		String description = metaViewData.getPropertyAsString("organcat") == null?"":metaViewData.getPropertyAsString("organcat");
		map.put("DOCABSTRACT", description);
		//关键字
		String keywords = metaViewData.getPropertyAsString("keywords") == null?"":metaViewData.getPropertyAsString("keywords");
		map.put("DOCKEYWORDS", keywords);
		//来源、作者
		String organcat = metaViewData.getPropertyAsString("organcat");
		if(organcat != null && !"".equals(organcat)){
			ClassInfo classInfo = ClassInfo.findById(Integer.parseInt(organcat));
			map.put("DOCSOURCENAME", classInfo.getName());
			map.put("DOCAUTHOR", classInfo.getName());
		}
		return map;
	}
	
	/*
	 * 获取N分钟之前时间
	 * */
	private String getTimeByMin(int time)throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long hm = time * 60000;
		long dqhm = new Date().getTime();
		return format.format(new Date(dqhm - hm));
	}
	
	/*
	 * 获取当前时间
	 * */
	private String getNowTime()throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}
	
	private ArrayList<HashMap<String,String>> queryListById(String docid,String channelId)throws WCMException{
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT WC.CHNLID,WC.MODAL,WD.DOCID,WD.DOCTITLE,WD.DOCCONTENT,WD.DOCHTMLCON,WD.DOCABSTRACT,WD.DOCRELTIME,WD.DOCKEYWORDS,WD.DOCAUTHOR,WD.DOCSOURCENAME,WD.ISPUSH FROM WCMCHNLDOC WC LEFT JOIN WCMDOCUMENT WD ON WC.DOCID = WD.DOCID WHERE WC.DOCSTATUS = 10 AND WC.MODAL IN(1,3) AND WC.DOCID IN ("+docid+") AND WC.CHNLID = " + channelId;
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if (resultSet != null) {
				while(resultSet.next()){
					HashMap<String,String> map = new HashMap<String,String>();
					map.put("DOCID", resultSet.getString("DOCID") == null?"":resultSet.getString("DOCID"));
					map.put("CHNLID", resultSet.getString("CHNLID") == null?"":resultSet.getString("CHNLID"));
					map.put("DOCTITLE", resultSet.getString("DOCTITLE") == null?"":resultSet.getString("DOCTITLE"));
					map.put("DOCCONTENT", resultSet.getString("DOCCONTENT") == null?"":resultSet.getString("DOCCONTENT"));
					map.put("DOCHTMLCON", resultSet.getString("DOCHTMLCON") == null?"":resultSet.getString("DOCHTMLCON"));
					map.put("DOCRELTIME", resultSet.getString("DOCRELTIME") == null?"":resultSet.getString("DOCRELTIME"));
					
					String modal = resultSet.getString("MODAL");
					if("3".equals(modal)){
						map = this.getDataByMetaDataId(resultSet.getString("DOCID"),map);
					}else{
						map.put("DOCABSTRACT", resultSet.getString("DOCABSTRACT") == null?"":resultSet.getString("DOCABSTRACT"));
						map.put("DOCKEYWORDS", resultSet.getString("DOCKEYWORDS") == null?"":resultSet.getString("DOCKEYWORDS"));
						map.put("DOCAUTHOR", resultSet.getString("DOCAUTHOR") == null?"":resultSet.getString("DOCAUTHOR"));
						map.put("DOCSOURCENAME", resultSet.getString("DOCSOURCENAME") == null?"":resultSet.getString("DOCSOURCENAME"));
					}
					list.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
