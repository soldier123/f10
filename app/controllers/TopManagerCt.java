package controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.topmanager.HoldShare;
import dto.topmanager.HoldingChange;
import dto.topmanager.TopManager;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 高管 控制器
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:45
 */
@With(value = {BaseInfoIntercept.class})
public class TopManagerCt extends Controller {

    public static void info(String scode){
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();

        Type topManageListType = new TypeToken<List<TopManager>>() {
        }.getType();

        //高管人员. (P353), 董事会(P351), 监事会(P352)
        List<TopManager> executiveList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.TopManager.topManagerP353 + sec.institutionId, topManageListType, gson);
        List<TopManager> boardList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.TopManager.topManagerP351 + sec.institutionId, topManageListType, gson);
        List<TopManager> aufsichtsratList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.TopManager.topManagerP352 + sec.institutionId, topManageListType, gson);

        //高管离职信息
        List<TopManager> leaveList = RedisUtil.fetchFromRedisWithDecompress(RedisKey.TopManager.leaveOffice + sec.institutionId, topManageListType, gson);

        //高管持股信息
        F.T3<String, String, String> t3 = holdShare(gson, sec);
        String picDataStr = t3._1;
        String barXDataStr = t3._2;
        String barYDataStr = t3._3;

        //高管持股变动                                                               RedisKey.TopManager.holdingchange + symbol
        List<HoldingChange> changeList = RedisUtil.fetchFromRedis(RedisKey.TopManager.holdingchange + sec.code,
                new TypeToken<List<HoldingChange>>(){}.getType(), gson);

        render(sec,
            executiveList, boardList, aufsichtsratList,
            leaveList,
            picDataStr, barXDataStr, barYDataStr,
            changeList
        );
    }

    private static F.T3<String, String, String> holdShare(Gson gson, BondSec sec) {
        //高管持股信息
        List<HoldShare> holdShareList = RedisUtil.fetchFromRedis(RedisKey.TopManager.holdShare + sec.institutionId, new TypeToken<List<HoldShare>>() {
        }.getType(), gson);

        //格式如 [['王石', 5900], ['郁亮', 7800]]
        String picDataStr = "[]"; //饼图数据

        //格式如: ['王石', '郁亮']
        String barXDataStr = "[]";//柱状x轴数据

        //格式如: [{'name': '2011-12-31','data': [1200,1400]}]
        String barYDataStr = "[]"; //柱状图Y轴数据

        if(holdShareList != null && holdShareList.size() > 0){
            List<Object[]> picData = Lists.newArrayList();
            List<Object> barXData = Lists.newArrayList();
            List<Object> barYData = Lists.newArrayList();

            int itemCount = 0;
            long otherOldNumSum = 0; //其它之和
            for (HoldShare h : holdShareList) {
                if (itemCount < 4 && h.holdNum != 0) {
                    Object[] itermArr = new Object[2];
                    itermArr[0] = h.holdName;
                    itermArr[1] = h.holdNum;
                    picData.add(itermArr);
                    barXData.add(h.holdName);
                    barYData.add(h.holdNum);
                } else {
                    otherOldNumSum += h.holdNum;
                }
                itemCount++;
            }

            //加入其它
            if (otherOldNumSum > 0) {
                Object[] itermArr = new Object[2];
                itermArr[0] = "其它股东";
                itermArr[1] = otherOldNumSum;
                picData.add(itermArr);

                barXData.add("其它股东");
                barYData.add(otherOldNumSum);
            }
            picDataStr = gson.toJson(picData);
            barXDataStr = gson.toJson(barXData);

            Map<String,Object> barYDataMap = Maps.newHashMap();
            barYDataMap.put("name", CommonUtils.getFormatDate( "yyyy-MM-dd", holdShareList.get(0).endDate));
            barYDataMap.put("data", barYData);
            barYDataStr = gson.toJson(Lists.newArrayList(barYDataMap));
        }

        return F.T3(picDataStr, barXDataStr, barYDataStr);
    }

}
