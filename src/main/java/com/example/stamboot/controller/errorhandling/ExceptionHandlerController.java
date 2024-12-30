package com.example.stamboot.controller.errorhandling;


import com.mgnt.utils.TextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@ResponseBody
public class ExceptionHandlerController {

    private static final Log logger = LogFactory.getLog(ExceptionHandlerController.class);
    private static final String GENERAL_ERROR_MESSAGE_PREFIX = "Unexpected error occurred: ";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String genericExceptionHandler(Exception e, WebRequest request) {
        logger.error(GENERAL_ERROR_MESSAGE_PREFIX + TextUtils.getStacktrace(e));
        return TextUtils.formatStringToPreserveIndentationForHtml(GENERAL_ERROR_MESSAGE_PREFIX + e.getMessage());
    }
}
