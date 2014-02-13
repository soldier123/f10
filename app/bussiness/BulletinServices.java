package bussiness;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dbutils.CustomDbUtil;
import dbutils.ExtractDbUtil;
import dbutils.SqlLoader;
import dto.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import util.CommonUtils;
import util.Page;
import util.UserComposeInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * description: 市场资讯--公司公告
 * User: 刘泓江
 * Date: 13-1-17
 * Time: 上午11:34
 */
public class BulletinServices {
    final static int COUNT_SIZE = 21;//一页显示的记录条数
   /**
    * 根据查询条件 筛选公司公告信息
    * @auther 刘泓江
    * @param keyWord 页面传过来的搜索关键字
    * @param code 左边导航栏公告对应的code
    * @param classifyList  public/js/bulletinOrg.js 转换为 List<BulletinClassify>
    * @param pageNo 由页面传入 用于滚动分页 记录当前页数,注意：当调用导出excel方法时传入的pageNo=-1 表示导出所有结果集
    * @return 查询结果集放入List<BulletinListDto>
    */
   public static List<BulletinListDto> getBulletinInfo(String keyWord, String code, List<BulletinClassify> classifyList, int pageNo, boolean isExport){
       List conditionList = new ArrayList();//查询条件放入list
       BulletinClassify bulletinClassify = new BulletinClassify();
       StringBuilder sql = new StringBuilder(SqlLoader.getSqlById("bulletinInfo"));//注意：查询结果集必须是24小时以内的信息
       StringBuilder leftJoinSql = new StringBuilder(SqlLoader.getSqlById("leftJoinBulletinInfo"));//注意：查询结果集必须是24小时以内的信息
       String queryAIdByClassifyIdSql = SqlLoader.getSqlById("queryIdByClassifyId");//根据classfyId查询announcementID
       if( StringUtils.isNotBlank(keyWord)){//输入关键字搜索
            sql = sql.append("and ( c.symbol like ? or a.title like ?) ");
            conditionList.add("%"+keyWord+"%");
            conditionList.add("%"+keyWord+"%");
       }else if(StringUtils.isNotBlank(code)){//点击左侧导航栏搜索
                for( BulletinClassify item :classifyList){
                    if(code.equals(item.code)){
                        bulletinClassify = item;
                        break;
                    }
                    if(item.children!=null){
                        for(BulletinClassify c_item :item.children){
                            if(code.equals(c_item.code)){
                                bulletinClassify = c_item;
                                break;
                            }
                        }
                    }
                }
                String typeGroup = BulletinClassify.formatStr(bulletinClassify.classfyId);//将数组变成指定格式
                if (!"".equals(typeGroup)){
                    if(!typeGroup.contains("%")){
                        //根据classfyId查询announcementID
                        List<BulletinClasssifyDto> list =  CustomDbUtil.queryCustomDBBeanList( queryAIdByClassifyIdSql.replace("#TypeGroup",typeGroup), BulletinClasssifyDto.class);
                        String announcementIDArr = BulletinClassify.formatDtoStr(list);//将数组变成指定格式
                        if(StringUtils.isNotBlank(announcementIDArr)){
                            sql = sql.append("AND a.announcementID IN(" +announcementIDArr + ")");
                        }else{
                            sql = sql.append("AND 1=-1 ");
                        }
                    }else{
                        if("'%'".equals(typeGroup)){//查询全部公告
                            return result(pageNo, leftJoinSql, conditionList, isExport);
                        }else{
                            sql = sql.append("AND a.title like(" + typeGroup + ")");
                        }
                    }
                }
           }else{
               return   result(pageNo, leftJoinSql, conditionList, isExport);
           }
     return   result(pageNo, sql, conditionList, isExport);
   }
    /**
     * 根据查询条件 筛选公司公告信息
     * @auther 刘泓江
     * @param keyWord 页面传过来的搜索关键字
     * @param code 左边导航栏公告对应的code
     * @param classifyList  public/js/bulletinOrg.js 转换为 List<BulletinClassify>
     * @param pageNo 由页面传入 用于滚动分页 记录当前页数,注意：当调用导出excel方法时传入的pageNo=-1 表示导出所有结果集
     * @return 查询结果集放入List<BulletinListDto>
     */

