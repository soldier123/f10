package controllers;

import dto.BondSec;
import dto.financing.Financing;
import org.apache.commons.lang.StringUtils;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * 基本信息(f10) 拦截器, 提供一些公用的功能
 * before, after, finally, catch 的 priority 0 -- 29 做为系统保留使用
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:37
 */
public class BaseInfoIntercept extends Controller {

    @Before(priority = 1)
    static void verifyScode() {
        String scode = params.get("scode");
        BondSec sec = null;
        Boolean hasFinancing = false;
        if (StringUtils.isBlank(scode)) {
             params.put("scode", BondSec.DEFAULT_CODE);
            renderArgs.put("c", BondSec.DEFAULT_CODE);
            sec = BondSec.fetchBondSecByCode(BondSec.DEFAULT_CODE);
            if (sec.secId != 0L) {
                hasFinancing = Financing.financingSecSet.contains(sec.secId);
            }
            renderArgs.put("hasFinancing", hasFinancing);

        } else {
            sec = BondSec.fetchBondSecByCode(scode);
            if (sec == null) {
                black();
            } else {
                params.put("scode", scode);
                renderArgs.put("c", scode);
                hasFinancing = Financing.financingSecSet.contains(sec.secId);
                renderArgs.put("hasFinancing", hasFinancing);
            }
        }
        if(!hasFinancing && request.action.equalsIgnoreCase("FinancingCt.info") &&request.actionMethod.equalsIgnoreCase("info")){
            redirect("NewestInfoCt.info",scode);
        }
    }

    /**
     * 没有找到对应的信息~
     */
    public static void black(){
        render();
    }
}
