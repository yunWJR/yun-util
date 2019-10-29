package com.yun.util.module.config;

import com.yun.util.module.rsp.RspDataCodeType;
import com.yun.util.module.rsp.RspDataException;
import com.yun.util.module.rsp.RspDataT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局的的异常拦截器
 * @author: yun
 * @createdOn: 2019-02-25 13:23.
 */
@Slf4j
@RestControllerAdvice
@Component
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String profile;

    private Boolean isProEvn;

    public boolean isProEvn() {
        if (isProEvn == null) {
            isProEvn = "pro".equals(profile);
        }

        return isProEvn;
    }

    public boolean isDetailsInProEvn() {
        return true;
    }

    /**
     * 参数非法异常.
     * @param e the e
     * @return the wrapper
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public RspDataT illegalArgumentException(IllegalArgumentException e) {
        log.error(getLogFileExceptionMsg(e));

        if (isProEvn()) {
            return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数异常");
        }

        return new RspDataT(RspDataCodeType.ComErr, e.getMessage());
    }

    /**
     * Hibernate 抛出的参数验证异常
     * @param e
     * @return
     */
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
    @ExceptionHandler(RspDataException.class)
    @ResponseBody
    public RspDataT handleBusinessException(RspDataException e) {
        log.error(getLogFileExceptionMsg(e));

        return new RspDataT(e.getRst().getCode(), e.getRst().getErrorMsg());
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

        if (isProEvn()) {
            return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数异常");
        }

        String errMsg = this.getExceptionMsg(e);

        return RspDataT.ComErrBean(errMsg);
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

        if (isProEvn()) {
            return new RspDataT(RspDataCodeType.ComErr, isDetailsInProEvn() ? e.getMessage() : "参数验证失败");
        }

        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        String errMsg = null;
        if (errors.size() > 0) {
            FieldError err = (FieldError) errors.get(0);
            if (err != null) {
                errMsg = String.format("参数(%s) 错误：%s", err.getField(), err.getDefaultMessage());
            }

            // todo 其他类型
        }

        if (errMsg == null) {
            errMsg = e.getMessage();
        }

        return new RspDataT(RspDataCodeType.ComErr, errMsg);
    }

    // region --private method

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
        return e.getMessage();
    }

    // endregion
}