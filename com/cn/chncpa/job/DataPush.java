package com.cn.chncpa.job;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.User;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.wcm.content.domain.AppendixMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.config.Config;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.job.BaseStatefulJob;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;

public class DataPush extends BaseStatefulJob {

	private static Logger s_logger = Logger.getLogger(FlowAutoAdopt.class);

	public DataPush() {

	}

	protected void execute() throws WCMException {
		
		String RWMS = CMyString.showNull(getArgAsString("RWMS"), "定时任务");// 任务描述

		String sSfqy = CMyString.showNull(getArgAsString("sfqy"), "0");// 是否启用，0不启用
																		// 1启用

		if (!("1".equals(sSfqy))) {
			s_logger.info(RWMS + " 未启用该策略！");
			return;
		}

		s_logger.info(RWMS + " 执行开始" + new CMyDateTime().now().toString());
		
		/*获取当前时间*/
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String thistime = format.format(new Date()) + " 01:00:00";
		/*获取会议数据*/
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT WCMMETATABLEMEETING.MetaDataId,WCMMETATABLEMEETING.BM,WCMMETATABLEMEETING.END_TIME,WCMMETATABLEMEETING.GROUP_TYPE FROM WCMCHNLDOC inner join WCMMETATABLEMEETING on WCMCHNLDOC.DOCID = WCMMETATABLEMEETING.MetaDataId WHERE WCMCHNLDOC.DOCSTATUS IN (1,2) AND WCMMETATABLEMEETING.END_TIME <= TO_DATE('"+thistime+"','yyyy-mm-dd hh:mi:ss') AND WCMMETATABLEMEETING.SFTS = 0";
		StringBuilder ids = new StringBuilder();
		StringBuilder bm = new StringBuilder();
		StringBuilder dkz = new StringBuilder();
		ArrayList result = new ArrayList();
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql);
			if (resultSet != null) {
				while (resultSet.next()) {
					ids.append(resultSet.getString("MetaDataId") + ",");
					bm.append(resultSet.getString("BM") + ",");
					dkz.append(resultSet.getString("GROUP_TYPE") + ",");
				}
			}else{
				s_logger.info(RWMS + " 执行开始" + "：未发现有效数据--推送结束");
				return;
			}
			String[] id = ids.toString().split(",");
			String[] bms = bm.toString().split(",");
			String[] groups = dkz.toString().split(",");
			for(int i = 0;i < id.length;i++){
				result.addAll(this.getThroughData(id[i], bms[i], groups[i]));
			}
			/*更新数据*/
			this.updateMeetingData(ids.toString());
			/*推送数据*/
			this.dataMove(result);
		} catch (Exception e) {

		} finally {
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
	 * 根据会议信息获取文稿数据
	 * */
	public ArrayList getThroughData(String id,String bm,String group)throws Exception{
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		ArrayList result = new ArrayList();
		String sql = "";
		if("8".equals(bm)){
			sql = "SELECT WCMCHNLDOC.DOCSTATUS,WCMMETATABLEITU_T.MetaDataId,WCMMETATABLEITU_T.HYMC,WCMMETATABLEITU_T.WGMCZW,WCMMETATABLEITU_T.WGMCYW,WCMMETATABLEITU_T.TJZB,WCMMETATABLEITU_T.GGDW,WCMMETATABLEITU_T.WGNR FROM WCMCHNLDOC inner join WCMMETATABLEITU_T on WCMCHNLDOC.DOCID = WCMMETATABLEITU_T.MetaDataId WHERE WCMMETATABLEITU_T.CHANNELID = 74 AND WCMCHNLDOC.DOCSTATUS IN(16,18) AND WCMMETATABLEITU_T.HYMC = '"+id+"' AND WCMMETATABLEITU_T.TJZB = " + group;
		}else if("6".equals(bm)){
			sql = "SELECT WCMCHNLDOC.DOCSTATUS,WCMMETATABLEITU_D.MetaDataId,WCMMETATABLEITU_D.HYMC,WCMMETATABLEITU_D.WGMC,WCMMETATABLEITU_D.TJZB,WCMMETATABLEITU_D.GGDW,WCMMETATABLEITU_D.WGNR FROM WCMCHNLDOC inner join WCMMETATABLEITU_D on WCMCHNLDOC.DOCID = WCMMETATABLEITU_D.MetaDataId WHERE WCMMETATABLEITU_D.CHANNELID = 75 AND WCMCHNLDOC.DOCSTATUS IN(16,18) AND WCMMETATABLEITU_D.HYMC = '"+id+"' AND WCMMETATABLEITU_D.TJZB = "+group;
		}else{
			sql = "SELECT WCMCHNLDOC.DOCSTATUS,WCMMETATABLELSH.MetaDataId,WCMMETATABLELSH.HYMC,WCMMETATABLELSH.TAMC,WCMMETATABLELSH.TJZB,WCMMETATABLELSH.GGDW,WCMMETATABLELSH.TABJNRMDJYY FROM WCMCHNLDOC inner join WCMMETATABLELSH on WCMCHNLDOC.DOCID = WCMMETATABLELSH.MetaDataId WHERE WCMMETATABLELSH.CHANNELID = 88 AND WCMCHNLDOC.DOCSTATUS IN(16,18) AND WCMMETATABLELSH.HYMC = '"+id+"' AND WCMMETATABLELSH.TJZB = " + group;
		}
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql);
			if (resultSet != null) {
				while (resultSet.next()) {
					HashMap map = new HashMap();
					//ID
					map.put("ID",resultSet.getString("MetaDataId"));
					String hyid = resultSet.getString("HYMC");
					//会议名称
					map.put("HYMC",MetaViewData.findById(Integer.parseInt(hyid)).getPropertyAsString("MEETING_NAME"));	
					//会议日期
					map.put("HYRQ",MetaViewData.findById(Integer.parseInt(hyid)).getPropertyAsString("START_TIME"));
					//提交组别
					map.put("TJZB",resultSet.getString("TJZB"));
					if("8".equals(bm)){
						//中文文稿名称
						map.put("WGMCZW",resultSet.getString("WGMCZW"));	
						//英文文稿名称
						map.put("WGMCYW",resultSet.getString("WGMCYW") == null?"":resultSet.getString("WGMCYW"));	
						//文稿内容
						map.put("WGNR",resultSet.getString("WGNR"));
					}else if("6".equals(bm)){
						//中文文稿名称
						map.put("WGMC",resultSet.getString("WGMC"));
						//文稿内容
						map.put("WGNR",resultSet.getString("WGNR"));
					}else{
						map.put("TAMC", resultSet.getString("TAMC"));
						//提案内容
						map.put("TANR", resultSet.getString("TABJNRMDJYY"));
					}
					//供稿单位
					String ggdw = resultSet.getString("GGDW");
					ggdw = ggdw.split(";")[0];
					map.put("GGDW", ggdw);
					result.add(map);
				}
			}
		} catch (Exception e) {

		} finally {
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
	 * 调用数据迁移接口
	 * */
	public void dataMove(ArrayList list)throws Exception{
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		//String sServiceId = "wcm6_viewdocument";
		String sServiceId = "wcm6_document";
        String sMethodName = "save";
		//DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(list.size() != 0){
			for(int i = 0;i < list.size();i++){
			//for(int i = 0;i < 1;i++){
				Map oPostData = new HashMap();
				HashMap map = (HashMap)list.get(i);
				String bm = map.get("BM").toString();
				oPostData.put("ObjectId", new Integer(0));
				oPostData.put("DOCTYPE", new Integer(20));
				//推送栏目ID
				oPostData.put("ChannelId", this.getGroupMapping(map.get("TJZB").toString()));
		        //会议日期
		        oPostData.put("docreltime", map.get("HYRQ").toString());
		        //提交组别
		        oPostData.put("docclass", Group.findById(Integer.parseInt(map.get("TJZB").toString())).getName());
		        //供稿单位
		        oPostData.put("vchar1",map.get("GGDW").toString());
		        if("8".equals(bm)){
		        	//文稿中文名称
		        	oPostData.put("doctitle",map.get("WGMCZW").toString());
		        	//文稿中文名称
		        	oPostData.put("DOCPEOPLE",map.get("WGMCYW").toString());
		        	//文稿内容
		        	oPostData.put("dochtmlcon",map.get("WGNR").toString());
		        }else if("6".equals(bm)){
		        	//文稿名称
		        	oPostData.put("doctitle", map.get("WGMC").toString());
		        	//文稿内容
		        	oPostData.put("dochtmlcon", map.get("WGNR").toString());
		        }else{
		        	//文稿名称
		        	oPostData.put("doctitle", map.get("TAMC").toString());
		        	//文稿内容
		        	oPostData.put("dochtmlcon", map.get("TANR").toString());
		        }
		        //System.out.println("oPostData=====" + oPostData);
		        MetaViewData metaViewData = MetaViewData.findById(Integer.parseInt(map.get("ID").toString()));
		        AppendixMgr appendixMgr = new AppendixMgr();
		        Appendixes appendixes = appendixMgr.getAppendixes(metaViewData.getDocument(), null);
		        //推送
		        Dispatch dispatch = WCMServiceCaller.Call(sServiceId, sMethodName, oPostData,true);
		        //上传附件
		        if(appendixes.size() != 0){
	        		Map json = dispatch.getJson();
	            	String DocId = JsonHelper.getValueAsString(json, "RESULT");
	            	this.uploadAppendix(DocId,appendixes);
	        	}
			}
		}
	}
	
	/*推送附件*/
	public void uploadAppendix(String docid,Appendixes appendixes)throws Exception{
		String sServiceId = "wcm6_document";
		String sMethodName = "saveAppendixes";
		String srcPath = "/usr/local/TRS/TRSWCMV7/WCMData/protect/";
		File file = null;
		Dispatch oDispatch = null;
		for (int i = 0; i < appendixes.size(); i++) {
			Appendix app = (Appendix)appendixes.getAt(i);
			String filepath = app.getFile();
			String qz1 = filepath.substring(0, 8) + "/";
			String qz2 = filepath.substring(0, 10) + "/";
			String filename = srcPath + qz1 + qz2 + filepath;
			file = new File(filename);
			if(file.exists()){
				oDispatch = WCMServiceCaller.UploadFile(filename);
				Map oPostData = new HashMap();
				oPostData.put("DocId", Integer.valueOf(docid));
				String filetype = "10";
				String fixDesc = "附件" + (i + 1);
				oPostData.put("AppendixType", Integer.valueOf(filetype));
				String appfile = oDispatch.getUploadShowName();
				String postData = "<OBJECTS><OBJECT ID='0' APPFILE='"
		                + appfile
		                + "' APPLINKALT='[object Object]' APPFLAG='"+filetype+"' APPDESC='"+fixDesc+"'/></OBJECTS>";
				oPostData.put("APPENDIXESXML", postData);
				WCMServiceCaller.Call(sServiceId, sMethodName, oPostData,true);
			}else{
				continue;
			}
		}
	}

	/*
	 * 获取配置对口组对应关系
	 * */
	public String getGroupMapping(String group)throws Exception{
		String value = Config.findByKey(new Integer(254)).getValue();
		String result = "";
		String[] key_value = value.split(",");
		for(int i = 0;i < key_value.length;i++){
			String[] values = key_value[i].split("=");
			if(group.equals(values[0])){
				result = values[1];
			}
		}
		return result;
	}
	
	public void inits()throws Exception{
		/*获取当前时间*/
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String thistime = format.format(new Date()) + " 01:00:00";
		/*获取会议数据*/
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT WCMMETATABLEMEETING.MetaDataId,WCMMETATABLEMEETING.BM,WCMMETATABLEMEETING.END_TIME,WCMMETATABLEMEETING.GROUP_TYPE FROM WCMCHNLDOC inner join WCMMETATABLEMEETING on WCMCHNLDOC.DOCID = WCMMETATABLEMEETING.MetaDataId WHERE WCMCHNLDOC.DOCSTATUS IN (1,2) AND WCMMETATABLEMEETING.END_TIME <= TO_DATE('"+thistime+"','yyyy-mm-dd hh:mi:ss') AND WCMMETATABLEMEETING.SFTS = 0";
		StringBuilder ids = new StringBuilder();
		StringBuilder bm = new StringBuilder();
		StringBuilder dkz = new StringBuilder();
		ArrayList result = new ArrayList();
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql);
			if (resultSet != null) {
				while (resultSet.next()) {
					ids.append(resultSet.getString("MetaDataId") + ",");
					bm.append(resultSet.getString("BM") + ",");
					dkz.append(resultSet.getString("GROUP_TYPE") + ",");
				}
			}
			String[] id = ids.toString().split(",");
			String[] bms = bm.toString().split(",");
			String[] groups = dkz.toString().split(",");
			for(int i = 0;i < id.length;i++){
				result.addAll(this.getThroughData(id[i], bms[i], groups[i]));
			}
			/*更新会议信息*/
			this.updateMeetingData(ids.toString());
			/*推送数据*/
			this.dataMove(result);
		} catch (Exception e) {

		} finally {
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
	 * 更新会议信息状态
	 * */
	public void updateMeetingData(String id)throws Exception{
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		String[] ids = id.split(",");
		String sql = null;
		try {
			for (int i = 0; i < ids.length; i++) {
				if(!"".equals(ids[i]) && ids[i] != null){
					sql = "UPDATE WCMMETATABLEMEETING SET SFTS = 1 WHERE METADATAID = " + ids[i];
					db = DBManager.getDBManager();
					conn = db.getConnection();
					stmt = conn.createStatement();
					stmt.executeUpdate(sql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
		
	}
}
