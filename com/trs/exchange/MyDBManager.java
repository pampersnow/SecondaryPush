package com.trs.exchange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.trs.infra.I18NMessage;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBConnectionConfig;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.ExceptionNumber;
import com.trs.infra.util.database.CDBText;
import com.trs.infra.util.database.ConnectionPool;
import com.trs.infra.util.database.DBType;

public class MyDBManager {
    ConnectionPool m_oDBPool = null;

    DBConnectionConfig dbConfig = null;

    DBType m_oCurrDBType = null;

    public MyDBManager(String _sSrcName, String _sURL, String _sUserName,
            String _sPassword, Class _dbTypeClass) throws Exception {
        m_oCurrDBType = (DBType) _dbTypeClass.newInstance();
        connect(_sSrcName, _sURL, _sUserName, _sPassword,
                _dbTypeClass.getName());
    }

   

    public DBType getDbType() {
        return m_oCurrDBType;
    }

    public String getSrcNameOfDB() {
        return dbConfig.getName();
    }

    private void connect(String _sSrcName, String _sURL, String _sUserName,
            String _sPassword, String _sClassName) throws Exception {
        if (m_oDBPool != null) {
            return;
        }
        // 1 �������ݿ��������ö���
        dbConfig = makeDBConfig(_sURL, _sUserName, _sPassword, _sClassName);
        dbConfig.setName(_sSrcName);

        // 2 �������ݿ����ӻ����

        try {
            m_oDBPool = new ConnectionPool(dbConfig);
        } catch (SQLException ex) {
            throw new WCMException(ExceptionNumber.ERR_CONNECTION_GETFAIL,
                    "�������ݿ�����쳣�����ݿ���ϢΪ��" + dbConfig, ex);
        }
    }

    private DBConnectionConfig makeDBConfig(String _sURL, String _sUserName,
            String _sPassword, String _sClassName) {
        // 1.1 ��ȡ��ǰϵͳʹ�õ����ã��ٶ���ͬһ��DB��
        DBManager dbMgr = DBManager.getDBManager();
        DBConnectionConfig currDBConfig = dbMgr.getDBConnConfig();

        DBConnectionConfig dbConfig = new DBConnectionConfig();
        dbConfig.setConnectionURL(_sURL);
        dbConfig.setConnectionUser(_sUserName);
        dbConfig.setConnectionPassword(_sPassword);
        dbConfig.setClassName(_sClassName);

        // ����WCM���ӳص�ĳЩ����
        dbConfig.setCacheScheme(currDBConfig.getCacheScheme());
        dbConfig.setDowithClob(currDBConfig.isDowithClob());
        dbConfig.setTimeToLiveOverUse(currDBConfig.getTimeToLiveOverUse());
        dbConfig.setMonitorInterval(currDBConfig.getMonitorInterval());

        dbConfig.setInitConnects(5);
        dbConfig.setMaxConnects(20);
        return dbConfig;
    }

    public Connection getConnection() throws Exception {
        if (m_oDBPool == null)
            throw new Exception("Please connect db��");

        Connection oConn = null;
        try {
            oConn = m_oDBPool.getConnection(); // ȡ��Ч�����ݿ�����
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_CONNECTION_GETFAIL,
                    "��ȡ���ݿ�����ʱʧ��(Application.getConnection)", ex);
        } // end try

