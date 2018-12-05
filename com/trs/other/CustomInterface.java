package com.trs.other;

import java.util.ArrayList;

import com.trs.cms.auth.persistent.ObjectMember;
import com.trs.cms.auth.persistent.ObjectMembers;
import com.trs.cms.auth.persistent.Role;
import com.trs.cms.auth.persistent.Roles;
import com.trs.cms.auth.persistent.User;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.support.config.ConfigServer;
import com.trs.webframework.context.MethodContext;
import com.trs.webframework.provider.ISelfDefinedServiceProvider;

public class CustomInterface implements ISelfDefinedServiceProvider{

	public String getTJList(MethodContext _oMethodContext)throws Throwable{
		System.out.println("统计表:请求成功。。。。");  
		String sResult = "[";
		String userName = _oMethodContext.getValue("uname");
		User user = User.findByName(userName);
		if(user != null){
			Roles roles = user.getRoles();
			if(roles != null && roles.size() != 0){
				String rids = "";
				for (int i = 0; i < roles.size(); i++) {
					Role role = (Role)roles.getAt(i);
					if(role == null || "Everyone".equals(role.getName()))continue;
					rids += role.getId() + ",";
				}
				if (!"".equals(rids)) {
					rids = rids.substring(0, rids.length() - 1);
				}
				String omids = "";
				if (!"".equals(rids)) {
					StringBuilder sql = new StringBuilder("MEMBERID IN (" + rids+ ") AND OBJTYPE = 101 AND VISIBLE = 1");
					WCMFilter filter = new WCMFilter("", sql.toString(), "OBJID");
					ObjectMembers objectMembers = ObjectMembers.openWCMObjs(user, filter);
					if (objectMembers != null && objectMembers.size() != 0) {
						for (int j = 0; j < objectMembers.size(); j++) {
							ObjectMember objectMember = (ObjectMember) objectMembers
									.getAt(j);
							if (objectMember == null)
								continue;
							omids += objectMember.getObjId() + "_" + objectMember.isDoOperation() + ",";
						}
					}
				}
				if (!"".equals(omids)) {
					omids = omids.substring(0, omids.length() - 1);
				}
				Channel tjChannel = Channel.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue("ZZB_TJBB_PID", "0")));
				if(tjChannel != null){
					ArrayList childrens = (ArrayList) tjChannel.getAllChildren(user);
					if(childrens.size() != 0){
						for (int a = 0; a < childrens.size(); a++) {
							Channel children = (Channel) childrens.get(a);
							if(children == null)continue;
							String[] arr = omids.split(",");
							for (int b = 0; b < arr.length; b++) {
								if(Integer.parseInt(arr[b].split("_")[0]) == children.getId()){
									sResult += this.getJson(children,arr[b].split("_")[1]) + ",";
								}
							}
						}
						if(!"[".equals(sResult)){
							sResult = sResult.substring(0, sResult.lastIndexOf(","));
						}
					}
				}
			}
		}
		sResult += "]";
		return sResult;
	}
	
	public String getZLBBList(MethodContext _oMethodContext)throws Throwable{
		System.out.println("数据质量报表:请求成功。。。。");
		String sResult = "[";
		String userName = _oMethodContext.getValue("uname");
		User user = User.findByName(userName);
		if(user != null){
			Roles roles = user.getRoles();
			if(roles != null && roles.size() != 0){
				String rids = "";
				for (int i = 0; i < roles.size(); i++) {
					Role role = (Role)roles.getAt(i);
					if(role == null || "Everyone".equals(role.getName()))continue;
					rids += role.getId() + ",";
				}
				if (!"".equals(rids)) {
					rids = rids.substring(0, rids.length() - 1);
				}
				String omids = "";
				if (!"".equals(rids)) {
					StringBuilder sql = new StringBuilder("MEMBERID IN (" + rids+ ") AND OBJTYPE = 101 AND VISIBLE = 1");
					WCMFilter filter = new WCMFilter("", sql.toString(), "OBJID");
					ObjectMembers objectMembers = ObjectMembers.openWCMObjs(user, filter);
					if (objectMembers != null && objectMembers.size() != 0) {
						for (int j = 0; j < objectMembers.size(); j++) {
							ObjectMember objectMember = (ObjectMember) objectMembers
									.getAt(j);
							if (objectMember == null)
								continue;
							omids += objectMember.getObjId() + "_" + objectMember.isDoOperation() + ",";
						}
					}
				}
				if (!"".equals(omids)) {
					omids = omids.substring(0, omids.length() - 1);
				}
				Channel tjChannel = Channel.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue("ZZB_ZLBB_PID", "0")));
				if(tjChannel != null){
					ArrayList childrens = (ArrayList) tjChannel.getAllChildren(user);
					if(childrens.size() != 0){
						for (int a = 0; a < childrens.size(); a++) {
							Channel children = (Channel) childrens.get(a);
							if(children == null)continue;
							String[] arr = omids.split(",");
							for (int b = 0; b < arr.length; b++) {
								if(Integer.parseInt(arr[b].split("_")[0]) == children.getId()){
									sResult += this.getJson(children,arr[b].split("_")[1]) + ",";
								}
							}
						}
						if(!"[".equals(sResult)){
							sResult = sResult.substring(0, sResult.lastIndexOf(","));
						}
					}
				}
			}
		}
		sResult += "]";
		return sResult;
	}
	
	public String getDateList(MethodContext _oMethodContext)throws Throwable{
		System.out.println("数据开关:请求成功。。。。");
		String sResult = "[";
		String userName = _oMethodContext.getValue("uname");
		User user = User.findByName(userName);
		if(user != null){
			Roles roles = user.getRoles();
			if(roles != null && roles.size() != 0){
				String rids = "";
				for (int i = 0; i < roles.size(); i++) {
					Role role = (Role)roles.getAt(i);
					if(role == null || "Everyone".equals(role.getName()))continue;
					rids += role.getId() + ",";
				}
				if (!"".equals(rids)) {
					rids = rids.substring(0, rids.length() - 1);
				}
				String omids = "";
				if (!"".equals(rids)) {
					StringBuilder sql = new StringBuilder("MEMBERID IN (" + rids+ ") AND OBJTYPE = 101 AND VISIBLE = 1");
					WCMFilter filter = new WCMFilter("", sql.toString(), "OBJID");
					ObjectMembers objectMembers = ObjectMembers.openWCMObjs(user, filter);
					if (objectMembers != null && objectMembers.size() != 0) {
						for (int j = 0; j < objectMembers.size(); j++) {
							ObjectMember objectMember = (ObjectMember) objectMembers
									.getAt(j);
							if (objectMember == null)
								continue;
							omids += objectMember.getObjId() + "_" + objectMember.isDoOperation() + ",";
						}
					}
				}
				if (!"".equals(omids)) {
					omids = omids.substring(0, omids.length() - 1);					
				}
				Channel tjChannel = Channel.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue("ZZB_SJKG_PID", "0")));
				if(tjChannel != null){
					ArrayList childrens = (ArrayList) tjChannel.getAllChildren(user);
					if(childrens.size() != 0){
						for (int a = 0; a < childrens.size(); a++) {
							Channel children = (Channel) childrens.get(a);
							if(children == null)continue;
							String[] arr = omids.split(",");
							for (int b = 0; b < arr.length; b++) {
								if(Integer.parseInt(arr[b].split("_")[0]) == children.getId()){
									sResult += this.getJson(children,"false") + ",";
								}
							}
						}
						if(!"[".equals(sResult)){
							sResult = sResult.substring(0, sResult.lastIndexOf(","));
						}
					}
				}
			}
		}
		sResult += "]";
		return sResult;
	}
	
	private String getJson(Channel channel,String isCan)throws WCMException{
		String json = "{\"ID\":\"" + channel.getId() + "\",\"NAME\":\""+channel.getName() + "\",\"URL\":\"" + channel.getLinkUrl() + "\",\"ISCAN\":\"" + isCan + "\"}";
		return json;
	}
}
