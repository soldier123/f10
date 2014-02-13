package controllers;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.IO;
import play.libs.MimeTypes;
import play.libs.WS;
import play.mvc.Controller;
import play.templates.JavaExtensions;

import java.io.UnsupportedEncodingException;

/**
 * User: wenzhihong
 * Date: 13-5-17
 * Time: 下午7:32
 */
public class AttachCt extends Controller {
    private static final URLCodec encoder = new URLCodec();

    //研报附件下载
    public static void attachDownload(String url) {
        url = url.replace('\\', '/');
        String fileName = url.substring(url.lastIndexOf('/'));
        fileName = fileName.substring(1);
        String contentType = MimeTypes.getContentType(fileName);
        try {
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename="
                            + encoder.encode(fileName, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new UnexpectedException(e);
        }
        response.setContentTypeIfNotSet(contentType);

        String pre = Play.configuration.getProperty("bulletinAttachPreUrl");
        url = JavaExtensions.urlEncode(url.replace('\\', '/'));
        url = StringUtils.replace(url.replace('\\', '/'), "+", "%20"); //这个是jdk的bug, url里的空格不能转成 %20
        String fullUrl = pre + url;
        Logger.info("下载附件url=" + fullUrl);
         try{
             IO.copy(WS.url(fullUrl).get().getStream(), response.out);
         }catch (Exception e){
             response.headers.remove("");
             redirect("Application.fileNotFound","000001");
         }


    }
}
