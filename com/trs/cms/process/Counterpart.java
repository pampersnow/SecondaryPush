package com.trs.cms.process;

import com.trs.cms.process.definition.FlowAction;
import com.trs.cms.process.engine.ExecuteContext;
import com.trs.cms.process.engine.IActionHandler;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;

public class Counterpart implements IActionHandler {

	public Counterpart() {
	}

	public boolean execute(ExecuteContext _context, FlowAction _action)
			throws WCMException {
		// 声明文档
		Document document = null;
		// 获取工作流中文档信息
		IFlowContent flowContent = _context.getFlowContent();
		// 实例化文档
		if (flowContent.getSubinstance() instanceof Document) {
			// 判断工作流中是否为文档
			document = (Document) flowContent.getSubinstance();
		}
		// 获取该文档的所有附件
		Appendixes appendixes = Appendixes.findAppendixesByObj(document);
		// 删除所有附件
		appendixes.removeAll();

		return true;
	}
}
