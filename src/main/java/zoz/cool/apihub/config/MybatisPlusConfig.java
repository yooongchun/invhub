package zoz.cool.apihub.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis相关配置
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"zoz.cool.apihub.dao.mapper"})
public class MybatisPlusConfig {
}