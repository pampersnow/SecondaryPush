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
 * <p>Created:         2016/6/3 14:06:26</p>
 * <p>Last Modified:   2016/6/3 14:07:52</p>
 * <p>Description:
 *      class Book ���� Book����Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2016/6/3 14:06:26 ��������
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

    /** �������ͱ�� */
    public final static int     OBJ_TYPE      = 1769548728;

    /** �������ݴ洢�����ݿ��� */
    public final static String  DB_TABLE_NAME = "XWCMBOOK";

	/** �������ݴ洢��ID�ֶ��� */
    public final static String  DB_ID_NAME    = "BOOKID";

    /** ���캯��������Ĭ�Ͻӿ� */
    public Book(){
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
  * ��ȡ���ԣ�BookName
   * @return  BookName
   */
  public String getBookName(){
       return this.getPropertyAsString("BOOKNAME");   
}

  /**
   * �������ԣ�BookName
   * @param	_sBookName ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setBookName( String _sBookName ) throws WCMException{
      return this.setProperty("BOOKNAME", _sBookName ); 
  }

 /**
  * ��ȡ���ԣ�BookAuthor
   * @return  BookAuthor
   */
  public String getBookAuthor(){
       return this.getPropertyAsString("BOOKAUTHOR");   
}

  /**
   * �������ԣ�BookAuthor
   * @param	_sBookAuthor ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setBookAuthor( String _sBookAuthor ) throws WCMException{
      return this.setProperty("BOOKAUTHOR", _sBookAuthor ); 
  }

 /**
  * ��ȡ���ԣ�BookCount
   * @return  BookCount
   */
  public int getBookCount(){
       return this.getPropertyAsInt("BOOKCOUNT",0);   
}

  /**
   * �������ԣ�BookCount
   * @param	_nBookCount ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setBookCount( int _nBookCount ) throws WCMException{
      return this.setProperty("BOOKCOUNT", _nBookCount ); 
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
			
 if( getProperty("BOOKAUTHOR")==null ){ 
                 throw new WCMException( ExceptionNumber.ERR_PROPERTY_NOT_SET,"����BookAuthorû������(Book.isValid)"); 
             }
        }
        else{  //�༭ģʽ����������ʱ���Ѿ�����У�飡
            if( ! this.isModified()){
                throw new WCMException( ExceptionNumber.ERR_PROPERTY_NOT_MODIFIED,
					"��������û�и���(Book.isValid)" );
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
    public final static Book findById(int _nId) throws WCMException {
        //���ID����Ч��
        if (_nId <= 0) {
            return null;
        }

        //��ȡָ��ID�Ķ���
        return (Book) BaseObj.findById(Book.class, _nId);

    }//END: findById()

    /**
     * ������ǰ����[Book]��ʵ��
     * 
     * @return ��ǰ�����ʵ��[Book]
     * @throws WCMException
     */
    public final static Book createNewInstance() throws WCMException {
        return (Book) BaseObj.createNewInstance(Book.class);
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
    public final static Book findByKey(Object _oKey) throws WCMException {
        return (Book) BaseObj.findByKey(Book.class, _oKey);
    }


}