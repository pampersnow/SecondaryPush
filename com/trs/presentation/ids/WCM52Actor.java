package com.trs.presentation.ids;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.domain.GroupMgr;
import com.trs.cms.auth.domain.UserMgr;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.Groups;
import com.trs.cms.auth.persistent.User;
import com.trs.idm.client.actor.ActorException;
import com.trs.idm.client.actor.SSOGroup;
import com.trs.idm.client.actor.SSOUser;
import com.trs.idm.client.actor.StdHttpSessionBasedActor;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyString;
import com.trs.presentation.util.LoginHelper;
import com.trs.presentation.util.LoginPasswordEncrypter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

public class WCM52Actor extends StdHttpSessionBasedActor {
	private static Logger logger = Logger.getLogger(WCM52Actor.class);
	private static final String LOGIN_FLAG = "WCM52.loginUser";
	private static final boolean ENABLE_FROM_IDS = true;

	public boolean checkLocalLogin(HttpSession session) {
		if (session == null) {
			return false;
		}
		return session.getAttribute("WCM52.loginUser") != null;
	}

	private Group getGroupFromIdsId(String _sId) {
		WCMFilter filter = new WCMFilter("", "nameinids='" + _sId + "'", "");
		try {
			Groups result = Groups.openWCMObjs(User.getSystem(), filter);
			if (result.size() > 0)
				return (Group) result.getAt(0);
		} catch (Exception e) {
			logger.error("get group fail![nameinids = " + _sId, e);
		}
		return null;
	}

	private User registerUser(SSOUser _user) {
		if (_user == null) {
			logger.error("[registerUser]方法中的SSOUser对象为null！");
			return null;
		}

		String sUserName = dealWithName(_user.getUserName());

		if (logger.isDebugEnabled()) {
			logger.debug("开始注册用户[" + sUserName + "]!");
		}

		User sysUser = User.getSystem();
		ContextHelper.initContext(sysUser);
		try {
			User user = User.createNewInstance();
			user.setName(sUserName);
			user.setNickName(_user.getNickName());

			user.setPassword(ConfigServer.getServer().getInitProperty(
					"SYSUSERPWD"));

			user.setTrueName(_user.getTrueName());
			user.setAddress(_user.getProperty("address"));
			user.setTel(_user.getProperty("tel"));
			user.setMobile(_user.getProperty("mobile"));
			user.setEmail(_user.getMail());
			user.setReminderQuestion("CreateBy?");
			user.setReminderAnswer("IDS");
			user.setStatus(30);

			setIdsExtProps(_user, user);

			user.save(sysUser);

			if (logger.isDebugEnabled()) {
				logger.debug("注册用户[" + sUserName + "]完成!");
			}
			logger.info("注册用户[" + sUserName + "]成功!");

			return user;
		} catch (WCMException ex) {
			logger.error("注册用户[" + sUserName + "]失败！", ex);
		}
		return null;
	}

	public void loadLoginUser(HttpServletRequest _req, SSOUser _user)
			throws ActorException {
		String sUserName = dealWithName(_user.getUserName());

		User user = User.findByName(sUserName);
		if (user == null) {
			user = registerUser(_user);

			for (int i = 0; i < _user.getSSOGroups().size(); i++) {
				moveToGroup(_user, (SSOGroup) _user.getSSOGroups().get(i));
			}
		}

		if (user == null) {
			logger.error("用户[" + sUserName + "]注册失败！");
		}

		HttpSession session = _req.getSession();
		if (user != null) {
			LoginHelper currLoginHelper = new LoginHelper(_req,
					session.getServletContext());
			try {
				currLoginHelper.loginByIDS(user.getName());
			} catch (Throwable e) {
				logger.error(session.getId() + "login fail, user[" + sUserName
						+ "]  fail to login!", e);
			}
		} else {
			logger.error(session.getId() + " login fail, user=null! UserName="
					+ sUserName + ", req=" + _req.getRequestURI() + ", ip="
					+ _req.getRemoteAddr());
		}
	}

	private String dealWithName(String _sUserName) {
		String sUserName = _sUserName;

		String sStr = " - ";
		int nStartIndex = sUserName.indexOf(sStr);
		int nEndIndex = sUserName.indexOf("@");

		if (nStartIndex != -1) {
			if (nEndIndex != -1)
				sUserName = sUserName.substring(nStartIndex + sStr.length(),
						nEndIndex);
			else {
				sUserName = sUserName.substring(nStartIndex + sStr.length());
			}
		} else if (nEndIndex != -1) {
			sUserName = sUserName.substring(0, nEndIndex);
		}

		return sUserName;
	}

