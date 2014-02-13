package controllers;

import bussiness.ReportService;
import bussiness.TemplateService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.*;
import play.Play;
import play.data.binding.As;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.mvc.Util;
import play.mvc.With;
import play.vfs.VirtualFile;
import util.CommonUtils;
import util.Page;
import util.UserComposeInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:研究报告模块
 * User: wenzhihong
 * Date: 12-9-12
 * Time: 上午11:48
 */
@With({AuthorBaseIntercept.class})
public class ReportCt extends BaseController {
    protected static final String EXCEL_FILE_SUFFIX = "xls";
    static List<ReportClassify> reportCategories;

    static {
        VirtualFile vf = Play.getVirtualFile("conf/reportClassify.js");
        String json = CommonUtils.readJsonConfigFile2String(vf.inputstream());
        reportCategories = new Gson().fromJson(json, new TypeToken<List<ReportClassify>>() {
        }.getType());
    }

    /**
     * 研报列表
     *
     * @param sp     查询参数
     * @param pageNo
     */
    public static void reportInfo(ReportParameter sp, int pageNo) {
        UserComposeInfo uci = fetchUserComposeInfo();
        if (sp == null) {
            sp = new ReportParameter();
        }
        if (pageNo < 1) {
            pageNo = 1;
        }

        //F.T4<List<ReportDto>, Page, ReportParameter, Long> t2 = ReportService.reportList(sp, pageNo, false);
        F.T4<List<ReportDto>, Page, ReportParameter, Long> t2 = ReportService.reportListFromES(sp, pageNo, false);
        List<ReportDto> reportList = t2._1;
        Page page = t2._2;
        sp = t2._3;        //研报查询参数
        long total = t2._4;
        //研报分类列表
        List<ReportClassify> reportClassifyList = reportCategories;

        //我的订阅
        List<UserTemplate> utList = null;
        if (uci == null) {
            utList = Lists.newArrayList();
        } else {
            utList = TemplateService.fetchUserTemplateList(uci, TemplateService.TemplateType.report);
        }

        //板块树
        //List<PlateTreeDto> plateTreeDtoList = ReportService.getPlateTRree();
        //String treeData = new Gson().toJson(plateTreeDtoList).replaceAll("\"", "'");
        int pageSize = page.pageSize;
        render(reportList, reportClassifyList, page, sp, utList, pageNo, total, pageSize);
    }

    //滚动分页用到
    public static void moreReportInfo(ReportParameter sp, int pageNo) {
        if (sp == null) {
            sp = new ReportParameter();
        }
        //F.T4<List<ReportDto>, Page, ReportParameter, Long> t2 = ReportService.reportList(sp, pageNo, false);
        F.T4<List<ReportDto>, Page, ReportParameter, Long> t2 = ReportService.reportListFromES(sp, pageNo, false);
        List<ReportDto> reportList = t2._1;
        Long total = t2._4;
        int pageSize = t2._2.pageSize;
        render(reportList, sp, pageNo, total, pageSize);
    }

    /**
     * 根据研报Id显示详细内容
     *
     * @param reportId
     */
    public static void detail(long reportId) {
        ReportDetailDto reportDetailDto = ReportService.getDetail(reportId);
        if(reportDetailDto==null){
            reportDetailDto = new ReportDetailDto();
            reportDetailDto.summary="无相关摘要";
        }
        renderJSON(reportDetailDto);
    }

