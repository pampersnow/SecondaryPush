/**
 * 
 */
package com.trs.example.flow.destroytask;

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.definition.Flow;
import com.trs.cms.process.engine.FlowDoc;
import com.trs.components.wcm.resource.Status;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;

/**
 * @author caohui
 * 
 */
public class DestroyTaskFlowContent implements IFlowContent {

    private DestroyTask m_oDestroyTask = null;

    private FlowDoc m_oCurrFlowDoc = null;

    /**
     * 
     */
    public DestroyTaskFlowContent() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.content.ICMSObjSubstanceContent#getType()
     */
    public int getType() {
        return DestroyTask.OBJ_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.content.ICMSObjSubstanceContent#getId()
     */
    public int getId() {
        return m_oDestroyTask.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.content.ICMSObjSubstanceContent#getSubstance()
     */
    public CMSObj getSubstance() {
        return m_oDestroyTask;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.content.ICMSObjSubstanceContent#setSubstance(com.trs.cms.
     * content.CMSObj)
     */
    public void setSubstance(CMSObj _oCmsObj) {
        m_oDestroyTask = (DestroyTask) _oCmsObj;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.content.ICMSObjSubstanceContent#getInfo()
     */
    public String getInfo() {
        return m_oDestroyTask.getTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.content.ICMSObjSubstanceContent#loadById(int)
     */
    public boolean loadById(int _nId) throws WCMException {
        m_oDestroyTask = DestroyTask.findById(_nId);
        return m_oDestroyTask != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.content.ICMSObjSubstanceContent#makeFrom(com.trs.cms.content
     * .CMSObj)
     */
    public void makeFrom(CMSObj _oCmsObj) throws WCMException {
        m_oDestroyTask = (DestroyTask) _oCmsObj;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#canEdit(com.trs.cms.auth.persistent.
     * User)
     */
    public boolean canEdit(User _oCurrUser) throws WCMException {
        return m_oDestroyTask.canEdit(_oCurrUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getOwnerFlow()
     */
    public Flow getOwnerFlow() throws WCMException {
        return Flow.findById(m_oDestroyTask.getFlowId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getDesc()
     */
    public String getDesc() throws WCMException {
        return m_oDestroyTask.getTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getContentType()
     */
    public int getContentType() {
        return getType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getSubinstanceId()
     */
    public int getSubinstanceId() throws WCMException {
        return getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#setSubinstance(int)
     */
    public void setSubinstance(int _nSubinstanceId) throws WCMException {
        loadById(_nSubinstanceId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#setSubinstance(com.trs.cms.content.CMSObj
     * )
     */
    public void setSubinstance(CMSObj _oSubinstance) throws WCMException {
        makeFrom(_oSubinstance);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getSubinstance()
     */
    public CMSObj getSubinstance() throws WCMException {
        return getSubstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#updateStatus(com.trs.cms.auth.persistent
     * .User, int)
     */
    public void updateStatus(User _oUser, int _oStatusId) throws WCMException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#cancelUpdate(com.trs.cms.auth.persistent
     * .User)
     */
    public void cancelUpdate(User _oUser) throws WCMException {
        m_oDestroyTask.cancelUpdate(_oUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getCrUser()
     */
    public User getCrUser() throws WCMException {
        return m_oDestroyTask.getCrUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getStatusId()
     */
    public int getStatusId() throws WCMException {
        /* 表示撤销正在审批中 */
        return 26;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getStatusDesc()
     */
    public String getStatusDesc() throws WCMException {
        Status status = Status.findById(getStatusId());
        if (status == null)
            return "系统更新错误，没有增加相应的状态";
        return status.getDisp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getContentShowPage()
     */
    public String getContentShowPage() throws WCMException {
        return "../application/destroytask/destroytask_detail.jsp?ObjectId="
                + this.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getDeletePage()
     */
    public String getDeletePage() throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getPublishPage()
     */
    public String getPublishPage() throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getContentAddEditPage()
     */
    public String getContentAddEditPage() throws WCMException {
        return "../application/destroytask/destroytask_submit.jsp?ObjectId="
                + this.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#setFlowDoc(com.trs.cms.process.engine
     * .FlowDoc)
     */
    public void setFlowDoc(FlowDoc _oFlowDoc) {
        m_oCurrFlowDoc = _oFlowDoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getFlowDoc()
     */
    public FlowDoc getFlowDoc() {
        return m_oCurrFlowDoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getProperty(java.lang.String)
     */
    public String getProperty(String _sName) throws WCMException {
        return m_oDestroyTask.getPropertyAsString(_sName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#setStartTime(com.trs.infra.util.CMyDateTime
     * )
     */
    public void setStartTime(CMyDateTime _o_dtStartTime) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getStartTime()
     */
    public CMyDateTime getStartTime() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#setSerialNum(long)
     */
    public void setSerialNum(long _o_lSerialNum) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#getSerialNum()
     */
    public long getSerialNum() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.cms.process.IFlowContent#isDeleted()
     */
    public boolean isDeleted() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.cms.process.IFlowContent#setFlow(com.trs.cms.process.definition
     * .Flow)
     */
    public void setFlow(Flow _oFlow) {
        try {
            String[] pUpdateSQL = { //
            "update " + DestroyTask.DB_TABLE_NAME + " set FlowId=? where "
                    + DestroyTask.DB_ID_NAME + "=?" //
            };
            int[] pParameters = { _oFlow.getId(), m_oDestroyTask.getId() };
            DBManager.getDBManager().sqlExecuteUpdate(pUpdateSQL, pParameters);

            m_oDestroyTask.refreshProperty("FlowId", _oFlow.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
