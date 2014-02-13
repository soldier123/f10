package controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.BondSec;
import dto.companyinfo.AgencyOrgDto;
import dto.companyinfo.EqIpoInfoDto;
import dto.companyinfo.MarketQuotationDto;
import dto.dividend.AddIssuingDetail;
import dto.dividend.AllotmentDetail;
import dto.dividend.CashBonusDetail;
import dto.dividend.RaiseFundOverall;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

import java.util.List;

/**
 * 分红融资
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:46
 */
@With(value = {BaseInfoIntercept.class} )
public class DividendCt extends Controller {
    public  static void info(String scode){
        Gson gson = CommonUtils.createGson();
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        BondSec secA = BondSec.fetchSecByInstitutionId(sec.institutionId, BondSec.Type_A);
        //个股的
        RaiseFundOverall secRaiseFund = RedisUtil.fetchFromRedis(RedisKey.Dividend.cashBonusAndRaiseFund + sec.secId, RaiseFundOverall.class, gson);
        //市场的
        RaiseFundOverall marketRaiseFund = RedisUtil.fetchFromRedis(RedisKey.Dividend.allMarketCashBonusAndRaiseFund, RaiseFundOverall.class, gson);

        if(secRaiseFund == null){
            secRaiseFund = new RaiseFundOverall();
        }
        if(marketRaiseFund == null){
            marketRaiseFund = new RaiseFundOverall();
        }

        //分红明细
        List<CashBonusDetail>  cashBonusDetailList = RedisUtil.fetchFromRedis(RedisKey.Dividend.cashBonusDetail + secA.secId,
                new TypeToken<List<CashBonusDetail>>(){}.getType(), gson);

        //增发明细.
        List<AddIssuingDetail> addIssuingDetailList = RedisUtil.fetchFromRedis(RedisKey.Dividend.addIssuingDetail + sec.secId,
                new TypeToken<List<AddIssuingDetail>>(){}.getType(), gson);

        //配股明细
        List<AllotmentDetail> allotmentDetailList = RedisUtil.fetchFromRedis(RedisKey.Dividend.allotmentDetail + sec.secId,
                new TypeToken<List<AllotmentDetail>>() {
                }.getType(), gson);

        //上市首日
        EqIpoInfoDto ipo = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.eqIpoInfo + secA.secId, EqIpoInfoDto.class, gson);
        AgencyOrgDto orgDto = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.agencyOrg + secA.secId, AgencyOrgDto.class, gson);
        MarketQuotationDto marketQuotationDto = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.ipoMarketQuotation + secA.secId, MarketQuotationDto.class, gson);
        render(sec, secRaiseFund, marketRaiseFund,
                cashBonusDetailList, addIssuingDetailList, allotmentDetailList,
                ipo, orgDto, marketQuotationDto
        );
    }
}
