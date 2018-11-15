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
import com.trs.components.common.publish.PublishConstants;
import com.trs.components.common.publish.domain.PublishServer;
import com.trs.components.common.publish.persistent.element.IPublishContent;
import com.trs.components.common.publish.persistent.element.PublishElementFactory;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;

public class AutoPush extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.cn.chncpa.job.AutoPush.class);
	
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
		
		if("".equals(fromChannel)){
			return;
		}
		try {
			ArrayList<HashMap<String,String>> result = this.queryPushedData(Integer.parseInt(howtime),fromChannel);
			s_logger.info("获取到："+result.size()+"条数据");
			
			if(result.size() != 0){
				this.pushData(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void publishToWcm(String docid)throws Exception{
		ContextHelper.initContext(User.findByName("admin"));
		Document oDocument = Document.findById(Integer.parseInt(docid));
		s_logger.info("发布数据 docid："+docid);
		IPublishContent content = PublishElementFactory.makeContentFrom(oDocument, null);
		PublishServer publishServer = PublishServer.getInstance();
		publishServer.publishContent(content,PublishConstants.PUBLISH_CONTENT);
	}
	
	private void pushData(ArrayList<HashMap<String,String>> dataList)throws Exception{
		try {
			for (int i = 0; i < dataList.size(); i++) {
				HashMap<String,String> map = dataList.get(i);
				String docid = map.get("DOCID");
				//String recid = map.get("RECID");
				//String chnlid = map.get("CHNLID");
				this.publishToWcm(docid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<HashMap<String,String>> queryPushedData(int howtime,String fromChannel)throws Exception{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		String startTime = this.getTimeByMin(howtime);
		String endTime = this.getNowTime();
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT WD.DOCID,WCD.RECID,WCD.CHNLID FROM WCMDOCUMENT WD INNER JOIN WCMCHNLDOC WCD ON WD.DOCID = WCD.DOCID ");
		sql.append("WHERE WCD.CHNLID IN ("+fromChannel+") AND WD.ZZ = '1' AND WCD.DOCSTATUS = 2 ");
		//sql.append("AND WD.CRTIME BETWEEN '"+startTime+"' AND '"+endTime+"' ");
		sql.append("AND WD.CRTIME BETWEEN TO_DATE('"+startTime+"','yyyy-mm-dd hh24:mi:ss') AND TO_DATE('"+endTime+"','yyyy-mm-dd hh24:mi:ss') ");
		sql.append("ORDER BY WD.DOCID ");
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
	
	private String getTimeByMin(int time)throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long hm = time * 60000;
		long dqhm = new Date().getTime();
		return format.format(new Date(dqhm - hm));
	}
	
	private String getNowTime()throws WCMException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}
	
	public void start(String howtime,String fromChannel)throws Exception{
		ArrayList<HashMap<String,String>> result = this.queryPushedData(Integer.parseInt(howtime), fromChannel);
		this.pushData(result);
	}
}
