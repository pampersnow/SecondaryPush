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
     * ���볷������
     * 
     * @param _oMethodContext
     *            ���봫�����²�����<br>
     *            ObjectType : ���������ݵ����� <br>
     *            ObjectIds �����������ݵ�ID���У��Զ��ŷָ�<br>
     *            ObjectTitles : ���������ݵı������У���~�ָ�<br>
     *            Desc ������������<br>
     * @throws Exception
     */
    public void applyDestoryTask(MethodContext _oMethodContext)
            throws Exception {
        // 1 ��ȡ��ǰ�������û�
        User currUser = ContextHelper.getLoginUser();

        // 2 ������ȡ
        String sObjectIds = _oMethodContext.getValue("ObjectIds");
        if (CMyString.isEmpty(sObjectIds)) {
            throw new WCMException("û��ָ��Ҫ���������ݣ�");
        }

        String sObjectTitles = _oMethodContext.getValue("ObjectTitles");
        if (CMyString.isEmpty(sObjectTitles)) {
            throw new WCMException("û��ָ��Ҫ�������ݵı��⣡");
        }

        int nObjectType = _oMethodContext.getValue("ObjectType", 0);
        if (nObjectType != ChnlDoc.OBJ_TYPE && nObjectType != Document.OBJ_TYPE) {
            throw new WCMException("Ŀǰֻ֧�ֳ����ĵ�ʱ����������̣�");
        }

        String sDesc = _oMethodContext.getValue("Desc");
        if (CMyString.isEmpty(sDesc)) {
            throw new WCMException("û����д���������ɣ�");
        }

        // 3 ��������Ҫ���������ݣ�������빤�������������
        int[] pObjectIds = CMyString.splitToInt(sObjectIds, ",");
        String[] pObjectTitles = sObjectTitles.split("~");
        for (int i = 0; i < pObjectIds.length; i++) {
            int nObjectId = pObjectIds[i];

            // �ж������Ƿ���Ҫ���볷�����̣�������ԣ���ô����һ������������
            int nFlowIdOfSrcContent = 0;
            Flow oFlowOfSrcContent = getFlowOfSrcContent(nObjectType, nObjectId);
            // ״̬�����ѷ��������ݲ�����
            if (oFlowOfSrcContent == null)
                continue;
            if (oFlowOfSrcContent != null) {
                nFlowIdOfSrcContent = oFlowOfSrcContent.getId();
            }

            // 3.1 ��¼��������
            DestroyTask task = DestroyTask.createNewInstance();
            task.setContent(nObjectType, nObjectId);
            task.setTitle(pObjectTitles[i]);
            task.setDesc(sDesc);
            task.setFlowId(nFlowIdOfSrcContent);
            task.save(currUser);

            // 3.2 ���빤����
            // 3.2.1 ��ȡ�������Ĺ����������û�����ù���������ֱ�ӷ�����������
            if (nFlowIdOfSrcContent == 0) {
                DestroyTaskHelper.destoryDirect(nObjectType, nObjectId);
                continue;
            }

            // 3.2.2 ���볷�����������
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
