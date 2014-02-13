package controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.relatedstock.HolderItem;
import dto.relatedstock.SameInduItem;
import dto.relatedstock.SameShareHolder;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.util.List;
import java.util.Map;

/**
 * 关联个股
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:49
 */
@With(value = {BaseInfoIntercept.class})
public class RelatedStockCt extends Controller {

    public static void info(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();

        //取10大股东
        List<HolderItem> holderItemList = RedisUtil.fetchFromRedis(RedisKey.RelatedStock.top10Holders + sec.institutionId,
                new TypeToken<List<HolderItem>>() {
                }.getType(), gson);

        Map<HolderItem, List<SameShareHolder>> mapList = Maps.newLinkedHashMap();
        if (holderItemList != null && holderItemList.size() > 0) {
            List<SameShareHolder> sameShareHolderList = RedisUtil.fetchFromRedis(RedisKey.RelatedStock.sameshareholder + sec.institutionId,
                    new TypeToken<List<SameShareHolder>>(){}.getType(), gson);

            if(sameShareHolderList != null && sameShareHolderList.size() > 0){
                for (HolderItem h : holderItemList) {
                    List<SameShareHolder> subList = mapList.get(h);
                    if(subList == null){
                        subList = Lists.newLinkedList();
                        mapList.put(h, subList);
                    }

                    for (SameShareHolder ssh : sameShareHolderList) {
                        if(ssh.holderId == h.id){
                            subList.add(ssh);
                        }
                    }
                }
            }
        }

        //同行业个股(证监会二级行业)
        List<SameInduItem> sameInduItemList = RedisUtil.fetchFromRedis(RedisKey.RelatedStock.sameindustryholder + BondSec.fetchInduLevel2CodeByCode(scode),
                new TypeToken<List<SameInduItem>>(){}.getType(), gson);

        render(sec, holderItemList, mapList, sameInduItemList);


    }
}
