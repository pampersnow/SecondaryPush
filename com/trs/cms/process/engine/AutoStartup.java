package com.trs.cms.process.engine;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.auth.persistent.Users;
import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.config.ToUsersCreatorConfig;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;

public class AutoStartup implements IToUsersCreator{

	public AutoStartup() {
	}

	public Users createToUsers(FlowContext _context, ToUsersCreatorConfig _config)
			throws WCMException {
		ContextHelper.initContext(User.findByName("admin"));

	    Document document = null;
	    IFlowContent flowContent = _context.getFlowContent();
	    if ((flowContent.getSubinstance() instanceof Document)) {
	      document = (Document)flowContent.getSubinstance();
	    }

	    MetaViewData metaViewData = MetaViewData.findById(document.getId());
	    int groupId = Integer.parseInt(metaViewData.getPropertyAsString("TJZB"));
	    Group group = Group.findById(groupId);

	    User admin = User.findByName("admin");
	    Users users = Users.createNewInstance(admin);

	    String value = group.getAttributeValue("GROUPLEADER");

	    User newName = User.findByName(value);

	    users.addElement(newName);

	    return users;
	}

	public Group getGroupRange(FlowContext arg0, ToUsersCreatorConfig arg1)
			throws WCMException {
		return null;
	}

	public boolean isRangedFromGroup() {
		return false;
	}

}
