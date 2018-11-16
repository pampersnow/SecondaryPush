package com.trs.other;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.center.MetaViewDatas;
import com.trs.infra.common.WCMException;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;
import com.trs.webframework.controler.JSPRequestProcessor;

public class SynUtil {

	private static HashMap<String,String> driverMap = new HashMap<String,String>();
	private static HashMap<String,String> IpMap = new HashMap<String,String>();
	static{
		driverMap.put("1", "oracle.jdbc.driver.OracleDriver,jdbc:oracle:thin:@//0.0.9.0:1521/db,root,1234");
		IpMap.put("1", "0.0.0.1");
	}
	/*
	 * 同步站点映射
	 * 1.可接收的服务标识
	 * 2.可推送的服务标识
	 * 3.保存映射数据的栏目ID
	 * */
	public void siteJoin_syn(int jsd_sysid,int tsd_sysid,int cid)throws WCMException{
		if(jsd_sysid == tsd_sysid){	//查询系统内
			ArrayList<HashMap<String,String>> xtn = this.queryXtn(jsd_sysid);
			if(xtn.size() != 0){
				//for循环   map集合   this.接口
				System.err.println(xtn.get(0));
				for (int i = 0; i < xtn.size(); i++) {
					HashMap<String, String> hashmap = new HashMap<String, String>();
					
					
					this.insertSiteJoin(hashmap, cid);
				}
			}
		}else{						//查询系统间
			ArrayList<HashMap<String,String>> xtj = this.queryXtj(jsd_sysid,tsd_sysid);
		}
	}
	
	private ArrayList<HashMap<String,String>> queryXtn(int sysid)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		try {
			sql.append("SELECT xw.ACCEPTSITEID,ww.SITENAME as ASITENAME,xw.PUSHSITEID,www.SITENAME as PSITENAME FROM XWCMSITEPUSH xw LEFT JOIN WCMWEBSITE ww ON xw.ACCEPTSITEID = ww.SITEID LEFT JOIN WCMWEBSITE www ON xw.PUSHSITEID = www.SITEID  order by xw.spid");
			conn = this.getConnection(driverMap.get(""+sysid));
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql.toString());
			if(resultSet != null){
				while(resultSet.next()){
					HashMap<String,String> data = new HashMap<String,String>();
					data.put("JSD_IP", IpMap.get(sysid+""));
					data.put("TSD_IP", IpMap.get(sysid+""));
					data.put("JSD_ID", resultSet.getString("ACCEPTSITEID") == null?"":resultSet.getString("ACCEPTSITEID"));
					data.put("TSD_ID", resultSet.getString("PUSHSITEID") == null?"":resultSet.getString("PUSHSITEID"));
					data.put("JSD_LMMC", resultSet.getString("ASITENAME") == null?"":resultSet.getString("ASITENAME"));
					data.put("TSD_LMMC", resultSet.getString("PSITENAME") == null?"":resultSet.getString("PSITENAME"));					
					result.add(data);
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
	
	
	
	private ArrayList<HashMap<String,String>> queryXtj(int jsd_sysid,int tsd_sysid)throws WCMException{
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
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
	/*
	 * 查询所有服务器集合
	 * */
	public ArrayList<HashMap<String,String>> queryServers(int viewId)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);
		String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "queryViewDatas";
		HashMap parameters = new HashMap();
		parameters.put("ViewId",viewId);
		parameters.put("PageSize", 2000);
		MetaViewDatas metadatas = (MetaViewDatas)processor.excute(sServiceId, sMethodName, parameters);
		if(metadatas != null && metadatas.size() != 0){
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metaViewData = (MetaViewData)metadatas.getAt(i);
				HashMap<String,String> map = new HashMap<String,String>();
				map.put("SERVER_IP", metaViewData.getPropertyAsString("SERVER_IP"));
				map.put("SERVER_NAME", metaViewData.getPropertyAsString("SERVER_NAME"));
				map.put("SERVER_PORT", metaViewData.getPropertyAsString("SERVER_PORT"));
				result.add(map);
			}
		}
		return result;
	}
	
