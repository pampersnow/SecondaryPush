package com.trs.example.flow.destroytask;

import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.definition.FlowAction;
import com.trs.cms.process.engine.ExecuteContext;
import com.trs.cms.process.engine.handlers.PublishContentActionHandler;
import com.trs.infra.common.WCMException;

public class PublishContentActionHandlerContainsDestory extends
        PublishContentActionHandler {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(PublishContentActionHandlerContainsDestory.class);

    public PublishContentActionHandlerContainsDestory() {
        // TODO Auto-generated constructor stub
    }

    public boolean execute(ExecuteContext _context, FlowAction _action)
            throws WCMException {
        // 判断如果是撤销申请，自动发布行为变为撤销发布
        IFlowContent content = _context.getFlowContent();
        if (content.getContentType() == DestroyTask.OBJ_TYPE) {
            DestroyTask task = (DestroyTask) content.getSubinstance();

            try {
                DestroyTaskHelper.destoryDirect(task.getContentType(),
                        task.getContentId());
            } catch (Exception e) {
                logger.error("撤销申请通过，但是执行撤销发布失败！", e);
                return false;
            }

            return true;
        }

        return super.execute(_context, _action);
    }

}
