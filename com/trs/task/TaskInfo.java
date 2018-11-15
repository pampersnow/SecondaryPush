package com.trs.task;

/**
 * <p>Title:        TRS WCM</p>
 * <p>Copyright:    Copyright (c) 2004</p>
 * <p>Company:      www.trs.com.cn</p>
 * @author			TRS WCM
 * @copyright		www.trs.com.cn
 * @created by		XWCMAutoTool 2.12
 * @version			5.2
 *
 * <p>Created:         2010/12/22 11:25:31</p>
 * <p>Last Modified:   2010/12/22 11:28:21</p>
 * <p>Description:
 *      class TaskInfo ���� TaskInfo����Ķ����ʵ��</p>
 * <p>Update Logs:
 *		[1] TRS WCM@2010/12/22 11:25:31 ��������
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

public class TaskInfo extends CMSObj{

    /** �������ͱ�� */
    public final static int     OBJ_TYPE      = 1338389866;

    /** �������ݴ洢�����ݿ��� */
    public final static String  DB_TABLE_NAME = "XWCMTASKINFO";

	/** �������ݴ洢��ID�ֶ��� */
    public final static String  DB_ID_NAME    = "TASKINFOID";

    /** ���캯��������Ĭ�Ͻӿ� */
    public TaskInfo(){
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
  * ��ȡ���ԣ�Name
   * @return  Name
   */
  public String getName(){
       return this.getPropertyAsString("TNAME");   
}

  /**
   * �������ԣ�Name
   * @param	_sName ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setName( String _sName ) throws WCMException{
      return this.setProperty("TNAME", _sName ); 
  }

 /**
  * ��ȡ���ԣ�Desc
   * @return  Desc
   */
  public String getDesc(){
       return this.getPropertyAsString("TDESC");   
}

  /**
   * �������ԣ�Desc
   * @param	_sDesc ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setDesc( String _sDesc ) throws WCMException{
      return this.setProperty("TDESC", _sDesc ); 
  }

 /**
  * ��ȡ���ԣ�Type
   * @return  Type
   */
  public int getType(){
       return this.getPropertyAsInt("TTYPE",0);   
}

  /**
   * �������ԣ�Type
   * @param	_nType ����ֵ
   * @return ��ָ������ֵ��Ч�����������óɹ����򷵻�true�����򷵻�false. 
   * @throws WCMException ����ֵ��Ч������ʧ�ܣ����׳��쳣��
   */
  public boolean setType( int _nType ) throws WCMException{
      return this.setProperty("TTYPE", _nType ); 
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
					"��������û�и���(TaskInfo.isValid)" );
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
    public final static TaskInfo findById(int _nId) throws WCMException {
        //���ID����Ч��
        if (_nId <= 0) {
            return null;
        }

        //��ȡָ��ID�Ķ���
        return (TaskInfo) BaseObj.findById(TaskInfo.class, _nId);

    }//END: findById()

    /**
     * ������ǰ����[TaskInfo]��ʵ��
     * 
     * @return ��ǰ�����ʵ��[TaskInfo]
     * @throws WCMException
     */
    public final static TaskInfo createNewInstance() throws WCMException {
        return (TaskInfo) BaseObj.createNewInstance(TaskInfo.class);
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
    public final static TaskInfo findByKey(Object _oKey) throws WCMException {
        return (TaskInfo) BaseObj.findByKey(TaskInfo.class, _oKey);
    }


}