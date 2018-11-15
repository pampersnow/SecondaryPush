package com.trs.example.flow.destroytask;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.2
 * @version			5.2
 *
 * <p>Created:         2011/10/11 16:52:50</p>
 * <p>Last Modified:   2011/10/11 17:03:02</p>
 * <p>Description:
 *      class DestroyTasks ���� DestroyTask���϶���Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2011/10/11 16:52:50 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;

public class DestroyTasks extends CMSBaseObjs {

    /**
     * ���캯��
     * 
     * @see public DestroyTasks( User _currUser, int _initCapacity, int
     *      _incCapacity );
     */
    public DestroyTasks(User _currUser) {
        super(_currUser);
    }

    /**
     * ���캯��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @param _initCapacity
     *            ��ʼ������
     * @param _incCapacity
     *            ����������ʱ��ÿ��������
     */
    public DestroyTasks(User _currUser, int _initCapacity, int _incCapacity) {
        super(_currUser, _initCapacity, _incCapacity);
    }

    // =============================================================
    // ���ظ����ж���ĳ���ӿں���

    /**
     * ȡ�������ݴ洢�����ݱ�����
     * 
     * @return �������ݴ洢�����ݱ�����
     */
    protected String getDbTableName() {
        return DestroyTask.DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return DestroyTask.DB_ID_NAME;
    }

    /**
     * ȡ����Ԫ����
     * 
     * @return ����Ԫ����
     */
    public Class getElementClass() {
        return DestroyTask.class;
    }

    // ==============================================================
    // ���ϲ���

    // ==============================================================
    // XML����/����

    // ==============================================================================
    // �߼�����
    /**
     * ��ȡָ��ID���еĶ��󼯺�
     * 
     * @param _sIds
     *            ָ���Ķ���ID����
     * @return ָ�����󼯺ϣ�DestroyTasks��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static DestroyTasks findByIds(User _currUser, String _sIds) {
        DestroyTasks currDestroyTasks = createNewInstance(_currUser);
        // ���ID����Ч��
        if (_sIds == null || _sIds.length() <= 0) {
            return currDestroyTasks;
        }

        currDestroyTasks.addElement(_sIds);

        // ��ȡָ��ID�Ķ���
        return currDestroyTasks;
    }// END: findById()

    /**
     * ͨ��ָ��һ��Filter��ȡ��ǰ����[DestroyTasks]
     * 
     * @param _filter
     *            ָ����Filter
     * @return ��ǰ����[DestroyTasks]
     * @throws WCMException
     */
    public final static DestroyTasks openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        DestroyTasks currDestroyTasks = createNewInstance(_currUser);
        currDestroyTasks.open(_filter);
        return currDestroyTasks;
    }

    /**
     * ������ǰ���϶���[DestroyTasks]��ʵ��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @return
     * @throws WCMException
     */
    public static DestroyTasks createNewInstance(User _currUser) {
        return new DestroyTasks(_currUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.infra.persistent.BaseObjs#newInstance()
     */
    public BaseObjs newInstance() throws Exception {
        return new DestroyTasks(this.currUser);
    }
}