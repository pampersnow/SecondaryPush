package com.trs.example.flow.destroytask;

import com.trs.DreamFactory;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.process.FlowContentFactory;
import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.IFlowServer;
import com.trs.cms.process.definition.Flow;
import com.trs.cms.process.engine.FlowContext;
import com.trs.components.wcm.content.persistent.ChnlDoc;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.components.wcm.resource.Status;
import com.trs.infra.common.WCMException;
import com.trs.infra.util.CMyString;
import com.trs.webframework.context.MethodContext;
import com.trs.webframework.provider.ISelfDefinedServiceProvider;

public class DestroyTaskServiceProvider implements ISelfDefinedServiceProvider {

    /**
     * 申请撤销发布
     * 
     * @param _oMethodContext
     *            必须传入以下参数：<br>
     *            ObjectType : 被撤销内容的类型 <br>
     *            ObjectIds ：被撤销内容的ID序列，以逗号分隔<br>
     *            ObjectTitles : 被撤销内容的标题序列，以~分隔<br>
     *            Desc ：撤销的理由<br>
     * @throws Exception
     */
    public void applyDestoryTask(MethodContext _oMethodContext)
            throws Exception {
        // 1 获取当前操作的用户
        User currUser = ContextHelper.getLoginUser();

        // 2 参数获取
        String sObjectIds = _oMethodContext.getValue("ObjectIds");
        if (CMyString.isEmpty(sObjectIds)) {
            throw new WCMException("没有指定要撤销的内容！");
        }

        String sObjectTitles = _oMethodContext.getValue("ObjectTitles");
        if (CMyString.isEmpty(sObjectTitles)) {
            throw new WCMException("没有指定要撤销内容的标题！");
        }

        int nObjectType = _oMethodContext.getValue("ObjectType", 0);
        if (nObjectType != ChnlDoc.OBJ_TYPE && nObjectType != Document.OBJ_TYPE) {
            throw new WCMException("目前只支持撤销文档时进入审核流程！");
        }

        String sDesc = _oMethodContext.getValue("Desc");
        if (CMyString.isEmpty(sDesc)) {
            throw new WCMException("没有填写撤销的理由！");
        }

        // 3 遍历所有要撤销的内容，逐个进入工作流，驱动审核
        int[] pObjectIds = CMyString.splitToInt(sObjectIds, ",");
        String[] pObjectTitles = sObjectTitles.split("~");
        for (int i = 0; i < pObjectIds.length; i++) {
            int nObjectId = pObjectIds[i];

            // 判断内容是否需要进入撤销流程，如果可以，那么返回一个工作流对象
            int nFlowIdOfSrcContent = 0;
            Flow oFlowOfSrcContent = getFlowOfSrcContent(nObjectType, nObjectId);
            // 状态不是已发或者数据不存在
            if (oFlowOfSrcContent == null)
                continue;
            if (oFlowOfSrcContent != null) {
                nFlowIdOfSrcContent = oFlowOfSrcContent.getId();
            }

            // 3.1 记录撤销申请
            DestroyTask task = DestroyTask.createNewInstance();
            task.setContent(nObjectType, nObjectId);
            task.setTitle(pObjectTitles[i]);
            task.setDesc(sDesc);
            task.setFlowId(nFlowIdOfSrcContent);
            task.save(currUser);

            // 3.2 进入工作流
            // 3.2.1 获取被撤销的工作流，如果没有配置工作流，就直接发出撤销请求
            if (nFlowIdOfSrcContent == 0) {
                DestroyTaskHelper.destoryDirect(nObjectType, nObjectId);
                continue;
            }

            // 3.2.2 进入撤销审核流程中
            IFlowContent content = FlowContentFactory.makeFlowContent(
                    DestroyTask.OBJ_TYPE, task.getId());
            IFlowServer flowServer = (IFlowServer) DreamFactory
                    .createObjectById("IFlowServer");
            FlowContext flowContext = new FlowContext(currUser, content);
            flowContext.setPostDesc(sDesc);
            flowServer.submitTo(flowContext);
        }
    }

    private Flow getFlowOfSrcContent(int _nObjectType, int _nObjectId)
            throws WCMException {
        Document document = null;
        int nDocId = 0;
        switch (_nObjectType) {
        case ChnlDoc.OBJ_TYPE:

            ChnlDoc chnlDoc = ChnlDoc.findById(_nObjectId);
            if (chnlDoc == null)
                return null;

            nDocId = chnlDoc.getDocId();
            if (chnlDoc.getStatusId() != Status.STATUS_ID_PUBLISHED) {
                return null;
            }
            document = Document.findById(nDocId);

            break;
        case Document.OBJ_TYPE:
            nDocId = _nObjectId;
            document = Document.findById(nDocId);
            if (document == null
                    || document.getStatusId() != Status.STATUS_ID_PUBLISHED) {
                return null;
            }
            break;
        default:
            break;
        }

        IFlowContent oScrFlowContent = FlowContentFactory
                .makeFlowContent(document);
        Flow flow = oScrFlowContent.getOwnerFlow();
        if (flow == null)
            return new Flow();

        return flow;

    }
}
