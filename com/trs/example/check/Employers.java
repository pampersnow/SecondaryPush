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
 * <p>Created:         2012/5/31 13:16:40</p>
 * <p>Last Modified:   2012/5/31 13:20:52</p>
 * <p>Description:
 *      class Employers ���� Employer���϶���Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2012/5/31 13:16:40 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;


public class Employers extends CMSBaseObjs{

    /**
     * ���캯��
     * 
     * @see public Employers( User _currUser, int
     *      _initCapacity, int _incCapacity );
     */
    public Employers(User _currUser) {
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
    public Employers(User _currUser, int _initCapacity, int _incCapacity) {
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
        return Employer.DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * 
     * @return ID�ֶ���
     */
    public String getIdFieldName() {
        return Employer.DB_ID_NAME;
    }

    /**
     * ȡ����Ԫ����
     * 
     * @return ����Ԫ����
     */
    public Class getElementClass() {
        return Employer.class;
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
     * @return ָ�����󼯺ϣ�Employers��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static Employers findByIds(User _currUser, String _sIds) {
        Employers currEmployers = createNewInstance(_currUser);
        //���ID����Ч��
        if (_sIds == null || _sIds.length() <= 0) {
            return currEmployers;
        }

        currEmployers.addElement(_sIds);

        //��ȡָ��ID�Ķ���
        return currEmployers;
    }//END: findById()

    /**
     * ͨ��ָ��һ��Filter��ȡ��ǰ����[Employers]
     * 
     * @param _filter
     *            ָ����Filter
     * @return ��ǰ����[Employers]
     * @throws WCMException
     */
    public final static Employers openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        Employers currEmployers = createNewInstance(_currUser);
        currEmployers.open(_filter);
        return currEmployers;
    }

    /**
     * ������ǰ���϶���[Employers]��ʵ��
     * 
     * @param _currUser
     *            ��ǰ�����û�
     * @return
     * @throws WCMException
     */
    public static Employers createNewInstance(User _currUser) {
        return new Employers(_currUser);
    }

    /* (non-Javadoc)
     * @see com.trs.infra.persistent.BaseObjs#newInstance()
     */
    public BaseObjs newInstance() throws Exception {
        return new Employers(this.currUser);
    }
}