	public void logout(HttpSession session) throws ActorException {
		if (session != null) {
			session.invalidate();
			if (logger.isDebugEnabled())
				logger.debug("logout succeed !");
		}
	}

	public boolean addUser(SSOUser _user, HttpServletRequest arg1)
			throws ActorException {
		ContextHelper.initContext(User.getSystem());

		String userName = _user.getUserName();
		if (logger.isDebugEnabled()) {
			logger.debug("开始添加用户 [" + userName + "]");
		}

		boolean addOk = false;
		try {
			User user = User.findByName(userName);
			if (user == null) {
				user = registerUser(_user);
			} else if (user.getStatus() != 30) {
				if (logger.isDebugEnabled()) {
					logger.debug("enable [" + user + "]");
				}
				user.setStatus(30);
				user.save(User.getSystem());
			}

			for (int i = 0; i < _user.getSSOGroups().size(); i++) {
				moveToGroup(_user, (SSOGroup) _user.getSSOGroups().get(i));
			}
			addOk = user != null;
		} catch (Exception ex) {
			logger.error("添加用户[" + userName + "]失败！", ex);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("用户 [" + userName + "]添加成功！");
		}

		return addOk;
	}

	public boolean disableUser(SSOUser _user) throws ActorException {
		ContextHelper.initContext(User.getSystem());
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Begin to disable user [" + _user.getUserName()
						+ "]！");
			}

