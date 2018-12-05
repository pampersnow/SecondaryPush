package com.trs.other;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.center.MetaViewDatas;
import com.trs.components.wcm.content.domain.AppendixMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.components.wcm.content.persistent.ChnlDoc;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.support.config.ConfigServer;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;
import com.trs.webframework.context.MethodContext;
import com.trs.webframework.controler.JSPRequestProcessor;
import com.trs.webframework.provider.ISelfDefinedServiceProvider;

import net.sf.json.JSONArray;

public class CustomService implements ISelfDefinedServiceProvider{

	private static Logger logger = Logger.getLogger(CustomService.class);
	
	/*
	 * 查询中转服务器视图服务集
	 * */
	public String queryServers(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		int nViewId = _oMethodContext.getValue("ViewId", 0);
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";   
		HashMap parameters = new HashMap();
		parameters.put("ViewId",nViewId);
		parameters.put("PageSize", 2000);
		parameters.put("OrderBy", "WCMMETATABLEFWJ.SYSCONNID");
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			sResult = this.getStringByMetaViewDatas(metadatas);
		}
		return sResult;
	}
	
	/*
	 * 查询中转服务器视图映射集
	 * */
	public String queryMappingByIp(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		int nViewId = _oMethodContext.getValue("ViewId", 0);
		String argIp = _oMethodContext.getValue("ArgIp");
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";
        HashMap parameters = new HashMap();
		parameters.put("ViewId",nViewId);
		parameters.put("PageSize", 2000);
		parameters.put("_sqlWhere_", "wcmmetatableysj.SRC_SERVER_IP = '"+argIp+"' ");
		parameters.put("OrderBy","wcmmetatableysj.MetaDataId");
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			sResult = this.getStringByMetaViewDatas2(metadatas);
		}
		return sResult;
	}
	
	/*
	 * 查询目标服务器所有站点
	 * */
	public String queryTargetTree(MethodContext _oMethodContext)throws Throwable{
		String resultTree = "";
		String sServiceId = "wcm6_website";
        String sMethodName = "query";
        String targetIp = _oMethodContext.getValue("TargetIp");
        String searchArg = _oMethodContext.getValue("SearchArg");
        HashMap oPostData = new HashMap();
        oPostData.put("SiteType", 0);
        oPostData.put("PageSize", 2000);
        oPostData.put("OrderBy", "SITEORDER DESC");
        if(searchArg != null && !"".equals(searchArg)){
        	oPostData.put("QuerySiteName", searchArg);
        }
        Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName, oPostData,true,targetIp);
        Map json = oDispatch.getJson();
        List list = JsonHelper.getList(json, "WEBSITES.WEBSITE");
        ArrayList<HashMap<String,Object>> treeList = new ArrayList<HashMap<String,Object>>();
        if(list != null){
        	HashMap<String,Object> parentMap = new HashMap<String,Object>();
        	parentMap.put("id", 1);
        	parentMap.put("name", "所有站点");
        	parentMap.put("pid", 0);
        	treeList.add(parentMap);
			for (int i = 0; i < list.size(); i++) {
				HashMap<String,Object> map = new HashMap<String,Object>();
				map.put("id", JsonHelper.getValueAsString((Map)list.get(i), "SITEID"));
				map.put("name", JsonHelper.getValueAsString((Map)list.get(i), "SITENAME"));
				map.put("pid", 1);
				treeList.add(map);
			}
		}
        resultTree = JSONArray.fromObject(treeList).toString();
        return resultTree;
	}
	
	public String queryChannelMappingByIp(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		int nViewId = _oMethodContext.getValue("ViewId", 0);
		String argIp = _oMethodContext.getValue("ArgIp");
		String argCid = _oMethodContext.getValue("ArgCid");
		String argTarIp = _oMethodContext.getValue("ArgTarIp");
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";
        HashMap parameters = new HashMap();
		parameters.put("ViewId",nViewId);
		parameters.put("PageSize", 2000);
		String sqlWhere = "wcmmetatablelmysj.SRC_IP = '"+argIp+"' ";
		if(argCid != null && !"".equals(argCid)){
			sqlWhere += "AND wcmmetatablelmysj.SRC_LMID = '"+argCid+"' ";
		}
		if(argTarIp != null && !"".equals(argTarIp)){
			sqlWhere += "AND wcmmetatablelmysj.TARGET_IP = '"+argTarIp+"' ";
		}
		parameters.put("_sqlWhere_",sqlWhere);
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			sResult = this.getStringByMetaViewDatas3(metadatas);
		}
		return sResult;
	}
	
	public String queryChannelMappingByIps(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		int nViewId = _oMethodContext.getValue("ViewId", 0);
		String argIp = _oMethodContext.getValue("ArgIp");
		String argCid = _oMethodContext.getValue("ArgCid");
		String argTarIp = _oMethodContext.getValue("ArgTarIp");
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";
        HashMap parameters = new HashMap();
		parameters.put("ViewId",nViewId);
		parameters.put("PageSize", 2000);
		String sqlWhere = "wcmmetatablelmysj.SRC_IP = '"+argTarIp+"' ";
		if(argCid != null && !"".equals(argCid)){
			sqlWhere += "AND wcmmetatablelmysj.TARGET_LMID = '"+argCid+"' ";
		}
		if(argTarIp != null && !"".equals(argTarIp)){
			sqlWhere += "AND wcmmetatablelmysj.TARGET_IP = '"+argIp+"' ";
		}
		parameters.put("_sqlWhere_",sqlWhere);
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			sResult = this.getStringByMetaViewDatas3(metadatas);
		}
		return sResult;
	}
	
	public String queryTargetChannelsBySiteId(MethodContext _oMethodContext)throws Throwable{
		String resultTree = "";
		String sServiceId = "wcm6_channel";
        String sMethodName = "query";
        String siteid = _oMethodContext.getValue("TargetSiteId");
        String targetIp = _oMethodContext.getValue("TargetIp");
        HashMap parameters = new HashMap();
		parameters.put("SiteId",siteid);
		parameters.put("PageSize", 2000);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName, parameters,true,targetIp);
        Map json = oDispatch.getJson();
        List list = JsonHelper.getList(json, "CHANNELS.CHANNEL");
        ArrayList<HashMap<String,Object>> treeList = new ArrayList<HashMap<String,Object>>();
        if(list != null){
			for (int i = 0; i < list.size(); i++) {
				String acceptnode = JsonHelper.getValueAsString((Map)list.get(i), "ATTRIBUTE.ACCEPTNODE");
				if(acceptnode == null || "".equals(acceptnode) || "0".equals(acceptnode))continue;
				HashMap<String,Object> map = new HashMap<String,Object>();
				map.put("CHANNELID", JsonHelper.getValueAsString((Map)list.get(i), "CHANNELID"));
				map.put("CHANNELNAME", JsonHelper.getValueAsString((Map)list.get(i), "CHNLDESC"));
				map.put("SITEID", JsonHelper.getValueAsString((Map)list.get(i), "SITE.ID"));
				treeList.add(map);
				if("true".equals(JsonHelper.getValueAsString((Map)list.get(i), "HASCHILDREN"))){
					treeList.addAll(queryMoreChannels(JsonHelper.getValueAsString((Map)list.get(i), "CHANNELID"), targetIp));
				}
			}
		}
        resultTree = JSONArray.fromObject(treeList).toString();
        return resultTree;
	}
	
	private ArrayList<HashMap<String,Object>> queryMoreChannels(String channelid,String ip_port)throws Throwable{
		String sServiceId = "wcm61_channel";
        String sMethodName = "query";
        List resultList = new ArrayList();
        Map map = new HashMap();
		map.put("ChannelId", channelid);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName, map,true,ip_port);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "CHANNELS.CHANNEL");
		if(list != null){
			for (int i = 0; i < list.size(); i++) {
				String acceptnode = JsonHelper.getValueAsString((Map)list.get(i), "ATTRIBUTE.ACCEPTNODE");
				if(acceptnode == null || "".equals(acceptnode) || "0".equals(acceptnode))continue;
				Map childMap = new HashMap();
				childMap.put("CHANNELID", JsonHelper.getValueAsString((Map)list.get(i), "CHANNELID"));
				childMap.put("CHANNELNAME", JsonHelper.getValueAsString((Map)list.get(i), "CHNLDESC"));
				childMap.put("SITEID", channelid);
				resultList.add(childMap);
				if("true".equals(JsonHelper.getValueAsString((Map)list.get(i), "HASCHILDREN"))){
					resultList.addAll(queryMoreChannels(JsonHelper.getValueAsString((Map)list.get(i),"CHANNELID"), ip_port));
				}
			}
		}
		return (ArrayList) resultList;
	}
	
	public String saveData(MethodContext _oMethodContext)throws Throwable{
		String result = "";
		String sServiceId = "wcm6_viewdocument";
		String sMethodName = "save";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sourceDataId = _oMethodContext.getValue("DataId");
	    String channelId = _oMethodContext.getValue("TargetCid");
	    String targetIp = _oMethodContext.getValue("TargetIp");
	    String zzip = _oMethodContext.getValue("ZzIp");
	    
	    String[] dataIdsArr = sourceDataId.split(",");
	    String[] cids = channelId.split(",");
	    
	    Dispatch oDispatch = null;
	    AppendixMgr mgr = new AppendixMgr();
	    for (int i = 0; i < dataIdsArr.length; i++) {
	    	for (int j = 0; j < cids.length; j++) {
	    		Document document = Document.findById(Integer
						.parseInt(dataIdsArr[i]));
		    	if (document == null)
					continue;
		    	HashMap<String, Object> oPostData = new HashMap<String, Object>();
				oPostData.put("ObjectId", 0);
				oPostData.put("ChannelId", Integer.parseInt(cids[j]));
				oPostData.put("CRTIME", format.format(new Date()));
				oPostData.put("CRUSER", document.getCrUser().getName());
				oPostData.put("DOCTITLE", document.getTitle());
				oPostData.put("DOCSTATUS", 1);
				oPostData.put("DOCTYPE", document.getType());

				oPostData.put("SYSCONNID", document.getPropertyAsInt("SYSCONNID", 0));
				oPostData.put("SRCSITEID", document.getPropertyAsInt("SRCSITEID", 0));
				oPostData.put("TUISONG", 1);
				oPostData.put("DOCOUTUPID", document.getPropertyAsInt("DOCOUTUPID", 0));
				
				oPostData.put("SUBDOCTITLE", document.getSubTitle() == null?"":document.getSubTitle());
				oPostData.put("DOCAUTHOR", document.getPropertyAsString("DOCAUTHOR") == null?"":document.getPropertyAsString("DOCAUTHOR"));
				oPostData.put("DOCPEOPLE", document.getPeople() == null?"":document.getPeople());
				oPostData.put("DOCKEYWORDS", document.getKeywords() == null?"":document.getKeywords());
				oPostData.put("DOCABSTRACT", document.getAbstract() == null?"":document.getAbstract());
				oPostData.put("DOCSOURCENAME", document.getPropertyAsString("DOCSOURCENAME") == null?"":document.getPropertyAsString("DOCSOURCENAME"));
				
				if(targetIp.split(":")[0].equals(ConfigServer.getServer().getSysConfigValue("ZKY_YZW_IP", "0"))){
					oPostData.put("AUDIUSER", document.getCrUser().getName());
					oPostData.put("AUDITED", 1);
					oPostData.put("AUDITIME", format.format(new Date()));
				}
				switch (document.getType()) {
				case 10:
					oPostData.put("DOCCONTENT", document.getContent());
					oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
							oPostData, true, targetIp);
					break;
				case 20:
					oPostData.put("DOCHTMLCON", this.cutContent(document.getHtmlContent(),zzip));
					oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
							oPostData, true, targetIp);
					break;
				case 30:
					oPostData.put("DOCLINK",
							document.getPropertyAsString("DOCLINK"));
					oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
							oPostData, true, targetIp);
					break;
				case 40:
					String appfile = this.pushFileBeforeDocument(
							document.getPropertyAsString("DOCFILENAME"), targetIp);
					oPostData.put("DOCFILENAME", appfile);
					oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
							oPostData, true, targetIp);
					break;
				}
				
				Map json = oDispatch.getJson();
				String DocId = JsonHelper.getValueAsString(json, "RESULT");
				
				//if(j == cids.length - 1){
					//result += dataIdsArr[i] + ":" + cids[j] + ":" + DocId;
				//}else{
					result += dataIdsArr[i] + ":" + cids[j] + ":" + DocId + ",";
				//}
				
				ContextHelper.initContext(User.findByName("admin"));
				Appendixes appendixes_file = mgr.getAppendixes(document, 10);
				Appendixes appendixes_jpg = mgr.getAppendixes(document, 20);
				Appendixes appendixes_link = mgr.getAppendixes(document, 40);
				if (appendixes_file.size() != 0) {
					this.pushFileAfterDocument(appendixes_file,10,
							DocId, targetIp);
				}
				if (appendixes_jpg.size() != 0) {
					this.pushFileAfterDocument(appendixes_jpg,20,DocId, targetIp);
				}
				if(appendixes_link.size() != 0){
					this.pushLinkAppendix(appendixes_link, 40, DocId, targetIp);
				}
			}
	    }
		return result;
	}
	
	public String querySitesBySysId(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		String nViewId = _oMethodContext.getValue("ViewId");
		String sysid = _oMethodContext.getValue("Sysconnid");
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId",nViewId);
		parameters.put("_sqlWhere_", "wcmmetatablezdj.SYSCONNID = '"+sysid+"' ");
		parameters.put("PageSize", 2000);
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			sResult = this.getStringByMetaViewDatas4(metadatas);
		}
		return sResult;
	}
	
	public String findDocById(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		String docid = _oMethodContext.getValue("Docid");
		String siteid = _oMethodContext.getValue("Siteid");
		String targetIp = _oMethodContext.getValue("TargetIp");
		
		String sServiceId = "wcm6_document";
		String sMethodName = "findById";
		
		Map map = new HashMap();
		map.put("ObjectId", docid);
		map.put("SiteId", siteid);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName, map,true,targetIp);
		Map json = oDispatch.getJson();
		if(json != null){
			sResult = "{\"DOCID\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.DOCID") + "\","
					+ "\"CRUSER\":\""+JsonHelper.getValueAsString(json, "DOCUMENT.CRUSER") + "\","
					+ "\"CRTIME\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.CRTIME") + "\","
					+ "\"SITEID\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.SITEID") + "\","
					+ "\"CHANNELNAME\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.DOCCHANNEL.NAME") + "\","
					+ "\"STATUS\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.DOCSTATUS.NAME") + "\"}";
			
		}
		return sResult;
	}	
	
	public String findDocsById(MethodContext _oMethodContext)throws Throwable{
		String sResult = "[";
		String docid = _oMethodContext.getValue("Docid");
		String siteid = _oMethodContext.getValue("Siteid");
		String targetIp = _oMethodContext.getValue("TargetIp");
		String viewid = _oMethodContext.getValue("SiteViewid");
		
		String sServiceId = "wcm61_viewdocument";
		String sMethodName = "query";
		
		String sites = this.querySitesByIp(targetIp.split(":")[0],viewid);
		Map map = new HashMap();
		map.put("SiteIds", sites);
		map.put("DocumentSelectFields","DOCID,CRUSER,CRTIME,SITEID,TUISONG,SYSCONNID");
		map.put("_sqlWhere_", "DOCOUTUPID = " + docid + " and SRCSITEID = " + siteid + " ");
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName, map,true,targetIp);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "VIEWDOCUMENTS.VIEWDOCUMENT");
        if(list != null && list.size() != 0){
			for (int i = 0; i < list.size(); i++) {
				if(i == list.size() - 1){
					sResult += "{\"DOCID\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "DOCID") + "\","
							+ "\"CRUSER\":\""+JsonHelper.getValueAsString((Map)list.get(i), "CRUSER") + "\","
							+ "\"CRTIME\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "CRTIME") + "\","
							+ "\"SITEID\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "SITEID") + "\","
							+ "\"TUISONG\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "TUISONG") + "\","
							+ "\"SYSCONNID\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "SYSCONNID") + "\","
							+ "\"CHANNELNAME\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "DOCCHANNEL.NAME") + "\","
							+ "\"STATUS\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "DOCSTATUS.NAME") + "\"}";
				}else{
					sResult += "{\"DOCID\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "DOCID") + "\","
							+ "\"CRUSER\":\""+JsonHelper.getValueAsString((Map)list.get(i), "CRUSER") + "\","
							+ "\"CRTIME\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "CRTIME") + "\","
							+ "\"SITEID\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "SITEID") + "\","
							+ "\"TUISONG\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "TUISONG") + "\","
							+ "\"SYSCONNID\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "SYSCONNID") + "\","
							+ "\"CHANNELNAME\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "DOCCHANNEL.NAME") + "\","
							+ "\"STATUS\":\"" + JsonHelper.getValueAsString((Map)list.get(i), "DOCSTATUS.NAME") + "\"},";
				}
			}
		}
        sResult += "]";
		return sResult;
	}	
	
	public String findFsByDocid(MethodContext _oMethodContext)throws Throwable{
		String sResult = "";
		String docid = _oMethodContext.getValue("Docid");
		String driver = ConfigServer.getServer().getSysConfigValue("ZKY_YZW_DRIVER", "0");
		String yzzsite = ConfigServer.getServer().getSysConfigValue("ZKY_YZWZZ_SITE", "0");
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT xwd.DOCUMENTSCORE FROM XWCMDOCSCORE xwd INNER JOIN WCMDOCUMENT wd ON xwd.DOCUMENTID = wd.DOCID WHERE wd.docoutupid = "+docid+" and siteid = " + yzzsite);
		try {
			conn = this.getConnection(driver);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if(resultSet != null){
				while(resultSet.next()){
					String fs = resultSet.getString("DOCUMENTSCORE") == null?"0.0":resultSet.getString("DOCUMENTSCORE");
					sResult = "{\"FS\":\"" + fs + "\"}";
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				this.closeConnection(resultSet, stmt, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sResult;
	}
	
	private Connection getConnection(String driver)throws Exception{
		String driverClassName = driver.split(",")[0];
		Class.forName(driverClassName);
		String url = driver.split(",")[1];
		String username = driver.split(",")[2];
		String password = driver.split(",")[3];
		return DriverManager.getConnection(url,username,password);
	}
	
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
	
	private String querySitesByIp(String ip,String nViewId)throws WCMException{
		String result = "";
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId",nViewId);
		parameters.put("_sqlWhere_", "wcmmetatablezdj.SERVER_IP = '"+ip+"' ");
		parameters.put("PageSize", 2000);
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metaViewData = (MetaViewData) metadatas.getAt(i);
				if(metaViewData == null)continue;
				result += metaViewData.getPropertyAsString("SITEID") + ",";
			}
		}
		if(!"".equals(result)){
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	/*
	 * 推送附件
	 */
	private String pushFileAfterDocument(Appendixes appendixes,int type,String DocId,
			String ip) throws WCMException {
		String finalName = "";
		String sServiceId = "wcm6_document";
		String sMethodName = "saveAppendixes";
		String rootPath = ConfigServer.getServer().getSysConfigValue("WCMData",
				"0");
		// rootPath = rootPath.replaceAll("\\\\","");
		String typePath = "";
		StringBuilder postData = new StringBuilder("<OBJECTS>");
		Map oPostData = new HashMap();
		for (int i = 0; i < appendixes.size(); i++) {
			Appendix appendix = (Appendix) appendixes.getAt(i);
			String oldFile = appendix.getFile();
			String suffixes = oldFile.substring(oldFile.lastIndexOf(".") + 1,
					oldFile.length());
			String fileType = "";
			if ("jpg".equals(suffixes) || "gif".equals(suffixes)
					|| "png".equals(suffixes) || "bmp".equals(suffixes)) {
				typePath = "webpic";
			} else {
				typePath = "protect";
			}
			String firstPath = oldFile.substring(0, 8);
			String secondPath = oldFile.substring(0, 10);

			Dispatch oDispatch = WCMServiceCaller.UploadFiles(rootPath + "/"
					+ typePath + "/" + firstPath + "/" + secondPath + "/"
					+ oldFile, ip);
			String fileName = oDispatch.getUploadShowName();
			if (i == appendixes.size() - 1) {
				finalName += fileName;
			} else {
				finalName += fileName + ",";
			}
			postData.append("<OBJECT ID='0' APPFILE='"
					+ oDispatch.getUploadShowName()
					+ "' APPLINKALT='' APPFLAG='"+type+"' APPDESC='"
					+ appendix.getDesc() + "' SRCFILE='"+appendix.getDesc()+"'/>");
		}
		postData.append("</OBJECTS>");
		oPostData.put("DocId", Integer.valueOf(DocId));
		oPostData.put("AppendixType", type);
		oPostData.put("APPENDIXESXML", postData.toString());
		WCMServiceCaller.Calls(sServiceId, sMethodName, oPostData, true, ip);
		return finalName;
	}

	/*
	 * 推送文件类型文档附件
	 */
	private String pushFileBeforeDocument(String oldFile, String ip)
			throws WCMException {
		String rootPath = ConfigServer.getServer().getSysConfigValue("WCMData",
				"0");
		// rootPath = rootPath.replaceAll("\\\\","");
		String typePath = "";
		String suffixes = oldFile.substring(oldFile.lastIndexOf(".") + 1,
				oldFile.length());
		if ("jpg".equals(suffixes) || "gif".equals(suffixes)
				|| "png".equals(suffixes) || "bmp".equals(suffixes)) {
			typePath = "webpic";
		} else {
			typePath = "protect";
		}
		String firstPath = oldFile.substring(0, 8);
		String secondPath = oldFile.substring(0, 10);
		Dispatch oDispatch = WCMServiceCaller.UploadFiles(
				rootPath + "/" + typePath + "/" + firstPath + "/" + secondPath
						+ "/" + oldFile, ip);
		String appfile = oDispatch.getUploadShowName();
		return appfile;
	}
	
	/*
	 * 推送链接型文档
	 * */
	private void pushLinkAppendix(Appendixes appendixes, int type,String DocId,
			String ip)throws WCMException{
		String sServiceId = "wcm6_document";
		String sMethodName = "saveAppendixes";
		StringBuilder postData = new StringBuilder("<OBJECTS>");
		Map oPostData = new HashMap();
		for (int i = 0; i < appendixes.size(); i++) {
			Appendix appendix = (Appendix) appendixes.getAt(i);
			String oldFile = appendix.getFile();
			//Dispatch oDispatch = WCMServiceCaller.UploadFiles(oldFile, ip);
			postData.append("<OBJECT ID='0' APPFILE='"
					+ oldFile
					+ "' APPLINKALT='' APPFLAG='"+type+"' APPDESC='"
					+ appendix.getDesc() + "' SRCFILE='"+appendix.getDesc()+"'/>");
		}
		postData.append("</OBJECTS>");
		oPostData.put("DocId", Integer.valueOf(DocId));
		oPostData.put("AppendixType", type);
		oPostData.put("APPENDIXESXML", postData.toString());
		WCMServiceCaller.Calls(sServiceId, sMethodName, oPostData, true, ip);
	}
	
	/*
	 * 切换正文图片路径
	 * */
	private String cutContent(String oContent,String zzIp)throws Throwable{
		String newContent = "";
		if(oContent != null && !"".equals(oContent)){
			HashMap<String,String> picMap = new HashMap<String,String>();
			String img = "";  
	        Pattern p_image;  
	        Matcher m_image;  
	        List<String> pics = new ArrayList<String>();  
	        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";  
	        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);  
	        m_image = p_image.matcher(oContent);  
	        while (m_image.find()) {  
	            img = img + "," + m_image.group();  
	            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);  
	            while (m.find()) {  
	                String pic = m.group(1);  
	                picMap.put(pic, pic);
	            }  
	        } 
	        newContent = oContent;
	        Set keys = picMap.keySet();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = picMap.get(key);
				String newValue = this.getAllPath(value,zzIp);
				newContent = newContent.replaceAll(value, newValue);
			}
		}
		return newContent;
	}
	
	/*
	 * 更改IP
	 * */
	private String getAllPath(String pic,String ip)throws Throwable{
		String root = "http://"+ip+"/webpic/";
		String first = pic.substring(0, 8) + "/";
		String second = pic.substring(0, 10) + "/";
		String result = root + first + second + pic;
		return result;
	}
	
	
	private String getStringByMetaViewDatas(MetaViewDatas metadatas)throws Throwable{
		String result = "[";
		if(metadatas != null && metadatas.size() != 0){
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metaViewData = (MetaViewData)metadatas.getAt(i);
				if(i == metadatas.size() - 1){
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SERVER_IP\":\""+metaViewData.getPropertyAsString("SERVER_IP") + "\",\"SERVER_NAME\":\"" + metaViewData.getPropertyAsString("SERVER_NAME") + "\",\"SERVER_PORT\":\"" + metaViewData.getPropertyAsString("SERVER_PORT") + "\",\"USER_NAME\":\"" + metaViewData.getPropertyAsString("USER_NAME") + "\",\"PASS_WORD\":\"" + metaViewData.getPropertyAsString("PASS_WORD") + "\",\"RECID\":\"" + this.findRecIdByMetaViewData(metaViewData) + "\",\"SYSCONNID\":\"" + metaViewData.getPropertyAsString("SYSCONNID") + "\"}";
				}else{
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SERVER_IP\":\""+metaViewData.getPropertyAsString("SERVER_IP") + "\",\"SERVER_NAME\":\"" + metaViewData.getPropertyAsString("SERVER_NAME") + "\",\"SERVER_PORT\":\"" + metaViewData.getPropertyAsString("SERVER_PORT") + "\",\"USER_NAME\":\"" + metaViewData.getPropertyAsString("USER_NAME") + "\",\"PASS_WORD\":\"" + metaViewData.getPropertyAsString("PASS_WORD") + "\",\"RECID\":\"" + this.findRecIdByMetaViewData(metaViewData) + "\",\"SYSCONNID\":\"" + metaViewData.getPropertyAsString("SYSCONNID") + "\"},";
				}
			}
		}
		result += "]";
		return result;
	}
	
	private String getStringByMetaViewDatas2(MetaViewDatas metadatas)throws Throwable{
		String result = "[";
		if(metadatas != null && metadatas.size() != 0){
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metaViewData = (MetaViewData)metadatas.getAt(i);
				if(i == metadatas.size() - 1){
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SRC_SERVER_IP\":\""+metaViewData.getPropertyAsString("SRC_SERVER_IP") + "\",\"TARGET_SERVER_IP\":\"" + metaViewData.getPropertyAsString("TARGET_SERVER_IP") + "\",\"TARGET_SERVER_NAME\":\"" + metaViewData.getPropertyAsString("TARGET_SERVER_NAME") + "\",\"TARGET_SERVER_PORT\":\"" + metaViewData.getPropertyAsString("TARGET_SERVER_PORT") + "\",\"JOIN_USERNAME\":\"" + metaViewData.getPropertyAsString("JOIN_USERNAME") + "\",\"JOIN_PASSWORD\":\"" + metaViewData.getPropertyAsString("JOIN_PASSWORD") + "\",\"JOIN_STATUS\":\"" + metaViewData.getPropertyAsString("JOIN_STATUS") + "\",\"RECID\":\"" + this.findRecIdByMetaViewData(metaViewData) + "\"}";
				}else{
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SRC_SERVER_IP\":\""+metaViewData.getPropertyAsString("SRC_SERVER_IP") + "\",\"TARGET_SERVER_IP\":\"" + metaViewData.getPropertyAsString("TARGET_SERVER_IP") + "\",\"TARGET_SERVER_NAME\":\"" + metaViewData.getPropertyAsString("TARGET_SERVER_NAME") + "\",\"TARGET_SERVER_PORT\":\"" + metaViewData.getPropertyAsString("TARGET_SERVER_PORT") + "\",\"JOIN_USERNAME\":\"" + metaViewData.getPropertyAsString("JOIN_USERNAME") + "\",\"JOIN_PASSWORD\":\"" + metaViewData.getPropertyAsString("JOIN_PASSWORD") + "\",\"JOIN_STATUS\":\"" + metaViewData.getPropertyAsString("JOIN_STATUS") + "\",\"RECID\":\"" + this.findRecIdByMetaViewData(metaViewData) + "\"},";
				}
			}
		}
		result += "]";
		return result;
	}
	
	private String getStringByMetaViewDatas3(MetaViewDatas metadatas)throws Throwable{
		String result = "[";
		if(metadatas != null && metadatas.size() != 0){
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metaViewData = (MetaViewData)metadatas.getAt(i);
				if(i == metadatas.size() - 1){
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SRC_IP\":\""+metaViewData.getPropertyAsString("SRC_IP") + "\",\"SRC_LMID\":\"" + metaViewData.getPropertyAsString("SRC_LMID") + "\",\"SRC_LMMC\":\"" + metaViewData.getPropertyAsString("SRC_LMMC") + "\",\"TARGET_IP\":\"" + metaViewData.getPropertyAsString("TARGET_IP") + "\",\"TARGET_LMID\":\"" + metaViewData.getPropertyAsString("TARGET_LMID") + "\",\"TARGET_LMMC\":\"" + metaViewData.getPropertyAsString("TARGET_LMMC") + "\",\"RECID\":\"" + this.findRecIdByMetaViewData(metaViewData) + "\"}";
				}else{
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SRC_IP\":\""+metaViewData.getPropertyAsString("SRC_IP") + "\",\"SRC_LMID\":\"" + metaViewData.getPropertyAsString("SRC_LMID") + "\",\"SRC_LMMC\":\"" + metaViewData.getPropertyAsString("SRC_LMMC") + "\",\"TARGET_IP\":\"" + metaViewData.getPropertyAsString("TARGET_IP") + "\",\"TARGET_LMID\":\"" + metaViewData.getPropertyAsString("TARGET_LMID") + "\",\"TARGET_LMMC\":\"" + metaViewData.getPropertyAsString("TARGET_LMMC") + "\",\"RECID\":\"" + this.findRecIdByMetaViewData(metaViewData) + "\"},";
				}
			}
		}
		result += "]";
		return result;
	}
	
	private String getStringByMetaViewDatas4(MetaViewDatas metadatas)throws Throwable{
		String result = "[";
		if(metadatas != null && metadatas.size() != 0){
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metaViewData = (MetaViewData)metadatas.getAt(i);
				if(i == metadatas.size() - 1){
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SYSCONNID\":\""+metaViewData.getPropertyAsString("SYSCONNID") + "\",\"SITEID\":\"" + metaViewData.getPropertyAsString("SITEID") + "\",\"SITENAME\":\"" + metaViewData.getPropertyAsString("SITENAME") + "\"}";
				}else{
					result += "{\"METADATAID\":\"" + metaViewData.getMetaDataId() + "\",\"SYSCONNID\":\""+metaViewData.getPropertyAsString("SYSCONNID") + "\",\"SITEID\":\"" + metaViewData.getPropertyAsString("SITEID") + "\",\"SITENAME\":\"" + metaViewData.getPropertyAsString("SITENAME") + "\"},";
				}
			}
		}
		result += "]";
		return result;
	}
	
	private String getStringByJson(Map json)throws Throwable{
		String result = "[";
		result += "{\"DOCID\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.DOCID") + "\",\"DOCTITLE\":\""+JsonHelper.getValueAsString(json, "DOCUMENT.DOCTITLE") + "\",\"CRUSER\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.CRUSER") + "\",\"CRTIME\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.CRTIME")+ "\",\"SITE\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.SITEID") + "\",\"CHANNEL\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.DOCCHANNEL.NAME")+ "\",\"STATUS\":\"" + JsonHelper.getValueAsString(json, "DOCUMENT.DOCSTATUS.NAME") + "\"}";
		result += "]";
		return result;
	}
	
	private int findRecIdByMetaViewData(MetaViewData metadata)throws Throwable{
		Document document = Document.findById(metadata.getMetaDataId());
		ChnlDoc chnlDoc = ChnlDoc.findByDocument(document);
		return chnlDoc.getId();
	}
	
}
