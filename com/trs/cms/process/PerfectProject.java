package com.trs.cms.process;

import com.trs.cms.process.definition.FlowAction;
import com.trs.cms.process.engine.ExecuteContext;
import com.trs.cms.process.engine.IActionHandler;
import com.trs.infra.common.WCMException;

public class PerfectProject implements IActionHandler{

	public boolean execute(ExecuteContext arg0, FlowAction arg1)
			throws WCMException {
		System.out.println("开始进入工作流编辑文档");
		/*//声明文档
		Document document = null;
		//获取工作流中文档信息
		IFlowContent flowContent = _context.getFlowContent();
		//实例化文档
		if(flowContent.getSubinstance() instanceof Document){
			//判断工作流中是否为文档
			document = (Document)flowContent.getSubinstance();
		}
		//获取下一节点名称
		String nodeName = _context.getFlowContext().getBranch().getNextNodeName();
		//判断操作
		if("退回发布人".equals(nodeName)){
			//获取该文档的所有附件
			Appendixes appendixes = Appendixes.findAppendixesByObj(document);
			//删除所有附件
			appendixes.removeAll();
		}else if("结束".equals(nodeName)){
			this.create_statistics(document);
		}*/
		return true;
	}

}
