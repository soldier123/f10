package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.majormatter.BlockTrade;
import dto.majormatter.Guarantee;
import dto.majormatter.Violation;
import dto.newestinfo.GreatInventRemind;
import dto.stockholdercapital.LimitedAndLift;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.util.List;

/**
 * 重大事项
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:48
 */
@With(value = {BaseInfoIntercept.class})
public class MajorMatterCt extends Controller {
    public static void info(String scode){
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        Gson gson = CommonUtils.createGson();

        //大事提醒
        List<GreatInventRemind> remindList = RedisUtil.fetchFromRedis(RedisKey.NewestInfo.great_InventRemind + secA.secId,
            new TypeToken<List<GreatInventRemind>>(){}.getType(), gson);


        //违规处理
        List<Violation> violationList = RedisUtil.fetchFromRedis(RedisKey.MajorMatter.violation + sec.institutionId,
                new TypeToken<List<Violation>>(){}.getType(), gson);

        //对外担保
        List<Guarantee> guaranteeList = RedisUtil.fetchFromRedis(RedisKey.MajorMatter.guarantee + sec.institutionId,
                new TypeToken<List<Guarantee>>(){}.getType(), gson);

        //限售解禁
        List<LimitedAndLift> limitedAndLiftList = RedisUtil.fetchFromRedis(RedisKey.StockHolderCapital.limitAndLift + sec.institutionId,
                new TypeToken<List<LimitedAndLift>>(){}.getType(), gson);

        String key = RedisKey.MajorMatter.blocktrade + sec.secId;
        //大宗交易
        List<BlockTrade> blockTradeList = RedisUtil.lrange(key, BlockTrade.class, gson, 0, 19);//取前20个
        render(sec, remindList, violationList, guaranteeList, limitedAndLiftList, blockTradeList);
    }

    //更多大宗交易
    public static void moreBlocktrade(String scode) {
        Gson gson = CommonUtils.createGson();
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        String key = RedisKey.MajorMatter.blocktrade + sec.secId;
        List<BlockTrade> blockTradeList = RedisUtil.lrange(key, BlockTrade.class, gson, 0, 199);//取前200条
        render(blockTradeList, sec);
    }
}