    public static List<BulletinListDto> getBulletinInfoFromES(String keyWord, String code, List<BulletinClassify> classifyList, int pageNo, boolean isExport){
        List conditionList = new ArrayList();//查询条件放入list
        BulletinClassify bulletinClassify = new BulletinClassify();
        StringBuilder sql = new StringBuilder(SqlLoader.getSqlById("bulletinInfo"));//注意：查询结果集必须是24小时以内的信息
        StringBuilder leftJoinSql = new StringBuilder(SqlLoader.getSqlById("leftJoinBulletinInfo"));//注意：查询结果集必须是24小时以内的信息
        String queryAIdByClassifyIdSql = SqlLoader.getSqlById("queryIdByClassifyId");//根据classfyId查询announcementID
        AdvanceSearchDto advanceSearchDto = new AdvanceSearchDto();
        advanceSearchDto.itype.add("2");//2公告
        String df = "yyyyMMddHHmmss";
        Date maxDate = MessageIndexService.getMaxDateFromES("2");
        advanceSearchDto.endTime = DateFormatUtils.format(maxDate,df);
        advanceSearchDto.startTime = DateFormatUtils.format(DateUtils.addDays(maxDate,-1),df);//取最近24小时
        if( StringUtils.isNotBlank(keyWord)){//输入关键字搜索
            advanceSearchDto.keyword = keyWord;
        }else if(StringUtils.isNotBlank(code)){//点击左侧导航栏搜索
            for( BulletinClassify item :classifyList){
                if(code.equals(item.code)){
                    bulletinClassify = item;
                    break;
                }
                if(item.children!=null){
                    for(BulletinClassify c_item :item.children){
                        if(code.equals(c_item.code)){
                            bulletinClassify = c_item;
                            break;
                        }
                    }
                }
            }
        //    String typeGroup = BulletinClassify.formatStr(bulletinClassify.classfyId);//将数组变成指定格式
            //点击导航里的全部时做一下特舒处理
            if(bulletinClassify.classfyId!=null && bulletinClassify.classfyId.length>0 ){
                //当用点击全部的时候 分类传过来是%
                if("%".equals(bulletinClassify.classfyId[0].trim())){
                    advanceSearchDto.acids = Lists.newArrayList();
                }else{
                    advanceSearchDto.acids = Lists.newArrayList(bulletinClassify.classfyId);
                }
            }

        }
        if(isExport){//是导出的时候导出1000条最多
            advanceSearchDto.pageNo=1;
            advanceSearchDto.pageSize=1000;
        }else {
            advanceSearchDto.pageNo=pageNo>0?pageNo:1;
            advanceSearchDto.pageSize = COUNT_SIZE;
        }
        Gson gson = CommonUtils.createIncludeNulls();
        String asdJson = advanceSearchDto.toJson();
        Logger.info("公告,send data to es= %s", asdJson);
        JsonElement result = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
        JsonObject jsonObject = result.getAsJsonObject();
        JsonElement messageData = jsonObject.get("data");
        Type type = new TypeToken<List<BulletinListDto>>() {
        }.getType();
        List<BulletinListDto> bulletinListDtoList = gson.fromJson(messageData, type);
        return   bulletinListDtoList;
    }

    //根据不同的SQL获得不同结果集
    public static List result(int pageNo,StringBuilder sql,List conditionList, boolean isExport){
        if(conditionList == null){
            conditionList = new ArrayList();
        }
        List<BulletinListDto> resultList;
        StringBuilder coutSql = new StringBuilder("select count(*) from (\n" + sql + "\n) distTable  \n");
        Long total = CustomDbUtil.queryCount(coutSql.toString(),conditionList.toArray());
        Page page = new Page(total.intValue(), pageNo,COUNT_SIZE);
        if (isExport) {
                if(total>1000){
                    total = 1000L;
                }
                sql  = sql.append("order by DECLAREDATE desc, symbol DESC  limit 0,"+total+" \n");
            } else {
                if ((pageNo-1) * page.pageSize >= total) {
                    return new ArrayList<BulletinListDto>();
                }
                sql  = sql.append("order by DECLAREDATE desc, symbol DESC  limit\n"+page.beginIndex+","+COUNT_SIZE+"\n");
            }
        resultList = CustomDbUtil.queryCustomDBBeanList(sql.toString(), BulletinListDto.class, conditionList.toArray());
        return resultList;
    }

