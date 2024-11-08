package zoz.cool.apihub.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 启动异步任务
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "default")
    public ThreadPoolTaskExecutor executorDefault() {
        return getAsyncExecutor(2, 10, 100, "async-default-");
    }

    @Bean(name = "inv")
    public ThreadPoolTaskExecutor executorInv() {
        return getAsyncExecutor(10, 50, 1000, "async-inv-");
    }

    @Bean(name = "inv-check")
    public ThreadPoolTaskExecutor executorInvCheck() {
        return getAsyncExecutor(5, 10, 10000, "async-inv-check-");
    }

    /**
     * 指定默认线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        return executorDefault();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("线程池执行任务发送未知错误,执行方法：{}", method.getName(), ex);
    }

    private ThreadPoolTaskExecutor getAsyncExecutor(int poolSize, int maxPoolSize, int queueCapacity, String threadNamePrefix) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数
        taskExecutor.setCorePoolSize(poolSize);
        //线程池维护线程的最大数量,只有在缓冲队列满了之后才会申请超过核心线程数的线程
        taskExecutor.setMaxPoolSize(maxPoolSize);
        //缓存队列
        taskExecutor.setQueueCapacity(queueCapacity);
        //设置空闲时间,当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
        taskExecutor.setKeepAliveSeconds(180);
        //异步方法内部线程名称
        taskExecutor.setThreadNamePrefix(threadNamePrefix);
        // 当线程池的任务缓存队列已满并且线程池中的线程数目达到maximumPoolSize，如果还有任务到来就会采取任务拒绝策略
        //* ThreadPoolExecutor.CallerRunsPolicy：重试添加当前的任务，自动重复调用 execute() 方法，直到成功
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }
}