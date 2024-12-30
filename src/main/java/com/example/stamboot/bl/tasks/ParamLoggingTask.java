package com.example.stamboot.bl.tasks;

import com.mgnt.lifecycle.management.backgroundrunner.BaseBackgroundRunnable;
import com.mgnt.utils.entities.TimeInterval;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

@Component
@Lazy(false)
public class ParamLoggingTask  extends BaseBackgroundRunnable {

    private static final Log logger = LogFactory.getLog(ParamLoggingTask.class);
    private static final TimeInterval DEFAULT_VALUE = new TimeInterval(1, TimeUnit.HOURS);

    private ConcurrentSkipListSet<String> usedParams = new ConcurrentSkipListSet<>();

    private String paramLoggingTimeIntervalStr = "10s";

    private TimeInterval paramLoggingTimeInterval;
    private StringBuilder sb = new StringBuilder();

    @Override
    protected void initParamsForSpecificImplementation() {
        initTimeIntervalParam(getParamLoggingTimeIntervalStr(), DEFAULT_VALUE, null);
    }

    @Override
    public TimeInterval getTaskExecutionInterval() {
        return getParamLoggingTimeInterval();
    }

    @Override
    public void setParamValue(TimeInterval timeInterval, String s) {
        setParamLoggingTimeInterval(timeInterval);
    }

    @Override
    public void run() {
        if(!usedParams.isEmpty()) {
            sb.setLength(0);
            sb.append("Parameters used so far:");
            usedParams.forEach(param -> sb.append("\n").append(param));
            usedParams.clear();
            logger.info(sb);
        }
    }

    public void collectPassedParams(MultiValueMap<String, String> paramMap) {
        paramMap.forEach((key, val) -> {
            String paramKeyAndVal = key + " : " + val;
            if(!usedParams.contains(paramKeyAndVal)) {
                usedParams.add(paramKeyAndVal);
            }
        });
    }


    public TimeInterval getParamLoggingTimeInterval() {
        return paramLoggingTimeInterval;
    }

    public void setParamLoggingTimeInterval(TimeInterval paramLoggingTimeInterval) {
        this.paramLoggingTimeInterval = paramLoggingTimeInterval;
    }

    public String getParamLoggingTimeIntervalStr() {
        return paramLoggingTimeIntervalStr;
    }

    public void setParamLoggingTimeIntervalStr(String paramLoggingTimeIntervalStr) {
        this.paramLoggingTimeIntervalStr = paramLoggingTimeIntervalStr;
    }
}
