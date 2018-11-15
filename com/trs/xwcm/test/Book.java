package com.trs.xwcm.test;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM Developer
 * @copyright		www.trs.com
 * @created by		XWCMAutoTool 2.12
 * @version			5.2
 *
 * <p>Created:         2010/12/18 13:58:25</p>
 * <p>Last Modified:   2010/12/18 13:59:32</p>
 * <p>Description:
 *      class Book —— Book对象的定义和实现</p>
 * <p>Update Logs:
 *		[1] TRS WCM Developer@2010/12/18 13:58:25 创建对象
 *		[2] 
 */

import com.trs.cms.auth.persistent.User;
import com.trs.cms.content.CMSObj;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.util.ExceptionNumber;
import com.trs.infra.util.database.CDBText;

import com.trs.infra.util.*;
import com.trs.infra.util.database.*;

public class Book extends CMSObj{

    /** 对象类型编号 */
    public final static int     OBJ_TYPE      = 1111893446;

    /** 对象数据存储的数据库名 */
    public final static String  DB_TABLE_NAME = "XWCMBOOK";

	/** 对象数据存储的ID字段名 */
    public final static String  DB_ID_NAME    = "BOOKID";

    /** 构造函数：保留默认接口 */
    public Book(){
        super();
    }

//==============================================================================
//重载父类中的抽象接口函数

    /**
     * 取对象数据存储的数据表名称
     * @return 对象数据存储的数据表名称
     */
    public String getDbTableName(){
        return DB_TABLE_NAME;
    }

    /**
     * 取对象数据存储的ID字段名
     * @return  ID字段名
     */
    public String getIdFieldName(){
        return DB_ID_NAME;
    }

    /**
     * 取得该对象的类型编号
     * @return 对象的类型编号
     */
    public int getWCMType(){
        return OBJ_TYPE;
    }

//==============================================================================
//属性读写操作


 /**
  * 提取属性：Title
   * @return  Title
   */
  public String getTitle(){
       return this.getPropertyAsString("BTITLE");   
}

  /**
   * 设置属性：Title
   * @param	_sTitle 属性值
   * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false. 
   * @throws WCMException 属性值无效或设置失败，会抛出异常。
   */
  public boolean setTitle( String _sTitle ) throws WCMException{
      return this.setProperty("BTITLE", _sTitle ); 
  }

 /**
  * 提取属性：Desc
   * @return  Desc
   */
  public String getDesc(){
       return this.getPropertyAsString("BDESC");   
}

  /**
   * 设置属性：Desc
   * @param	_sDesc 属性值
   * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false. 
   * @throws WCMException 属性值无效或设置失败，会抛出异常。
   */
  public boolean setDesc( String _sDesc ) throws WCMException{
      return this.setProperty("BDESC", _sDesc ); 
  }

  /** 属性：CrUser  创建用户
   *  由父类WCMObj提供接口支持。 
   */

  /** 属性：CrTime 创建时间
   *  由父类WCMObj提供接口支持。 
   */


//==============================================================================
//数据库操作

    /**
     * 检验对象数据有效性
     * @return 若对象数据设置有效，则返回true；否则返回false.
     * @throws WCMException  对象属性设置不完整，或属性值不正确，或对象重复，会抛出异常。
     */
    public boolean isValid() throws WCMException{
        if( isAddMode() ){
			//检查非空属性是否设置
			
 if( getProperty("BTITLE")==null ){ 
                 throw new WCMException( ExceptionNumber.ERR_PROPERTY_NOT_SET,"属性Title没有设置(Book.isValid)"); 
             }
        }
        else{  //编辑模式，属性设置时，已经做了校验！
            if( ! this.isModified()){
                throw new WCMException( ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
					"对象属性没有更改(Book.isValid)" );
            }
        }//end if

		return true;
    }//END: isValid()


    /**
     * 将对象数据写入数据库
     * @param _currUser  当前操作用户
     * @throws WCMException  写入数据失败，会抛出异常。
     */
    public void insert( User _currUser ) throws WCMException{
        if ( isAddMode() && this.isModified() ){
			//设置属性的缺省值
			


            super.insert( _currUser );  //insert into Db
        }//end if
    }

    //==============================================================================
    //逻辑操作

    /**
     * 提取指定ID的对象
     * 
     * @param _nId
     *            指定的对象ID
     * @return 若找到指定ID的对象，则返回对象实体；否则，返回null。
     * @throws WCMException
     *             若提取对象失败，会抛出异常。
     */
    public final static Book findById(int _nId) throws WCMException {
        //检测ID的有效性
        if (_nId <= 0) {
            return null;
        }

        //提取指定ID的对象
        return (Book) BaseObj.findById(Book.class, _nId);

    }//END: findById()

    /**
     * 产生当前对象[Book]的实例
     * 
     * @return 当前对象的实例[Book]
     * @throws WCMException
     */
    public final static Book createNewInstance() throws WCMException {
        return (Book) BaseObj.createNewInstance(Book.class);
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
    public final static Book findByKey(Object _oKey) throws WCMException {
        return (Book) BaseObj.findByKey(Book.class, _oKey);
    }


}