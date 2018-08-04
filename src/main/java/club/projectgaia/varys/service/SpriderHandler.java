package club.projectgaia.varys.service;

import club.projectgaia.varys.domain.po.NewsAbstract;
import club.projectgaia.varys.domain.po.NewsType;
import club.projectgaia.varys.repository.NewsAbstractRepository;
import club.projectgaia.varys.repository.NewsTypeRepository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpriderHandler {
    @Autowired
    NewsAbstractRepository newsAbstractRepository;

    @Autowired
    NewsTypeRepository newsTypeRepository;

    @Resource(name = "httpClientManagerFactoryBean")
    private CloseableHttpClient client;

    public static final Logger log = LoggerFactory.getLogger(SpriderHandler.class);

    public void test() throws Exception {

        String t = "{\"status\":0,\"data\":{\"list\":[{\"DocID\":1122919298,\"Title\":\"精彩视频\",\"NodeId\":1136053,\"PubTime\":\"2018-05-31 16:55:48\",\"LinkUrl\":\"\",\"Abstract\":\\{ showColumnTit: 'yes', columnClass: '', columnTitClass: 'skinTit2', columnTitIsPic: '', columnTitBgPic: '', showColumnTitMore: '', columnPcMarginTop: '', columnMbMarginTop: '', columnPcMarginBottom: '', columnMbMarginBottom: '', columnPcPaddingTop: '', columnMbPaddingTop: '', columnPcPaddingBottom: '', columnMbPaddingBottom: '', columnTitPcH: '', columnTitMbH: '', SetCompose: [{ composeName: 'foucs-1', ComposeClass: '', advSkin: [''], composeCon: { composeConClass: '', topDistance: '20px', bottomDistance: '', advSkin: [''], modules: [{ dataId: '01', moduleTit: '', moduleTitLink: '', moduleSubTit: '', modulePcH: '', moduleMbH: '', MaxNum: 6, moduleClass: 'margin10B ElemlisB', advSkin: [''], animation: [], SetElem: { elemPcH: '', elemMbH: '', picPcH: '569px', picMbH: '223px', titPcH: '', titMbH: '', abstracPcH: '', abstracMbH: '', elemPcDistanceB: '', elemMbDistanceB: '', picPcDistanceB: '', picMbDistanceB: '', titPcDistanceB: '', titMbDistanceB: '', abstracPcDistanceB: '', abstracMbDistanceB: '', } }] } }],}\",\"keyword\":null,\"Editor\":null,\"Author\":\"刘梦姣\",\"IsLink\":1,\"SourceName\":null,\"PicLinks\":\"\",\"IsMoreImg\":0,\"imgarray\":[],\"SubTitle\":null,\"Attr\":63,\"m4v\":null,\"tarray\":[],\"uarray\":[],\"allPics\":[],\"IntroTitle\":null,\"Ext1\":null,\"Ext2\":null,\"Ext3\":null,\"Ext4\":null,\"Ext5\":null,\"Ext6\":null,\"Ext7\":null,\"Ext8\":null,\"Ext9\":null,\"Ext10\":null}]},\"totalnum\":125}";
        System.out.println(t.indexOf("\"Abstract\":\\"));
        System.out.println(t.replaceFirst("\"Abstract\":\\\\.*}\",", ""));
        JSON.toJSON(t.replaceFirst("\"Abstract\":\\\\.*}\"", ""));

    }

    public void getType() throws Exception {
        try (FileReader fr = new FileReader("/Users/deepclue/Desktop/index.txt");
             BufferedReader br = new BufferedReader(fr);
             FileWriter fw = new FileWriter("/Users/deepclue/Desktop/save.txt");
             BufferedWriter bw = new BufferedWriter(fw)) {
            String url = "http://qc.wa.news.cn/nodeart/list?nid=%s&pgnum=1&cnt=1";
            Pattern p = Pattern.compile(".com/([a-zA-Z]+)/?20");

            String index = br.readLine();
            int i = 0;
            int total = 0;
            while (index != null) {
                i++;
                HttpGet get = new HttpGet(String.format(url, index));
                get.setHeader("Accept", "*/*");
                get.setHeader("Accept-Encoding", "gzip, deflate");
                get.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
                get.setHeader("Connection", "keep-alive");
                get.setHeader("Host", "qc.wa.news.cn");
                get.setHeader("Referer", "http://www.xinhuanet.com");
                get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
                //get.setHeader("Cookie", "wdcid=6550ff071e1722e0; tma=4434860.51068288.1532867142302.1532867142302.1532867142302.1; tmd=1.4434860.51068288.1532867142302.; fingerprint=78461c690986347c3c3400a83e1f74ff; bfd_g=87205254007bf95200005f7b0160a96a5b573e49; pc=e60e67c3e2e2ba368070a8fbdb573ec7.1532442926.1532867392.17");

                CloseableHttpResponse response = client.execute(get);
                NewsType newsType = new NewsType();

                try {
                    String result = EntityUtils.toString(response.getEntity(), "utf-8");
                    if (result.indexOf("\"Abstract\":\\")>0){
                        result = result.replaceFirst("\"Abstract\":\\\\.*}\",", "");
                    }
                    JSONObject retJson = JSON.parseObject(result.substring(1, result.length() - 1));

                    newsType.setCount(retJson.getInteger("totalnum"));
                    total += retJson.getInteger("totalnum");
                    newsType.setNid(index);
                    JSONObject news = retJson.getJSONObject("data").getJSONArray("list").getJSONObject(0);
                    Matcher m = p.matcher(news.getString("LinkUrl"));
                    if (m.find()) {
                        newsType.setNewsType(m.group(1));
                    }
                    newsType.setTitle(news.getString("Title"));
                    newsTypeRepository.save(newsType);
                } catch (JSONException e) {
                    log.warn(index + " beJson error!");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    log.warn(index + " null pointer");
                } catch (DataIntegrityViolationException e) {
                    log.warn(index + " save db error");
                    bw.write(JSON.toJSONString(newsType));
                    bw.newLine();
                    bw.flush();
                } catch (Exception e) {

                    log.info("");
                } finally {
                    index = br.readLine();
                }

                if (i % 1000 == 0) {
                    log.info(i + "");
                }
            }

            log.info("total news:" + total);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public void getNewsAbstract() throws Exception {
        Long stamp = 1533369117247L;
        String newsType = "world";
        String url = "http://qc.wa.news.cn/nodeart/list?nid=113680&pgnum=%d&cnt=100";
        int i = 0;
        int total = 0;
        while (true) {
            i++;
            stamp++;
            List<NewsAbstract> save = new ArrayList<>();
            HttpGet get = new HttpGet(String.format(url, i));
            //get.setHeader("Accept", "*/*");
            get.setHeader("Accept-Encoding", "gzip, deflate");
            get.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            get.setHeader("Connection", "keep-alive");
            get.setHeader("Host", "qc.wa.news.cn");
            get.setHeader("Referer", "http://www.xinhuanet.com/auto/syc.htm");
            get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
            //get.setHeader("Cookie", "wdcid=6550ff071e1722e0; tma=4434860.51068288.1532867142302.1532867142302.1532867142302.1; tmd=1.4434860.51068288.1532867142302.; fingerprint=78461c690986347c3c3400a83e1f74ff; bfd_g=87205254007bf95200005f7b0160a96a5b573e49; pc=e60e67c3e2e2ba368070a8fbdb573ec7.1532442926.1532867392.17");

            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                result = result.substring(1, result.length() - 1);

                try {
                    JSONObject retJson = JSON.parseObject(result.substring(0, result.length() - 1));
                    JSONArray newsList = retJson.getJSONObject("data").getJSONArray("list");
                    for (int s = 0; s < newsList.size(); s++) {
                        NewsAbstract news = newsList.getObject(s, NewsAbstract.class);
                        if (newsList.getJSONObject(s).getJSONArray("allPics").size() > 0) {
                            news.setPics(newsList.getJSONObject(s).getJSONArray("allPics").getString(0));
                        }
                        if (StringUtils.isNotEmpty(newsType)) {
                            news.setNewsType(newsType);
                        }
                        total++;
                        save.add(news);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    log.info("total:{}", total);
                    break;
                }

                newsAbstractRepository.saveAll(save);
            }

        }
    }

    public void getIndex() {
        try (FileWriter fw = new FileWriter("C:\\Users\\Administrator\\Desktop\\index.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            int i = 0;
            while (true) {
                i++;
                if (i % 1000 == 0) {
                    bw.flush();
                    log.info(i + "");
                }
                if (!String.valueOf(i).startsWith("1")) {
                    continue;
                }
                String url = "http://qc.wa.news.cn/nodeart/list?nid=%d&pgnum=1&cnt=1";
                HttpGet get = new HttpGet(String.format(url, i));
                get.setHeader("Accept", "*/*");
                get.setHeader("Accept-Encoding", "gzip, deflate");
                get.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
                get.setHeader("Connection", "keep-alive");
                get.setHeader("Host", "qc.wa.news.cn");
                get.setHeader("Referer", "http://www.xinhuanet.com/auto/syc.htm");
                get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
                get.setHeader("Cookie", "wdcid=6550ff071e1722e0; tma=4434860.51068288.1532867142302.1532867142302.1532867142302.1; tmd=1.4434860.51068288.1532867142302.; fingerprint=78461c690986347c3c3400a83e1f74ff; bfd_g=87205254007bf95200005f7b0160a96a5b573e49; pc=e60e67c3e2e2ba368070a8fbdb573ec7.1532442926.1532867392.17");

                CloseableHttpResponse response = client.execute(get);

                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                if (result.contains("\"data\":{\"list\"")) {
                    log.info(i + " can use");
                    bw.write(i + "");
                    bw.newLine();
                }
                if (i == 20000000) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}