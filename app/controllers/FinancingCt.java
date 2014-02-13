package controllers;

import com.google.gson.Gson;
import dto.BondSec;
import dto.financing.Financing;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.util.List;

/**
 * 融资融券
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:50
 */
@With(value = {BaseInfoIntercept.class})
public class FinancingCt extends Controller {
    public static void info(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();
        List<Financing> financingList = RedisUtil.lrange(RedisKey.Financing.financing + sec.secId, Financing.class, gson, 0, 9);//把前面10条取出来
        render(sec, financingList);
    }


    public static void more(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();
        List<Financing> financingList = RedisUtil.lrange(RedisKey.Financing.financing + sec.secId, Financing.class, gson, 0, 300);//把前面301条取出来
        render(sec, financingList);
    }


}
