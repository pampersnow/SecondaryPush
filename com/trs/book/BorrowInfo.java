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
 *      class BorrowInfo ���� BorrowInfo����Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2016/6/3 14:08:50 ��������
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

    /** �������ͱ�� */
    public final static int     OBJ_TYPE      = 299980386;

    /** �������ݴ洢�����ݿ��� */
    public final static String  DB_TABLE_NAME = "XWCMBORROWINFO";

	/** �������ݴ洢��ID�ֶ��� */
    public final static String  DB_ID_NAME    = "BORROWINFOID";

    /** ���캯��������Ĭ�Ͻӿ� */
    public BorrowInfo(){
        super();
    }

//==============================================================================
//���ظ����еĳ���ӿں���

    /**
     * ȡ�������ݴ洢�����ݱ�����
     * @return �������ݴ洢�����ݱ�����
     */
    public String getDbTableName(){
        return DB_TABLE_NAME;
    }

    /**
     * ȡ�������ݴ洢��ID�ֶ���
     * @return  ID�ֶ���
     */
    public String getIdFieldName(){
        return DB_ID_NAME;
    }

    /**
     * ȡ�øö�������ͱ��
     * @return ��������ͱ��
     */
    public int getWCMType(){
        return OBJ_TYPE;
    }

//==============================================================================
//���Զ�д����


 /**
  * ��ȡ���ԣ�BookId
   * @return  BookId
   */
  public int getBookId(){
       return this.getPropertyAsInt("BOOKID",0);   
}

  /**
   * �������ԣ�BookId
   * @param	_nBookId ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setBookId( int _nBookId ) throws WCMException{
      return this.setProperty("BOOKID", _nBookId ); 
  }

 /**
  * ��ȡ���ԣ�BookReader
   * @return  BookReader
   */
  public String getBookReader(){
       return this.getPropertyAsString("BOOKREADER");   
}

  /**
   * �������ԣ�BookReader
   * @param	_sBookReader ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setBookReader( String _sBookReader ) throws WCMException{
      return this.setProperty("BOOKREADER", _sBookReader ); 
  }

 /**
  * ��ȡ���ԣ�BorrowTime
   * @return  BorrowTime
   */
  public CMyDateTime getBorrowTime(){
       return (CMyDateTime)this.getProperty("BORROWTIME");   
}

  /**
   * �������ԣ�BorrowTime
   * @param	_dtBorrowTime ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setBorrowTime( CMyDateTime _dtBorrowTime ) throws WCMException{
      return this.setProperty("BORROWTIME", _dtBorrowTime ); 
  }

 /**
  * ��ȡ���ԣ�ReturnTime
   * @return  ReturnTime
   */
  public CMyDateTime getReturnTime(){
       return (CMyDateTime)this.getProperty("RETURNTIME");   
}

  /**
   * �������ԣ�ReturnTime
   * @param	_dtReturnTime ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setReturnTime( CMyDateTime _dtReturnTime ) throws WCMException{
      return this.setProperty("RETURNTIME", _dtReturnTime ); 
  }

  /** ���ԣ�CrUser  �����û�
   *  �ɸ���WCMObj�ṩ�ӿ�֧�֡� 
   */

  /** ���ԣ�CrTime ����ʱ��
   *  �ɸ���WCMObj�ṩ�ӿ�֧�֡� 
   */


//==============================================================================
//���ݿ����

    /**
     * �������������Ч��
     * @return ����������������Ч���򷵻�true�����򷵻�false.
     * @throws WCMException  �����������ò�������������ֵ����ȷ��������ظ������׳��쳣��
     */
    public boolean isValid() throws WCMException{
        if( isAddMode() ){
			//���ǿ������Ƿ�����
			
        }
        else{  //�༭ģʽ����������ʱ���Ѿ�����У�飡
            if( ! this.isModified()){
                throw new WCMException( ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
					"��������û�и���(BorrowInfo.isValid)" );
            }
        }//end if

		return true;
    }//END: isValid()


    /**
     * ����������д�����ݿ�
     * @param _currUser  ��ǰ�����û�
     * @throws WCMException  д������ʧ�ܣ����׳��쳣��
     */
    public void insert( User _currUser ) throws WCMException{
        if ( isAddMode() && this.isModified() ){
			//�������Ե�ȱʡֵ
			


            super.insert( _currUser );  //insert into Db
        }//end if
    }

    //==============================================================================
    //�߼�����

    /**
     * ��ȡָ��ID�Ķ���
     * 
     * @param _nId
     *            ָ���Ķ���ID
     * @return ���ҵ�ָ��ID�Ķ����򷵻ض���ʵ�壻���򣬷���null��
     * @throws WCMException
     *             ����ȡ����ʧ�ܣ����׳��쳣��
     */
    public final static BorrowInfo findById(int _nId) throws WCMException {
        //���ID����Ч��
        if (_nId <= 0) {
            return null;
        }

        //��ȡָ��ID�Ķ���
        return (BorrowInfo) BaseObj.findById(BorrowInfo.class, _nId);

    }//END: findById()

    /**
     * ������ǰ����[BorrowInfo]��ʵ��
     * 
     * @return ��ǰ�����ʵ��[BorrowInfo]
     * @throws WCMException
     */
    public final static BorrowInfo createNewInstance() throws WCMException {
        return (BorrowInfo) BaseObj.createNewInstance(BorrowInfo.class);
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
    public final static BorrowInfo findByKey(Object _oKey) throws WCMException {
        return (BorrowInfo) BaseObj.findByKey(BorrowInfo.class, _oKey);
    }


}