package util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: panzhiwei
 * Date: 13-4-19
 * Time: 下午5:01
 * To change this template use File | Settings | File Templates.
 */
public final class NewsHelper {

    private static List<String> FIRST_NEWS_CLASSIFY = Arrays.asList("020101","02010101","020103","02010301","02010303","020103030705","020103030903","020111","02011501","02011503","02011901","020199");
    private static List<String> SECOED_NEWS_CLASSIFY = Arrays.asList("0201030519","0201030523","0202150101","0202150103","020403","020405","02040501","02040503","02040505","02040507","02040509","020407","02040701","02040703","02040705","02040707","02040709","02040711","02040713","02040715");
    private static List<String> THIRD_NEWS_CLASSIFY = Arrays.asList("02010305","0201030501","0201030507","0201030509","0201030511","0201030515","0201030517","02010307","0201030701","0201030705","020103070501","020103070503","0201030707","0201030709","020103070901","020103070903","020103070905","02011507","0201150701","0201150705","0201150707","02011905","02011911","02011913","02011915","020121","02012101","02012103","02012105","02012107","02012109","02012111","020125","02012511","020301","02030101","02030103","0203010301","0203010303","0203010501","0203010503","02030107","02030109","020313","029917","029918");
    private static List<String> FORTH_NEWS_CLASSIFY = Arrays.asList("0201030301","0201030303","0201030305","0201030307","0201030703","020109","02011505","02011903");
    private static List<String> FIFTH_NEWS_CLASSIFY = Arrays.asList("020103050501","0201030513","0201030521","5","02011303","02011305","02011307","02011309","02011313","02011315","02011317","02011399","0201139901","02011509","0201150901","020123","02012301","02012303","02012305","02012505","02012507","02030105");
    private static List<String> SIX_NEWS_CLASSIFY = Arrays.asList("0201030503","0201030514","0201150703","02011907","020305","02030501","02030503");
    private static List<String> SEVEN_NEWS_CLASSIFY = Arrays.asList("0201030505","020103050503","020103050505","020103050507","02011909","020307");
    private static List<String> EIGHT_NEWS_CLASSIFY = Arrays.asList("020103030701","020103030703","0201030309","020103030901","020103030905","020103030907","020103030909","020103030911","020103030913","020127","02012703","02012705","02012707","020309","020311");
    private static List<String> NINE_NEWS_CLASSIFY = Arrays.asList("02010501","02010503","02010505","02010507","02010509","02010511","02010513","02010515","02010517","020107","02010701","02010703","0201070301","02010705","02010707","02010709","02010711","02010713","02010715","02010717","02011311","02011513","02012501","02012503","02012509","02012513","02030301","02030303","0203030301","0203030303","0203030305","02030305");
    private static Map<String, List<String>> newsClassifyIdMapping = Maps.newHashMap();

    static {
        newsClassifyIdMapping.put("1", FIRST_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("2", SECOED_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("3", THIRD_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("4", FORTH_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("5", FIFTH_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("6", SIX_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("7", SEVEN_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("8", EIGHT_NEWS_CLASSIFY);
        newsClassifyIdMapping.put("9", NINE_NEWS_CLASSIFY);

    }

    public static List<String> getRealNewsClassifyIdByCode(String myCode) {
        List<String> realCodeList = newsClassifyIdMapping.get(myCode);
        if (realCodeList == null) {
            realCodeList = Lists.newArrayList();
        }

        return realCodeList;

    }

    public static List<String> getRealNewsClassifyIdByCode(List<String> myCodes) {
        List<String> result = Lists.newArrayList();
        for (String myCode : myCodes) {
            if (StringUtils.isNotBlank(myCode)) {
                result.addAll(getRealNewsClassifyIdByCode(myCode));
            }
        }
        return result;
    }

}