    //导出
    public static void exportReport(ReportParameter sp, int pageNo) {
        if (sp == null) {
            sp = new ReportParameter();
        }
        if (pageNo < 1) {
            pageNo = 1;
        }
        //F.T4<List<ReportDto>, Page, ReportParameter, Long> t4 = ReportService.reportList(sp, pageNo, true);
        F.T4<List<ReportDto>, Page, ReportParameter, Long> t4 = ReportService.reportListFromES(sp, pageNo, true);
        List<ReportDto> reportList = t4._1;
        if (reportList == null) {
            reportList = Lists.newArrayList();
        }
        int no = 0; //序号
        for (ReportDto reportDto : reportList) {
           no++;
           reportDto.rowNum = no;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        renderArgs.put("dateFormat", dateFormat);

        renderArgs.put(RenderExcel.RA_FILENAME, "研报列表.xls");
        request.format = EXCEL_FILE_SUFFIX;
        List<ReportDto> list = reportList; //这句不能删除, 因为模板里用到了 list变量.
        render(list);
    }

    /**
     * 删除订阅研报
     *
     * @param id 订阅id
     */
    public static void delCndReport(Long id) {
        UserComposeInfo uci = fetchUserComposeInfo();
        TemplateService.deleteUserTemplateById(id, uci);
        Map<String, Object> json = Maps.newHashMap();
        json.put("success", true);
        json.put("msg", "删除成功");
        renderJSON(json);
    }

    /**
     * 判断名称是否重复
     *
     * @param name
     */
    public static void findReportByName(String name) {
        Map<String, Object> json = Maps.newHashMap();
        UserComposeInfo uci = fetchUserComposeInfo();
        if (!TemplateService.hasSameName(name, uci, TemplateService.TemplateType.report)) {
            json.put("success", true);
            renderJSON(json);
        } else {
            json.put("success", false);
            renderJSON(json);
        }
    }

    /**
     * 保存研报订阅
     *
     * @param cnd
     * @param name
     */
    public static void addReportCnd(ReportOrderCnd cnd, String name) {
        UserComposeInfo uci = fetchUserComposeInfo();
        Map<String, Object> json = new HashMap<String, Object>();
        if (cnd == null) {
            json.put("success", false);
            json.put("msg", "订阅条件不存在,请重新输入");
        } else {
            String content = CommonUtils.createGson().toJson(cnd);
            long id = TemplateService.addUserTemplate(uci, name, content, TemplateService.TemplateType.report);
            if (id > 0) {
                json.put("success", true);
                json.put("msg", "保存成功!");
                json.put("id", id);
                json.put("info", content);
            } else {
                json.put("success", false);
                json.put("mag", "保存失败!");
            }
        }

        renderJSON(json);
    }

    /**
     * 编辑研报订阅信息
     *
     * @param cnd
     * @param name
     * @param id
     */
    public static void editReportCnd(ReportOrderCnd cnd, String name, Long id) {
        Map<String, Object> json = new HashMap<String, Object>();
        if (cnd == null) {
            json.put("success", false);
            json.put("msg", "订阅条件不存在,请重新输入");
        } else {
            String content = CommonUtils.createGson().toJson(cnd);
            UserComposeInfo uci = fetchUserComposeInfo();
            TemplateService.editUserTemplateById(id, name, content, uci);
            json.put("success", true);
            json.put("msg", "保存成功");
            json.put("info", content);
        }
        renderJSON(json);
    }

    //根据板块树id查找子节点
    public static void getPlateTreeByPlateTreeId(String id, boolean isChecked) {
        List<PlateTreeDto> plateTreeDtoList = ReportService.getPlateTreeByPlateTreeId(id);
        Map<String, Object> json = Maps.newHashMap();
        String plateTreeStr = "";
        if (plateTreeDtoList != null && plateTreeDtoList.size() > 0) {
            plateTreeStr = "[";
            for (PlateTreeDto plateTreeDto : plateTreeDtoList) {
                List<PlateTreeDto> list = ReportService.getPlateTreeByPlateTreeId(plateTreeDto.id.toString());
                if (list != null && list.size() > 0) {
                    plateTreeStr += "{text:'" + plateTreeDto.name + "','plateId':'" + plateTreeDto.id + "',children:[], isexpand:false},";
                } else {
                    plateTreeStr += "{text:'" + plateTreeDto.name + "','plateId':'" + plateTreeDto.id + "', isexpand:false},";
                }

            }
            plateTreeStr = plateTreeStr.substring(0, plateTreeStr.length() - 1);
            plateTreeStr += "]";
            json.put("treeData", plateTreeStr);
        }

        renderJSON(json);
    }

    /**
     * 根据证券代码或简称自动查找股票
     *
     * @param name_startsWith
     * @return
     */
    public static void findStockAutoComplete(String name_startsWith) {
        List<Map<String, Object>> mapList = ReportService.findStockAutoComplete(name_startsWith);
        renderJSON(mapList);
    }

    /**
     * 根据股票代码查股票
     *
     * @param symbolArr
     */
    public static void findStockBySymbol(@As(",") String[] symbolArr) {
        List<Map<String, Object>> mapList = ReportService.findStockBySymbol(symbolArr);
        renderJSON(mapList);
    }

    /**
     * 根据板块id得到板块名称
     *
     * @param reportPlateTreeArr
     */
    public static void getPlateTreeName(@As(",") String[] reportPlateTreeArr) {
        List<PlateTreeDto> plateTreeDtoList = ReportService.getPlateTreeName(reportPlateTreeArr);
        renderJSON(plateTreeDtoList);
    }

    /**
     * 搜索条件重命名或新建
     */
    public static void renameOrNewCond(Long id, String name) {
        UserComposeInfo uci = fetchUserComposeInfo();
        if (id == -999) { //这是新加一个
            Map<String, Object> json = null;

                json = addCond(new ReportOrderCnd(), name, uci);

            if (json == null) {
                json = new HashMap<String, Object>();
            }
            renderJSON(json);
        } else {
            UserTemplate ut = TemplateService.findUserTemplateById(id);
            Map<String, Object> json = new HashMap<String, Object>();
            if (ut == null) {
                json.put("op", false);
                json.put("msg", "自定义查询条件不存在");
            } else {
                TemplateService.editUserTemplateById(id, name, new Gson().toJson(new ReportOrderCnd()),uci);
                json.put("op", true);
                json.put("msg", "改名成功");
            }
            renderJSON(json);
        }

    }

    @Util
    static Map<String, Object> addCond(Object cnd, String cndName, UserComposeInfo uci) {
        Map<String, Object> json = new HashMap<String, Object>();

        //先检查一下是否重名
        if (TemplateService.hasSameName(cndName, uci, TemplateService.TemplateType.report)) {
            json.put("op", false);
            json.put("msg", "名称已存在");
            json.put("cndName", cndName);
            json.put("sameName", true);
        } else {
            Long id = TemplateService.addUserTemplate(uci, cndName, new Gson().toJson(cnd), TemplateService.TemplateType.report);
            json.put("op", true);
            json.put("msg", "增加成功");
            json.put("id", id);
            json.put("utInfo", cnd);
        }
        return json;
    }
}
