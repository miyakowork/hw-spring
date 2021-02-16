package com.lagou.edu.service.impl;

import com.lagou.edu.annotation.aop.Transactional;
import com.lagou.edu.annotation.component.Component;
import com.lagou.edu.annotation.dependency.Autowired;
import com.lagou.edu.dao.AccountDao;
import com.lagou.edu.excpetion.TestException;
import com.lagou.edu.pojo.Account;
import com.lagou.edu.service.TransferService;

/**
 * 改造为注入
 *
 * @author 应癫
 */
@Component("transferService")
@Transactional(rollbackForClass = Exception.class)
public class TransferServiceImpl implements TransferService {

    /**
     * 注入工厂中的 bean
     */
    @Autowired
    private AccountDao accountDao;


    /**
     * 使用改造后的声明式注解事务
     *
     * @param fromCardNo
     * @param toCardNo
     * @param money
     * @throws Exception
     */
    @Override
    @Transactional(rollbackForClass = TestException.class)//首先我们吧这里的异常改为 TestException, 发生错误如果不属于这个类或者为这个类的子类是不会回滚的
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
        Account from = accountDao.queryAccountByCardNo(fromCardNo);
        Account to = accountDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney() - money);
        to.setMoney(to.getMoney() + money);

        accountDao.updateAccountByCardNo(to);
        int c = 1 / 0;
        accountDao.updateAccountByCardNo(from);

    }
}
