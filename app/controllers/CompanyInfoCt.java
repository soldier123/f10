package controllers;

import com.google.gson.Gson;
import dto.BondSec;
import dto.companyinfo.*;
import play.modules.redis.Redis;
import play.mvc.Controller;
import play.mvc.With;
import util.CommonUtils;
import util.RedisKey;
import util.RedisUtil;

/**
 * 公司概况 控制器
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:35
 */
@With(value = {BaseInfoIntercept.class})
public class CompanyInfoCt extends Controller{

    public static void info(String scode) {
        BondSec sec = BondSec.fetchBondSecByCode(scode);
        Gson gson = CommonUtils.createGson();
        AgencyOrgDto agencyOrg = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.agencyOrg + sec.secId, AgencyOrgDto.class, gson);
        EqIpoInfoDto eqIpoInfoDto = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.eqIpoInfo + sec.secId, EqIpoInfoDto.class, gson);
        EqIpoResultDto eqIpoResultDto = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.eqIpoResult + sec.secId, EqIpoResultDto.class, gson);
        SharesStructureInfoDto sharesStructureInfoDto = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.sharesStructureInfo + sec.institutionId, SharesStructureInfoDto.class, gson);
        InstitutionInfoDto institutionInfoDto = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.institutioninfo + sec.institutionId, InstitutionInfoDto.class, gson);

        String s = Redis.get(RedisKey.CompanyInfo.institutioninfo + sec.institutionId);
        MarketQuotationDto ipoMaketQuotation = RedisUtil.fetchFromRedis(RedisKey.CompanyInfo.ipoMarketQuotation + sec.secId, MarketQuotationDto.class, gson);

        render(agencyOrg, eqIpoInfoDto, eqIpoResultDto, sharesStructureInfoDto, institutionInfoDto, sec, ipoMaketQuotation);
    }
}
