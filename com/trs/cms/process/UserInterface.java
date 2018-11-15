package com.trs.cms.process;

import java.util.HashMap;

import com.trs.DreamFactory;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.domain.UserMgr;
import com.trs.cms.auth.persistent.User;

public class UserInterface {

	/*
	 * 新增用户
	 * */
	public void insertUser(HashMap map)throws Exception{
		
		ContextHelper.initContext(User.getSystem());
		User user = User.createNewInstance();
		 
		user.setName(map.get("username").toString()); 
		user.setNickName(map.get("nickname").toString());
		 
		user.setPassword(map.get("password").toString());
		user.setTrueName(map.get("truename").toString());
		user.setEmail(map.get("email").toString());
		user.setReminderQuestion(map.get("reminderquestion").toString());
		user.setReminderAnswer(map.get("reminderanswer").toString());
		user.setAttribute("ZGH", "66");
		user.setStatus(User.USER_STATUS_REG);
		  
		user.save();
	}
	
	/*
	 * 修改用户
	 * */
	public boolean updateUser(HashMap map)throws Exception{
		ContextHelper.initContext(User.getSystem());
		User user = User.findByName(map.get("username").toString());
		try {
			if(!user.canEdit(User.getSystem())){
				throw new RuntimeException("update user fail,can't lock obj.");
			}
			 user.setNickName(map.get("nickname") == null?user.getNickName():map.get("nickname").toString());
			 user.setTrueName(map.get("truename") == null?user.getTrueName():map.get("truename").toString());
			 user.setEmail(map.get("email") == null?user.getEmail():map.get("email").toString());
			 user.setReminderQuestion(map.get("reminderquestion") == null?user.getReminderQuestion():map.get("reminderquestion").toString());
			 user.setReminderAnswer(map.get("reminderanswer") == null?user.getReminderAnswer():map.get("reminderanswer").toString());
			 if("0".equals(map.get("status").toString())){
				 user.setStatus(User.USER_STATUS_REG);
			 }else{
				 user.setStatus(User.USER_STATUS_DEL);
			 }
			 user.save(User.getSystem()); 
			 return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 * 删除用户
	 * */
	public boolean deleteUser(String username)throws Exception{
		ContextHelper.initContext(User.getSystem());
		User user = User.findByName(username);
		try {
			UserMgr oUserMgr = (UserMgr) DreamFactory.createObjectById("UserMgr");
			if(user != null){
				oUserMgr.delete(user, false);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}