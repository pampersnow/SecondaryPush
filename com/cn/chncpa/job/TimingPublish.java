package com.cn.chncpa.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.wcm.content.domain.DocumentMgr;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.components.wcm.resource.Status;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;

public class TimingPublish extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.cn.chncpa.job.TimingPublish.class);
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
		String fromChannel = CMyString.showNull(getArgAsString("fromChannel"), "0");
		s_logger.info("得到参数：要获取"+howtime+"分钟之前数据");
		
		ArrayList<HashMap<String,String>> result = null;
		try {
			result = this.queryToPublishData(Integer.parseInt(howtime),fromChannel);
			s_logger.info("获取到"+result.size()+"条符合条件的数据");
			if(result.size() != 0){
				this.publishData(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	private void publishData(ArrayList<HashMap<String,String>> result)throws Exception{
		ContextHelper.initContext(User.findByName("admin"));
		DocumentMgr dm = new DocumentMgr();
		for (int i = 0; i < result.size(); i++) {
			HashMap<String,String> map = result.get(i);
			String docid = map.get("DOCID");
			String chnlid = map.get("CHNLID");
			s_logger.info("发布文档，ID:"+docid);
			dm.changeStatus(Document.findById(Integer.parseInt(docid)), Channel.findById(Integer.parseInt(chnlid)), Status.findById(10));
		}
	}
	
	private ArrayList<HashMap<String,String>> queryToPublishData(int howtime,String fromChannel)throws Exception{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		String startTime = this.getTimeByMin(howtime);
		String endTime = this.getNowTime();
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT WD.DOCID,WC.RECID,WC.CHNLID FROM WCMDOCUMENT WD INNER JOIN WCMCHNLDOC WC ON WD.DOCID = WC.DOCID ");
		sql.append("WHERE WD.DOCCHANNEL IN ("+fromChannel+") AND WC.DOCSTATUS = 2 AND WC.MODAL = 1 AND WD.WH = '1' ");
		sql.append("AND WD.CRTIME BETWEEN TO_DATE('"+startTime+"','yyyy-mm-dd hh24:mi:ss') AND TO_DATE('"+endTime+"','yyyy-mm-dd hh24:mi:ss') ");
		//sql.append("AND WD.CRTIME BETWEEN '"+startTime+"' AND '"+endTime+"'");
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
					map.put("RECID", resultSet.getString("RECID") == null?"":resultSet.getString("RECID"));
					map.put("CHNLID", resultSet.getString("CHNLID") == null?"":resultSet.getString("CHNLID"));
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
}
