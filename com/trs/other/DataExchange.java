package com.trs.other;

import java.math.BigDecimal;
import java.sql.Connection;
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

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.wcm.content.domain.AppendixMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.components.wcm.content.persistent.WebSite;
import com.trs.components.wcm.content.persistent.WebSites;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.config.ConfigServer;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;
import com.trs.webframework.controler.JSPRequestProcessor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DataExchange {

	/*
	 * 查询所有服务器 
	 */
	public ArrayList<HashMap<String, String>> queryServers(String ip_port,
			String viewId) throws WCMException {
		ArrayList<HashMap<String, String>> servers = new ArrayList<HashMap<String, String>>();
		String sServiceId = "custom_mycustom";
		String sMethodName = "queryServers";
		Map oPostData = new HashMap();
		oPostData.put("ViewId", new Integer(viewId));
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		if (json != null && !"".equals(json)) {
			JSONArray jsonArray = JSONArray.fromObject(json);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("METADATAID", object.getString("METADATAID"));
				hashMap.put("RECID", object.getString("RECID"));
				hashMap.put("SERVER_IP", object.getString("SERVER_IP"));
				hashMap.put("SERVER_NAME", object.getString("SERVER_NAME"));
				hashMap.put("SERVER_PORT", object.getString("SERVER_PORT"));
				hashMap.put("USER_NAME", object.getString("USER_NAME"));
				hashMap.put("PASS_WORD", object.getString("PASS_WORD"));
				hashMap.put("SYSCONNID", object.getString("SYSCONNID"));
				servers.add(hashMap);
			}
		}
		return servers;
	}

	/*
	 * 查询当前IP所有服务器映射
	 */
	public ArrayList<HashMap<String, String>> queryMappingByMyIp(
			String ip_port, String viewId, String myIp) throws WCMException {
		ArrayList<HashMap<String, String>> mappings = new ArrayList<HashMap<String, String>>();
		String sServiceId = "custom_mycustom";
		String sMethodName = "queryMappingByIp";
		Map oPostData = new HashMap();
		oPostData.put("ViewId", viewId);
		oPostData.put("ArgIp", myIp);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		if (json != null && !"".equals(json)) {
			JSONArray jsonArray = JSONArray.fromObject(json);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("METADATAID", object.getString("METADATAID"));
				hashMap.put("RECID", object.getString("RECID"));
				hashMap.put("SRC_SERVER_IP", object.getString("SRC_SERVER_IP"));
				hashMap.put("TARGET_SERVER_IP",
						object.getString("TARGET_SERVER_IP"));
				hashMap.put("TARGET_SERVER_NAME",
						object.getString("TARGET_SERVER_NAME"));
				hashMap.put("TARGET_SERVER_PORT",
						object.getString("TARGET_SERVER_PORT"));
				hashMap.put("JOIN_USERNAME", object.getString("JOIN_USERNAME"));
				hashMap.put("JOIN_PASSWORD", object.getString("JOIN_PASSWORD"));
				hashMap.put("JOIN_STATUS", object.getString("JOIN_STATUS"));
				mappings.add(hashMap);
			}
		}
		return mappings;
	}
	
	/*
	 * 检验服务连接是否存在
	 * */
	public boolean checkExist(String ip_port,HashMap<String, Object> oPostData)throws WCMException{
		boolean bool = true;
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId", oPostData.get("ViewId"));
		parameters.put("_sqlWhere_", "wcmmetatableysj.SRC_SERVER_IP = '"+oPostData.get("SRC_SERVER_IP")+"' AND wcmmetatableysj.TARGET_SERVER_IP = '"+oPostData.get("TARGET_SERVER_IP")+"'");
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				parameters, true, ip_port);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "METAVIEWDATAS.METAVIEWDATA");
		if(list != null){
			bool = false;
		}
		return bool;
	}
	
	/*
	 * 检验站点配置是否存在
	 * */
	public boolean checkExists(String ip_port,HashMap<String, Object> oPostData)throws WCMException{
		boolean bool = true;
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId", oPostData.get("ViewId"));
		parameters.put("_sqlWhere_", "wcmmetatablelmysj.SRC_IP = '"+oPostData.get("SRC_IP")+"' AND wcmmetatablelmysj.TARGET_IP = '"+oPostData.get("TARGET_IP")+"' AND wcmmetatablelmysj.SRC_LMID = '"+oPostData.get("SRC_LMID")+"' AND wcmmetatablelmysj.TARGET_LMID = '"+oPostData.get("TARGET_LMID")+"'");
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				parameters, true, ip_port);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "METAVIEWDATAS.METAVIEWDATA");
		if(list != null){
			bool = false;
		}
		return bool;
	}

	/*
	 * 保存服务器映射
	 */
	public String saveServerMapping(String ip_port,
			HashMap<String, Object> oPostData) throws WCMException {
		String result = "";
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "savemetaviewdata";
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		if (oDispatch != null) {
			result = "ok";
		}
		return result;
	}

	/*
	 * 删除映射(服务器、站点)
	 */
	public String delMapping(String ip_port, HashMap<String, Object> oPostData)
			throws WCMException {
		String result = "";
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "deleteViewDatas";
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		if (oDispatch != null) {
			result = "ok";
		}
		return result;
	}

	/*
	 * 查询本服务器所有站点
	 */
	public String queryMyTree(String searchArg) throws WCMException {
		String resultTree = "";
		ContextHelper.initContext(User.findByName("admin"));
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_website";
		String sMethodName = "query";
		HashMap parameters = new HashMap();
		parameters.put("SiteType", 0);
		if(!"".equals(searchArg)){
			parameters.put("QuerySiteName", searchArg);
		}
		parameters.put("PageSize", 2000);
		parameters.put("OrderBy", "SITEORDER DESC");
		WebSites sites = (WebSites) processor.excute(sServiceId, sMethodName,
				parameters);
		if (sites != null && sites.size() != 0) {
			ArrayList<HashMap<String, Object>> treeList = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> parentMap = new HashMap<String, Object>();
			parentMap.put("id", 1);
			parentMap.put("name", "所有站点");
			parentMap.put("pid", 0);
			treeList.add(parentMap);
			for (int i = 0; i < sites.size(); i++) {
				WebSite webSite = (WebSite) sites.getAt(i);
				if (webSite == null)
					continue;
				HashMap<String, Object> treeMap = new HashMap<String, Object>();
				treeMap.put("id", webSite.getId());
				treeMap.put("name", webSite.getName());
				treeMap.put("pid", 1);
				treeList.add(treeMap);
			}
			resultTree = JSONArray.fromObject(treeList).toString();
		}

		return resultTree;
	}

	/*
	 * 查询目标服务器所有站点
	 */
	public String queryTargetTree(String targetIp, String ip_port,String searchArg)
			throws WCMException {
		String sServiceId = "custom_mycustom";
		String sMethodName = "queryTargetTree";
		Map oPostData = new HashMap();
		oPostData.put("TargetIp", targetIp);
		if(!"".equals(searchArg)){
			oPostData.put("SearchArg", searchArg);
		}
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		return json;
	}

	/*
	 * 查询站点映射集(列表)
	 */
	public ArrayList<HashMap<String, String>> queryMappingList(String ip_port,
			String viewId, String srcip, String myCid, String tarIp)
			throws WCMException {
		ArrayList<HashMap<String, String>> mappings = new ArrayList<HashMap<String, String>>();
		String sServiceId = "custom_mycustom";
		String sMethodName = "queryChannelMappingByIp";
		Map oPostData = new HashMap();
		oPostData.put("ViewId", viewId);
		oPostData.put("ArgIp", srcip);
		if (myCid != null && !"".equals(myCid)) {
			oPostData.put("ArgCid", myCid);
		}
		if (tarIp != null && !"".equals(tarIp)) {
			oPostData.put("ArgTarIp", tarIp);
		}
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		if (json != null && !"".equals(json)) {
			JSONArray jsonArray = JSONArray.fromObject(json);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("METADATAID", object.getString("METADATAID"));
				hashMap.put("RECID", object.getString("RECID"));
				hashMap.put("SRC_IP", object.getString("SRC_IP"));
				hashMap.put("SRC_LMID", object.getString("SRC_LMID"));
				hashMap.put("SRC_LMMC", object.getString("SRC_LMMC"));
				hashMap.put("TARGET_IP", object.getString("TARGET_IP"));
				hashMap.put("TARGET_LMID", object.getString("TARGET_LMID"));
				hashMap.put("TARGET_LMMC", object.getString("TARGET_LMMC"));
				mappings.add(hashMap);
			}
		}
		return mappings;
	}

	public String queryTreeForPush(String ip_port, String viewId, String srcip,
			String myCid, String tarIp) throws WCMException {
		String resultTree = "";
		ArrayList<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
		// 1.查询站点映射集
		ArrayList<HashMap<String, String>> siteMappings = this
				.queryMappingLists(ip_port, viewId, srcip, myCid,
						tarIp.split(":")[0]);
		// 2.查询目标栏目集
		String sServiceId = "custom_mycustom";
		String sMethodName = "queryTargetChannelsBySiteId";
		ArrayList<HashMap<String, String>> channels = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < siteMappings.size(); i++) {
			HashMap<String, String> map = siteMappings.get(i);
			Map oPostData = new HashMap();
			oPostData.put("TargetIp", tarIp);
			oPostData.put("TargetSiteId", map.get("SRC_LMID"));

			HashMap<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("id", map.get("SRC_LMID"));
			resultMap.put("name", map.get("SRC_LMMC"));
			resultMap.put("pid", "0");
			resultList.add(resultMap);

			Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId,
					sMethodName, oPostData, true, ip_port);
			String json = oDispatch.getResponseText();
			if (json != null && !"".equals(json)) {
				JSONArray jsonArray = JSONArray.fromObject(json);
				for (int j = 0; j < jsonArray.size(); j++) {
					JSONObject object = jsonArray.getJSONObject(j);
					HashMap<String, String> resultMap2 = new HashMap<String, String>();
					resultMap2.put("id", object.getString("CHANNELID"));
					resultMap2.put("name", object.getString("CHANNELNAME"));
					resultMap2.put("pid", object.getString("SITEID"));
					resultList.add(resultMap2);
				}
			}
		}
		resultTree = JSONArray.fromObject(resultList).toString();
		return resultTree;
	}

	/*
	 * 查询站点映射集(推送)
	 */
	public ArrayList<HashMap<String, String>> queryMappingLists(String ip_port,
			String viewId, String srcip, String myCid, String tarIp)
			throws WCMException {
		ArrayList<HashMap<String, String>> mappings = new ArrayList<HashMap<String, String>>();
		String sServiceId = "custom_mycustom";
		String sMethodName = "queryChannelMappingByIps";
		Map oPostData = new HashMap();
		oPostData.put("ViewId", viewId);
		oPostData.put("ArgIp", srcip);
		if (myCid != null && !"".equals(myCid)) {
			oPostData.put("ArgCid", myCid);
		}
		if (tarIp != null && !"".equals(tarIp)) {
			oPostData.put("ArgTarIp", tarIp);
		}
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		if (json != null && !"".equals(json)) {
			JSONArray jsonArray = JSONArray.fromObject(json);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("METADATAID", object.getString("METADATAID"));
				hashMap.put("RECID", object.getString("RECID"));
				hashMap.put("SRC_IP", object.getString("SRC_IP"));
				hashMap.put("SRC_LMID", object.getString("SRC_LMID"));
				hashMap.put("SRC_LMMC", object.getString("SRC_LMMC"));
				hashMap.put("TARGET_IP", object.getString("TARGET_IP"));
				hashMap.put("TARGET_LMID", object.getString("TARGET_LMID"));
				hashMap.put("TARGET_LMMC", object.getString("TARGET_LMMC"));
				mappings.add(hashMap);
			}
		}
		return mappings;
	}

	/*
	 * 向中转服务器推送文档
	 */
	public void exchangeData(String sourceDataId, String zzChannelId,
			String zzIp_port, String targetChannelId, String targetIp,String srcIp,String curruser)
			throws WCMException {
		String viewId = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_VIEWIDS", "0").split(",")[0].split(":")[1];
		String sServiceId = "wcm6_viewdocument";
		String sMethodName = "save";
		String finalId = "";
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String[] dataIdsArr = sourceDataId.split(",");
		Dispatch oDispatch = null;
		AppendixMgr mgr = new AppendixMgr();
		for (int i = 0; i < dataIdsArr.length; i++) {
			Document document = Document.findById(Integer
					.parseInt(dataIdsArr[i]));
			if (document == null)
				continue;
			HashMap<String, Object> oPostData = new HashMap<String, Object>();
			oPostData.put("ObjectId", 0);
			oPostData.put("ChannelId", Integer.parseInt(zzChannelId));
			oPostData.put("CRTIME", format.format(new Date()));
			oPostData.put("CRUSER", curruser);
			oPostData.put("DOCTITLE", document.getTitle());
			oPostData.put("DOCSTATUS", 1);
			oPostData.put("DOCTYPE", document.getType());
			
			oPostData.put("SYSCONNID", Integer.parseInt(this.getSysconnidByIp(zzIp_port, viewId, srcIp)));
			oPostData.put("SRCSITEID", document.getChannel().getSiteId());
			oPostData.put("DOCOUTUPID", document.getId());
			
			oPostData.put("SUBDOCTITLE", document.getSubTitle() == null?"":document.getSubTitle());
			oPostData.put("DOCAUTHOR", document.getPropertyAsString("DOCAUTHOR") == null?"":document.getPropertyAsString("DOCAUTHOR"));
			oPostData.put("DOCPEOPLE", document.getPeople() == null?"":document.getPeople());
			oPostData.put("DOCKEYWORDS", document.getKeywords() == null?"":document.getKeywords());
			oPostData.put("DOCABSTRACT", document.getAbstract() == null?"":document.getAbstract());
			oPostData.put("DOCSOURCENAME", document.getPropertyAsString("DOCSOURCENAME") == null?"":document.getPropertyAsString("DOCSOURCENAME"));
			switch (document.getType()) {
			case 10:
				oPostData.put("DOCCONTENT", document.getContent());
				oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
						oPostData, true, zzIp_port);
				break;
			case 20:
				oPostData.put("DOCHTMLCON", this.cutContent(document.getHtmlContent(),srcIp));
				oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
						oPostData, true, zzIp_port);
				break;
			case 30:
				oPostData.put("DOCLINK",
						document.getPropertyAsString("DOCLINK"));
				oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
						oPostData, true, zzIp_port);
				break;
			case 40:
				String appfile = this.pushFileBeforeDocument(
						document.getPropertyAsString("DOCFILENAME"), zzIp_port);
				oPostData.put("DOCFILENAME", appfile);
				oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
						oPostData, true, zzIp_port);
				break;
			}
			Map json = oDispatch.getJson();
			String DocId = JsonHelper.getValueAsString(json, "RESULT");
			if(i == dataIdsArr.length - 1){
				finalId += DocId;
			}else{
				finalId += DocId + ",";
			}
			ContextHelper.initContext(User.findByName("admin"));
			Appendixes appendixes_file = mgr.getAppendixes(document, 10);
			Appendixes appendixes_jpg = mgr.getAppendixes(document, 20);
			Appendixes appendixes_link = mgr.getAppendixes(document, 40);
			if (appendixes_file.size() != 0) {
				this.pushFileAfterDocument(appendixes_file,10,DocId, zzIp_port);
			}
			if (appendixes_jpg.size() != 0) {
				this.pushFileAfterDocument(appendixes_jpg,20,DocId, zzIp_port);
			}
			if(appendixes_link.size() != 0){
				this.pushLinkAppendix(appendixes_link, 40, DocId, zzIp_port);
			}
		}
		String result = this.exchangeDataToTarget(zzIp_port,finalId, targetChannelId, targetIp);
		//this.synup(sourceDataId,result,targetIp);
	}

	/*
	 * 向目标服务器推送文档
	 */
	private String exchangeDataToTarget(String ip_port,String docId,String targetCid,String targetIp)throws WCMException{
		String sServiceId = "custom_mycustom";
		String sMethodName = "saveData";
		Map oPostData = new HashMap();
		oPostData.put("DataId", docId);
		oPostData.put("TargetCid", targetCid);
		oPostData.put("TargetIp", targetIp);
		oPostData.put("ZzIp", ip_port);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String result = oDispatch.getResponseText();
		return result;
	}
	
	/*
	 * 推送附件
	 */
	private String pushFileAfterDocument(Appendixes appendixes, int type,String DocId,
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
	 * 推送链接型附件
	 */
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
	
	private String getSysconnidByIp(String ip_port,String viewId,String srcIp)throws WCMException{
		String result = "";
		ArrayList<HashMap<String, String>> servers = this.queryServers(ip_port, viewId);
		for (int i = 0; i < servers.size(); i++) {
			HashMap<String, String> hashMap = servers.get(i);
			if(srcIp.split(":")[0].equals(hashMap.get("SERVER_IP"))){
				result = hashMap.get("SYSCONNID");
				return result;
			}
		}
		return result;
	}
	
	private String getIpBySysid(String ip_port,String viewId,int sysid)throws WCMException{
		String result = "";
		ArrayList<HashMap<String, String>> servers = this.queryServers(ip_port, viewId);
		for (int i = 0; i < servers.size(); i++) {
			HashMap<String, String> hashMap = servers.get(i);
			if(sysid == Integer.parseInt(hashMap.get("SYSCONNID"))){
				result = hashMap.get("SERVER_IP") + ":" + hashMap.get("SERVER_PORT");
				return result;
			}
		}
		return result;
	}
	
	private String cutContent(String oContent,String zzIp)throws WCMException{
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
	
	private String getAllPath(String pic,String ip)throws WCMException{
		String root = "http://"+ip+"/webpic/";
		String first = pic.substring(0, 8) + "/";
		String second = pic.substring(0, 10) + "/";
		String result = root + first + second + pic;
		return result;
	}
	
	public String findServerName(String ip_port,String view_id,int arg)throws WCMException{
		String result = "";
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId", view_id);
		parameters.put("_sqlWhere_", "wcmmetatablefwj.SYSCONNID = '" + arg + "' ");
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				parameters, true, ip_port);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "METAVIEWDATAS.METAVIEWDATA");
		if(list != null && list.size() != 0){
			result = JsonHelper.getValueAsString((Map)list.get(0), "SERVER_NAME");
		}
		return result;
	}
	
	public String findServerIpPort(String ip_port,String view_id,int arg)throws WCMException{
		String result = "";
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId", view_id);
		parameters.put("_sqlWhere_", "wcmmetatablefwj.SYSCONNID = '" + arg + "' ");
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				parameters, true, ip_port);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "METAVIEWDATAS.METAVIEWDATA");
		if(list != null && list.size() != 0){
			result = JsonHelper.getValueAsString((Map)list.get(0), "SERVER_IP") + ":" + JsonHelper.getValueAsString((Map)list.get(0), "SERVER_PORT");
		}
		return result;
	}
	
	public String findSiteName(String ip_port,String view_id,int sysconnid,int siteid)throws WCMException{
		String result = "";
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId", view_id);
		parameters.put("_sqlWhere_", "wcmmetatablezdj.SYSCONNID = "+sysconnid+" and wcmmetatablezdj.SITEID = "+siteid+" ");
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				parameters, true, ip_port);
		Map json = oDispatch.getJson();
		List list = JsonHelper.getList(json, "METAVIEWDATAS.METAVIEWDATA");
		if(list != null && list.size() != 0){
			result = JsonHelper.getValueAsString((Map)list.get(0), "SITENAME");
		}
		return result;
	}
	
	public String findSitesBySysId(String ip_port,String view_id,String sysconnid)throws WCMException{
		String sServiceId = "custom_mycustom";
		String sMethodName = "querySitesBySysId";
		HashMap parameters = new HashMap();
		parameters.put("ViewId", view_id);
		parameters.put("Sysconnid", sysconnid);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				parameters, true, ip_port);
		String result = oDispatch.getResponseText();
		return result;
	}
	
	/*
	 * 同步更新推送记录
	 * */
	private void synup(String srcDocId,String resDocId,String targetIp)throws WCMException{
		String[] srcs = srcDocId.split(",");
		String[] ress = resDocId.split(",");
		if(srcs.length == ress.length){
			DBManager db = null;
			Connection conn = null;
			Statement stmt = null;
			for (int i = 0; i < srcs.length; i++) {
				String src_docid = srcs[i];
				Document document = Document.findById(Integer.parseInt(src_docid));
				String docpushsource = document.getPropertyAsString("DOCPUSHSOURCE");
				if(docpushsource == null || "null".equals(docpushsource)){
					docpushsource = "";
				}
				docpushsource += this.getSysconnidByIp(ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_IPPORT", "0"), ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_VIEWIDS", "0").split(",")[0].split(":")[1], targetIp.split(":")[0]) + "_" + ress[i].split(":")[1] + "_" + ress[i].split(":")[2] + ",";
				String sql = "UPDATE WCMDOCUMENT SET DOCPUSHSOURCE = '" +docpushsource+ "' WHERE DOCID = " + src_docid;
				try {
					db = DBManager.getDBManager();
					conn = db.getConnection();
					stmt = conn.createStatement();
					stmt.executeUpdate(sql);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
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
	 * 查询被推送的
	 * */
	public HashMap<String,String> queryBts(int docoutupid,int srcsiteid,int sysid)throws WCMException{
		HashMap<String, String> hashMap = new HashMap<String, String>();
		String sServiceId = "custom_mycustom";
		String sMethodName = "findDocById";
		String ip_port = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_IPPORT", "0");
		String viewId = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_VIEWIDS", "0").split(",")[0].split(":")[1];
		Map oPostData = new HashMap();
		oPostData.put("Docid", docoutupid);
		oPostData.put("Siteid", srcsiteid);
		oPostData.put("TargetIp", this.getIpBySysid(ip_port, viewId, sysid));
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		if (json != null && !"".equals(json)) {
			JSONObject object = JSONObject.fromObject(json);
			hashMap.put("DOCID", object.getString("DOCID"));
			hashMap.put("CRUSER", object.getString("CRUSER"));
			hashMap.put("CRTIME", object.getString("CRTIME"));
			hashMap.put("SITEID", object.getString("SITEID"));
			hashMap.put("CHANNELNAME", object.getString("CHANNELNAME"));
			hashMap.put("STATUS", object.getString("STATUS"));
		}
		return hashMap;
	}
	
	/*
	 * 查询推送过的文档
	 * */
	
	public ArrayList<HashMap<String,String>> queryTsd(int docid,int siteid)throws WCMException{
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		String sServiceId = "custom_mycustom";
		String sMethodName = "findDocsById";
		String ip_port = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_IPPORT", "0");
		String viewId = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_VIEWIDS", "0").split(",")[0].split(":")[1];
		String site_viewid = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_VIEWIDS", "0").split(",")[3].split(":")[1];
		ArrayList<HashMap<String, String>> servers = this.queryServers(ip_port, viewId);
		for (int i = 0; i < servers.size(); i++) {
			HashMap<String, String> server = servers.get(i);
			String ip = server.get("SERVER_IP") + ":" + server.get("SERVER_PORT");
			String sname = server.get("SERVER_NAME");
			String sysid = server.get("SYSCONNID");
//if(!"127.0.0.1:8080".equals(ip))continue;
			Map oPostData = new HashMap();
			oPostData.put("Docid", docid);
			oPostData.put("Siteid", siteid);
			oPostData.put("TargetIp", ip);
			oPostData.put("SiteViewid", site_viewid);
			Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
					oPostData, true, ip_port);
			String json = oDispatch.getResponseText();
			if (json != null && !"[]".equals(json)) {
				JSONArray jsonArray = JSONArray.fromObject(json);
				for (int j = 0; j < jsonArray.size(); j++) {
					JSONObject object = jsonArray.getJSONObject(j);
					HashMap<String, String> hashMap = new HashMap<String, String>();
					hashMap.put("DOCID", object.getString("DOCID"));
					hashMap.put("CRUSER", object.getString("CRUSER"));
					hashMap.put("CRTIME", object.getString("CRTIME"));
					hashMap.put("SYSCONNID", sysid);
					hashMap.put("SERVER_NAME", sname);
					hashMap.put("TUISONG", object.getString("TUISONG"));
					hashMap.put("SITENAME", this.findSiteName(ip_port, site_viewid, Integer.parseInt(sysid), Integer.parseInt(object.getString("SITEID"))));
					hashMap.put("CHANNELNAME", object.getString("CHANNELNAME"));
					hashMap.put("STATUS", object.getString("STATUS"));
					list.add(hashMap);
				}
			}
		}
		return list;
	}
	
	public double getFsByDocId(int docid)throws WCMException{
		double fs = 0.0;
		String ip_port = ConfigServer.getServer().getSysConfigValue("ZKY_ZZFWQ_IPPORT", "0");
		String sServiceId = "custom_mycustom";
		String sMethodName = "findFsByDocid";
		Map oPostData = new HashMap();
		oPostData.put("Docid", docid);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, ip_port);
		String json = oDispatch.getResponseText();
		if (json != null && !"".equals(json)) {
			JSONObject object = JSONObject.fromObject(json);
			BigDecimal bd = new BigDecimal(object.getString("FS"));
			fs = bd.doubleValue();
		}
		return fs;
	}
}
