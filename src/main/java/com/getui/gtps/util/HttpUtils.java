package com.getui.gtps.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Http请求工具类
 *
 * @author wangxu
 * date: 2020/12/28
 * email：wangx2@getui.com
 */
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    private final static String PREFIX = "--";
    private final static String LINE_END = "\r\n";

    public static HttpResponse post(String spec, Map<String, String> parameters, int timeout) {
        HttpURLConnection con = null;
        DataOutputStream out = null;
        try {
            URL url = new URL(spec);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.setDoOutput(true);
            out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterBuilder.getParamsString(parameters));
            out.flush();
            return FullResponseBuilder.getFullResponse(con);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            if (con != null) {
                con.disconnect();
            }
        }
        return HttpResponse.fail(null);
    }

    public static HttpResponse postFile(String spec, Map<String, String> headParameters, Map<String, String> requestParameters, File file, int timeout) {
        HttpURLConnection con = null;
        OutputStream out = null;
        try {
            URL url = new URL(spec);
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "*/*");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Charset", "UTF-8");
            ParameterBuilder.addHeadParams(con, headParameters);
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            con.connect();
            // 往服务器端写内容 也就是发起http请求需要带的参数
            out = new DataOutputStream(con.getOutputStream());
            // 请求上传文件部分
            ParameterBuilder.writeFile(file, out);
            // 请求参数部分
            ParameterBuilder.writeParams(requestParameters, out);
            // 请求结束标志
            String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
            out.write(endTarget.getBytes());
            out.flush();
            out.close();
            return FullResponseBuilder.getFullResponse(con);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return HttpResponse.fail(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            if (con != null) {
                con.disconnect();
            }
        }
    }


    private static class ParameterBuilder {
        private static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }

        private static void addHeadParams(HttpURLConnection con, Map<String, String> headParameters) {
            if (headParameters != null && headParameters.size() > 0) {
                headParameters.forEach(con::setRequestProperty);
            }
        }

        private static void writeParams(Map<String, String> requestText, OutputStream os) throws IOException {
            StringBuilder requestParams = new StringBuilder();
            Set<Map.Entry<String, String>> set = requestText.entrySet();
            for (Map.Entry<String, String> entry : set) {
                requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_END);
                requestParams.append("Content-Type: text/plain; charset=utf-8").append(LINE_END);
                requestParams.append("Content-Transfer-Encoding: 8bit").append(LINE_END);
                requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                requestParams.append(entry.getValue());
                requestParams.append(LINE_END);
            }
            os.write(requestParams.toString().getBytes());
            os.flush();
        }

        private static void writeFile(File file, OutputStream os) throws IOException {
            String requestParams = PREFIX + BOUNDARY + LINE_END +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" +
                    file.getName() + "\"" +
                    LINE_END +
                    "Content-Type:" +
                    new MimetypesFileTypeMap().getContentType(file) +
                    LINE_END + LINE_END;// 参数头设置完以后需要两个换行，然后才是参数内容
            os.write(requestParams.getBytes());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1) {
                    os.write(b, 0, n);
                }
            }
            os.write(LINE_END.getBytes());
            os.flush();
        }
    }

    public static class FullResponseBuilder {
        public static HttpResponse getFullResponse(HttpURLConnection con) {
            int code = 0;
            String responseMessage = null;
            StringBuilder content = new StringBuilder();
            try {
                code = con.getResponseCode();
                responseMessage = con.getResponseMessage();
                // read response content
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
            } catch (IOException ignored) {
                // 此次异常开发时尽量避免
            }
            return new HttpResponse(code, responseMessage, content.toString());
        }
    }
}
