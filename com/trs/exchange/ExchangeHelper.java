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

            // 注意：不做判断 !_rsData.isAfterLast() && !_rsData.isBeforeFirst()
            // 因为：jdbc.odbc中不支持回滚
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
                        if (!_rsData.wasNull()) { // why@2002-04-22 判断字段是否为空
                            objValue = new Long(lValue);
                            // 不使用Integer，以防止long型数据溢出
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
                case Types.BIGINT: { // 整型
                    long lValue = _rsData.getLong(i);
                    if (!_rsData.wasNull()) { // why@2002-04-22 判断字段是否为空
                        objValue = new Long(lValue);
                        // 不使用Integer，以防止long型数据溢出
                    }
                    break;
                }
                case java.sql.Types.CHAR: // added by hxj
                case Types.VARCHAR: { // 变长字符串
                    objValue = _rsData.getString(i);
                    break;
                }
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP: { // 日期时间型
                    objValue = new CMyDateTime();
                    ((CMyDateTime) objValue).setDateTimeWithRs(_rsData, i);
                    break;
                }
                case Types.CLOB:
                case Types.LONGVARCHAR: { // 大文本数据
                    objValue = new CDBText(_oDBType);
                   ((CDBText) objValue).readFromRs(_rsData, i);                   
                    break;
                }
                case Types.DOUBLE: { // 双精度数据
                    double dValue = _rsData.getDouble(i);
                    if (!_rsData.wasNull())
                        objValue = new Double(dValue);
                    break;
                }
                default: { // 其他类型
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
                    "从数据库中读取对象信息时出错", ex);
        } catch (CMyException ex) {
            throw new WCMException(ExceptionNumber.ERR_MYEXCEPTION,
                     "从数据库中读取对象信息时出错", ex);
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_UNKNOWN,
                    "从数据库中读取对象信息时出错", ex);
        }// endtry
    }
    
    /**
     * 判断文件后缀名是否是不允许上传的文件后缀名
     */
    public static boolean isForbidFileExt(String _sFileExt) {        
        // 获取系统允许上传文件后缀配置
        String strSuffixConfig = ConfigServer.getServer().getInitProperty(
                "FILE_UPLOAD_SUFFIX_CONFIG");
        // 为空表示为禁止
        if (CMyString.isEmpty(strSuffixConfig))
            return true;
        
        String sUpperCaseExt = _sFileExt.toUpperCase();

        // 如果是启用了白名单
        if ("FILE_UPLOAD_ALLOW_SUFFIX".equals(strSuffixConfig)) {
            String strAllowExt = ConfigServer.getServer().getInitProperty(
                    strSuffixConfig);
            if (CMyString.isEmpty(strAllowExt))
                return true;
            strAllowExt = strAllowExt.toUpperCase().trim();
            return ("," + strAllowExt + ",").indexOf("," + sUpperCaseExt + ",") < 0;
        }
        // 如果是启用了黑名单
        else if ("FILE_UPLOAD_FORBIDEN_SUFFIX".equals(strSuffixConfig)) {
            String strForbidExt = ConfigServer.getServer().getInitProperty(
                    strSuffixConfig);
            if (CMyString.isEmpty(strForbidExt))
                return false;
            strForbidExt = strForbidExt.toUpperCase().trim();
            return ("," + strForbidExt + ",").indexOf("," + sUpperCaseExt + ",") > 0;
        }
        // 默认为禁止
        return true;
    }

    public static String parserInnerText(String _sHTMLContent) {
        // 替换Script
        String sReg = "(?is)<script.*?>.*?</script>";
        String sResult = _sHTMLContent.replaceAll(sReg, "");

        // 替换所有TAG
        sReg = "(?is)<[^>].*?>";
        sResult = sResult.replaceAll(sReg, "");

        // 替换所有实体
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
