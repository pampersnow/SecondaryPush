package com.trs.cms.process;

//import com.trs.cms.auth.persistent.Group;
//import com.trs.cms.auth.persistent.Role;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.process.definition.FlowAction;
import com.trs.cms.process.engine.ExecuteContext;
import com.trs.cms.process.engine.IActionHandler;
import com.trs.components.common.message.Message;
import com.trs.components.common.message.MessageServer;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.util.CMyDateTime;

public class SendMessage implements IActionHandler{

	public SendMessage() {

	}

	public boolean execute(ExecuteContext _context, FlowAction _action)
			throws WCMException {
		//声明文档对象
		Document document = null;
		IFlowContent flowContent = _context.getFlowContent();
		if(flowContent.getSubinstance() instanceof Document){
			//判断工作流中是否为文档
			document = (Document)flowContent.getSubinstance();
		}
		try {
			//获取上一步操作的操作人
			/*Users users = _context.getFlowContext().getCurrFlowDoc().getPreNode().getOperUsers(currentUser);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < users.size(); i++) {
				sb.append(users.getAt(i).getId() + ",");
			}*/
			User preUser = _context.getFlowContext().getCurrFlowDoc().getPostUser();
			//获取工作流中文档标题
			MetaViewData mvd = MetaViewData.findById(document.getId()); 
			//实例化消息对象
			Message msg = Message.createNewInstance();
			msg.setTitle("工作消息提醒——"+mvd.getPropertyAsString("WGMCZW")+"[文档-"+document.getId()+"]");
			msg.setBody("文稿被拒绝");
			msg.setSendTypes(new String[] { "Message" });
			msg.setFlag(Message.FLAG_NODELETE);
			msg.setValidTime(CMyDateTime.now());
			msg.setReceivers(User.OBJ_TYPE, preUser.getId() + ",");
			/*msg.setReceivers(Group.OBJ_TYPE, "5,6,7");
			msg.setReceivers(Role.OBJ_TYPE, "1,3");
			//发送消息
*/			MessageServer.send(msg);

		} catch (WCMException ex) {
			//s_logger.debug("send a message test failed:", ex);
			//fail("测试消息发送失败:" + ex.getStackTraceText());
		} catch (Exception ex2) {
			//s_logger.debug("send a message test failed:", ex2);
			//fail("测试消息发送失败:" + ex2.getMessage());
		}
		return true;
	}

}
