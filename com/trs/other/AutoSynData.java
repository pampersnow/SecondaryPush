package com.trs.other;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.wcm.content.domain.ChannelMgr;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.infra.common.WCMException;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;
import com.trs.webframework.controler.JSPRequestProcessor;

public class AutoSynData extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.trs.other.AutoSynData.class);
	
	public void start(String dbtype)throws WCMException{
		String jdbcDriver = "";
		if("mysql".equals(dbtype)){
			jdbcDriver = "com.mysql.jdbc.Driver,jdbc:mysql://127.0.0.1:3306/test,root,root";
		}else{
			jdbcDriver = "oracle.jdbc.driver.OracleDriver,jdbc:oracle:thin:@//192.168.20.212:1521/ORCL,trs,trs";
		}
		ArrayList<HashMap<String,String>> sjkg = this.querySjkgData(jdbcDriver);
		//ArrayList<HashMap<String,String>> sjkg = this.testSjkg();
		s_logger.info("获取数据开关集合：" + sjkg);
		ArrayList<HashMap<String,String>> zlbb = this.queryZlbbData(jdbcDriver);
		//ArrayList<HashMap<String,String>> zlbb = this.testZlbb();
		s_logger.info("获取质量报表集合：" + zlbb);
		ArrayList<HashMap<String,String>> tjbb = this.queryTjbbData(jdbcDriver);
		//ArrayList<HashMap<String,String>> tjbb = this.testTjbb();
		s_logger.info("获取统计报表集合：" + tjbb);
	}
	
	protected void execute() throws WCMException {
		String sSfqy = CMyString.showNull(getArgAsString("sfqy"), "0");
		if (!("1".equals(sSfqy))) {
			s_logger.info("定时同步授权数据 未启用该策略！");
			return;
		}
		
		s_logger.info("定时任务 执行开始" + new CMyDateTime().now().toString());
		
		String dbtype = CMyString.showNull(getArgAsString("dbType"), "");
		String jdbcDriver = "";
		if("mysql".equals(dbtype)){
			jdbcDriver = CMyString.showNull(getArgAsString("m_jdbcDriver"), "");
		}else{
			jdbcDriver = CMyString.showNull(getArgAsString("o_jdbcDriver"), "");
		}
		
		s_logger.info("获取到数据库驱动：" + jdbcDriver);
		
		ArrayList<HashMap<String,String>> sjkg = this.querySjkgData(jdbcDriver);
		//ArrayList<HashMap<String,String>> sjkg = this.testSjkg();
		s_logger.info("获取数据开关集合：" + sjkg.size());
		ArrayList<HashMap<String,String>> zlbb = this.queryZlbbData(jdbcDriver);
		//ArrayList<HashMap<String,String>> zlbb = this.testZlbb();
		s_logger.info("获取质量报表集合：" + zlbb.size());
		ArrayList<HashMap<String,String>> tjbb = this.queryTjbbData(jdbcDriver);
		//ArrayList<HashMap<String,String>> tjbb = this.testTjbb();
		s_logger.info("获取统计报表集合：" + tjbb.size());
		
		Channel tjbbChannel = Channel.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue("ZZB_TJBB_PID", "0")));
		Channel zlbbChannel = Channel.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue("ZZB_ZLBB_PID", "0")));
		Channel sjkgChannel = Channel.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue("ZZB_SJKG_PID", "0")));
		
		if(tjbb != null && tjbb.size() != 0){
			this.synTjbbData(tjbbChannel,tjbb);
		}
		if(zlbb != null && zlbb.size() != 0){
			this.synZlbbData(zlbbChannel,zlbb);
		}
		if(sjkg != null && sjkg.size() != 0){
			this.synSjkgData(sjkgChannel,sjkg);
		}
	}
	
	private void synTjbbData(Channel tjbbChannel,ArrayList<HashMap<String,String>> tjbb)throws WCMException{
		ArrayList childrens = (ArrayList) tjbbChannel.getAllChildren(User.findByName("admin"));
		if(tjbb.size() != 0 && childrens.size() != tjbb.size()){
			System.out.println("开始同步统计报表数据。。。");
			for (int i = 0; i < childrens.size(); i++) {
				Channel children = (Channel) childrens.get(i);
				System.out.println("即将删除栏目。。。"+children.getName());
				this.delChannel(children.getId());
			}
			for (int i = 0; i < tjbb.size(); i++) {
				HashMap<String,String> map = tjbb.get(i);
				System.out.println("即将保存栏目。。。"+map.get("REPROTNAME"));
				this.insertChannel(map.get("REPROTNAME"), map.get("REPROTURL"), tjbbChannel);
			}
		}
	}
	
	private void synZlbbData(Channel zlbbChannel,ArrayList<HashMap<String,String>> zlbb)throws WCMException{
		ArrayList childrens = (ArrayList)zlbbChannel.getAllChildren(User.findByName("admin"));
		if(zlbb.size() != 0 && childrens.size() != zlbb.size()){
			System.out.println("开始同步质量报表数据。。。");
			for (int i = 0; i < childrens.size(); i++) {
				Channel children = (Channel) childrens.get(i);
				System.out.println("即将删除栏目。。。"+children.getName());
				this.delChannel(children.getId());
			}
			for (int i = 0; i < zlbb.size(); i++) {
				HashMap<String,String> map = zlbb.get(i);
				System.out.println("即将保存栏目。。。"+map.get("REPORTNAME"));
				this.insertChannel(map.get("REPORTNAME"), map.get("REPORTURL"), zlbbChannel);
			}
		}
	}

	private void synSjkgData(Channel sjkgChannel,ArrayList<HashMap<String,String>> sjkg)throws WCMException{
		ArrayList childrens = (ArrayList)sjkgChannel.getAllChildren(User.findByName("admin"));
		if(sjkg.size() != 0 && childrens.size() != sjkg.size()){
			System.out.println("开始同步数据开关数据。。。");
			for (int i = 0; i < childrens.size(); i++) {
				Channel children = (Channel) childrens.get(i);
				System.out.println("即将删除栏目。。。"+children.getName());
				this.delChannel(children.getId());
			}
			for (int i = 0; i < sjkg.size(); i++) {
				HashMap<String,String> map = sjkg.get(i);
				System.out.println("即将保存栏目。。。"+map.get("DATESTR"));
				this.insertChannel(map.get("DATESTR"), map.get("DATESTR"), sjkgChannel);
			}
		}
	}
	
	private ArrayList<HashMap<String,String>> queryTjbbData(String driver)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		try {
			sql.append("SELECT UUID,REPORTNAME,REPORTURL FROM ZZB_REPORT.T_STATISTIC_DESC");
			conn = this.getConnection(driver);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if(resultSet != null){
				while(resultSet.next()){
					HashMap<String,String> map = new HashMap<String,String>();
					map.put("UUID", resultSet.getString("UUID") == null?"":resultSet.getString("UUID"));
					map.put("REPORTNAME", resultSet.getString("REPORTNAME") == null?"":resultSet.getString("REPORTNAME"));
					map.put("REPORTURL", resultSet.getString("REPORTURL") == null?"":resultSet.getString("REPORTURL"));
					//map.put("CREATE_DATE", resultSet.getString("CREATE_DATE") == null?"":resultSet.getString("Create_Date"));
					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				this.closeConnection(resultSet, stmt, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private ArrayList<HashMap<String,String>> queryZlbbData(String driver)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		try {
			sql.append("SELECT UUID,REPORTNAME,REPORTURL FROM ZZB_REPORT.T_QUALITY_DESC");
			conn = this.getConnection(driver);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if(resultSet != null){
				while(resultSet.next()){
					HashMap<String,String> map = new HashMap<String,String>();
					map.put("UUID", resultSet.getString("UUID") == null?"":resultSet.getString("UUID"));
					map.put("REPORTNAME", resultSet.getString("REPORTNAME") == null?"":resultSet.getString("REPORTNAME"));
					map.put("REPORTURL", resultSet.getString("REPORTURL") == null?"":resultSet.getString("REPORTURL"));
					//map.put("CREATE_DATE", resultSet.getString("CREATE_DATE") == null?"":resultSet.getString("Create_Date"));
					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				this.closeConnection(resultSet, stmt, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private ArrayList<HashMap<String,String>> querySjkgData(String driver)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		try {
			sql.append("SELECT UUID,DATESTR FROM ZZB_REPORT.T_DATE_LIST");
			conn = this.getConnection(driver);
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if(resultSet != null){
				while(resultSet.next()){
					HashMap<String,String> map = new HashMap<String,String>();
					map.put("UUID", resultSet.getString("UUID") == null?"":resultSet.getString("UUID"));
					map.put("DATESTR", resultSet.getString("DATESTR") == null?"":resultSet.getString("DATESTR"));
					//map.put("CREATE_DATE", resultSet.getString("CREATE_DATE") == null?"":resultSet.getString("CREATE_DATE"));
					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				this.closeConnection(resultSet, stmt, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private Connection getConnection(String driver)throws Exception{
		String[] arr = driver.split(",");
		String driverClassName = arr[0];
		Class.forName(driverClassName);
		String url = arr[1];
		String username = arr[2];
		String password = arr[3];
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
	
	private ArrayList<HashMap<String,String>> testTjbb()throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> map1 = new HashMap<String,String>();
		map1.put("UUID", "1");
		map1.put("ReportName", "表1");
		map1.put("ReportURL", "http://www.111.com/");
		result.add(map1);
		HashMap<String,String> map2 = new HashMap<String,String>();
		map2.put("UUID", "2");
		map2.put("ReportName", "表2");
		map2.put("ReportURL", "http://www.222.com/");
		result.add(map2);
		return result;
	}
	
	private ArrayList<HashMap<String,String>> testZlbb()throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> map1 = new HashMap<String,String>();
		map1.put("UUID", "1");
		map1.put("ReportName", "表11");
		map1.put("ReportURL", "http://www.111111.com/");
		result.add(map1);
		HashMap<String,String> map2 = new HashMap<String,String>();
		map2.put("UUID", "2");
		map2.put("ReportName", "表22");
		map2.put("ReportURL", "http://www.222222.com/");
		result.add(map2);
		return result;
	}
	
	private ArrayList<HashMap<String,String>> testSjkg()throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> map1 = new HashMap<String,String>();
		map1.put("UUID", "1");
		map1.put("DATESTR", "2018.03");
		result.add(map1);
		/*HashMap<String,String> map2 = new HashMap<String,String>();
		map2.put("UUID", "2");
		map2.put("DATESTR", "2018.05");
		result.add(map2);*/
		return result;
	}
	
	private void delChannel(int id)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		JSPRequestProcessor processor = new JSPRequestProcessor(null,null);
		String sServiceId = "wcm61_channel";
		String sMethodName = "delete";
		HashMap param = new HashMap();
		param.put("ObjectIds",id);
		param.put("Drop",true);
		processor.excute(sServiceId,sMethodName, param);
	}
	
	private void insertChannel(String cname,String clink,Channel parentChannel)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		JSPRequestProcessor processor = new JSPRequestProcessor(null,
				null);
		String sServiceId = "wcm61_channel";
		String sMethodName = "save";
		HashMap param = new HashMap();
		param.put("ObjectId",0);
		param.put("ChnlName",cname);
		param.put("ChnlDesc",cname);
		param.put("LinkUrl",clink);
		param.put("ParentId",parentChannel.getId());

		param.put("CHNLTYPE",0);
		param.put("CHNLPROP",4);
		param.put("ATTRIBUTE","OPENTYPE=_self&PUBLISHLIMIT=&PUBSTARTDATE=&LISTTYPE=0");
		param.put("CONTENTADDEDITPAGE","../document/document_addedit.jsp");
		param.put("CONTENTLISTPAGE","../document/document_list.html");
		param.put("CONTENTSHOWPAGE","../document/document_detail.jsp");
		param.put("USEDOCLEVEL","0");
		processor.excute(sServiceId,sMethodName, param);
	}
}
