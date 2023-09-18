package cn.hdy.common.project.filter;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.ErrorTypeAwareLogger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.filter.ExceptionFilter;

/**
 * @author 混沌鸭
 **/
@Activate(group = {"provider", "consumer"})
public class CustomExceptionFilter implements Filter, BaseFilter.Listener {
    private ErrorTypeAwareLogger logger = LoggerFactory.getErrorTypeAwareLogger(ExceptionFilter.class);

    public CustomExceptionFilter() {
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        if (result.hasException()){
            Throwable exception = result.getException();
            logger.info("==============================");
            logger.info(exception);
            logger.info("==============================");
            String message = exception.getMessage();
            if (message == null || message.length() > 50){
                // 进入此处应该是代码逻辑存在错误
                message = "系统内部异常";
            }
            result.setException(new RuntimeException(message));
        }
        return result;
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
    }

    @Override
    public void onError(Throwable e, Invoker<?> invoker, Invocation invocation) {
        this.logger.error("5-36", "", "", "Got unchecked and undeclared exception which called by " + RpcContext.getServiceContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName() + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
    }

    public void setLogger(ErrorTypeAwareLogger logger) {
        this.logger = logger;
    }
}
