package com.trs.cms.process;

import com.trs.DreamFactory;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.cms.process.definition.Flow;
import com.trs.cms.process.definition.FlowNode;
import com.trs.cms.process.definition.FlowNodeBranch;
import com.trs.cms.process.definition.FlowNodeBranchs;
import com.trs.cms.process.engine.FlowContext;
import com.trs.cms.process.engine.FlowDoc;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyDateTime;

public class SubmitFlow implements IFlowContent{

	public void submitNewFlow(Document currObj,User currUser,String desc,String flowType)throws Exception{
		
		IFlowServer flowServer = (IFlowServer) DreamFactory.createObjectById("IFlowServer");
		
		IFlowContent flowContent = FlowContentFactory.makeFlowContent(currObj);
	
		FlowContext flowContext = null;
		// 判断文档是否设置了工作流
		if (flowContent.getOwnerFlow() != null) {
			return;
		}

		flowContext = new FlowContext(currUser, flowContent,desc,null, Flow.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue(flowType, "0"))));
	
		flowServer.submitTo(flowContext);
	}
	
	public void submitBeFlow(User currUser,String FlowDocId,String desc,String flowType)throws Exception{
		//获取待办信息
		FlowDoc flowDoc = FlowDoc.findById(Integer.parseInt(FlowDocId));
		//设置状态为处理过
		flowDoc.setWorked(true);
		//更新旧文档状态
		flowDoc.update();
		//获取流转文档
		IFlowContent oldFlowContent = flowDoc.getFlowContent();
        //转换成文档对象
        Document currObj = (Document)oldFlowContent.getSubinstance();
        FlowNode nextNode = null;
        //获取当前节点
        FlowNode currNode = flowDoc.getNode();
        //获取当前节点分支
        FlowNodeBranchs branchs = currNode.getBranchs(currUser);
        for(int i = 0;i < branchs.size();i++){
        	FlowNodeBranch branch = (FlowNodeBranch) branchs.getAt(i);
        	System.out.println("<<<往【"+branch.getNextNode(currUser).getName()+"】提交>>>");
        	//获取下一节点
        	nextNode = branch.getNextNode(currUser);
        }
		IFlowServer flowServer = (IFlowServer) DreamFactory.createObjectById("IFlowServer");
		
		IFlowContent flowContent = FlowContentFactory.makeFlowContent(currObj);
	
		FlowContext flowContext = null;
		// 判断文档是否设置了工作流
		if (flowContent.getOwnerFlow() != null) {
			return;
		}
		
		flowContext = new FlowContext(flowDoc.getToUser(), flowContent,desc,null, Flow.findById(Integer.parseInt(ConfigServer.getServer().getSysConfigValue(flowType, "0"))));
		
		flowContext.setNextNode(nextNode);
		
		flowServer.submitTo(flowContext);
	}
	
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public CMSObj getSubstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean loadById(int arg0) throws WCMException {
		// TODO Auto-generated method stub
		return false;
	}

	public void makeFrom(CMSObj arg0) throws WCMException {
		// TODO Auto-generated method stub
		
	}

	public void setSubstance(CMSObj arg0) {
		// TODO Auto-generated method stub
		
	}

	public boolean canEdit(User arg0) throws WCMException {
		// TODO Auto-generated method stub
		return false;
	}

	public void cancelUpdate(User arg0) throws WCMException {
		// TODO Auto-generated method stub
		
	}

	public String getContentAddEditPage() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContentShowPage() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getContentType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public User getCrUser() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDeletePage() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDesc() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public FlowDoc getFlowDoc() {
		// TODO Auto-generated method stub
		return null;
	}

	public Flow getOwnerFlow() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProperty(String arg0) throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPublishPage() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public long getSerialNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	public CMyDateTime getStartTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStatusDesc() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStatusId() throws WCMException {
		// TODO Auto-generated method stub
		return 0;
	}

	public CMSObj getSubinstance() throws WCMException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSubinstanceId() throws WCMException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFlow(Flow arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setFlowDoc(FlowDoc arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setSerialNum(long arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setStartTime(CMyDateTime arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setSubinstance(int arg0) throws WCMException {
		// TODO Auto-generated method stub
		
	}

	public void setSubinstance(CMSObj arg0) throws WCMException {
		// TODO Auto-generated method stub
		
	}

	public void updateStatus(User arg0, int arg1) throws WCMException {
		// TODO Auto-generated method stub
		
	}

	/*
	 * 获取流转人员对象集合
	 * */
	/*public Users getToUsers(User currUser)throws WCMException{
		Users users = null;
		RoleMgr rm = new RoleMgr();
		Role role = rm.findRoleByName("事业部经理");
		return role.getUsers(currUser);
	}*/
	
}