	/*
	 * 同步服务之间连接
	 * 1.服务集合
	 * 2.服务连接的栏目ID
	 * */
	public void server_syn(ArrayList<HashMap<String,String>> sData,int cid)throws WCMException{
		for (int i = 0; i < sData.size(); i++) {
			HashMap<String,String> source = sData.get(i);
			for (int j = 0; j < sData.size(); j++) {
				HashMap<String,String> target = sData.get(j);
				this.insertServerJoin(source, target, cid);
			}
		}
	}
	/*
	 * 站点同步
	 * 1.系统标识(多个)
	 * 2.系统IP(多个)
	 * 3.站点连接栏目ID
	 * */
	public void site_syn(String sysid,String ip,int cid)throws WCMException{
		String[] sysids = sysid.split(",");
		String[] ips = ip.split(",");
		String sServiceId = "wcm6_website";
        String sMethodName = "query";
        Dispatch oDispatch = null;
		if(sysids.length == ips.length){
			for (int i = 0; i < ips.length; i++) {
				HashMap<String,Object> oPostData = new HashMap<String,Object>();
				oPostData.put("SiteType", 0);
		        oPostData.put("PageSize", 2000);
		        oPostData.put("OrderBy", "SITEORDER DESC");
		        oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName, oPostData,true,ips[i]);
		        Map json = oDispatch.getJson();
		        List list = JsonHelper.getList(json, "WEBSITES.WEBSITE");
		        System.out.println(ip+"下：有"+list.size()+"个站点");
		        if(list != null && list.size() != 0){
		        	for (int j = 0; j < list.size(); j++) {
						String siteid = JsonHelper.getValueAsString((Map)list.get(j), "SITEID");
						String sitename = JsonHelper.getValueAsString((Map)list.get(j), "SITENAME");
						this.insertSite(sysids[i],ips[i].split(":")[0],siteid,sitename,cid);
					}
		        }
			}
		}
	}
	
	private void insertServerJoin(HashMap<String,String> source,HashMap<String,String> target,int cid)throws WCMException{
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "savemetaviewdata";
		HashMap<String,Object> oPostData = new HashMap<String,Object>();
		oPostData.put("ObjectId",0);
		oPostData.put("ChannelId",cid);
		oPostData.put("SRC_SERVER_IP",source.get("SERVER_IP"));
		oPostData.put("TARGET_SERVER_IP",target.get("SERVER_IP"));
		oPostData.put("TARGET_SERVER_NAME",target.get("SERVER_NAME"));
		oPostData.put("TARGET_SERVER_PORT",target.get("SERVER_PORT"));
		oPostData.put("JOIN_USERNAME","admin");
		oPostData.put("JOIN_PASSWORD","trsadmin");
		oPostData.put("JOIN_STATUS","1");
		System.out.println("成功添加:"+oPostData.get("SRC_SERVER_IP") + "与" + oPostData.get("TARGET_SERVER_IP")+"的连接");
		WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, "127.0.0.1:8080");
	}
	
	//接收SQL查询到的列表数据    2018-11-14
	private void insertSiteJoin(HashMap<String,String> dataMap,int cid)throws WCMException{
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "savemetaviewdata";
		HashMap<String,Object> oPostData = new HashMap<String,Object>();
		oPostData.put("ObjectId",0);
		oPostData.put("ChannelId",cid);
		oPostData.put("SRC_IP",dataMap.get("JSD_IP"));
		oPostData.put("SRC_LMID",dataMap.get("JSD_ID"));
		oPostData.put("SRC_LMMC",dataMap.get("JSD_LMMC"));
		oPostData.put("TARGET_IP",dataMap.get("TSD_IP"));
		oPostData.put("TARGET_LMID",dataMap.get("TSD_ID"));
		oPostData.put("TARGET_LMMC",dataMap.get("TSD_LMMC"));
		System.out.println("成功添加:"+oPostData.get("SRC_LMMC") + "与" + oPostData.get("TARGET_LMMC")+"的连接");
		WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, "127.0.0.1:8080");
	}

	private void insertSite(String sysid,String ip,String siteid,String sitename,int cid)throws WCMException{
		String sServiceId = "wcm6_MetaDataCenter";
		String sMethodName = "savemetaviewdata";
		HashMap<String,Object> oPostData = new HashMap<String,Object>();
		oPostData.put("ObjectId",0);
		oPostData.put("ChannelId",cid);
		oPostData.put("SYSCONNID",sysid);
		oPostData.put("SERVER_IP",ip);
		oPostData.put("SITEID",siteid);
		oPostData.put("SITENAME",sitename);
		Dispatch oDispatch = WCMServiceCaller.Calls(sServiceId, sMethodName,
				oPostData, true, "127.0.0.1:8080");
	}
}
