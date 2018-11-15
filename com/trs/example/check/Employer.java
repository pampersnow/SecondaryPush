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
 *      class Employer —— Employer对象的定义和实现</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2012/5/31 13:16:40 创建对象
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.ExceptionNumber;

public class Employer extends CMSObj {

    /** 对象类型编号 */
    public final static int OBJ_TYPE = 1595897109;

    /** 对象数据存储的数据表名 */
    public final static String DB_TABLE_NAME = "XWCMEMPLOYER";

    /** 对象数据存储的ID字段名 */
    public final static String DB_ID_NAME = "EMPLOYERID";

    /** 构造函数：保留默认接口 */
    public Employer() {
        super();
    }

    // ==============================================================================
    // 重载父类中的抽象接口函数

    /**
     * 取对象数据存储的数据表名称
     * 
     * @return 对象数据存储的数据表名称
     */
    public String getDbTableName() {
        return DB_TABLE_NAME;
    }

    /**
     * 取对象数据存储的ID字段名
     * 
     * @return ID字段名
     */
    public String getIdFieldName() {
        return DB_ID_NAME;
    }

    /**
     * 取得该对象的类型编号
     * 
     * @return 对象的类型编号
     */
    public int getWCMType() {
        return OBJ_TYPE;
    }

    // ==============================================================================
    // 属性读写操作

    /**
     * 提取属性：EName
     * 
     * @return EName
     */
    public String getEName() {
        return this.getPropertyAsString("ENAME");
    }

    /**
     * 设置属性：EName
     * 
     * @param _sEName
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setEName(String _sEName) throws WCMException {
        return this.setProperty("ENAME", _sEName);
    }

    /**
     * 提取属性：JoinDate
     * 
     * @return JoinDate
     */
    public CMyDateTime getJoinDate() {
        return (CMyDateTime) this.getProperty("JOINDATE");
    }

    /**
     * 设置属性：JoinDate
     * 
     * @param _dtJoinDate
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setJoinDate(CMyDateTime _dtJoinDate) throws WCMException {
        return this.setProperty("JOINDATE", _dtJoinDate);
    }

    /**
     * 提取属性：Email
     * 
     * @return Email
     */
    public String getEmail() {
        return this.getPropertyAsString("EMAIL");
    }

    /**
     * 设置属性：Email
     * 
     * @param _sEmail
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setEmail(String _sEmail) throws WCMException {
        return this.setProperty("EMAIL", _sEmail);
    }

    /**
     * 提取属性：Department
     * 
     * @return Department
     */
    public String getDepartment() {
        return this.getPropertyAsString("DEPARTMENT");
    }

    /**
     * 设置属性：Department
     * 
     * @param _sDepartment
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setDepartment(String _sDepartment) throws WCMException {
        return this.setProperty("DEPARTMENT", _sDepartment);
    }

    /**
     * 属性：CrUser 创建用户 由父类WCMObj提供接口支持。
     */

    /**
     * 属性：CrTime 创建时间 由父类WCMObj提供接口支持。
     */

    // ==============================================================================
    // 数据库操作

    /**
     * 检验对象数据有效性
     * 
     * @return 若对象数据设置有效，则返回true；否则返回false.
     * @throws WCMException
     *             对象属性设置不完整，或属性值不正确，或对象重复，会抛出异常。
     */
    public boolean isValid() throws WCMException {
        if (isAddMode()) {
            // 检查非空属性是否设置

            if (getProperty("ENAME") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性EName没有设置(Employer.isValid)");
            }
            if (getProperty("JOINDATE") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性JoinDate没有设置(Employer.isValid)");
            }
            if (getProperty("EMAIL") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性Email没有设置(Employer.isValid)");
            }
            if (getProperty("DEPARTMENT") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性Department没有设置(Employer.isValid)");
            }
        } else { // 编辑模式，属性设置时，已经做了校验！
            if (!this.isModified()) {
                throw new WCMException(
                        ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
                        "对象属性没有更改(Employer.isValid)");
            }
        }// end if

        return true;
    }// END: isValid()

    /**
     * 将对象数据写入数据库
     * 
     * @param _currUser
     *            当前操作用户
     * @throws WCMException
     *             写入数据失败，会抛出异常。
     */
    public void insert(User _currUser) throws WCMException {
        if (isAddMode() && this.isModified()) {
            // 设置属性的缺省值

            super.insert(_currUser); // insert into Db
        }// end if
    }

    // ==============================================================================
    // 逻辑操作

    /**
     * 提取指定ID的对象
     * 
     * @param _nId
     *            指定的对象ID
     * @return 若找到指定ID的对象，则返回对象实体；否则，返回null。
     * @throws WCMException
     *             若提取对象失败，会抛出异常。
     */
    public final static Employer findById(int _nId) throws WCMException {
        // 检测ID的有效性
        if (_nId <= 0) {
            return null;
        }

        // 提取指定ID的对象
        return (Employer) BaseObj.findById(Employer.class, _nId);

    }// END: findById()

    /**
     * 产生当前对象[Employer]的实例
     * 
     * @return 当前对象的实例[Employer]
     * @throws WCMException
     */
    public final static Employer createNewInstance() throws WCMException {
        return (Employer) BaseObj.createNewInstance(Employer.class);
    }

    /**
     * 提取指定缓冲区Key的对象
     * 
     * @param _oKey
     *            指定缓冲区的Key
     * @return 若找到缓冲区Key的对象，则返回对象实体；否则，返回null。
     * @throws WCMException
     *             若提取对象失败，会抛出异常。
     */
    public final static Employer findByKey(Object _oKey) throws WCMException {
        return (Employer) BaseObj.findByKey(Employer.class, _oKey);
    }

}