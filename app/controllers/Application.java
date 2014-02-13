package controllers;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import play.Play;
import play.mvc.Controller;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class Application extends Controller {

    public static void index(String scode) {
        render();
    }


    public static void setupbin(){
        String[] ext = {"exe", "doc"};
        Collection<File> fs = FileUtils.listFiles(Play.getFile("public/setupbin"), null, false);
        List<String> pathList = Lists.newArrayList();
        for (File f : fs) {
            pathList.add(f.getName());
        }

        render(pathList);
    }
    public static void fileNotFound(){
        render();
    }


}