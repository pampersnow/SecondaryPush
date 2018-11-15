package com.trs.task;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.2
 * @version			5.2
 *
 * <p>Created:         2010/12/22 11:25:31</p>
 * <p>Last Modified:   2010/12/22 11:28:21</p>
 * <p>Description:
 *      class TaskInfos ���� TaskInfo���϶���Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2010/12/22 11:25:31 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;


public class TaskInfos extends CMSBaseObjs{

    /**
     * ���캯��
     * 
     * @see public TaskInfos( User _currUser, int
     *      _initCapacity, int _incCapacity );
     */
    public TaskInfos(User _currUser) {
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
    public TaskInfos(User _currUser, int _initCapacity, int _incCapacity) {
        super(_currUser, _initCapacity, _incCapacity);
    }

    //=============================================================
    //���ظ����ж���ĳ���ӿں���

    /**
     * ȡ�������ݴ洢�����ݱ�����
     * 
     * @return �������ݴ洢�����ݱ�����
     */
    protected String getDbTableName() {
        return TaskInfo.DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return TaskInfo.DB_ID_NAME;
    }

    /**
     * ȡ����Ԫ����
     * 
     * @return ����Ԫ����
     */
    public Class getElementClass() {
        return TaskInfo.class;
    }

    

    //==============================================================
    //���ϲ���

    //==============================================================
    // XML����/����

    //==============================================================================
    //�߼�����
    /**
     * ��ȡָ��ID���еĶ��󼯺�
     * 
     * @param _sIds
     *            ָ���Ķ���ID����
     * @return ָ�����󼯺ϣ�TaskInfos��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static TaskInfos findByIds(User _currUser, String _sIds) {
        TaskInfos currTaskInfos = createNewInstance(_currUser);
        //���ID����Ч��
        if (_sIds == null || _sIds.length() <= 0) {
            return currTaskInfos;
        }

        currTaskInfos.addElement(_sIds);

        //��ȡָ��ID�Ķ���
        return currTaskInfos;
    }//END: findById()

    /**
     * ͨ��ָ��һ��Filter��ȡ��ǰ����[TaskInfos]
     * 
     * @param _filter
     *            ָ����Filter
     * @return ��ǰ����[TaskInfos]
     * @throws WCMException
     */
    public final static TaskInfos openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        TaskInfos currTaskInfos = createNewInstance(_currUser);
        currTaskInfos.open(_filter);
        return currTaskInfos;
    }

    /**
     * ������ǰ���϶���[TaskInfos]��ʵ��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @return
     * @throws WCMException
     */
    public static TaskInfos createNewInstance(User _currUser) {
        return new TaskInfos(_currUser);
    }

    /* (non-Javadoc)
     * @see com.trs.infra.persistent.BaseObjs#newInstance()
     */
    public BaseObjs newInstance() throws Exception {
        return new TaskInfos(this.currUser);
    }
}