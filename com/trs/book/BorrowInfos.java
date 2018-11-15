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
 *      class BorrowInfos —— BorrowInfo集合对象的定义和实现</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2016/6/3 14:08:50 创建对象
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSBaseObjs;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObjs;
import com.trs.infra.persistent.WCMFilter;


public class BorrowInfos extends CMSBaseObjs{

    /**
     * 构造函数
     * 
     * @see public BorrowInfos( User _currUser, int
     *      _initCapacity, int _incCapacity );
     */
    public BorrowInfos(User _currUser) {
        super(_currUser);
    }

    /**
     * 构造函数
     * 
     * @param _currUser
     *            当前操作用户
     * @param _initCapacity
     *            初始化容量
     * @param _incCapacity
     *            当容量不足时，每次增容量
     */
    public BorrowInfos(User _currUser, int _initCapacity, int _incCapacity) {
        super(_currUser, _initCapacity, _incCapacity);
    }

    //=============================================================
    //重载父类中定义的抽象接口函数

    /**
     * 取对象数据存储的数据表名称
     * 
     * @return 对象数据存储的数据表名称
     */
    protected String getDbTableName() {
        return BorrowInfo.DB_TABLE_NAME;
    }

    /**
     * 取对象数据存储的ID字段名
     * 
     * @return ID字段名
     */
    public String getIdFieldName() {
        return BorrowInfo.DB_ID_NAME;
    }

    /**
     * 取集合元素类
     * 
     * @return 集合元素类
     */
    public Class getElementClass() {
        return BorrowInfo.class;
    }

    

    //==============================================================
    //集合操作

    //==============================================================
    // XML导入/导出

    //==============================================================================
    //逻辑操作
    /**
     * 提取指定ID序列的对象集合
     * 
     * @param _sIds
     *            指定的对象ID序列
     * @return 指定对象集合（BorrowInfos）
     * @throws WCMException
     *             若提取对象失败，会抛出异常。
     */
    public final static BorrowInfos findByIds(User _currUser, String _sIds) {
        BorrowInfos currBorrowInfos = createNewInstance(_currUser);
        //检测ID的有效性
        if (_sIds == null || _sIds.length() <= 0) {
            return currBorrowInfos;
        }

        currBorrowInfos.addElement(_sIds);

        //提取指定ID的对象
        return currBorrowInfos;
    }//END: findById()

    /**
     * 通过指定一个Filter获取当前集合[BorrowInfos]
     * 
     * @param _filter
     *            指定的Filter
     * @return 当前集合[BorrowInfos]
     * @throws WCMException
     */
    public final static BorrowInfos openWCMObjs(User _currUser,
            WCMFilter _filter) throws WCMException {
        BorrowInfos currBorrowInfos = createNewInstance(_currUser);
        currBorrowInfos.open(_filter);
        return currBorrowInfos;
    }

    /**
     * 产生当前集合对象[BorrowInfos]的实例
     * 
     * @param _currUser
     *            当前操作用户
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