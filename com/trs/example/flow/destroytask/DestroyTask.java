package com.trs.example.flow.destroytask;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.12
 * @version			5.2
 *
 * <p>Created:         2011/10/11 16:52:50</p>
 * <p>Last Modified:   2011/10/12 11:28:47</p>
 * <p>Description:
 *      class DestroyTask ���� DestroyTask����Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2011/10/11 16:52:50 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.ExceptionNumber;

public class DestroyTask extends CMSObj {

    /** �������ͱ�� */
    public final static int OBJ_TYPE = 1911367336;

    /** �������ݴ洢�����ݿ��� */
    public final static String DB_TABLE_NAME = "XWCMDESTROYTASK";

    /** �������ݴ洢��ID�ֶ��� */
    public final static String DB_ID_NAME = "DESTROYTASKID";

    /** ���캯��������Ĭ�Ͻӿ� */
    public DestroyTask() {
        super();
    }

    // ==============================================================================
    // ���ظ����еĳ���ӿں���

    /**
     * ȡ�������ݴ洢�����ݱ�����
     * 
     * @return �������ݴ洢�����ݱ�����
     */
    public String getDbTableName() {
        return DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return DB_ID_NAME;
    }

    /**
     * ȡ�øö�������ͱ��
     * 
     * @return ��������ͱ��
     */
    public int getWCMType() {
        return OBJ_TYPE;
    }

    // ==============================================================================
    // ���Զ�д����

    /**
     * ��ȡ���ԣ�Title
     * 
     * @return Title
     */
    public String getTitle() {
        return this.getPropertyAsString("DTITLE");
    }

    /**
     * �������ԣ�Title
     * 
     * @param _sTitle
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setTitle(String _sTitle) throws WCMException {
        return this.setProperty("DTITLE", _sTitle);
    }

    /**
     * ��ȡ���ԣ�Desc
     * 
     * @return Desc
     */
    public String getDesc() {
        return this.getPropertyAsString("DDESC");
    }

    /**
     * �������ԣ�Desc
     * 
     * @param _sDesc
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setDesc(String _sDesc) throws WCMException {
        return this.setProperty("DDESC", _sDesc);
    }

    public CMSObj getContent() throws WCMException {
        return (CMSObj) CMSObj.findById(getContentType(), getContentId());
    }

    /**
     * @param _nContentType
     * @param _nContentId
     * @return
     * @throws WCMException
     */
    public boolean setContent(int _nContentType, int _nContentId)
            throws WCMException {
        return setContentType(_nContentType) && setContentId(_nContentId);
    }

    /**
     * ��ȡ���ԣ�ContentType
     * 
     * @return ContentType
     */
    public int getContentType() {
        return this.getPropertyAsInt("CONTENTTYPE", 0);
    }

    /**
     * �������ԣ�ContentType
     * 
     * @param _nContentType
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setContentType(int _nContentType) throws WCMException {
        return this.setProperty("CONTENTTYPE", _nContentType);
    }

    /**
     * ��ȡ���ԣ�ContentId
     * 
     * @return ContentId
     */
    public int getContentId() {
        return this.getPropertyAsInt("CONTENTID", 0);
    }

    /**
     * �������ԣ�ContentId
     * 
     * @param _nContentId
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setContentId(int _nContentId) throws WCMException {
        return this.setProperty("CONTENTID", _nContentId);
    }

    /**
     * ��ȡ���ԣ�CurrStep
     * 
     * @return CurrStep
     */
    public String getCurrStep() {
        return this.getPropertyAsString("DCURRSTEP");
    }

    /**
     * �������ԣ�CurrStep
     * 
     * @param _sCurrStep
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setCurrStep(String _sCurrStep) throws WCMException {
        return this.setProperty("DCURRSTEP", _sCurrStep);
    }

    /**
     * ��ȡ���ԣ�FlowId
     * 
     * @return FlowId
     */
    public int getFlowId() {
        return this.getPropertyAsInt("FLOWID", 0);
    }

    /**
     * �������ԣ�FlowId
     * 
     * @param _nFlowId
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setFlowId(int _nFlowId) throws WCMException {
        return this.setProperty("FLOWID", _nFlowId);
    }

    /**
     * ���ԣ�CrUser �����û� �ɸ���WCMObj�ṩ�ӿ�֧�֡�
     */

    /**
     * ���ԣ�CrTime ����ʱ�� �ɸ���WCMObj�ṩ�ӿ�֧�֡�
     */

    // ==============================================================================
    // ���ݿ����

    /**
     * �������������Ч��
     * 
     * @return ����������������Ч���򷵻�true�����򷵻�false.
     * @throws WCMException
     *             �����������ò�������������ֵ����ȷ��������ظ������׳��쳣��
     */
    public boolean isValid() throws WCMException {
        if (isAddMode()) {
            // ���ǿ������Ƿ�����

            if (getProperty("DTITLE") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����Titleû������(DestroyTask.isValid)");
            }
            if (getProperty("DDESC") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����Descû������(DestroyTask.isValid)");
            }
            if (getProperty("CONTENTTYPE") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����ContentTypeû������(DestroyTask.isValid)");
            }
            if (getProperty("CONTENTID") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����ContentIdû������(DestroyTask.isValid)");
            }
            if (getProperty("FLOWID") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����FlowIdû������(DestroyTask.isValid)");
            }
        } else { // �༭ģʽ����������ʱ���Ѿ�����У�飡
            if (!this.isModified()) {
                throw new WCMException(
                        ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
                        "��������û�и���(DestroyTask.isValid)");
            }
        }// end if

        return true;
    }// END: isValid()

    /**
     * ����������д�����ݿ�
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @throws WCMException
     *             д������ʧ�ܣ����׳��쳣��
     */
    public void insert(User _currUser) throws WCMException {
        if (isAddMode() && this.isModified()) {
            // �������Ե�ȱʡֵ

            super.insert(_currUser); // insert into Db
        }// end if
    }

    // ==============================================================================
    // �߼�����

    /**
     * ��ȡָ��ID�Ķ���
     * 
     * @param _nId
     *            ָ���Ķ���ID
     * @return ���ҵ�ָ��ID�Ķ����򷵻ض���ʵ�壻���򣬷���null��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static DestroyTask findById(int _nId) throws WCMException {
        // ���ID����Ч��
        if (_nId <= 0) {
            return null;
        }

        // ��ȡָ��ID�Ķ���
        return (DestroyTask) BaseObj.findById(DestroyTask.class, _nId);

    }// END: findById()

    /**
     * ������ǰ����[DestroyTask]��ʵ��
     * 
     * @return ��ǰ�����ʵ��[DestroyTask]
     * @throws WCMException
     */
    public final static DestroyTask createNewInstance() throws WCMException {
        return (DestroyTask) BaseObj.createNewInstance(DestroyTask.class);
    }

    /**
     * ��ȡָ��������Key�Ķ���
     * 
     * @param _oKey
     *            ָ����������Key
     * @return ���ҵ�������Key�Ķ����򷵻ض���ʵ�壻���򣬷���null��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static DestroyTask findByKey(Object _oKey) throws WCMException {
        return (DestroyTask) BaseObj.findByKey(DestroyTask.class, _oKey);
    }

}