package com.trs.dzda;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.center.MetaViewDatas;
import com.trs.webframework.context.MethodContext;
import com.trs.webframework.controler.JSPRequestProcessor;
import com.trs.webframework.provider.ISelfDefinedServiceProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class UserServiceProvider implements ISelfDefinedServiceProvider {
	private static Logger logger = Logger.getLogger(UserServiceProvider.class);
	
	public String login(MethodContext _oMethodContext) throws Throwable {
		String sResult = "error";

		int nChannelId = _oMethodContext.getValue("ChannelId", 0);
		String sUserName = _oMethodContext.getValue("userName");
		String sPassWord = _oMethodContext.getValue("passWord");
		String sCryptPwd = Common.cryptPassword(sPassWord);

		StringBuffer sWhere = new StringBuffer();
		sWhere.append("WCMMetaTableLoginUsers.username='" + sUserName + "'");
		sWhere.append(" and WCMMetaTableLoginUsers.password='" + sCryptPwd
				+ "'");
		sWhere.append(" and WCMMetaTableLoginUsers.status='30'");

		HashMap parameters = new HashMap();
		String sServiceId = "wcm61_metarecdata";
		String sMethodName = "queryViewDatas";
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);

		parameters.put("ChannelId", nChannelId);
		parameters.put("IsOr", "false");
		parameters.put("_sqlWhere_", sWhere.toString());
		parameters.put("ViewDataSelectFields","username,realname,email,mobile,lltime,piclink,integral");

		MetaViewDatas metadatas = (MetaViewDatas) processor.excute(sServiceId,
				sMethodName, parameters);
		if (metadatas.size() > 0) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(0);
			if (metadata == null)
				return sResult;
			return metadata.getPropertyAsString("username") + ","
					+ metadata.getPropertyAsString("realname") + ","
					+ metadata.getPropertyAsString("email") + ","
					+ metadata.getPropertyAsString("mobile") + ","
					+ metadata.getPropertyAsString("piclink") + ","
					+ metadata.getPropertyAsString("integral") + "," 
					+ metadata.getPropertyAsString("lltime");
		}
		return sResult;
	}

	public String verifyUser(MethodContext _oMethodContext) throws Throwable {
		String sResult = "error";

		int nChannelId = _oMethodContext.getValue("ChannelId", 0);
		String sUserName = _oMethodContext.getValue("userName");
		String sMobile = _oMethodContext.getValue("mobile");
		String sEmail = _oMethodContext.getValue("email");

		StringBuffer sWhere = new StringBuffer();
		sWhere.append("WCMMetaTableLoginUsers.username='" + sUserName + "'");
		sWhere.append(" and WCMMetaTableLoginUsers.mobile='" + sMobile + "'");
		sWhere.append(" and WCMMetaTableLoginUsers.email='" + sEmail + "'");

		HashMap parameters = new HashMap();
		String sServiceId = "wcm61_metarecdata";
		String sMethodName = "queryViewDatas";
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);

		parameters.put("ChannelId", nChannelId);
		parameters.put("IsOr", "false");
		parameters.put("_sqlWhere_", sWhere.toString());

		MetaViewDatas metadatas = (MetaViewDatas) processor.excute(sServiceId,
				sMethodName, parameters);
		if (metadatas.size() > 0) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(0);
			if (metadata == null)
				return sResult;
			return metadata.getPropertyAsString("MetaDataId");
		}
		return sResult;
	}

	public String resetPwd(MethodContext _oMethodContext) throws Throwable {
		String sObjectIds = _oMethodContext.getValue("ObjectIds");
		String sPassWord = _oMethodContext.getValue("passWord");
		String sCryptPwd = Common.cryptPassword(sPassWord);

		User loginUser = getLoginUser();
		try {
			MetaViewDatas metadatas = MetaViewDatas.findByIds(sObjectIds);
			for (int i = 0; i < metadatas.size(); i++) {
				MetaViewData metadata = (MetaViewData) metadatas.getAt(i);
				if (metadata != null) {
					metadata.validCanEditAndLock(loginUser);
					metadata.setProperty("password", sCryptPwd);
					metadata.save(loginUser);
				}
			}
			return "success";
		} catch (Exception e) {
			logger.error(e);
		}
		return "error";
	}

	public String addUser(MethodContext _oMethodContext) throws Throwable {
		int nChannelId = _oMethodContext.getValue("ChannelId", 0);
		String sUserName = _oMethodContext.getValue("userName");
		String sPassWord = _oMethodContext.getValue("passWord");
		String sMobile = _oMethodContext.getValue("mobile");
		String sEmail = _oMethodContext.getValue("email");
		String sRealName = _oMethodContext.getValue("realName");
		int nActive = _oMethodContext.getValue("active", 0);
		int nStatus = nActive == 1 ? 30 : 0;
		String sCryptPwd = Common.cryptPassword(sPassWord);

		if (checkUserName(sUserName, nChannelId)) {
			return "该用户名已经注册，请更改用户名后重试！";
		}

		HashMap parameters = new HashMap();
		String sServiceId = "wcm61_metaviewdata";
		String sMethodName = "saveMetaViewData";
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);

		parameters.put("ObjectId", "0");
		parameters.put("ChannelId", nChannelId);
		parameters.put("userName", sUserName);
		parameters.put("passWord", sCryptPwd);
		parameters.put("mobile", sMobile);
		parameters.put("email", sEmail);
		parameters.put("realName", sRealName);
		parameters.put("status", nStatus);
		parameters.put("integral", "20");

		processor.excute(sServiceId, sMethodName, parameters).toString();
		return "success";
	}

	public void changeStatus(MethodContext _oMethodContext) throws Throwable {
		String sObjectIds = _oMethodContext.getValue("ObjectIds");
		int nStatus = _oMethodContext.getValue("status", -1);

		User loginUser = getLoginUser();

		MetaViewDatas metadatas = MetaViewDatas.findByIds(sObjectIds);
		for (int i = 0; i < metadatas.size(); i++) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(i);
			if (metadata != null) {
				metadata.validCanEditAndLock(loginUser);
				metadata.setProperty("status", nStatus);
				metadata.save(loginUser);
			}
		}
	}

	public void updatePic(MethodContext _oMethodContext) throws Throwable {
		String sObjectIds = _oMethodContext.getValue("ObjectIds");
		String picLink = _oMethodContext.getValue("picLink");
		User loginUser = getLoginUser();
		MetaViewDatas metadatas = MetaViewDatas.findByIds(sObjectIds);
		for (int i = 0; i < metadatas.size(); i++) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(i);
			if (metadata != null) {
				metadata.validCanEditAndLock(loginUser);
				metadata.setProperty("piclink", picLink);
				metadata.save(loginUser);
			}
		}
	}
	
	public void upLoginTime(MethodContext _oMethodContext)throws Exception{
		String sObjectIds = _oMethodContext.getValue("ObjectIds");
		String loginTime = _oMethodContext.getValue("LoginTime");
		String integral = _oMethodContext.getValue("Integral");
		User loginUser = getLoginUser();
		MetaViewDatas metadatas = MetaViewDatas.findByIds(sObjectIds);
		for (int i = 0; i < metadatas.size(); i++) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(i);
			if (metadata != null) {
				metadata.validCanEditAndLock(loginUser);
				metadata.setProperty("lltime", loginTime);
				metadata.setProperty("integral", integral);
				metadata.save(loginUser);
			}
		}
	}

	private boolean checkUserName(String sUserName, int nChannelId)
			throws Throwable {
		HashMap parameters = new HashMap();
		String sServiceId = "wcm61_metarecdata";
		String sMethodName = "queryViewDatas";
		JSPRequestProcessor processor = new JSPRequestProcessor(null, null);

		parameters.put("ChannelId", nChannelId);
		parameters.put("IsOr", "false");
		parameters.put("_sqlWhere_", "WCMMetaTableLoginUsers.username='"
				+ sUserName + "'");

		MetaViewDatas metadatas = (MetaViewDatas) processor.excute(sServiceId,
				sMethodName, parameters);

		return metadatas.size() != 0;
	}

	private User getLoginUser() {
		User loginUser = ContextHelper.getLoginUser();
		return loginUser;
	}

	public String rechargeTheUser(MethodContext _oMethodContext)
			throws Throwable {
		String sResult = "";
		String sObjectIds = _oMethodContext.getValue("ObjectIds");
		String sAmount = _oMethodContext.getValue("Amount");
		User loginUser = getLoginUser();
		MetaViewDatas metadatas = MetaViewDatas.findByIds(sObjectIds);
		for (int i = 0; i < metadatas.size(); i++) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(i);
			if(metadata != null){
				metadata.validCanEditAndLock(loginUser);
				String integral = metadata.getPropertyAsString("integral");
				if(integral == null || "".equals(integral) || "null".equals(integral)){
					integral = "0";
				}
				sResult = (Integer.parseInt(integral) + Integer.parseInt(sAmount) * 10) + "";
				metadata.setProperty("integral", sResult);
				metadata.save(loginUser);
			}
		}
		return sResult;
	}
	
	public String getIntegralByUser(MethodContext _oMethodContext)throws Throwable {
		String sResult = "";
		String sObjectIds = _oMethodContext.getValue("ObjectIds");
		User loginUser = getLoginUser();
		MetaViewDatas metadatas = MetaViewDatas.findByIds(sObjectIds);
		for (int i = 0; i < metadatas.size(); i++) {
			MetaViewData metadata = (MetaViewData) metadatas.getAt(i);
			if(metadata != null){
				String integral = metadata.getPropertyAsString("integral");
				if(integral == null || "".equals(integral) || "null".equals(integral)){
					sResult = "0";
				}else{
					sResult = integral;
				}
			}
		}
		return sResult;
	}
}