package com.trs.book;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.12
 * @version			5.2
 *
 * <p>Created:         2016/6/3 14:08:50</p>
 * <p>Last Modified:   2016/6/3 14:11:07</p>
 * <p>Description:
 *      class BorrowInfo —— BorrowInfo对象的定义和实现</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2016/6/3 14:08:50 创建对象
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

public class BorrowInfo extends CMSObj{

    /** 对象类型编号 */
    public final static int     OBJ_TYPE      = 299980386;

    /** 对象数据存储的数据库名 */
    public final static String  DB_TABLE_NAME = "XWCMBORROWINFO";

	/** 对象数据存储的ID字段名 */
    public final static String  DB_ID_NAME    = "BORROWINFOID";

    /** 构造函数：保留默认接口 */
    public BorrowInfo(){
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
  * 提取属性：BookId
   * @return  BookId
   */
  public int getBookId(){
       return this.getPropertyAsInt("BOOKID",0);   
}

  /**
   * 设置属性：BookId
   * @param	_nBookId 属性值
   * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false. 
   * @throws WCMException 属性值无效或设置失败，会抛出异常。
   */
  public boolean setBookId( int _nBookId ) throws WCMException{
      return this.setProperty("BOOKID", _nBookId ); 
  }

 /**
  * 提取属性：BookReader
   * @return  BookReader
   */
  public String getBookReader(){
       return this.getPropertyAsString("BOOKREADER");   
}

  /**
   * 设置属性：BookReader
   * @param	_sBookReader 属性值
   * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false. 
   * @throws WCMException 属性值无效或设置失败，会抛出异常。
   */
  public boolean setBookReader( String _sBookReader ) throws WCMException{
      return this.setProperty("BOOKREADER", _sBookReader ); 
  }

 /**
  * 提取属性：BorrowTime
   * @return  BorrowTime
   */
  public CMyDateTime getBorrowTime(){
       return (CMyDateTime)this.getProperty("BORROWTIME");   
}

  /**
   * 设置属性：BorrowTime
   * @param	_dtBorrowTime 属性值
   * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false. 
   * @throws WCMException 属性值无效或设置失败，会抛出异常。
   */
  public boolean setBorrowTime( CMyDateTime _dtBorrowTime ) throws WCMException{
      return this.setProperty("BORROWTIME", _dtBorrowTime ); 
  }

 /**
  * 提取属性：ReturnTime
   * @return  ReturnTime
   */
  public CMyDateTime getReturnTime(){
       return (CMyDateTime)this.getProperty("RETURNTIME");   
}

  /**
   * 设置属性：ReturnTime
   * @param	_dtReturnTime 属性值
   * @return 若指定属性值有效，且属性设置成功，则返回true；否则返回false. 
   * @throws WCMException 属性值无效或设置失败，会抛出异常。
   */
  public boolean setReturnTime( CMyDateTime _dtReturnTime ) throws WCMException{
      return this.setProperty("RETURNTIME", _dtReturnTime ); 
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
			
        }
        else{  //编辑模式，属性设置时，已经做了校验！
            if( ! this.isModified()){
                throw new WCMException( ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
					"对象属性没有更改(BorrowInfo.isValid)" );
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
    public final static BorrowInfo findById(int _nId) throws WCMException {
        //检测ID的有效性
        if (_nId <= 0) {
            return null;
        }

        //提取指定ID的对象
        return (BorrowInfo) BaseObj.findById(BorrowInfo.class, _nId);

    }//END: findById()

    /**
     * 产生当前对象[BorrowInfo]的实例
     * 
     * @return 当前对象的实例[BorrowInfo]
     * @throws WCMException
     */
    public final static BorrowInfo createNewInstance() throws WCMException {
        return (BorrowInfo) BaseObj.createNewInstance(BorrowInfo.class);
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
    public final static BorrowInfo findByKey(Object _oKey) throws WCMException {
        return (BorrowInfo) BaseObj.findByKey(BorrowInfo.class, _oKey);
    }


}