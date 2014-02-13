package dto;

import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: liuhongjiang
 * Date: 13-1-17
 * Time: 下午6:25
 */
public class BulletinClassify {
    public String text;
    public String code;
    public String pcode;
    public String[] classfyId;

    public List<BulletinClassify> children;

    @Override
    public String toString() {
        return "StockPoolClassify{" +
                "name='" + text + '\'' +
                ", code='" + code + '\'' +
                ", pcode='" + pcode + '\'' +
                ", classfyId='" + classfyId + '\'' +
                ", children=" + children +
                '}';
    }

    public static String formatStr(String[] arr){
        StringBuilder sb = new StringBuilder();
        if(arr!=null&& arr.length!=0){
            for (String s :arr ) {
                if(StringUtils.isNotBlank(s))//剔除为空的值
                sb.append(",'" + s + "'");
            }
            return   StringUtils.isNotBlank(sb.toString()) ? sb.substring(1) : "";
        }
        return "";
    }

    public static String formatDtoStr(List<BulletinClasssifyDto> bulletinClasssifyDtos){
        StringBuilder sb = new StringBuilder();
        if(bulletinClasssifyDtos!=null&& bulletinClasssifyDtos.size()!=0){
            for (BulletinClasssifyDto bulletinClasssifyDto :bulletinClasssifyDtos ) {
                sb.append(",'" + bulletinClasssifyDto.announcementID + "'");
            }
            return   sb.substring(1);
        }
        Logger.info("[公告] 选择的公告树的节点没有对应的公司ID");
        return "";
    }

    public static String formatStr(List list){
        StringBuilder sb = new StringBuilder();
        if(list!=null&& list.size()!=0){
            for (Object s :list ) {
                sb.append(",'" + s + "'");
            }
            return   sb.substring(1);
        }
        return "";
    }
}
