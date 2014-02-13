package dto;

import play.data.binding.As;
import util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 资讯索引搜索条件
 * User: panzhiwei
 * Date: 13-4-1
 * Time: 下午2:41
 * To change this template use File | Settings | File Templates.
 */
public class MessageIndexCnd {
    @As(",")
    public List<String> newsSource = new ArrayList<String>();                         //新闻来源
    @As(",")
    public List<String> newsClassify = new ArrayList<String>();                      //新闻分类
    @As(",")
    public List<String> bulletinClassify = new ArrayList<String>();               //公告类型
    @As(",")
    public List<String> bulletinPlateTree = new ArrayList<String>();              //公告板块树
    @As(",")
    public List<String> reportPlateTree = new ArrayList<String>();                //研报板块树
    @As(",")
    public List<String> reportOrg = new ArrayList<String>();                      //研报来源
    @As(",")
    public List<String> reportClassify = new ArrayList<String>();                 //研报分类
    @As(",")
    public List<String> advanceType = new ArrayList<String>();

    public String toJson(){
        if (newsSource.size() == 1 && "".equals(newsSource.get(0))) {
            newsSource = new ArrayList<String>();
        }
        if (newsClassify.size() == 1 && "".equals(newsClassify.get(0))) {
            newsClassify = new ArrayList<String>();
        }
        if (bulletinClassify.size() == 1 && "".equals(bulletinClassify.get(0))) {
            bulletinClassify = new ArrayList<String>();
        }
        if (bulletinPlateTree.size() == 1 && "".equals(bulletinPlateTree.get(0))) {
            bulletinPlateTree = new ArrayList<String>();
        }
        if (reportPlateTree.size() == 1 && "".equals(reportPlateTree.get(0))) {
            reportPlateTree = new ArrayList<String>();
        }
        if (reportOrg.size() == 1 && "".equals(reportOrg.get(0))) {
            reportOrg = new ArrayList<String>();
        }
        if (reportClassify.size() == 1 && "".equals(reportClassify.get(0))) {
            reportClassify = new ArrayList<String>();
        }
        if (advanceType.size() == 1 && "".equals(advanceType.get(0))) {
            advanceType = new ArrayList<String>();
        }
        return CommonUtils.createGson().toJson(this);
    }



}