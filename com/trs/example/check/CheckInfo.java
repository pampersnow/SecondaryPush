package com.trs.example.check;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.12
 * @version			5.2
 *
 * <p>Created:         2012/5/31 10:03:01</p>
 * <p>Last Modified:   2012/5/31 16:02:17</p>
 * <p>Description:
 *      class CheckInfo ���� CheckInfo����Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2012/5/31 10:03:01 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.ExceptionNumber;

public class CheckInfo extends CMSObj {

    /** �������ͱ�� */
    public final static int OBJ_TYPE = 172762202;

    /** �������ݴ洢�����ݱ��� */
    public final static String DB_TABLE_NAME = "XWCMCHECKINFO";

    /** �������ݴ洢��ID�ֶ��� */
    public final static String DB_ID_NAME = "CHECKINFOID";

    /** ���캯��������Ĭ�Ͻӿ� */
    public CheckInfo() {
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
     * ��ȡ���ԣ�EmployerName
     * 
     * @return EmployerName
     */
    public String getEmployerName() {
        return this.getPropertyAsString("EMPLOYERNAME");
    }

    /**
     * �������ԣ�EmployerName
     * 
     * @param _sEmployerName
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setEmployerName(String _sEmployerName) throws WCMException {
        return this.setProperty("EMPLOYERNAME", _sEmployerName);
    }

    /**
     * ��ȡ���ԣ�CheckInTime
     * 
     * @return CheckInTime
     */
    public CMyDateTime getCheckInTime() {
        return (CMyDateTime) this.getProperty("CHECKINTIME");
    }

    /**
     * �������ԣ�CheckInTime
     * 
     * @param _dtCheckInTime
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setCheckInTime(CMyDateTime _dtCheckInTime)
            throws WCMException {
        return this.setProperty("CHECKINTIME", _dtCheckInTime);
    }

    /**
     * ��ȡ���ԣ�CheckOutTime
     * 
     * @return CheckOutTime
     */
    public CMyDateTime getCheckOutTime() {
        return (CMyDateTime) this.getProperty("CHECKOUTTIME");
    }

    /**
     * �������ԣ�CheckOutTime
     * 
     * @param _dtCheckOutTime
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setCheckOutTime(CMyDateTime _dtCheckOutTime)
            throws WCMException {
        return this.setProperty("CHECKOUTTIME", _dtCheckOutTime);
    }

    /**
     * ��ȡ���ԣ�WorkTime
     * 
     * @return WorkTime
     */
    public int getWorkTime() {
        return this.getPropertyAsInt("WORKTIME", 0);
    }

    /**
     * �������ԣ�WorkTime
     * 
     * @param _nWorkTime
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setWorkTime(int _nWorkTime) throws WCMException {
        return this.setProperty("WORKTIME", _nWorkTime);
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

            if (getProperty("EMPLOYERNAME") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����EmployerNameû������(CheckInfo.isValid)");
            }
            if (getProperty("CHECKINTIME") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����CheckInTimeû������(CheckInfo.isValid)");
            }
        } else { // �༭ģʽ����������ʱ���Ѿ�����У�飡
            if (!this.isModified()) {
                throw new WCMException(
                        ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
                        "��������û�и���(CheckInfo.isValid)");
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
    public final static CheckInfo findById(int _nId) throws WCMException {
        // ���ID����Ч��
        if (_nId <= 0) {
            return null;
        }

        // ��ȡָ��ID�Ķ���
        return (CheckInfo) BaseObj.findById(CheckInfo.class, _nId);

    }// END: findById()

    /**
     * ������ǰ����[CheckInfo]��ʵ��
     * 
     * @return ��ǰ�����ʵ��[CheckInfo]
     * @throws WCMException
     */
    public final static CheckInfo createNewInstance() throws WCMException {
        return (CheckInfo) BaseObj.createNewInstance(CheckInfo.class);
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
    public final static CheckInfo findByKey(Object _oKey) throws WCMException {
        return (CheckInfo) BaseObj.findByKey(CheckInfo.class, _oKey);
    }

}