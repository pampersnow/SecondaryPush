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
 * <p>Created:         2012/5/31 13:16:40</p>
 * <p>Last Modified:   2012/5/31 13:20:52</p>
 * <p>Description:
 *      class Employer ���� Employer����Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2012/5/31 13:16:40 ��������
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.ExceptionNumber;

public class Employer extends CMSObj {

    /** �������ͱ�� */
    public final static int OBJ_TYPE = 1595897109;

    /** �������ݴ洢�����ݱ��� */
    public final static String DB_TABLE_NAME = "XWCMEMPLOYER";

    /** �������ݴ洢��ID�ֶ��� */
    public final static String DB_ID_NAME = "EMPLOYERID";

    /** ���캯��������Ĭ�Ͻӿ� */
    public Employer() {
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
     * ��ȡ���ԣ�EName
     * 
     * @return EName
     */
    public String getEName() {
        return this.getPropertyAsString("ENAME");
    }

    /**
     * �������ԣ�EName
     * 
     * @param _sEName
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setEName(String _sEName) throws WCMException {
        return this.setProperty("ENAME", _sEName);
    }

    /**
     * ��ȡ���ԣ�JoinDate
     * 
     * @return JoinDate
     */
    public CMyDateTime getJoinDate() {
        return (CMyDateTime) this.getProperty("JOINDATE");
    }

    /**
     * �������ԣ�JoinDate
     * 
     * @param _dtJoinDate
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setJoinDate(CMyDateTime _dtJoinDate) throws WCMException {
        return this.setProperty("JOINDATE", _dtJoinDate);
    }

    /**
     * ��ȡ���ԣ�Email
     * 
     * @return Email
     */
    public String getEmail() {
        return this.getPropertyAsString("EMAIL");
    }

    /**
     * �������ԣ�Email
     * 
     * @param _sEmail
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setEmail(String _sEmail) throws WCMException {
        return this.setProperty("EMAIL", _sEmail);
    }

    /**
     * ��ȡ���ԣ�Department
     * 
     * @return Department
     */
    public String getDepartment() {
        return this.getPropertyAsString("DEPARTMENT");
    }

    /**
     * �������ԣ�Department
     * 
     * @param _sDepartment
     *            ����ֵ
     * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false.
     * @throws WCMException
     *             ����ֵ��Ч������ʧ�ܣ����׳��쳣��
     */
    public boolean setDepartment(String _sDepartment) throws WCMException {
        return this.setProperty("DEPARTMENT", _sDepartment);
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

            if (getProperty("ENAME") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����ENameû������(Employer.isValid)");
            }
            if (getProperty("JOINDATE") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����JoinDateû������(Employer.isValid)");
            }
            if (getProperty("EMAIL") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����Emailû������(Employer.isValid)");
            }
            if (getProperty("DEPARTMENT") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "����Departmentû������(Employer.isValid)");
            }
        } else { // �༭ģʽ����������ʱ���Ѿ�����У�飡
            if (!this.isModified()) {
                throw new WCMException(
                        ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
                        "��������û�и���(Employer.isValid)");
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
    public final static Employer findById(int _nId) throws WCMException {
        // ���ID����Ч��
        if (_nId <= 0) {
            return null;
        }

        // ��ȡָ��ID�Ķ���
        return (Employer) BaseObj.findById(Employer.class, _nId);

    }// END: findById()

    /**
     * ������ǰ����[Employer]��ʵ��
     * 
     * @return ��ǰ�����ʵ��[Employer]
     * @throws WCMException
     */
    public final static Employer createNewInstance() throws WCMException {
        return (Employer) BaseObj.createNewInstance(Employer.class);
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
    public final static Employer findByKey(Object _oKey) throws WCMException {
        return (Employer) BaseObj.findByKey(Employer.class, _oKey);
    }

}