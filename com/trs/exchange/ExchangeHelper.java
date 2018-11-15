package com.trs.exchange;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.BaseObj;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.ExceptionNumber;
import com.trs.infra.util.database.CDBText;
import com.trs.infra.util.database.DBType;

public class ExchangeHelper {
    public static final void readFromRs(BaseObj _oBaseObj, ResultSet _rsData,
            ResultSetMetaData _rsmdData, DBType _oDBType) throws WCMException {
            readFromRs(_oBaseObj, _rsData, _rsmdData, _oDBType, false);
    }
    
    public static final void readFromRs(BaseObj _oBaseObj, ResultSet _rsData,
            ResultSetMetaData _rsmdData, DBType _oDBType, boolean _bDataTypeIsString) throws WCMException {
        try {
            if (_rsData == null)
                return;

            Object objValue = null;

            // ע�⣺�����ж� !_rsData.isAfterLast() && !_rsData.isBeforeFirst()
            // ��Ϊ��jdbc.odbc�в�֧�ֻع�
            String sFieldName;
            ResultSetMetaData rsmdData = (_rsmdData != null ? _rsmdData
                    : _rsData.getMetaData());

            int nColCount = rsmdData.getColumnCount();
            for (int i = 1; i <= nColCount; i++) {
                sFieldName = rsmdData.getColumnLabel(i).toUpperCase();
                objValue = null;

                switch (rsmdData.getColumnType(i)) {
                case java.sql.Types.NUMERIC:
                case java.sql.Types.DECIMAL: {

                    if (rsmdData.getScale(i) <= 0) {
                        long lValue = _rsData.getLong(i);
                        if (!_rsData.wasNull()) { // why@2002-04-22 �ж��ֶ��Ƿ�Ϊ��
                            objValue = new Long(lValue);
                            // ��ʹ��Integer���Է�ֹlong���������
                        }
                    } else {
                        Double dValue = new Double(_rsData.getDouble(i));
                        if (!_rsData.wasNull())
                            objValue = dValue;
                    }
                    break;
                }
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.BIGINT: { // ����
                    long lValue = _rsData.getLong(i);
                    if (!_rsData.wasNull()) { // why@2002-04-22 �ж��ֶ��Ƿ�Ϊ��
                        objValue = new Long(lValue);
                        // ��ʹ��Integer���Է�ֹlong���������
                    }
                    break;
                }
                case java.sql.Types.CHAR: // added by hxj
                case Types.VARCHAR: { // �䳤�ַ���
                    objValue = _rsData.getString(i);
                    break;
                }
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP: { // ����ʱ����
                    objValue = new CMyDateTime();
                    ((CMyDateTime) objValue).setDateTimeWithRs(_rsData, i);
                    break;
                }
                case Types.CLOB:
                case Types.LONGVARCHAR: { // ���ı�����
                    objValue = new CDBText(_oDBType);
                   ((CDBText) objValue).readFromRs(_rsData, i);                   
                    break;
                }
                case Types.DOUBLE: { // ˫��������
                    double dValue = _rsData.getDouble(i);
                    if (!_rsData.wasNull())
                        objValue = new Double(dValue);
                    break;
                }
                default: { // ��������
                    objValue = _rsData.getObject(i);
                    break;
                }
                }

                if (objValue != null) {
                    if(_bDataTypeIsString){
                        _oBaseObj.setProperty(sFieldName, objValue.toString(), false);
                    }else{
                        _oBaseObj.setProperty(sFieldName, objValue, false);
                    }
                }// endif
            }// endfor

            _oBaseObj.removeProperty(_oBaseObj.getIdFieldName());
        } catch (SQLException ex) {
            throw new WCMException(ExceptionNumber.ERR_DBOP_FAIL,
                    "�����ݿ��ж�ȡ������Ϣʱ����", ex);
        } catch (CMyException ex) {
            throw new WCMException(ExceptionNumber.ERR_MYEXCEPTION,
                     "�����ݿ��ж�ȡ������Ϣʱ����", ex);
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_UNKNOWN,
                    "�����ݿ��ж�ȡ������Ϣʱ����", ex);
        }// endtry
    }
    
    /**
     * �ж��ļ���׺���Ƿ��ǲ������ϴ����ļ���׺��
     */
    public static boolean isForbidFileExt(String _sFileExt) {        
        // ��ȡϵͳ�����ϴ��ļ���׺����
        String strSuffixConfig = ConfigServer.getServer().getInitProperty(
                "FILE_UPLOAD_SUFFIX_CONFIG");
        // Ϊ�ձ�ʾΪ��ֹ
        if (CMyString.isEmpty(strSuffixConfig))
            return true;
        
        String sUpperCaseExt = _sFileExt.toUpperCase();

        // ����������˰�����
        if ("FILE_UPLOAD_ALLOW_SUFFIX".equals(strSuffixConfig)) {
            String strAllowExt = ConfigServer.getServer().getInitProperty(
                    strSuffixConfig);
            if (CMyString.isEmpty(strAllowExt))
                return true;
            strAllowExt = strAllowExt.toUpperCase().trim();
            return ("," + strAllowExt + ",").indexOf("," + sUpperCaseExt + ",") < 0;
        }
        // ����������˺�����
        else if ("FILE_UPLOAD_FORBIDEN_SUFFIX".equals(strSuffixConfig)) {
            String strForbidExt = ConfigServer.getServer().getInitProperty(
                    strSuffixConfig);
            if (CMyString.isEmpty(strForbidExt))
                return false;
            strForbidExt = strForbidExt.toUpperCase().trim();
            return ("," + strForbidExt + ",").indexOf("," + sUpperCaseExt + ",") > 0;
        }
        // Ĭ��Ϊ��ֹ
        return true;
    }

    public static String parserInnerText(String _sHTMLContent) {
        // �滻Script
        String sReg = "(?is)<script.*?>.*?</script>";
        String sResult = _sHTMLContent.replaceAll(sReg, "");

        // �滻����TAG
        sReg = "(?is)<[^>].*?>";
        sResult = sResult.replaceAll(sReg, "");

        // �滻����ʵ��
        String[] pEntityRegs = { "&(quot|#34);", "&(amp|#38);", "&(lt|#60);",
                "&(gt|#62);", "&(nbsp|#160);" };
        String[] pEntitys = { "\"", "&", "<", ">", " " };
        for (int i = 0; i < pEntityRegs.length; i++) {
            sResult = sResult.replaceAll(pEntityRegs[i], pEntitys[i]);
        }

        return sResult;
    }

    public static String extractFileExt(String _sFileName) {
        String sFileExt = _sFileName;
        int nPos = _sFileName.lastIndexOf('?');
        if (nPos >= 0) {
            sFileExt = sFileExt.substring(0, nPos);
        }
        nPos = sFileExt.lastIndexOf('.');
        if (nPos <= 0)
            return null;
    
        return sFileExt.substring(nPos + 1).toLowerCase();
    }
}
