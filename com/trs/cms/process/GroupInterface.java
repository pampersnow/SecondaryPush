package com.trs.cms.process;

import java.util.HashMap;

import com.trs.DreamFactory;
import com.trs.cms.auth.domain.GroupMgr;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.User;

public class GroupInterface {

	/*
	 * 新增组织֯
	 * */
	public void insertGroup(HashMap map,int parentId)throws Exception{
		Group oGroup = Group.createNewInstance();
		oGroup.setName(map.get("name").toString());
		oGroup.setDesc(map.get("desc").toString());
		oGroup.setParent(parentId);
		GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
		oGroupMgr.save(oGroup);
	}
	
	/*
	 * 删除组织
	 * */
	public void deleteGroup(int groupId)throws Exception{
		Group group = Group.findById(groupId);
		GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
		oGroupMgr.delete(group);
	}
	
	/*
	 * 向组织中添加用户
	 * */
	public void insertUser2Group(int groupId,int userId)throws Exception{
		User oUser = User.findById(userId);
		Group oGroup = Group.findById(groupId);
		GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
		oGroupMgr.addUser(oUser, oGroup);
	}
	
	/*
	 * 从组织中移除用户
	 * */
	public void removeUser4Group(int groupId,int userId)throws Exception{
		User oUser = User.findById(userId);
		Group oGroup = Group.findById(groupId);
		GroupMgr oGroupMgr = (GroupMgr) DreamFactory.createObjectById("GroupMgr");
		oGroupMgr.removeUser(oUser, oGroup);
	}
}