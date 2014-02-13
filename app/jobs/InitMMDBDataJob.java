package jobs;

import dbutils.ExtractDbUtil;
import dbutils.MemDbUtil;
import dbutils.SqlLoader;
import dto.StockInfoDto;
import dto.financing.Financing;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import util.RedisKey;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户程序启动创建内存数据库
 * User: liangbing
 * Date: 13-5-15
 * Time: 下午1:11
 */
@OnApplicationStart
public class InitMMDBDataJob extends Job {

    @Override
    public void doJob() throws Exception {
        initStockInfo();
    }

    /**
     * 初始化股票信息保存到内存数据库中
     */
    public void initStockInfo() {
        //删除表SQL
        String dropStockInfoSql = SqlLoader.getSqlById("dropStockInfo");
        //创建表
        String createStockInfosql = SqlLoader.getSqlById("createStockInfo");
        //如果存在就删除表,然后重新创建表
        MemDbUtil.execute(dropStockInfoSql);
        MemDbUtil.execute(createStockInfosql);

        //批量给内存数据库插入数据
        batchInsert();
    }


    /**
     * 往数据库里面查询证券代码简称的对应关系
     *
     * @return
     */
    public static List<StockInfoDto> getStockInfo() {
        String sql = SqlLoader.getSqlById("stkStockinfoSql");
        List<StockInfoDto> stockInfoList = ExtractDbUtil.queryExtractDBBeanList(sql, StockInfoDto.class);
        if (stockInfoList == null) {
            stockInfoList = new ArrayList<StockInfoDto>();
        }
        return stockInfoList;
    }


    public static void batchInsert() {

        //给内存数据库表插入数据的SQL语句
        String insertSql = SqlLoader.getSqlById("insertStockInfo");

        //获取要插入数据的list集合
        List<StockInfoDto> stockInfoDtoList = getStockInfo();

        //批量插入数据到内存数据库中
        if (stockInfoDtoList != null && stockInfoDtoList.size() > 0) {
            Object[][] params = new Object[stockInfoDtoList.size()][3];
            for (int row = 0; row < stockInfoDtoList.size(); row++) {
                StockInfoDto stockInfoDto = stockInfoDtoList.get(row);
                params[row][0] = stockInfoDto.symbol;
                params[row][1] = stockInfoDto.shortname;
                params[row][2] = stockInfoDto.pyshortname.replaceAll(" ",""); //去掉里面所有的空格,这个方法是不是有点不专业;
            }
            MemDbUtil.batchInsert(insertSql, params);
        }
    }
}
