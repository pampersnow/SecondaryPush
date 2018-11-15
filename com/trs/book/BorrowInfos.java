package com.trs.book;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.2
 * @version			5.2
 *
 * <p>Created:         2016/6/3 14:08:50</p>
 * <p>Last Modified:   2016/6/3 14:11:07</p>
 * <p>Description:
 *      class BorrowInfos ���� BorrowInfo���϶���Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2016/6/3 14:08:50 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;


public class BorrowInfos extends CMSBaseObjs{

    /**
     * ���캯��
     * 
     * @see public BorrowInfos( User _currUser, int
     *      _initCapacity, int _incCapacity );
     */
    public BorrowInfos(User _currUser) {
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
    public BorrowInfos(User _currUser, int _initCapacity, int _incCapacity) {
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
        return BorrowInfo.DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return BorrowInfo.DB_ID_NAME;
    }

    /**
     * ȡ����Ԫ����
     * 
     * @return ����Ԫ����
     */
    public Class getElementClass() {
        return BorrowInfo.class;
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
     * @return ָ�����󼯺ϣ�BorrowInfos��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static BorrowInfos findByIds(User _currUser, String _sIds) {
        BorrowInfos currBorrowInfos = createNewInstance(_currUser);
        //���ID����Ч��
        if (_sIds == null || _sIds.length() <= 0) {
            return currBorrowInfos;
        }

        currBorrowInfos.addElement(_sIds);

        //��ȡָ��ID�Ķ���
        return currBorrowInfos;
    }//END: findById()

    /**
     * ͨ��ָ��һ��Filter��ȡ��ǰ����[BorrowInfos]
     * 
     * @param _filter
     *            ָ����Filter
     * @return ��ǰ����[BorrowInfos]
     * @throws WCMException
     */
    public final static BorrowInfos openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        BorrowInfos currBorrowInfos = createNewInstance(_currUser);
        currBorrowInfos.open(_filter);
        return currBorrowInfos;
    }

    /**
     * ������ǰ���϶���[BorrowInfos]��ʵ��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @return
     * @throws WCMException
     */
    public static BorrowInfos createNewInstance(User _currUser) {
        return new BorrowInfos(_currUser);
    }

    /* (non-Javadoc)
     * @see com.trs.infra.persistent.BaseObjs#newInstance()
     */
    public BaseObjs newInstance() throws Exception {
        return new BorrowInfos(this.currUser);
    }
}