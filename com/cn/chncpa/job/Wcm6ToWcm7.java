package com.cn.chncpa.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.wcm.content.domain.DocumentMgr;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.ChnlDoc;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.components.wcm.resource.Status;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;

public class Wcm6ToWcm7 extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.cn.chncpa.job.Wcm6ToWcm7.class);
	@Override
	protected void execute() throws WCMException {
		String sSfqy = CMyString.showNull(getArgAsString("sfqy"), "0");
		String RWMS = CMyString.showNull(getArgAsString("RWMS"), "定时任务");
		if (!("1".equals(sSfqy))) {
			s_logger.info(RWMS + " 未启用该策略！");
			return;
		}
		s_logger.info(RWMS + " 执行开始" + new CMyDateTime().now().toString());
		
		//1.获取WCM6数据(8893)
		String howtime = CMyString.showNull(getArgAsString("howtime"), "0");
		s_logger.info("得到参数：要获取"+howtime+"分钟之前数据");
		
		String fromChannel = CMyString.showNull(getArgAsString("fromChannel"), "0");
		String toChannel = CMyString.showNull(getArgAsString("toChannel"), "0");
		
		if("".equals(fromChannel)){
			s_logger.info("来源栏目未指定,退出定时任务");
			return;
		}
		
		if("".equals(toChannel)){
			s_logger.info("目标栏目未指定,退出定时任务");
			return;
		}
		
		ArrayList<HashMap<String,String>> result = this.queryWcm6Data(Integer.parseInt(howtime),fromChannel);
		s_logger.info("获取到"+result.size()+"条符合条件的数据");
		
		//2.向指定栏目推送数据
		if(result.size() != 0){
			this.pushDataToWcm7(result,toChannel);
			
			//3.删除临时文件夹里的文件
			this.delTempFile();
		}else{
			s_logger.info("没有符合条件的数据");
			return;
		}
	}
	
	/*
	 * 获取WCM6-8893栏目下发布的数据
	 * */
	private ArrayList<HashMap<String,String>> queryWcm6Data(int howtime,String fromChannel)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		String startTime = this.getTimeByMin(howtime);
		String endTime = this.getNowTime();
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT WD.DOCID,WD.DOCTITLE,WD.DOCCONTENT,WD.DOCHTMLCON,WD.DOCABSTRACT,WD.DOCKEYWORDS,WD.DOCEDITOR,WD.DOCRELTIME,WD.CRUSER,WD.DOCWORDSCOUNT,WD.OPERTIME,WD.OPERUSER,WD.DOCSOURCENAME,WD.PRONAME,WD.DOCAUTHOR ");
		sql.append("FROM WCMDOCUMENT WD ");
		sql.append("WHERE WD.docstatus = 10 AND WD.docchannel IN ("+fromChannel+") ");
		sql.append("AND WD.DOCPUBTIME BETWEEN TO_DATE('"+startTime+"','yyyy-mm-dd hh24:mi:ss') AND TO_DATE('"+endTime+"','yyyy-mm-dd hh24:mi:ss') ");
		//sql.append("AND WD.DOCPUBTIME BETWEEN '"+startTime+"' AND '"+endTime+"' ");
		sql.append("ORDER BY WD.DOCID");
		try {
			conn = this.getConnection();
			stmt = conn.createStatement();
			s_logger.info("执行sql:"+sql.toString());
			resultSet = stmt.executeQuery(sql.toString());
			if (resultSet != null) {
				while(resultSet.next()){
					HashMap<String,String> map = new HashMap<String,String>();
					map.put("DOCID", resultSet.getString("DOCID") == null?"":resultSet.getString("DOCID"));
					map.put("DOCTITLE", resultSet.getString("DOCTITLE") == null?"":resultSet.getString("DOCTITLE"));
					map.put("DOCCONTENT", resultSet.getString("DOCCONTENT") == null?"":resultSet.getString("DOCCONTENT"));
					map.put("DOCHTMLCON", resultSet.getString("DOCHTMLCON") == null?"":resultSet.getString("DOCHTMLCON"));
					map.put("DOCABSTRACT", resultSet.getString("DOCABSTRACT") == null?"":resultSet.getString("DOCABSTRACT"));
					map.put("DOCKEYWORDS", resultSet.getString("DOCKEYWORDS") == null?"":resultSet.getString("DOCKEYWORDS"));
					map.put("DOCEDITOR", resultSet.getString("DOCEDITOR") == null?"":resultSet.getString("DOCEDITOR"));
					map.put("DOCRELTIME", resultSet.getString("DOCRELTIME") == null?"":resultSet.getString("DOCRELTIME"));
					map.put("CRUSER", resultSet.getString("CRUSER") == null?"":resultSet.getString("CRUSER"));
					map.put("DOCWORDSCOUNT", resultSet.getString("DOCWORDSCOUNT") == null?"":resultSet.getString("DOCWORDSCOUNT"));
					map.put("OPERTIME", resultSet.getString("OPERTIME") == null?"":resultSet.getString("OPERTIME"));
					map.put("OPERUSER", resultSet.getString("OPERUSER") == null?"":resultSet.getString("OPERUSER"));
					map.put("DOCSOURCENAME", resultSet.getString("DOCSOURCENAME") == null?"":resultSet.getString("DOCSOURCENAME"));
					map.put("PRONAME", resultSet.getString("PRONAME") == null?"":resultSet.getString("PRONAME"));
					map.put("DOCAUTHOR", resultSet.getString("DOCAUTHOR") == null?"":resultSet.getString("DOCAUTHOR"));
					result.add(map);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
				this.closeConnection(resultSet, stmt, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/*
	 * 推送数据WCM7-4039栏目
	 * */
	private void pushDataToWcm7(ArrayList<HashMap<String,String>> data,String toChannel)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		String[] toChannels = toChannel.split(",");
		try{
			String sServiceId = "wcm6_document";
	        String sMethodName = "save";
			for(int i = 0;i < data.size();i++){
				for(int j = 0;j < toChannels.length;j++){
				Map oPostData = new HashMap();
				HashMap<String,String> dataMap = data.get(i);
				
				String oldDocid = dataMap.get("DOCID");
				this.checkIsPush(oldDocid);
				
				oPostData.put("ObjectId", new Integer(0));
				oPostData.put("DOCTYPE", new Integer(20));
				oPostData.put("ChannelId", toChannels[j]);
				oPostData.put("DOCSTATUS", new Integer(2));
				
				oPostData.put("DOCTITLE", dataMap.get("DOCTITLE") == null?"":dataMap.get("DOCTITLE"));
				oPostData.put("DOCCONTENT", dataMap.get("DOCCONTENT") == null?"":dataMap.get("DOCCONTENT"));
				String content = dataMap.get("DOCHTMLCON") == null?"":dataMap.get("DOCHTMLCON");
				if(!"".equals(content)){
					content = this.contentSrc2NewPath(content);
				}
				oPostData.put("DOCHTMLCON", content);
				oPostData.put("DOCABSTRACT", dataMap.get("DOCABSTRACT") == null?"":dataMap.get("DOCABSTRACT"));
				oPostData.put("DOCKEYWORDS", dataMap.get("DOCKEYWORDS") == null?"":dataMap.get("DOCKEYWORDS"));
				oPostData.put("DOCEDITOR", dataMap.get("DOCEDITOR") == null?"":dataMap.get("DOCEDITOR"));
				oPostData.put("DOCRELTIME", dataMap.get("DOCRELTIME") == null?"":dataMap.get("DOCRELTIME"));
				oPostData.put("CRUSER", dataMap.get("CRUSER") == null?"":dataMap.get("CRUSER"));
				oPostData.put("DOCWORDSCOUNT", dataMap.get("DOCWORDSCOUNT") == null?"":dataMap.get("DOCWORDSCOUNT"));
				oPostData.put("OPERTIME", dataMap.get("OPERTIME") == null?"":dataMap.get("OPERTIME"));
				oPostData.put("OPERUSER", dataMap.get("OPERUSER") == null?"":dataMap.get("OPERUSER"));
				oPostData.put("DOCSOURCENAME", dataMap.get("DOCSOURCENAME") == null?"":dataMap.get("DOCSOURCENAME"));
				oPostData.put("PRONAME", dataMap.get("PRONAME") == null?"":dataMap.get("PRONAME"));
				oPostData.put("DOCAUTHOR", dataMap.get("DOCAUTHOR") == null?"":dataMap.get("DOCAUTHOR"));
				oPostData.put("ISPUSH", oldDocid);
				
				Dispatch dispatch = WCMServiceCaller.Call(sServiceId, sMethodName, oPostData,true);
				Map json = dispatch.getJson();
	        	String newDocid = JsonHelper.getValueAsString(json, "RESULT");
	        	s_logger.info("成功推送数据:"+newDocid);
	        	
	        	this.pushFileToWcm7(oldDocid,newDocid);
	        	this.publishDoc(newDocid);
	        	
				}
			}
			this.update6Data(data);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * 获取41附件
	 * */
	private void pushFileToWcm7(String oldDocid,String newDocid)throws WCMException{
		ArrayList<HashMap<String,String>> fileList = new ArrayList<HashMap<String,String>>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT WA.APPDOCID,WA.APPFILE,WA.APPDESC ");
		sql.append("FROM WCMAPPENDIX WA ");
		sql.append("WHERE WA.APPDOCID = " + oldDocid + " ");
		sql.append("ORDER BY WA.APPENDIXID");
		try {
			conn = this.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if(resultSet != null){
				while(resultSet.next()){
					String appfile = resultSet.getString("APPFILE") == null?"":resultSet.getString("APPFILE");
					String appdesc = resultSet.getString("APPDESC") == null?"":resultSet.getString("APPDESC");
					if(!"".equals(appfile)){
						boolean bool = this.copyFile(appfile);
						if(bool){
							HashMap<String,String> map = new HashMap<String,String>();
							map.put("APPFILE", appfile);
							map.put("APPDESC", appdesc);
							fileList.add(map);
						}
					}
				}
				if(fileList.size() != 0){
					s_logger.info("有"+fileList.size()+"个附件可上传；");
					this.pushFile(newDocid, fileList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				this.closeConnection(resultSet, stmt, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 上传附件
	 * */
	private void pushFile(String docid,ArrayList<HashMap<String,String>> fileList)throws WCMException{
		String sServiceId = "wcm6_document";
		String sMethodName = "saveAppendixes";
		String fileType = "20";
		String savePath = "/trs/TRS/TRSWCMV7/WCMData/files_temp/";
		//String savePath = "D:\\TRS\\TRSWCMV7\\WCMData\\files_temp\\";
		StringBuilder postData = new StringBuilder("<OBJECTS>");
		Map oPostData = new HashMap();
		for (HashMap<String,String> map : fileList) {
			String appfile = map.get("APPFILE");
			String appdesc = map.get("APPDESC");
			String sLocalFileName = savePath + appfile;
			String suffixes = appfile.substring(appfile.lastIndexOf(".") + 1,appfile.length());
			if ("jpeg".equals(suffixes.toLowerCase()) || "jpg".equals(suffixes.toLowerCase()) || "gif".equals(suffixes.toLowerCase()) || "png".equals(suffixes.toLowerCase()) || "bmp".equals(suffixes.toLowerCase())) {
				fileType = "20";
			} else {
				fileType = "10";
			}
			Dispatch oDispatch = WCMServiceCaller.UploadFile(sLocalFileName);
			String appfiles = "";
			String applinkalt = "";
			if (oDispatch != null) {
				appfiles = oDispatch.getUploadShowName();
				applinkalt = "[object Object]";
			} else {
				appfiles = sLocalFileName;
				applinkalt = "'SRCFILE='" + sLocalFileName + "";
			}
			postData.append("<OBJECT ID='0' APPFILE='" + appfiles
					+ "' APPLINKALT='" + applinkalt + "' APPFLAG='"
					+ fileType + "' APPDESC='" + appdesc + "'/>");
		}
		postData.append("</OBJECTS>");
		oPostData.put("DocId", Integer.valueOf(docid));
		oPostData.put("AppendixType", Integer.valueOf(fileType));
		oPostData.put("APPENDIXESXML", postData.toString());
		WCMServiceCaller.Call(sServiceId, sMethodName, oPostData, true);
	}
	/*
	 * 复制文件到53服务器
	 * */
	private boolean copyFile(String appfile)throws WCMException{
		boolean bool = false;
		InputStream is = null;
		OutputStream os = null;
		String outPath = "/trs/TRS/TRSWCMV7/WCMData/files_temp/";
		//String outPath = "D:\\TRS\\TRSWCMV7\\WCMData\\files_temp\\";
		try {
			String newAppfile = this.o2npath(appfile);
			URL url = new URL(newAppfile); 
			URLConnection con = url.openConnection(); 
			con.setConnectTimeout(5 * 1000);  
			is = con.getInputStream();
			byte[] bs = new byte[1024];
			int len;
			File file = new File(outPath);  
			if (!file.exists()) {  
				file.mkdirs();  
	        } 
			os = new FileOutputStream(outPath + appfile);  
			while ((len = is.read(bs)) != -1) {  
	            os.write(bs, 0, len);  
	        }
			File file2 = new File(outPath + appfile);
			if(file2.exists()){
				bool = true;
			}
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return bool;
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

	/*
	 * 工具方法：获取数据库连接
	 * */
	private Connection getConnection()throws Exception{
		String driverClassName = "oracle.jdbc.driver.OracleDriver";
		Class.forName(driverClassName);
		String url = "jdbc:oracle:thin:@//10.2.7.47:1521/newnybmh";
		String username = "trswcmv610882";
		String password = "trswcmv610882";
		/*String driverClassName = "com.mysql.jdbc.Driver";
		Class.forName(driverClassName);
		String url = "jdbc:mysql://localhost:3306/trswcmv7";
		String username = "root";
		String password = "root";*/
		return DriverManager.getConnection(url,username,password);
	}
	
	/*
	 * 工具方法：关闭数据库连接
	 * */
	private void closeConnection(ResultSet rs, Statement st,Connection conn)throws Exception{
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (st != null) {
					try {
						st.close();
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						try {
							conn.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
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
	
	/*
	 * 工具方法：将正文中图片路径替换
	 * */
	private String o2npath(String oldpath)throws Exception{
		String one = "http://10.2.5.41/webpic/";
		String two = oldpath.substring(0, 8) + "/";
		String three = oldpath.substring(0, 10) + "/";
		String newpath = one + two + three + oldpath;
		return newpath;
	}
	
	/*
	 * 工具方法：校验是否推送过
	 * */
	private void checkIsPush(String oldDocid)throws WCMException{
		s_logger.info("开始检验ID："+oldDocid+"数据是否推送过");
		String docid = "";
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT WD.DOCID,WD.ISPUSH FROM WCMDOCUMENT WD WHERE WD.DOCCHANNEL = 4039 AND WD.DOCSTATUS IN (1,2,10) AND WD.ISPUSH = '"+oldDocid+"'";
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			
			s_logger.info("查询要删除数据SQL:"+sql);
			resultSet = stmt.executeQuery(sql);
			if(resultSet != null){
				while(resultSet.next()){
					docid += resultSet.getString("DOCID") + ",";
				}
			}
			s_logger.info("要删除的ID:"+docid);
			if(!"".equals(docid)){
				this.delPushData(docid);
			}
		} catch (Exception e) {
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
	}
	
	/*
	 * 删除已推送过的数据
	 * */
	private void delPushData(String docid)throws WCMException{
		s_logger.info("删除推送过的数据，ID："+docid);
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		String[] docids = docid.split(",");
		DocumentMgr dm = new DocumentMgr();
		for(int i = 0;i < docids.length;i++){
			Document document = Document.findById(Integer.parseInt(docids[i]));
			dm.delete(document, Channel.findById(4039), true);
			//dm.delete(document, Channel.findById(140), true);
			s_logger.info("删除已推送数据:"+docid);
		}
	}
	
	/*
	 * 删除临时文件
	 * */
	private void delTempFile() throws WCMException {
		String tempPath = "/trs/TRS/TRSWCMV7/WCMData/files_temp/";
		//String tempPath = "D:\\TRS\\TRSWCMV7\\WCMData\\files_temp\\";
		File file = new File(tempPath);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (tempPath.endsWith(File.separator)) {
				temp = new File(tempPath + tempList[i]);
			} else {
				temp = new File(tempPath + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
		}
	}
	
	private void publishDoc(String docId)throws Exception{
		s_logger.info("开始发布数据："+docId);
		Document document = Document.findById(Integer.parseInt(docId));
		if(document == null)return;
		ChnlDoc chnlDoc = ChnlDoc.findByDocument(document);
		if(chnlDoc == null)return;
		String sServiceId = "wcm6_viewdocument";
		String sMethodName = "basicPublish";
		HashMap oPostData = new HashMap();
		oPostData.put("OBJECTIDS",chnlDoc.getId());
		WCMServiceCaller.Call(sServiceId, sMethodName, oPostData, true);
	}
	
	private void update6Data(ArrayList<HashMap<String,String>> data)throws Exception{
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		if(data.size() != 0){
			for (int i = 0; i < data.size(); i++) {
				HashMap<String,String> map = data.get(i);
				try {
					String docid = map.get("DOCID");
					conn = this.getConnection();
					stmt = conn.createStatement();
					String sql = "UPDATE WCMDOCUMENT SET ZZ = 1 WHERE DOCID = " + docid;
					s_logger.info("执行sql:"+sql);
					stmt.executeUpdate(sql);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	public void start()throws Exception{
		/*ArrayList<HashMap<String,String>> result = this.queryWcm6Data(60,"8893");
		this.pushDataToWcm7(result,"4039");*/
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("DOCTITLE", "测试标题");
		map.put("DOCHTMLCON", "测试正文");
		list.add(map);
		Wcm6ToWcm7 to7 = new Wcm6ToWcm7();
		to7.pushDataToWcm7(list, "138");
		
	}
}
