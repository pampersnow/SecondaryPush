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
 * <p>Created:         2016/6/3 14:06:26</p>
 * <p>Last Modified:   2016/6/3 14:07:52</p>
 * <p>Description:
 *      class Books ���� Book���϶���Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2016/6/3 14:06:26 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;


public class Books extends CMSBaseObjs{

    /**
     * ���캯��
     * 
     * @see public Books( User _currUser, int
     *      _initCapacity, int _incCapacity );
     */
    public Books(User _currUser) {
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
    public Books(User _currUser, int _initCapacity, int _incCapacity) {
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
        return Book.DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return Book.DB_ID_NAME;
    }

    /**
     * ȡ����Ԫ����
     * 
     * @return ����Ԫ����
     */
    public Class getElementClass() {
        return Book.class;
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
     * @return ָ�����󼯺ϣ�Books��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static Books findByIds(User _currUser, String _sIds) {
        Books currBooks = createNewInstance(_currUser);
        //���ID����Ч��
        if (_sIds == null || _sIds.length() <= 0) {
            return currBooks;
        }

        currBooks.addElement(_sIds);

        //��ȡָ��ID�Ķ���
        return currBooks;
    }//END: findById()

    /**
     * ͨ��ָ��һ��Filter��ȡ��ǰ����[Books]
     * 
     * @param _filter
     *            ָ����Filter
     * @return ��ǰ����[Books]
     * @throws WCMException
     */
    public final static Books openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        Books currBooks = createNewInstance(_currUser);
        currBooks.open(_filter);
        return currBooks;
    }

    /**
     * ������ǰ���϶���[Books]��ʵ��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @return
     * @throws WCMException
     */
    public static Books createNewInstance(User _currUser) {
        return new Books(_currUser);
    }

    /* (non-Javadoc)
     * @see com.trs.infra.persistent.BaseObjs#newInstance()
     */
    public BaseObjs newInstance() throws Exception {
        return new Books(this.currUser);
    }
}