        if (oConn == null) {
            throw new WCMException(ExceptionNumber.ERR_CONNECTION_GETFAIL,
                    "û���ҵ���Ч���õ����ݿ�����(Application.getConnection)");
        } // end if
        return oConn;
    }

    /**
     * �ͷ����ݿ�����
     * 
     * @param _oConn
     *            ���ݿ����Ӷ���
     */
    public void freeConnection(Connection _oConn) {
        if (m_oDBPool == null)
            return;
        m_oDBPool.free(_oConn);
    }

    public void close() {
        if (m_oDBPool == null)
            return;
        m_oDBPool.close();
        m_oDBPool = null;
    }

    public DBType getDBType() {
        return dbConfig.getDBType();
    }

    public void setParameters(PreparedStatement _oPreStmt, List listSearchValues)
            throws WCMException {
        if (_oPreStmt == null || listSearchValues == null
                || listSearchValues.size() <= 0)
            return;

        DBType currDbType = getDBType();
        Object value = null;
        int i = 0, nSize;
        try {
            for (i = 0, nSize = listSearchValues.size(); i < nSize; i++) {
                value = listSearchValues.get(i);
                if (value == null)
                    continue;

                if (value instanceof Long) {
                    _oPreStmt.setLong(i + 1, ((Long) value).longValue());
                } else if (value instanceof Float) {
                    _oPreStmt.setFloat(i + 1, ((Float) value).floatValue());
                } else if (value instanceof Double) {
                    _oPreStmt.setDouble(i + 1, ((Double) value).doubleValue());
                } else if (value instanceof Integer) {
                    _oPreStmt.setInt(i + 1, ((Integer) value).intValue());
                } else if (value instanceof String) {
                    currDbType.setStringFieldValue(_oPreStmt, i + 1,
                            (String) value);
                } else if (value instanceof CMyDateTime) {
                    _oPreStmt.setTimestamp(i + 1,
                            ((CMyDateTime) value).toTimestamp());
                } else if (value instanceof CDBText) {
                    _oPreStmt.setString(i + 1, ((CDBText) value).getText());
                } else {
                    throw new WCMException(
                            ExceptionNumber.ERR_PROPERTY_TYPE_INVALID, "δ֪�Ķ���"
                                    + (i + 1) + "��������");
                }
            }
        } catch (SQLException ex) {
            throw new WCMException(ExceptionNumber.ERR_DBOP_FAIL,
                    "��ѯ���ݿ⣬��дPreparedStatement����[Index=" + (i + 1) + ", value="
                            + value + "]ʱ����", ex);
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_UNKNOWN,
                    "��ѯ���ݿ⣬��дPreparedStatement����[Index=" + (i + 1) + ", value="
                            + value + "]ʱ����", ex);
        }// endtry
    }
    
    public String sqlExecuteQueryString(String _sSQL, List _lParameters)
            throws WCMException {
       Object oValue = sqlExecuteQuery(_sSQL, _lParameters);
       if(oValue == null)return null;
       
       return oValue.toString();
    }
    
    
    public int sqlExecuteQueryInt(String _sSQL, List _lParameters)
            throws WCMException {
       Object oValue = sqlExecuteQuery(_sSQL, _lParameters);
       if(oValue == null)return -999;
       
       return ((Number)oValue).intValue();
    }
    
    public Object sqlExecuteQuery(String _sSQL, List _lParameters)
            throws WCMException {
        Connection oConn = null;
        PreparedStatement oStmt = null;
        ResultSet rsData = null;

        try {
            // �����ݿ��м��������
            oConn = this.getConnection();
            oStmt = oConn.prepareStatement(_sSQL);
            oStmt.setMaxRows(1);
            setParameters(oStmt, _lParameters);

            rsData = oStmt.executeQuery();
            if (rsData.next()) {
                return  rsData.getObject(1);
            }
        } catch (SQLException ex) {
            throw new WCMException(ExceptionNumber.ERR_DBOP_FAIL, I18NMessage
                    .get(DBManager.class, "DBManager.label19", "��ѯ����ʧ�ܣ�SQL=")
                    + _sSQL, ex);
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_UNKNOWN, I18NMessage
                    .get(DBManager.class, "DBManager.label19", "��ѯ����ʧ�ܣ�SQL=")
                    + _sSQL, ex);
        } finally {
            try {
                if (oConn != null) {
                    this.freeConnection(oConn);
                }
                if (oStmt != null) {
                    oStmt.close();
                }
                if (rsData != null) {
                    rsData.close();
                }
            } catch (Exception ignore) {
            }
        } // end try

        return null;
    }

}
