package com.trs.example.flow.destroytask;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.12
 * @version			5.2
 *
 * <p>Created:         2011/10/11 16:52:50</p>
 * <p>Last Modified:   2011/10/12 11:28:47</p>
 * <p>Description:
 *      class DestroyTask —— DestroyTask对象的定义和实现</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2011/10/11 16:52:50 创建对象
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.ExceptionNumber;

public class DestroyTask extends CMSObj {

    /** 对象类型编号 */
    public final static int OBJ_TYPE = 1911367336;

    /** 对象数据存储的数据库名 */
    public final static String DB_TABLE_NAME = "XWCMDESTROYTASK";

    /** 对象数据存储的ID字段名 */
    public final static String DB_ID_NAME = "DESTROYTASKID";

    /** 构造函数：保留默认接口 */
    public DestroyTask() {
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
     * 提取属性：Title
     * 
     * @return Title
     */
    public String getTitle() {
        return this.getPropertyAsString("DTITLE");
    }

    /**
     * 设置属性：Title
     * 
     * @param _sTitle
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setTitle(String _sTitle) throws WCMException {
        return this.setProperty("DTITLE", _sTitle);
    }

    /**
     * 提取属性：Desc
     * 
     * @return Desc
     */
    public String getDesc() {
        return this.getPropertyAsString("DDESC");
    }

    /**
     * 设置属性：Desc
     * 
     * @param _sDesc
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setDesc(String _sDesc) throws WCMException {
        return this.setProperty("DDESC", _sDesc);
    }

    public CMSObj getContent() throws WCMException {
        return (CMSObj) CMSObj.findById(getContentType(), getContentId());
    }

    /**
     * @param _nContentType
     * @param _nContentId
     * @return
     * @throws WCMException
     */
    public boolean setContent(int _nContentType, int _nContentId)
            throws WCMException {
        return setContentType(_nContentType) && setContentId(_nContentId);
    }

    /**
     * 提取属性：ContentType
     * 
     * @return ContentType
     */
    public int getContentType() {
        return this.getPropertyAsInt("CONTENTTYPE", 0);
    }

    /**
     * 设置属性：ContentType
     * 
     * @param _nContentType
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setContentType(int _nContentType) throws WCMException {
        return this.setProperty("CONTENTTYPE", _nContentType);
    }

    /**
     * 提取属性：ContentId
     * 
     * @return ContentId
     */
    public int getContentId() {
        return this.getPropertyAsInt("CONTENTID", 0);
    }

    /**
     * 设置属性：ContentId
     * 
     * @param _nContentId
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setContentId(int _nContentId) throws WCMException {
        return this.setProperty("CONTENTID", _nContentId);
    }

    /**
     * 提取属性：CurrStep
     * 
     * @return CurrStep
     */
    public String getCurrStep() {
        return this.getPropertyAsString("DCURRSTEP");
    }

    /**
     * 设置属性：CurrStep
     * 
     * @param _sCurrStep
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setCurrStep(String _sCurrStep) throws WCMException {
        return this.setProperty("DCURRSTEP", _sCurrStep);
    }

    /**
     * 提取属性：FlowId
     * 
     * @return FlowId
     */
    public int getFlowId() {
        return this.getPropertyAsInt("FLOWID", 0);
    }

    /**
     * 设置属性：FlowId
     * 
     * @param _nFlowId
     *            属性值
     * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false.
     * @throws WCMException
     *             属性值无效或设置失败，会抛出异常。
     */
    public boolean setFlowId(int _nFlowId) throws WCMException {
        return this.setProperty("FLOWID", _nFlowId);
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

            if (getProperty("DTITLE") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性Title没有设置(DestroyTask.isValid)");
            }
            if (getProperty("DDESC") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性Desc没有设置(DestroyTask.isValid)");
            }
            if (getProperty("CONTENTTYPE") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性ContentType没有设置(DestroyTask.isValid)");
            }
            if (getProperty("CONTENTID") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性ContentId没有设置(DestroyTask.isValid)");
            }
            if (getProperty("FLOWID") == null) {
                throw new WCMException(ExceptionNumber.ERR_PROPERTY_NOT_SET,
                        "属性FlowId没有设置(DestroyTask.isValid)");
            }
        } else { // 编辑模式，属性设置时，已经做了校验！
            if (!this.isModified()) {
                throw new WCMException(
                        ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
                        "对象属性没有更改(DestroyTask.isValid)");
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
    public final static DestroyTask findById(int _nId) throws WCMException {
        // 检测ID的有效性
        if (_nId <= 0) {
            return null;
        }

        // 提取指定ID的对象
        return (DestroyTask) BaseObj.findById(DestroyTask.class, _nId);

    }// END: findById()

    /**
     * 产生当前对象[DestroyTask]的实例
     * 
     * @return 当前对象的实例[DestroyTask]
     * @throws WCMException
     */
    public final static DestroyTask createNewInstance() throws WCMException {
        return (DestroyTask) BaseObj.createNewInstance(DestroyTask.class);
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
    public final static DestroyTask findByKey(Object _oKey) throws WCMException {
        return (DestroyTask) BaseObj.findByKey(DestroyTask.class, _oKey);
    }

}