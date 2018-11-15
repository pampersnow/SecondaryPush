package com.cn.chncpa.job;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.trs.DreamFactory;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.domain.GroupMgr;
import com.trs.cms.auth.domain.UserMgr;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.Groups;
import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.job.BaseStatefulJob;

public class DataSynchronization extends BaseStatefulJob{

	private static Logger s_logger = Logger.getLogger(DataSynchronization.class);
	
	protected void execute() throws WCMException {
		String RWMS = CMyString.showNull(getArgAsString("RWMS"), "定时任务");// 任务描述

		String sSfqy = CMyString.showNull(getArgAsString("sfqy"), "0");// 是否启用，0不启用
																		// 1启用

		if (!("1".equals(sSfqy))) {
			s_logger.info(RWMS + " 未启用该策略！");
			return;
		}

		s_logger.info(RWMS + " 执行开始" + new CMyDateTime().now().toString());
		
		try {
			//1、获取核心库组织信息
			ArrayList hxkGroups = this.queryHXKGroups();
			//2、获取核心库用户信息
			ArrayList hxkUsers = this.queryHXKUsers();
			//3、获取WCM组织信息
			ArrayList wcmGroups = this.queryWCMGroups();
			//4、获取WCM用户信息
			ArrayList wcmUsers = this.queryWCMUsers();
			//同步数据
			this.dataSynchronization(hxkGroups,hxkUsers,wcmGroups,wcmUsers);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 同步数据
	 * */
	public void dataSynchronization(ArrayList hxkGroups,ArrayList hxkUsers,ArrayList wcmGroups,ArrayList wcmUsers)throws Exception{
		//同步组织
		if(wcmGroups.size() != 0 && hxkGroups.size() != 0){
			//组织数量不同
			if(wcmGroups.size() != hxkGroups.size()){
				//新增
				if(hxkGroups.size() > wcmGroups.size()){
					StringBuilder hxkd = new StringBuilder();
					for(int i = 0;i < hxkGroups.size();i++){
						HashMap hmap = (HashMap) hxkGroups.get(i);
						hxkd.append(hmap.get("MC").toString() + ",");
					}
					StringBuilder wcmd = new StringBuilder();
					for (int i = 0; i < wcmGroups.size(); i++) {
						HashMap wmap = (HashMap) wcmGroups.get(i);
						wcmd.append(wmap.get("GNAME").toString() + ",");
					}
					String newGroup = this.getDifferent(hxkd.toString(),wcmd.toString());
					if(newGroup != null && !"".equals(newGroup)){
						String[] newGroups = newGroup.split(",");
						GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
						for(int i = 0;i < newGroups.length;i++){
							Group oGroup = Group.createNewInstance();
							oGroup.setName(newGroups[i]);
							oGroup.setDesc(newGroups[i]);
							HashMap groupMap = this.findGroupByName(hxkGroups, newGroups[i]);
							if("教学部门".equals(groupMap.get("LX").toString())){
								oGroup.setParent(1);
							}else{
								oGroup.setParent(2);
							}
							oGroup.setAttribute("BMDM",groupMap.get("BMDM").toString());
							oGroupMgr.save(oGroup);
						}
					}
				}
				//删除
				if(hxkGroups.size() < wcmGroups.size()){
					StringBuilder hxkd = new StringBuilder();
					for(int i = 0;i < hxkGroups.size();i++){
						HashMap hmap = (HashMap) hxkGroups.get(i);
						hxkd.append(hmap.get("MC").toString() + ",");
					}
					StringBuilder wcmd = new StringBuilder();
					for (int i = 0; i < wcmGroups.size(); i++) {
						HashMap wmap = (HashMap) wcmGroups.get(i);
						wcmd.append(wmap.get("GNAME").toString() + ",");
					}
					String spilthGroup = this.getDifferent(wcmd.toString(),hxkd.toString());
					if(spilthGroup != null && !"".equals(spilthGroup)){
						String[] spilthGroups = spilthGroup.split(",");
						GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
						for (int i = 0; i < spilthGroups.length; i++) {
							int groupid = this.findGroupIdByName(wcmGroups,spilthGroups[i]);
							Group group = Group.findById(groupid);
							oGroupMgr.delete(group);
						}
					}
				}
			}
			//组织数量相同
			if(wcmGroups.size() == hxkGroups.size()){
				System.out.println("组织无需更新");
			}
		}
		
		//同步用户
		if(hxkUsers.size() != 0 && wcmUsers.size() != 0){
			//新增
			for (int i = 0; i < hxkUsers.size(); i++) {
				HashMap hmap = (HashMap) hxkUsers.get(i);
				String zgh = hmap.get("ZGH").toString();
				User oUser = User.findByName(zgh);
				if(oUser == null){
					ContextHelper.initContext(User.getSystem());
					User user = User.createNewInstance();
					user.setStatus(User.USER_STATUS_APPLY);
					//基本信息
					user.setName(hmap.get("ZGH").toString()); 												//职工号
					user.setNickName(hmap.get("GW").toString());												//岗位
					user.setPassword("beixin123456");	//密码		
					user.setTrueName(hmap.get("XM") == null?"":hmap.get("XM").toString());				//真实姓名
					user.setAttribute("DWDM", hmap.get("DWDM") == null?"":hmap.get("DWDM").toString());	//单位代码
					user.setAttribute("ZJH", hmap.get("ZJH") == null?"beixin123456":hmap.get("ZJH").toString());
					user.setAttribute("XQDM", hmap.get("XQDM") == null?"":hmap.get("XQDM").toString());
					user.setAttribute("ZWLBDM", hmap.get("ZWLBDM") == null?"":hmap.get("ZWLBDM").toString());
					user.setAttribute("GW", hmap.get("GW") == null?"":hmap.get("GW").toString());
					user.save();
					System.out.println("新增一条用户信息：" + hmap.get("ZGH").toString());
					//同步组织与用户关系
					if(hmap.get("DWDM") != null){
						Group oGroup = this.findGroupByAttribute(wcmGroups,hmap.get("DWDM").toString());
						GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
						User newUser = User.findByName(hmap.get("ZGH").toString());
						oGroupMgr.addUser(newUser, oGroup);
						System.out.println("同步组织信息："+oGroup.getName()+"_用户："+newUser.getName());
					}
				}else{
					String wcmg = oUser.getAttributeValue("DWDM");
					String hxkg = hmap.get("DWDM").toString();
					if(!hxkg.equals(wcmg)){
						GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
						//从旧组织移除
						Group oGroup = this.findGroupByAttribute(wcmGroups, wcmg);
						oGroupMgr.removeUser(oUser, oGroup);
						//加入到新组织
						Group nGroup = this.findGroupByAttribute(wcmGroups, hxkg);
						oGroupMgr.addUser(oUser, nGroup);
					}
				}
			}
			//删除
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < hxkUsers.size(); i++) {
				HashMap hmap = (HashMap) hxkUsers.get(i);
				String zgh = hmap.get("ZGH").toString();
				sb.append(zgh.toLowerCase() + ",");
			}
			for(int i = 0;i < wcmUsers.size();i++){
				HashMap wmap = (HashMap) wcmUsers.get(i);
				String username = wmap.get("USERNAME").toString();
				if(sb.toString().contains(username)){
					continue;
				}else{
					ContextHelper.initContext(User.getSystem());
					GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
					UserMgr oUserMgr = (UserMgr) DreamFactory.createObjectById("UserMgr");
					//先解除与组织关系
					User oUser = User.findByName(username);
					String attr = oUser.getAttributeValue("DWDM");
					Group oGroup = this.findGroupByAttribute(wcmGroups, attr);
					oGroupMgr.removeUser(oUser, oGroup);
					//删除用户
					oUserMgr.delete(oUser, false);
					System.out.println("删除一条用户数据：" + username);
				}
			}
		}
	}
	
	/*
	 * 提取新增的名称
	 * */
	public String getDifferent(String hxk,String wcm)throws Exception{
		String[] arr1 = hxk.split(",") ;
        String[] arr2 = wcm.split(",") ; 
        for (int i = 0; i < arr2.length; i++){
            for (int j = 0; j < arr1.length; j++){
                if (arr1[j].equals(arr2[i])){
                    arr1[j] = "" ;
                }
            }
        }
        StringBuffer sb = new StringBuffer() ;
        for (int j = 0; j < arr1.length; j++){
            if (!"".equals(arr1[j]) ){
                sb.append(arr1[j] + ",") ;
            }
        }
		return sb.toString().trim();
	}
	
	/*
	 * 获取单条组织信息
	 * */
	public HashMap findGroupByName(ArrayList hxkGroups,String groupName)throws Exception{
		HashMap result = null;
		for (int i = 0; i < hxkGroups.size(); i++) {
			HashMap map = (HashMap) hxkGroups.get(i);
			String gnm = map.get("MC").toString();
			if(groupName.equals(gnm)){
				result = map;
				break;
			}
		}
		return result;
	}
	
	/*
	 * 根据组织名称获取组织ID
	 * */
	public int findGroupIdByName(ArrayList wcmGroups,String groupName)throws Exception{
		int result = 0;
		for (int i = 0; i < wcmGroups.size(); i++) {
			HashMap map = (HashMap) wcmGroups.get(i);
			String gname = map.get("GNAME").toString();
			if(gname.equals(groupName)){
				String gid = map.get("GROUPID").toString();
				result = Integer.parseInt(gid);
				break;
			}
		}
		return result;
	}
	
	
	/*
	 * 根据组织属性获取组织
	 * */
	public Group findGroupByAttribute(ArrayList wcmGroups,String bmdm)throws Exception{
		Group result = null;
		for (int i = 0; i < wcmGroups.size(); i++) {
			HashMap map = (HashMap) wcmGroups.get(i);
			String gid = map.get("GROUPID").toString();
			Group group = Group.findById(Integer.parseInt(gid));
			String attr = group.getAttributeValue("BMDM");
			if(bmdm.equals(attr)){
				result = group;
				break;
			}
		}
		return result;
	}
	
	/*
	 * 获取组织数据
	 * */
	public ArrayList queryHXKGroups()throws Exception{
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ArrayList list = new ArrayList();
        try {
        	String driver = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@10.0.7.162:1521:orcl";
			String username = "WZQ";
			String password = "123456";
			String sql = "select * from xujl.BM";
			Class.forName(driver);
			connection = DriverManager.getConnection(url,username,password);
			statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet != null){
            	while (resultSet.next()) {
	            	Map map = new HashMap();
	                map.put("BMDM",resultSet.getString("BMDM"));
	                map.put("MC",resultSet.getString("MC") == null?"":resultSet.getString("MC"));
	                map.put("LX",resultSet.getString("LX") == null?"":resultSet.getString("LX"));
	                list.add(map);
            	}
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/*
	 * 获取用户数据
	 * */
	public ArrayList queryHXKUsers()throws Exception{
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ArrayList list = new ArrayList();
        try {
        	String driver = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@10.0.7.162:1521:orcl";
			String username = "WZQ";
			String password = "123456";
			String sql = "select * from xujl.T_JZG ORDER BY ZGH DESC";
			Class.forName(driver);
			connection = DriverManager.getConnection(url,username,password);
			statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet != null){
            	while (resultSet.next()) {
	            	Map map = new HashMap();
	                map.put("ZGH",resultSet.getString("ZGH"));
	                map.put("XM",resultSet.getString("XM") == null?"":resultSet.getString("XM"));
	                map.put("XMPY",resultSet.getString("XMPY") == null?"":resultSet.getString("XMPY"));
	                map.put("ZJLXDM",resultSet.getString("ZJLXDM") == null?"":resultSet.getString("ZJLXDM"));
	                map.put("ZJH",resultSet.getString("ZJH") == null?"beixin123456":resultSet.getString("ZJH"));
	                map.put("CSRQ",resultSet.getString("CSRQ") == null?"":resultSet.getString("CSRQ"));
	                map.put("XBDM",resultSet.getString("XBDM") == null?"":resultSet.getString("XBDM"));
	                map.put("MZDM",resultSet.getString("MZDM") == null?"":resultSet.getString("MZDM"));
	                map.put("RDRQ",resultSet.getString("RDRQ") == null?"":resultSet.getString("RDRQ"));
	                map.put("ZZMMDM",resultSet.getString("ZZMMDM") == null?"":resultSet.getString("ZZMMDM"));
	                map.put("XWDM",resultSet.getString("XWDM") == null?"":resultSet.getString("XWDM"));
	                map.put("TC",resultSet.getString("TC") == null?"":resultSet.getString("TC"));
	                map.put("DWDM",resultSet.getString("DWDM") == null?"":resultSet.getString("DWDM"));
	                map.put("XQDM",resultSet.getString("XQDM") == null?"":resultSet.getString("XQDM"));
	                map.put("CJGZRQ",resultSet.getString("CJGZRQ") == null?"":resultSet.getString("CJGZRQ"));
	                map.put("LXRQ",resultSet.getString("LXRQ") == null?"":resultSet.getString("LXRQ"));
	                map.put("CJRQ",resultSet.getString("CJRQ") == null?"":resultSet.getString("CJRQ"));
	                map.put("ZCXL",resultSet.getString("ZCXL") == null?"":resultSet.getString("ZCXL"));
	                map.put("JZGLB",resultSet.getString("JZGLB") == null?"":resultSet.getString("JZGLB"));
	                map.put("ZWLBDM",resultSet.getString("ZWLBDM") == null?"":resultSet.getString("ZWLBDM"));
	                map.put("ZWJB",resultSet.getString("ZWJB") == null?"":resultSet.getString("ZWJB"));
	                map.put("GW",resultSet.getString("GW") == null?"":resultSet.getString("GW"));
	                map.put("ZC",resultSet.getString("ZC") == null?"":resultSet.getString("ZC"));
	                map.put("NL",resultSet.getString("NL") == null?"":resultSet.getString("NL"));
	                map.put("SFZX",resultSet.getString("SFZX") == null?"":resultSet.getString("SFZX"));
	                list.add(map);
            	}
            }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	/*
	 * 获取WCM组织数据
	 * */
	public ArrayList queryWCMGroups()throws Exception{
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT * FROM WCMGROUP WHERE 1 = 1 AND PARENTID IN(1,2)";
		ArrayList list = new ArrayList();
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql);
			if(resultSet != null){
				while (resultSet.next()) {
					Map map = new HashMap();
	                map.put("GROUPID",resultSet.getString("GROUPID"));
	                map.put("GNAME",resultSet.getString("GNAME"));
	                map.put("GDESC",resultSet.getString("GDESC") == null?"":resultSet.getString("GDESC"));
	                map.put("ATTRIBUTE",resultSet.getString("ATTRIBUTE") == null?"":resultSet.getString("ATTRIBUTE"));
	                list.add(map);
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
		return list;
	}
	
	/*
	 * 获取WCM用户数据
	 * */
	public ArrayList queryWCMUsers()throws Exception{
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT * FROM WCMUSER WHERE 1 = 1 AND ISDELETED = 1 ORDER BY CRTIME DESC";
		ArrayList list = new ArrayList();
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql);
			if(resultSet != null){
				while (resultSet.next()) {
					boolean bool = this.isManager(resultSet.getString("USERID"));
					if(!bool){
						Map map = new HashMap();
		                map.put("USERID",resultSet.getString("USERID"));
		                map.put("USERNAME", resultSet.getObject("USERNAME"));
		                map.put("ATTRIBUTE",resultSet.getString("ATTRIBUTE") == null?"":resultSet.getString("ATTRIBUTE"));
		                list.add(map);
					}
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
		return list;
	}
	
	/*
	 * 判断用户是否是管理员
	 * */
	public boolean isManager(String userid)throws Exception{
		boolean bool = true;
		User user = User.findById(Integer.parseInt(userid));
		Groups groups = user.getGroups();
		if(groups != null && groups.size() != 0){
			for(int i = 0;i < groups.size();i++){
				Group group = (Group) groups.getAt(i);
				if(group.getId() == 62){
					bool = true;
					return bool;
				}else{
					bool = false;
				}
			}
		}else{
			bool = true;
		}
		return bool;
	}
	/*
	 * 初始化迁移数据
	 * */
	public void dataMove(ArrayList hxkGroups,ArrayList hxkUsers)throws Exception{
		try {
			//迁移组织
			/*if(hxkGroups.size() != 0){
				GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
				for(int i = 0;i < hxkGroups.size();i++){
					HashMap groupMap = (HashMap) hxkGroups.get(i);
					Group oGroup = Group.createNewInstance();
					oGroup.setName(groupMap.get("MC").toString());
					oGroup.setDesc(groupMap.get("MC").toString());
					oGroup.setAttribute("BMDM",groupMap.get("BMDM").toString());
					if("教学部门".equals(groupMap.get("LX").toString())){
						oGroup.setParent(1);
					}else{
						oGroup.setParent(2);
					}
					oGroupMgr.save(oGroup);
				}
			}*/
			//迁移用户
			if(hxkUsers.size() != 0){
				ContextHelper.initContext(User.getSystem());
				for(int i = 0;i < hxkUsers.size();i++){
					HashMap map = (HashMap) hxkUsers.get(i);
					User user = User.createNewInstance();
					if("0".equals(map.get("SFZX").toString())){	//不在校
						user.setStatus(User.USER_STATUS_DEL);
					}else{										//在校
						user.setStatus(User.USER_STATUS_REG);
					}
					//基本信息
					user.setName(map.get("ZGH").toString()); 	//职工号
					user.setNickName(map.get("GW").toString());	//岗位
					user.setPassword(map.get("ZJH") == null?"beixin123456":map.get("ZJH").toString());	//证件号		
					user.setTrueName(map.get("XM") == null?"":map.get("XM").toString());	//真实姓名
					//扩展属性
					//user.setAttribute("ZJLXDM", map.get("ZJLXDM") == null?"":map.get("ZJLXDM").toString());
					//user.setAttribute("CSRQ", map.get("CSRQ") == null?"":map.get("CSRQ").toString());
					//user.setAttribute("XBDM", map.get("XBDM") == null?"":map.get("XBDM").toString());
					//user.setAttribute("MZDM", map.get("MZDM") == null?"":map.get("MZDM").toString());
					//user.setAttribute("RDRQ", map.get("RDRQ") == null?"":map.get("RDRQ").toString());
					//user.setAttribute("ZZMMDM", map.get("ZZMMDM") == null?"":map.get("ZZMMDM").toString());
					//user.setAttribute("XWDM", map.get("XWDM") == null?"":map.get("XWDM").toString());
					//user.setAttribute("TC", map.get("TC") == null?"":map.get("TC").toString());
					user.setAttribute("ZJH", map.get("ZJH") == null?"beixin123456":map.get("ZJH").toString());	//证件号
					user.setAttribute("DWDM", map.get("DWDM") == null?"":map.get("DWDM").toString());
					user.setAttribute("XQDM", map.get("XQDM") == null?"":map.get("XQDM").toString());
					//user.setAttribute("CJGZRQ", map.get("CJGZRQ") == null?"":map.get("CJGZRQ").toString());
					//user.setAttribute("LXRQ", map.get("LXRQ") == null?"":map.get("LXRQ").toString());
					//user.setAttribute("CJRQ", map.get("CJRQ") == null?"":map.get("CJRQ").toString());
					//user.setAttribute("ZCXL", map.get("ZCXL") == null?"":map.get("ZCXL").toString());
					//user.setAttribute("JZGLB", map.get("JZGLB") == null?"":map.get("JZGLB").toString());
					user.setAttribute("ZWLBDM", map.get("ZWLBDM") == null?"":map.get("ZWLBDM").toString());
					user.setAttribute("GW", map.get("GW") == null?"":map.get("GW").toString());
					//user.setAttribute("ZWJB", map.get("ZWJB") == null?"":map.get("ZWJB").toString());
					//user.setAttribute("NL", map.get("NL") == null?"":map.get("NL").toString());
					user.save();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 初始化设置组织与用户关系
	 * */
	public void setGroupsAndUsersRelations(ArrayList wcmGroups,ArrayList wcmUsers)throws Exception{
		if(wcmGroups.size() != 0 && wcmUsers.size() != 0){
			for(int i = 0;i < wcmUsers.size();i++){
				HashMap userMap = (HashMap) wcmUsers.get(i);
				User user = User.findById(Integer.parseInt(userMap.get("USERID").toString()));
				String udwdm = user.getAttributeValue("DWDM");
				if(udwdm != null && !"".equals(udwdm)){
					for(int j = 0;j < wcmGroups.size();j++){
						HashMap groupMap = (HashMap) wcmGroups.get(j);
						Group group = Group.findById(Integer.parseInt(groupMap.get("GROUPID").toString()));
						String gbmdm = group.getAttributeValue("BMDM");
						if(gbmdm != null && !"".equals(gbmdm)){
							if(udwdm.equals(gbmdm)){
								GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
								oGroupMgr.addUser(user, group);
							}
						}
					}
				}
			}
		}
	}
	
	public void inits()throws Exception{
		//1、获取核心库组织信息
		ArrayList hxkGroups = this.queryHXKGroups();
		//2、获取核心库用户信息
		ArrayList hxkUsers = this.queryHXKUsers();
		//3、获取WCM组织信息
		ArrayList wcmGroups = this.queryWCMGroups();
		//4、获取WCM用户信息
		ArrayList wcmUsers = this.queryWCMUsers();
		
		this.dataSynchronization(hxkGroups,hxkUsers,wcmGroups,wcmUsers);
	}
	
	public ArrayList temp()throws Exception{
		ArrayList list = new ArrayList();
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
		try {
			String driver = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@10.0.7.162:1521:orcl";
			String username = "WZQ";
			String password = "123456";
			String sql = "select * from xujl.BM";
			Class.forName(driver);
			connection = DriverManager.getConnection(url,username,password);
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			ResultSetMetaData metaData = resultSet.getMetaData();
			for (int i = 0; i < metaData.getColumnCount(); i++) {
				Map map = new HashMap();
				map.put("columnName", metaData.getColumnName(i + 1));
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
}