    /**
     * 根据订阅ID获取公告列表
     *
     * @param orderId 订阅ID
     * @param uci
     * @return
     */
    public static List<BulletinListDto> getBulletinByOrderId(long orderId, List<BulletinClassify> classifyList, int pageNo, UserComposeInfo uci, boolean isExport) {
        ArrayList<String> announcementIDList = new ArrayList<String>();
        ArrayList<String> titleList = new ArrayList<String>();
        StringBuilder sql = new StringBuilder(SqlLoader.getSqlById("bulletinInfo"));
        String queryAIdByClassifyIdSql = SqlLoader.getSqlById("queryIdByClassifyId");
        String json = TemplateService.fetchUserTemplateContentById(orderId, uci);
        if (StringUtils.isNotBlank(json)) {
            BulletinOrderDto bulletinOrderDto = new Gson().fromJson(json, BulletinOrderDto.class);
            //根据分类ID查询公告
            String[] bulletinClassifyarrs = bulletinOrderDto.bulletinClassify;//获取订阅的的bulletinClassify 下的code数组
            if (bulletinClassifyarrs != null && bulletinClassifyarrs.length != 0) {
                List<String> temList = new ArrayList<String>();//定义一个临时数组
                //把所有的订阅classfyId压入到一个临时的数组temList中
                for (String code : bulletinClassifyarrs) {
                    for (BulletinClassify item : classifyList) {
                        if (code.equals(item.code)) {
                            String[] temArr = item.classfyId;
                            if (temArr != null && temArr.length > 0) {
                                for (String s : temArr) {
                                    temList.add(s);
                                }
                            }
                            break;
                        }
                        //如果有子类则继续遍历压入
                        if (item.children != null) {
                            for (BulletinClassify c_item : item.children) {
                                if (code.equals(c_item.code)) {
                                    String[] temArrC = c_item.classfyId;
                                    if (temArrC != null && temArrC.length > 0) {
                                        for (String sC : temArrC) {
                                            if(!temList.contains(sC)){
                                                temList.add(sC);
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                String typeGroup = BulletinClassify.formatStr(temList);
                if (!"".equals(typeGroup)) {
                    String AND = "AND"; //sql拼接"AND/OR"
                    String dash = "";  //sql拼接 "()"
                    for(String type :temList){       //把IN查询条件和LIKE查询条件分别放在不同的list中
                        if(!type.contains("%")){
                            announcementIDList.add(type);
                            continue;
                        }
                        titleList.add(type);
                    }
                    if(titleList.size()>0) {
                        dash = "(" ;
                    }
                    if(!titleList.contains("%")){   //IN查询
                        if(announcementIDList.size()>0){
                            typeGroup = BulletinClassify.formatStr(announcementIDList);
                            List<BulletinClasssifyDto> list = CustomDbUtil.queryCustomDBBeanList(queryAIdByClassifyIdSql.replace("#TypeGroup", typeGroup), BulletinClasssifyDto.class);
                            String announcementIDArr = BulletinClassify.formatDtoStr(list);
                            announcementIDArr = announcementIDArr.equals("")?null:announcementIDArr;
                            sql = sql.append("AND "+dash+" a.announcementID IN(" + announcementIDArr + ")");
                            AND = "OR";
                            dash = "";

                        }
                        if(titleList.size()>0){  //LIKE查询
                            sql = sql.append(" "+AND+dash+" a.title like('" + titleList.get(0) + "')");
                            if(titleList.size()>1){
                                for(int i =1;i<titleList.size();i++){
                                    sql = sql.append(" OR a.title like('" + titleList.get(i) + "')");
                                }
                            }
                            sql.append(")");
                        }

                    }
                }
            }

            //根据证券代码查询公告
            String[] symbolArr = bulletinOrderDto.symbolArr;//获取订阅的的symbolArr 下的symbol数组
            String[] plateTree = bulletinOrderDto.plateTree;
            if (plateTree != null && plateTree.length > 0 ) {
                List<String> symbolList = ReportService.findSecurityByPlateId(plateTree);
                for (int i = 0; i < symbolArr.length; i++) {
                    symbolList.add(symbolArr[i]);
                }
                symbolArr = symbolList.toArray(symbolArr);
            }

            if (symbolArr != null && symbolArr.length != 0 ) {
                String symbolGroup = BulletinClassify.formatStr(symbolArr);
                if (StringUtils.isNotBlank(symbolGroup)) {
                    sql = sql.append("AND c.SYMBOL IN(" + symbolGroup + ")");
                }
            }
            //因为plateTree  symbolArr的初始值是空值,判断是否勾选了板块数节点只能通过看第0个元素是否为空值
            if(!plateTree[0].equals("") && symbolArr[0].equals("") ){
                Logger.info("[公告] 有选择了板块树, 但是从板块树上找不到相应的股票代码, 这种情况的话, 就不相应查询出信息来");
                return null;
            }
            return result(pageNo,sql,null,isExport);
        }
        return null;
    }

    /**
     * 根据订阅ID获取公告列表
     *
     * @param orderId 订阅ID
     * @param uci
     * @return
     */
    public static List<BulletinListDto> getBulletinByOrderIdFromES(long orderId, List<BulletinClassify> classifyList, int pageNo, UserComposeInfo uci, boolean isExport) {
        ArrayList<String> announcementIDList = new ArrayList<String>();
        ArrayList<String> titleList = new ArrayList<String>();
        StringBuilder sql = new StringBuilder(SqlLoader.getSqlById("bulletinInfo"));
        String queryAIdByClassifyIdSql = SqlLoader.getSqlById("queryIdByClassifyId");
        String json = TemplateService.fetchUserTemplateContentById(orderId, uci);
        if (StringUtils.isNotBlank(json)) {
            BulletinOrderDto bulletinOrderDto = new Gson().fromJson(json, BulletinOrderDto.class);
            //根据分类ID查询公告
            String[] bulletinClassifyarrs = bulletinOrderDto.bulletinClassify;//获取订阅的的bulletinClassify 下的code数组
            if (bulletinClassifyarrs != null && bulletinClassifyarrs.length != 0) {
                List<String> temList = new ArrayList<String>();//定义一个临时数组
                //把所有的订阅classfyId压入到一个临时的数组temList中
                for (String code : bulletinClassifyarrs) {
                    for (BulletinClassify item : classifyList) {
                        if (code.equals(item.code)) {
                            String[] temArr = item.classfyId;
                            if (temArr != null && temArr.length > 0) {
                                for (String s : temArr) {
                                    temList.add(s);
                                }
                            }
                            break;
                        }
                        //如果有子类则继续遍历压入
                        if (item.children != null) {
                            for (BulletinClassify c_item : item.children) {
                                if (code.equals(c_item.code)) {
                                    String[] temArrC = c_item.classfyId;
                                    if (temArrC != null && temArrC.length > 0) {
                                        for (String sC : temArrC) {
                                            if(!temList.contains(sC)){
                                                temList.add(sC);
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                String typeGroup = BulletinClassify.formatStr(temList);
                if (temList.size()>0) {
                    for(String type :temList){       //把IN查询条件和LIKE查询条件分别放在不同的list中
                        if(!type.contains("%")){
                            announcementIDList.add(type);
                            continue;
                        }
                        titleList.add(type);//关键字的 暂时不用资讯索引里也没用  与资讯索引保持一致,但先留着备用吧
                    }
                }

            }
            //根据证券代码查询公告
            String[] symbolArr = bulletinOrderDto.symbolArr;//获取订阅的的symbolArr 下的symbol数组
            String[] plateTree = bulletinOrderDto.plateTree;
            AdvanceSearchDto advanceSearchDto = new AdvanceSearchDto();
            if(bulletinOrderDto !=null &&  bulletinOrderDto.plateTree!=null){
                advanceSearchDto.aiids= Lists.newArrayList(bulletinOrderDto.plateTree);
            }
            if(bulletinOrderDto !=null &&  bulletinOrderDto.symbolArr!=null){
                advanceSearchDto.symbols= Lists.newArrayList(bulletinOrderDto.symbolArr);
            }
            advanceSearchDto.acids = announcementIDList;
            advanceSearchDto.itype.add("2");
            String df = "yyyyMMddHHmmss";
            Date maxDate = MessageIndexService.getMaxDateFromES("2");
            advanceSearchDto.endTime = DateFormatUtils.format(maxDate,df);
            advanceSearchDto.startTime = DateFormatUtils.format(DateUtils.addDays(maxDate,-1),df);//取最近24小时
            if(isExport){
                advanceSearchDto.pageNo=1;
                advanceSearchDto.pageSize=1000;
            }else {
                advanceSearchDto.pageNo=pageNo>0?pageNo:1;
                advanceSearchDto.pageSize = COUNT_SIZE;
            }

            Gson gson = CommonUtils.createIncludeNulls();
            String asdJson = advanceSearchDto.toJson();
            Logger.info("公告,send data to es= %s", asdJson);
            JsonElement result = WS.url(MessageIndexService.requestUrl).body(asdJson).post().getJson();
            JsonObject jsonObject = result.getAsJsonObject();
            JsonElement messageData = jsonObject.get("data");
            Type type = new TypeToken<List<BulletinListDto>>() {
            }.getType();
            List<BulletinListDto> bulletinListDtoList = gson.fromJson(messageData, type);
            return bulletinListDtoList;
            //暂时先注释掉吧
           /* if (plateTree != null && plateTree.length > 0 ) {
                List<String> symbolList = ReportService.findSecurityByPlateId(plateTree);
                for (int i = 0; i < symbolArr.length; i++) {
                    symbolList.add(symbolArr[i]);
                }
                symbolArr = symbolList.toArray(symbolArr);
            }

            if (symbolArr != null && symbolArr.length != 0 ) {
                String symbolGroup = BulletinClassify.formatStr(symbolArr);
                if (StringUtils.isNotBlank(symbolGroup)) {
                    sql = sql.append("AND c.SYMBOL IN(" + symbolGroup + ")");
                }
            }
            //因为plateTree  symbolArr的初始值是空值,判断是否勾选了板块数节点只能通过看第0个元素是否为空值
            if(!plateTree[0].equals("") && symbolArr[0].equals("") ){
                Logger.info("[公告] 有选择了板块树, 但是从板块树上找不到相应的股票代码, 这种情况的话, 就不相应查询出信息来");
                return null;
            }
            return result(pageNo,sql,null,isExport);*/
        }
        return null;
    }

    /**
     * 保存研报订阅
     * @param dto
     * @param uci 用户信息包装
     * @param name
     */
    public static F.T2<Long,String>  addBulletinOrder(BulletinOrderDto dto, UserComposeInfo uci, String name){
        String utContent = CommonUtils.createGson().toJson(dto);
        String sql = SqlLoader.getSqlById("insertBulletin");
        long autoIncreaseId = CustomDbUtil.insert(sql, uci.uid, uci.utype, name, 3, utContent);
        return F.T2(autoIncreaseId, utContent);
    }

    /**
     * 编辑研报订阅信息
     * @param dto
     * @param name
     * @param id
     */
    public static String editReportDto(BulletinOrderDto dto, String name, Long id, UserComposeInfo uci){
        String utContent = CommonUtils.createGson().toJson(dto);
        String sql = SqlLoader.getSqlById("editBulletin");
        CustomDbUtil.updateCustomDB(sql, name, utContent, id, uci.uid, uci.utype);
        return utContent;
    }

    /**
     * 根据证券代码或简称自动查找股票
     * @param keys
     * @return
     */
    public static List<Map<String,Object>> findStockAutoComplete(String keys){
        String sql = SqlLoader.getSqlById("findStockByName_startsWith");
        List<Map<String,Object>> mapList = ExtractDbUtil.queryExtractDBMapList(sql, "%" + keys + "%", "%" + keys + "%");
        return mapList;
    }
}
