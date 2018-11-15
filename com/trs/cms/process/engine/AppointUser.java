package com.trs.cms.process.engine;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.Role;
import com.trs.cms.auth.persistent.Roles;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.auth.persistent.Users;
import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.config.ToUsersCreatorConfig;
import com.trs.cms.process.definition.FlowNode;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;

public class AppointUser implements IToUsersCreator{

	public Users createToUsers(FlowContext _context, ToUsersCreatorConfig _config)
			throws WCMException {
		ContextHelper.initContext(User.findByName("admin"));

	    Document document = null;
	    
	    IFlowContent flowContent = _context.getFlowContent();
	    
	    if ((flowContent.getSubinstance() instanceof Document)) {
	    	document = (Document)flowContent.getSubinstance();
	    }
	    
	    User admin = User.findByName("admin");
	    Users users = Users.createNewInstance(admin);
	    String nextUser = null;
	    //1、获取当前流转中对象
	    MetaViewData metaViewData = MetaViewData.findById(document.getId());
	    //2、获取工作流下一节点
	    FlowNode node = _context.getNextNode();
	    //3、获取节点名称
	    String nodeName = node.getName();
	    //4、获取组织
    	Group group = Group.findById(Integer.parseInt(metaViewData.getPropertyAsString("pteam")));
    	//5、获取组织下所有成员
    	Users members = group.getUsers(admin);
    	//6、根据下一节点选择成员
	    if("部门经理".equals(nodeName)){
	    	for(int i = 0;i < members.size();i++){
	    		User user = (User) members.getAt(i);
	    		Roles roles = user.getRoles();
	    		for(int j = 0;j < roles.size();j++){
	    			Role role = (Role) roles.getAt(j);
	    			String roleName = role.getName();
	    			if("部门经理".equals(roleName)){
	    				nextUser = user.getName();
	    			}
	    		}
	    	}
		    users.addElement(User.findByName(nextUser));
	    }else if("项目经理".equals(nodeName)){
	    	nextUser = metaViewData.getPropertyAsString("pmanager");
	    	users.addElement(User.findByName(nextUser));
	    }else if("项目成员".equals(nodeName)){
	    	nextUser = metaViewData.getPropertyAsString("pmember");
	    	String[] nextUsers = nextUser.split(",");
	    	for (int i = 0; i < nextUsers.length; i++) {
				users.addElement(User.findByName(nextUsers[i]));
			}
	    }else if("销售".equals(nodeName)){
	    	for(int i = 0;i < members.size();i++){
	    		User user = (User) members.getAt(i);
	    		Roles roles = user.getRoles();
	    		for(int j = 0;j < roles.size();j++){
	    			Role role = (Role) roles.getAt(j);
	    			String roleName = role.getName();
	    			if("销售".equals(roleName)){
	    				nextUser = user.getName();
	    			}
	    		}
	    	}
	    	users.addElement(User.findByName(nextUser));
	    }else if("销售助理".equals(nodeName)){
	    	for(int i = 0;i < members.size();i++){
	    		User user = (User) members.getAt(i);
	    		Roles roles = user.getRoles();
	    		for(int j = 0;j < roles.size();j++){
	    			Role role = (Role) roles.getAt(j);
	    			String roleName = role.getName();
	    			if("销售助理".equals(roleName)){
	    				nextUser = user.getName();
	    			}
	    		}
	    	}
	    	users.addElement(User.findByName(nextUser));
	    }else if("项目经理2".equals(nodeName)){
	    	nextUser = metaViewData.getPropertyAsString("CRUSER");
	    	users.addElement(User.findByName(nextUser));
	    }
	    return users;
	}

	public Group getGroupRange(FlowContext arg0, ToUsersCreatorConfig arg1)
			throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRangedFromGroup() {
		// TODO Auto-generated method stub
		return false;
	}

}
