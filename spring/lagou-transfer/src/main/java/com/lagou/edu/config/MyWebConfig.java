package com.lagou.edu.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.lagou.edu.annotation.component.Bean;
import com.lagou.edu.annotation.component.Configuration;
import com.lagou.edu.annotation.context.ComponentScan;
import com.lagou.edu.annotation.context.EnableTransactionManagement;
import com.lagou.edu.annotation.context.PropValue;
import com.lagou.edu.annotation.context.PropertySource;

/**
 * @author wuwenbin
 */
@Configuration
@ComponentScan("com.lagou.edu")
@PropertySource
@EnableTransactionManagement
public class MyWebConfig {

    @PropValue("jdbc.driver")
    private String driver;
    @PropValue("jdbc.url")
    private String url;
    @PropValue("jdbc.username")
    private String username;
    @PropValue("jdbc.password")
    private String password;

    @Bean
    public DruidDataSource druidDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(driver);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        return druidDataSource;
    }

}
