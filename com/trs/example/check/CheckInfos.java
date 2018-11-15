package com.trs.example.check;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.2
 * @version			5.2
 *
 * <p>Created:         2012/5/31 10:03:01</p>
 * <p>Last Modified:   2012/5/31 13:21:56</p>
 * <p>Description:
 *      class CheckInfos ���� CheckInfo���϶���Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2012/5/31 10:03:01 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;


public class CheckInfos extends CMSBaseObjs{

    /**
     * ���캯��
     * 
     * @see public CheckInfos( User _currUser, int
     *      _initCapacity, int _incCapacity );
     */
    public CheckInfos(User _currUser) {
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
    public CheckInfos(User _currUser, int _initCapacity, int _incCapacity) {
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
        return CheckInfo.DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return CheckInfo.DB_ID_NAME;
    }

    /**
     * ȡ����Ԫ����
     * 
     * @return ����Ԫ����
     */
    public Class getElementClass() {
        return CheckInfo.class;
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
     * @return ָ�����󼯺ϣ�CheckInfos��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static CheckInfos findByIds(User _currUser, String _sIds) {
        CheckInfos currCheckInfos = createNewInstance(_currUser);
        //���ID����Ч��
        if (_sIds == null || _sIds.length() <= 0) {
            return currCheckInfos;
        }

        currCheckInfos.addElement(_sIds);

        //��ȡָ��ID�Ķ���
        return currCheckInfos;
    }//END: findById()

    /**
     * ͨ��ָ��һ��Filter��ȡ��ǰ����[CheckInfos]
     * 
     * @param _filter
     *            ָ����Filter
     * @return ��ǰ����[CheckInfos]
     * @throws WCMException
     */
    public final static CheckInfos openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        CheckInfos currCheckInfos = createNewInstance(_currUser);
        currCheckInfos.open(_filter);
        return currCheckInfos;
    }

    /**
     * ������ǰ���϶���[CheckInfos]��ʵ��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @return
     * @throws WCMException
     */
    public static CheckInfos createNewInstance(User _currUser) {
        return new CheckInfos(_currUser);
    }

    /* (non-Javadoc)
     * @see com.trs.infra.persistent.BaseObjs#newInstance()
     */
    public BaseObjs newInstance() throws Exception {
        return new CheckInfos(this.currUser);
    }
}