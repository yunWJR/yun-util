package com.yun.util.module.config;

import com.yun.util.common.JsonUtil;
import com.yun.util.common.SpringEvn;
import com.yun.util.module.rsp.RspDataCodeType;
import com.yun.util.module.rsp.RspDataException;
import com.yun.util.module.rsp.RspDataT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * 全局的的异常拦截器
 * @author: yun
 * @createdOn: 2019-02-25 13:23.
 */
@RestControllerAdvice
// @Component
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private SpringEvn springEvn;

    public boolean isProEvn() {
        return springEvn.isProEvn();
    }

    /**
     * 可 overwrite
     * @return
     */
    public boolean isDetailsInProEvn() {
        return true;
    }

    // /**
    //  * Hibernate 抛出的参数验证异常
    //  * @param e
    //  * @return
    //  */
    // @ExceptionHandler(ConstraintViolationException.class)
    // @ResponseBody
    // public RspDataT ConstraintViolationException(ConstraintViolationException e) {
    //     log.error(getLogFileExceptionMsg(e));
    //
    //     if (isProEvn()) {
    //         return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数异常");
    //     }
    //
    //     String errMsg = null;
    //
    //     Object[] objs = e.getConstraintViolations().toArray();
    //     if (objs != null && objs.length > 0) {
    //         // todo 其他类型
    //         ConstraintViolationImpl obj = (ConstraintViolationImpl) objs[0];
    //
    //         if (obj != null) {
    //             errMsg = String.format("参数(%s) 错误：%s", obj.getPropertyPath(), obj.getMessage());
    //         }
    //     }
    //
    //     if (errMsg == null) {
    //         errMsg = "参数异常";
    //     }
    //
    //     log.error("参数非法异常={}", e.getMessage(), e);
    //     return new RspDataT(RspDataCodeType.ComErr, errMsg);
    // }

    /**
     * 参数非法异常.
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public RspDataT handleIllegalArgumentException(IllegalArgumentException e) {
        log.error(getLogFileExceptionMsg(e));

        RspDataT rst = handleIllegalArgumentExceptionPre(e);
        if (rst != null) {
            return rst;
        }

        if (isProEvn()) {
            return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数异常");
        }

        return new RspDataT(RspDataCodeType.ComErr, e.getMessage());
    }

    public RspDataT handleIllegalArgumentExceptionPre(IllegalArgumentException e) {
        return null;
    }

    /**
     * RspDataException 异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(RspDataException.class)
    @ResponseBody
    public RspDataT handleRspDataException(RspDataException e) {
        if (e.getRst() != null) {
            log.error(JsonUtil.toStr(e.getRst()));
        } else {
            log.error(getLogFileExceptionMsg(e));
        }

        RspDataT rst = handleRspDataExceptionPre(e);
        if (rst != null) {
            return rst;
        }

        if (e.getRst() != null) {

            return e.getRst();
        }

        return new RspDataT(RspDataCodeType.ComErr, e.getMessage());
    }

    public RspDataT handleRspDataExceptionPre(RspDataException e) {
        return null;
    }

    /**
     * 处理通用异常
     * @param e the e
     * @return the base rst bean
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RspDataT handleException(Exception e) {
        log.error(getLogFileExceptionMsg(e));

        RspDataT rst = handleExceptionPre(e);
        if (rst != null) {
            return rst;
        }

        if (isProEvn()) {
            return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数异常");
        }

        String errMsg = this.getExceptionMsg(e);

        return RspDataT.ComErrBean(errMsg);
    }

    public RspDataT handleExceptionPre(Exception e) {
        return null;
    }

    /**
     * 处理所有接口数据验证异常（@valid）
     * @param e the e
     * @return the base rst bean
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public RspDataT handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest req) {
        log.error(getLogFileExceptionMsg(e));

        RspDataT rst = handleMethodArgumentNotValidExceptionPre(e, req);
        if (rst != null) {
            return rst;
        }

        if (isProEvn()) {
            return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数验证失败");
        }

        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        StringBuilder errorStr = new StringBuilder("参数不合法：");
        for (FieldError error : errors) {
            String err = error.getField() + ":" + error.getDefaultMessage();
            // if (error.getClass().equals(FieldError.class)) {
            //     FieldError fErr = (FieldError) error;
            //
            //     err = fErr.getField() + ":" + fErr.getDefaultMessage();
            // } else {
            //     Class errorClass = error.getClass();
            //
            //     String fieldName = null;
            //     String errDt = null;
            //
            //     try {
            //         Field fF = errorClass.getField("field");
            //         if (fF != null) {
            //             Object value = fF.get(errorClass);
            //             if (value != null) {
            //                 fieldName = value.toString();
            //             }
            //         }
            //
            //         Field fDm = errorClass.getField("defaultMessage");
            //         if (fDm != null) {
            //             Object value = fDm.get(errorClass);
            //             if (value != null) {
            //                 errDt = value.toString();
            //             }
            //         }
            //     } catch (Exception ee) {
            //         ee.printStackTrace();
            //     }
            //
            //     if (fieldName != null && errDt != null) {
            //         err = fieldName + ":" + errDt;
            //     } else {
            //         err = error.getDefaultMessage();
            //     }
            // }

            errorStr.append(err).append(";");
        }

        return new RspDataT(RspDataCodeType.ComErr, errorStr.toString());
    }

    public RspDataT handleMethodArgumentNotValidExceptionPre(MethodArgumentNotValidException e, HttpServletRequest req) {
        return null;
    }

    // region -- method

    public String getExceptionMsg(Exception e) {
        String errMsg = null;
        if (e.getCause() != null) {
            errMsg = "msg:" + e.getCause().getMessage() + " locMsg:" + e.getCause().getLocalizedMessage();
        } else {
            errMsg = "msg:" + e.getMessage() + " locMsg:" + e.getLocalizedMessage();
        }

        // errMsg = e.toString();

        return errMsg;
    }

    public String getLogFileExceptionMsg(Exception e) {
        return e.getMessage() + "\n" + getStackMessage(e);
    }

    public String getStackMessage(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            // 将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }

    // endregion
}