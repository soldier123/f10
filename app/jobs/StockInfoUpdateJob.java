package jobs;

import dbutils.MemDbUtil;
import dbutils.SqlLoader;
import play.jobs.Job;
import play.jobs.On;

/**
 * 定时更新 内存表 stockinfo 内容
 * User: liangbing
 * Date: 13-5-15
 * Time: 下午1:20
 */
@On("0 10 0 * * ?")
public class StockInfoUpdateJob extends Job {

    @Override
    public void doJob() {
        updateStockInfo();
    }

    public void updateStockInfo(){
        String deleteSql = SqlLoader.getSqlById("deleteStockInfo");
        //先删除里面的数据
        MemDbUtil.execute(deleteSql);
        //插入新数据
        InitMMDBDataJob.batchInsert();
    }
}
