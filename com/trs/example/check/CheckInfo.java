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
 *      class CheckInfo —— CheckInfo对象的定义和实现</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2012/5/31 10:03:01 创建对象
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.ExceptionNumber;

public class CheckInfo extends CMSObj {

    /** 对象类型编号 */
    public final static int OBJ_TYPE = 172762202;

    /** 对象数据存储的数据表名 */
    public final static String DB_TABLE_NAME = "XWCMCHECKINFO";

    /** 对象数据存储的ID字段名 */
    public final static String DB_ID_NAME = "CHECKINFOID";

    /** 构造函数：保留默认接口 */
    public CheckInfo() {
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
     * 提取属性：EmployerName
     * 
     * @return EmployerName
     */
    public String getEmployerName() {
        return this.getPropertyAsString("EMPLOYERNAME");
    }

    /**
     * 设置属性：EmployerName
     * 
     * @param _sEmployerName
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setEmployerName(String _sEmployerName) throws WCMException {
        return this.setProperty("EMPLOYERNAME", _sEmployerName);
    }

    /**
     * 提取属性：CheckInTime
     * 
     * @return CheckInTime
     */
    public CMyDateTime getCheckInTime() {
        return (CMyDateTime) this.getProperty("CHECKINTIME");
    }

    /**
     * 设置属性：CheckInTime
     * 
     * @param _dtCheckInTime
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setCheckInTime(CMyDateTime _dtCheckInTime)
            throws WCMException {
        return this.setProperty("CHECKINTIME", _dtCheckInTime);
    }

    /**
     * 提取属性：CheckOutTime
     * 
     * @return CheckOutTime
     */
    public CMyDateTime getCheckOutTime() {
        return (CMyDateTime) this.getProperty("CHECKOUTTIME");
    }

    /**
     * 设置属性：CheckOutTime
     * 
     * @param _dtCheckOutTime
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setCheckOutTime(CMyDateTime _dtCheckOutTime)
            throws WCMException {
        return this.setProperty("CHECKOUTTIME", _dtCheckOutTime);
    }

    /**
     * 提取属性：WorkTime
     * 
     * @return WorkTime
     */
    public int getWorkTime() {
        return this.getPropertyAsInt("WORKTIME", 0);
    }

    /**
     * 设置属性：WorkTime
     * 
     * @param _nWorkTime
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setWorkTime(int _nWorkTime) throws WCMException {
        return this.setProperty("WORKTIME", _nWorkTime);
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

            if (getProperty("EMPLOYERNAME") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性EmployerName没有设置(CheckInfo.isValid)");
            }
            if (getProperty("CHECKINTIME") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性CheckInTime没有设置(CheckInfo.isValid)");
            }
        } else { // 编辑模式，属性设置时，已经做了校验！
            if (!this.isModified()) {
                throw new WCMException(
                        ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
                        "对象属性没有更改(CheckInfo.isValid)");
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
    public final static CheckInfo findById(int _nId) throws WCMException {
        // 检测ID的有效性
        if (_nId <= 0) {
            return null;
        }

        // 提取指定ID的对象
        return (CheckInfo) BaseObj.findById(CheckInfo.class, _nId);

    }// END: findById()

    /**
     * 产生当前对象[CheckInfo]的实例
     * 
     * @return 当前对象的实例[CheckInfo]
     * @throws WCMException
     */
    public final static CheckInfo createNewInstance() throws WCMException {
        return (CheckInfo) BaseObj.createNewInstance(CheckInfo.class);
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
    public final static CheckInfo findByKey(Object _oKey) throws WCMException {
        return (CheckInfo) BaseObj.findByKey(CheckInfo.class, _oKey);
    }

}