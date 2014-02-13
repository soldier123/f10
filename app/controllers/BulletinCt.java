package controllers;

import bussiness.BulletinServices;
import bussiness.ReportService;
import bussiness.TemplateService;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.*;
import play.Play;
import play.data.binding.As;
import play.modules.excel.RenderExcel;
import play.mvc.Http;
import play.mvc.Util;
import play.mvc.With;
import play.vfs.VirtualFile;
import util.CommonUtils;
import util.UserComposeInfo;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告
 * User: wenzhihong
 * Date: 13-1-17
 * Time: 上午10:25
 */
@With({AuthorBaseIntercept.class})
public class BulletinCt extends BaseController {


    //股票池分类列表
    static List<BulletinClassify> bulletinClassifies;
    protected static final String EXCEL_FILE_SUFFIX ="xls";

    static {
        VirtualFile vf = Play.getVirtualFile("public/js/treeResource/bulletinOrg.js");
        String json = CommonUtils.readJsonConfigFile2String(vf.inputstream());
        bulletinClassifies = new Gson().fromJson(json, new TypeToken<List<BulletinClassify>>() {
        }.getType());
    }

    //公告列表
    public static void bulletinInfo(String searchInfo, String code, int pageNo){
        UserComposeInfo uci = fetchUserComposeInfo();
        List navigateList  = bulletinClassifies;
        List<BulletinListDto> list =  findAllAnnSecurity(searchInfo,code,pageNo,false);

        //我的订阅
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.bulletin);
        render(list,navigateList,searchInfo,code,pageNo,utList);
    }

    /**
     * 根据订阅ID获取公告列表详情
     * @param orderId 公告列表Id
     * @param pageNo
     */
    public static void orderBulletinInfo(long orderId, int pageNo){
        UserComposeInfo uci = fetchUserComposeInfo();
        List navigateList  = bulletinClassifies;
        List<BulletinListDto> list =  BulletinServices.getBulletinByOrderIdFromES(orderId, navigateList, pageNo, uci, false);

        //我的订阅
        List<UserTemplate> utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.bulletin);
        render("@bulletinInfo", list,navigateList,pageNo,utList,orderId);
    }

    /**
     * 我的订阅滚动分页
     * @param orderId 订阅ID
     * @param pageNo
     */
    public static void orderMoreInfo(long orderId, int pageNo){
        UserComposeInfo uci = fetchUserComposeInfo();
        List navigateList  = bulletinClassifies;
        List<BulletinListDto> list =  BulletinServices.getBulletinByOrderIdFromES(orderId,navigateList,pageNo, uci, false);

        //我的订阅
        render("@moreBulletinInfo", list,navigateList,pageNo,orderId);
    }

    //滚动分页
    public static void moreBulletinInfo(String searchInfo,String code, int pageNo){
        List<BulletinListDto> list =  findAllAnnSecurity(searchInfo,code,pageNo,false);
        render(list,searchInfo,code,pageNo);
    }

    @Util
    private static List<BulletinListDto> findAllAnnSecurity(String searchInfo, String code, int pageNo, boolean isExport){
        return BulletinServices.getBulletinInfoFromES(searchInfo, code, bulletinClassifies, pageNo, isExport);
    }

    //excel导出
    public static void exportAnnSecurity(String searchInfo,String code,long orderId){
        Http.Response.current().accessControl("*", "GET,PUT,POST,DELETE", true);
        int pageNo =-1;//表示导出所有内容
        List<BulletinListDto> list;
        UserComposeInfo uci = fetchUserComposeInfo();
        List navigateList  = bulletinClassifies;
        if(orderId!=0){
            list = BulletinServices.getBulletinByOrderIdFromES(orderId,navigateList,pageNo, uci, true);
        }else{
            list = findAllAnnSecurity(searchInfo,code,pageNo,true);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        renderArgs.put("dateFormat",dateFormat);
        renderArgs.put(RenderExcel.RA_FILENAME,"公司公告.xls");
        request.format =  EXCEL_FILE_SUFFIX;
        render(list);
    }

    /**
     * 根据订阅名称查找订阅(唯一性)
     */
    public static void findBulletinByName(String name) {
        Map<String,Object> json = Maps.newHashMap();
        UserComposeInfo uci = fetchUserComposeInfo();
        if (! TemplateService.hasSameName(name, uci, TemplateService.TemplateType.bulletin)) {
            json.put("success",true);
            renderJSON(json);
        } else {
            json.put("success",false);
            renderJSON(json);
        }

    }

    /**
     * 保存公告订阅
     * @param dto
     * @param name
     */
    public static void addBulletinOrder(BulletinOrderDto dto, String name,Long cid){
        UserComposeInfo uci = fetchUserComposeInfo();

        Map<String,Object> json = new HashMap<String, Object>();
        if(cid==-999){//新增
            if(dto == null){
                json.put("success",false);
                json.put("msg","订阅条件不存在,请重新输入");
            }
            else {
                String content = CommonUtils.createGson().toJson(new BulletinOrderDto());
                if (TemplateService.hasSameName(name, uci, TemplateService.TemplateType.bulletin)) {
                    json.put("success", false);
                    json.put("msg", "您输入的名称已存在,请重新输入!");
                }else{
                    long id = TemplateService.addUserTemplate(uci, name, content, TemplateService.TemplateType.bulletin);
                    if(id > 0){
                        json.put("success",true);
                        json.put("msg","保存成功!");
                        json.put("id", id);
                        json.put("info",content);
                    }
                    else {
                        json.put("success",false);
                        json.put("msg","名称已存在!");
                    }
                }
            }
            renderJSON(json);
        }else {//修改
            editBulletinOrder(dto,name,cid,1);
        }
    }


    /**
     * 编辑公告信息
     * @param dto
     * @param name
     * @param id
     */
    public static void editBulletinOrder(BulletinOrderDto dto, String name,Long id,int iFlag){
        Map<String,Object> json = new HashMap<String, Object>();
        UserComposeInfo uci = fetchUserComposeInfo();
        if(dto == null){
            json.put("success",false);
            json.put("msg","订阅条件不存在,请重新输入");
        }
        else {
            if(iFlag==1){
                if (TemplateService.hasSameName(name, uci, TemplateService.TemplateType.bulletin)) {
                    json.put("success", false);
                    json.put("msg", "您输入的名称已存在,请重新输入!");
                }else{
                    String content = CommonUtils.createGson().toJson(dto);
                    TemplateService.editUserTemplateById(id, name, content, uci);
                    json.put("success", true);
                    json.put("msg", "保存成功");
                    json.put("info", content);
                    json.put("id", id);
                }
            }else {
                String content = CommonUtils.createGson().toJson(dto);
                TemplateService.editUserTemplateById(id, name, content, uci);
                json.put("success", true);
                json.put("msg", "保存成功");
                json.put("info", content);
                json.put("id", id);
            }
        }
        renderJSON(json);
    }

    /**
     * 删除订阅公告
     * @param id  订阅id
     */
    public static void delBulletinOrder(Long id){
        UserComposeInfo uci = fetchUserComposeInfo();
        TemplateService.deleteUserTemplateById(id, uci);
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("success",true);
        json.put("msg","删除成功");
        renderJSON(json);
    }


    /**
     * 根据证券代码或简称自动查找股票
     * @param keys
     * @return
     */
    public static void findStockAutoComplete(String keys){
        List<Map<String,Object>> mapList = ReportService.findStockAutoComplete(keys);
        renderJSON(mapList);
    }

    /**
     * 根据板块id得到板块名称
     * @param reportArr
     */
    public static void getPlateTreeName(@As(",")String[] reportArr){
        List<PlateTreeDto> plateTreeDtoList = ReportService.getPlateTreeName(reportArr);
        renderJSON(plateTreeDtoList);
    }

}