			User currUser = User.findByName(_user.getUserName());
			if (currUser != null) {
				currUser.setStatus(20);
				currUser.save();
			}
		} catch (Exception e) {
			logger.error("Disable user [userName=" + _user.getUserName()
					+ "] fail！", e);
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Disable user [" + _user.getUserName() + "] success！");
		}

		return true;
	}

	public boolean enableUser(SSOUser _user) throws ActorException {
		ContextHelper.initContext(User.getSystem());
		try {
			User currUser = User.findByName(_user.getUserName());
			if (currUser != null) {
				currUser.setStatus(30);
				currUser.save();
			}
		} catch (Exception e) {
			logger.error("EnableUser user [userName=" + _user.getUserName()
					+ "] fail!", e);
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("EnableUser user [" + _user.getUserName()
					+ "] success！");
		}
		return true;
	}

	public String extractUserName(HttpServletRequest _request)
			throws ActorException {
		if (_request == null) {
			return null;
		}
		String sUserName = _request.getParameter("UserName");
		if ((sUserName == null) || ("".equals(sUserName))) {
			return "";
		}

		if (logger.isDebugEnabled()) {
			logger.debug("UserName begin:::" + sUserName + "！");
		}

		sUserName = CMyString.getStr(sUserName, true);
		if (logger.isDebugEnabled()) {
			logger.debug("UserName end:::" + sUserName + "！");
		}

		return sUserName;
	}

	public String extractUserPwd(HttpServletRequest _request)
			throws ActorException {
		if (_request == null) {
			return null;
		}
		String sUserPassword = _request.getParameter("PassWord");
		if ((sUserPassword == null) || ("".equals(sUserPassword))) {
			return "";
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Password begin:::" + sUserPassword);
		}

		if (LoginPasswordEncrypter.isEncrypted(sUserPassword)) {
			sUserPassword = LoginPasswordEncrypter.deEncrypt(sUserPassword);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Password  end:::" + sUserPassword);
		}
		return sUserPassword;
	}

	public boolean removeUser(SSOUser _user, HttpServletRequest _req)
			throws ActorException {
		ContextHelper.initContext(User.getSystem());
		try {
			User user = User.findByName(_user.getUserName());
			if (user != null) {
				ActorHelper.getUserMgr().delete(user, true);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("RemoveUser user [" + _user.getUserName()
						+ "] success！");
			}
			return true;
		} catch (WCMException e) {
			logger.error("fail to removeUser user: " + _user.getUserName(), e);
		}
		return false;
	}

	public boolean updateUser(SSOUser _user, HttpServletRequest req)
			throws ActorException {
		ContextHelper.initContext(User.getSystem());
		String sUserName = _user.getUserName();
		try {
			User user = User.findByName(sUserName);
			if (!user.canEdit(User.getSystem())) {
				throw new RuntimeException("update user fail,can't lock obj.");
			}
			user.setNickName(_user.getNickName());

			user.setTrueName(_user.getTrueName());
			user.setAddress(_user.getProperty("address"));
			user.setTel(_user.getProperty("tel"));
			user.setMobile(_user.getProperty("mobile"));
			user.setEmail(_user.getMail());
			user.setReminderQuestion("CreateBy?");
			user.setReminderAnswer("IDS");
			user.setStatus(30);

			setIdsExtProps(_user, user);
			user.save(User.getSystem());
			if (logger.isDebugEnabled()) {
				logger.debug("UpdateUser user [" + _user.getUserName()
						+ "] success！");
			}
			return true;
		} catch (WCMException e) {
			e.printStackTrace(System.out);
			logger.error(
					"UpdateUser user fail: [UserName=" + _user.getUserName()
							+ "]!", e);
		}
		return false;
	}

	private void setIdsExtProps(SSOUser _ssoUser, User _currUser)
			throws WCMException {
		Set names = _ssoUser.propertyNames();
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();

			if (!name.startsWith("IDSEXT_")) {
				continue;
			}
			String sName = name.substring(7);
			if (DBManager.getDBManager().getFieldInfo("WCMUSER", sName) == null)
				_currUser.setAttribute(sName, _ssoUser.getProperty(name));
			else
				_currUser.setProperty(sName, _ssoUser.getProperty(name));
		}
	}

	public boolean userExist(SSOUser _user) throws ActorException {
		ContextHelper.initContext(User.getSystem());

		User user = User.findByName(_user.getUserName());
		return user != null;
	}

	public boolean addGroup(SSOGroup _group, HttpServletRequest req) {
		ContextHelper.initContext(User.getSystem());

		String path = _group.getGroupFullPath();

		String[] sGroupNames = getGroupInfos(path)[1];
		String[] sGroupIds = getGroupInfos(path)[0];

		Group topGroup = getGroupWithNameAndParentId(sGroupNames[0], 0);
		try {
			if (topGroup == null) {
				topGroup = saveParentGrp(topGroup, sGroupNames[0],
						sGroupIds[0], 0);
			}
			Group parent = topGroup;
			for (int i = 1; i < sGroupNames.length - 1; i++) {
				Group childGroup = getGroupWithNameAndParentId(sGroupNames[i],
						parent.getId());

				if (childGroup == null) {
					childGroup = saveParentGrp(childGroup, sGroupNames[i],
							sGroupIds[i], parent.getId());
				}
				parent = childGroup;
			}

			Group currGroup = getGroupWithNameAndParentId(
					_group.getGroupDisplayName(), parent.getId());

			saveGroup(_group, currGroup, parent.getId());

			if (logger.isDebugEnabled()) {
				logger.debug("Add group [" + _group.getGroupDisplayName()
						+ "] success！");
			}

			return true;
		} catch (Exception e) {
			logger.error("Add group fail!", e);
		}
		return false;
	}

	private Group saveParentGrp(Group currGroup, String _sGName,
			String _sInIdsId, int _nParentId) throws WCMException {
		if (currGroup == null) {
			currGroup = Group.createNewInstance();
			ContextHelper.initContext(User.getSystem());
			User currUser = ContextHelper.getLoginUser();
			currGroup.setName(_sGName);
			currGroup.setProperty("NAMEINIDS", _sInIdsId);
			currGroup.setParent(_nParentId);
			currGroup.setCrUser(currUser);
			currGroup.save(currUser);
		}

		return currGroup;
	}

	public boolean updateGroup(SSOGroup _group, HttpServletRequest req) {
		ContextHelper.initContext(User.getSystem());
		Group currGroup = null;
		try {
			currGroup = getGroupFromIdsId(_group.getGroupId());

			saveGroup(_group, currGroup, currGroup.getParentId());

			if (logger.isDebugEnabled()) {
				logger.debug("Update group [" + _group.getGroupDisplayName()
						+ "] success！");
			}

			return true;
		} catch (Exception e) {
			logger.error("update group fail ~ ", e);
		}
		return false;
	}

	public boolean delGroup(SSOGroup _group, HttpServletRequest req) {
		ContextHelper.initContext(User.getSystem());
		Group currGroup = getGroupFromIdsId(_group.getGroupId());
		if (currGroup == null) {
			logger.error("path of the group is not exist![path="
					+ _group.getGroupFullPath() + "]");
			return false;
		}
		try {
			GroupMgr gMgr = new GroupMgr();
			gMgr.delete(currGroup);
			if (logger.isDebugEnabled()) {
				logger.debug("Delete group [" + _group.getGroupDisplayName()
						+ "] success！");
			}

			return true;
		} catch (Exception e) {
			logger.error("delete group fail,groupname=" + currGroup.getName(),
					e);
		}
		return false;
	}

	private String[][] getGroupInfos(String groupPath) {
		if (groupPath == null) {
			return new String[0][0];
		}
		String[] paths = groupPath.split("/");
		String[][] names = new String[2][paths.length];
		for (int i = 0; i < paths.length; i++) {
			names[0][i] = paths[i].split("#")[0];
			names[1][i] = paths[i].split("#")[1];
		}
		return names;
	}

	private Group saveGroup(SSOGroup _group, Group currGroup, int parentId)
			throws WCMException {
		ContextHelper.initContext(User.getSystem());

		if (currGroup == null) {
			currGroup = Group.createNewInstance();
			currGroup.setParent(parentId);
			currGroup.setCrUser(ContextHelper.getLoginUser());
		}
		currGroup.setName(_group.getGroupDisplayName());

		//currGroup.setDesc(_group.getProperty("IDSEXT_groupCode"));
		currGroup.setDesc(_group.getProperty("IDSEXT_UNITCODE"));
		currGroup.setEmail(_group.getProperty("groupMail"));

		currGroup.setProperty("NAMEINIDS", _group.getGroupId());

		Properties properties = _group.getGroupProperty();
		Set names = properties.keySet();

		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();

			if (!name.startsWith("IDSEXT_")) {
				continue;
			}
			String sName = name.substring(7);
			if (DBManager.getDBManager().getFieldInfo("WCMGROUP", sName) == null)
				currGroup.setAttribute(sName, _group.getProperty(name));
			else {
				currGroup.setProperty(sName, _group.getProperty(name));
			}
		}
		ActorHelper.getGroupMgr().save(currGroup);
		return currGroup;
	}

	public Group getGroupByPath(String[] groupNames) {
		Group topGroup = getGroupWithNameAndParentId(groupNames[0], 0);
		if (topGroup == null)
			return null;
		Group parent = topGroup;
		for (int i = 1; i < groupNames.length; i++) {
			Group childGroup = getGroupWithNameAndParentId(groupNames[i],
					parent.getId());
			if (childGroup == null)
				return null;
			parent = childGroup;
		}
		return parent;
	}

	private Group getGroupWithNameAndParentId(String _sGName, int _nParentId) {
		String sWhere = "gname=? and parentid=?";
		try {
			WCMFilter filter = new WCMFilter("", sWhere, "");
			filter.addSearchValues(_sGName);
			filter.addSearchValues(_nParentId);
			Groups groups = Groups.openWCMObjs(User.getSystem(), filter);
			if (groups.size() > 0) {
				return (Group) groups.getAt(0);
			}
			return null;
		} catch (Exception e) {
		}
		return null;
	}

	public boolean moveToGroup(SSOUser _user, SSOGroup _group) {
		ContextHelper.initContext(User.getSystem());
		if ((_user == null) || (_group == null)) {
			logger.info("传入 的用户和组织为空！");
			return false;
		}

		User currUser = ActorHelper.getUserFromSSOUser(_user);
		Group currGroup = ActorHelper.getGroupFromSSOGroup(_group);
		if ((currUser == null) || (currGroup == null)) {
			logger.error("没有找到用户或者没有找到组织！");
			return false;
		}
		try {
			ActorHelper.getGroupMgr().addUser(currUser, currGroup);
		} catch (Exception e) {
			logger.error("添加用户到组织失败！[UserName=" + currUser.getName()
					+ ",GroupName=" + currGroup.getName() + "]", e);
			return false;
		}

		return true;
	}

	public boolean removeFromGroup(SSOUser _user, SSOGroup _group) {
		ContextHelper.initContext(User.getSystem());

		User currUser = ActorHelper.getUserFromSSOUser(_user);
		Group currGroup = ActorHelper.getGroupFromSSOGroup(_group);
		if ((currUser == null) || (currGroup == null)) {
			logger.error("没有找到用户或者没有找到组织！");
			return false;
		}
		try {
			ActorHelper.getGroupMgr().removeUser(currUser, currGroup);
			if (logger.isDebugEnabled())
				logger.error("将用户[" + currUser.getName() + "]从组织["
						+ currGroup.getName() + "]删除成功！");
		} catch (Exception e) {
			logger.error("将用户从组织中删除失败！[UserName=" + currUser.getName()
					+ ",GroupName=" + currGroup.getName() + "]", e);
			return false;
		}
		return true;
	}
}
