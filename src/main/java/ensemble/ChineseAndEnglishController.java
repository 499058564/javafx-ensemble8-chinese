package ensemble;

import org.apache.commons.lang3.StringUtils;

/**
 * @author yuziyang
 * @date 2023/3/4
 **/
public class ChineseAndEnglishController {
    public static String translatorOfChinese(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        final String property = EnsembleApp.nameProperties.getProperty(replaceAllSpecialStr(key));
        if(StringUtils.isNotEmpty(property)){
            key = property;
        }
        return key;
    }

    public static void printlnAwaitTranslateProperty(String name){
        if(StringUtils.isEmpty(name)){
            return;
        }
        System.out.println(String.format("%s=" , replaceAllSpecialStr(name)));
    }
    private static String replaceAllSpecialStr(String key){
        String replaceWith = "_";
        String[] specials = {" " , ":" , "," , "." , "<"};
        String replace = key;
        for (String str : specials){
            replace = replace.replace(str , replaceWith);
        }
        return replace;
    }


}
