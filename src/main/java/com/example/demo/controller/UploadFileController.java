package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.demo.request.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author skb
 * @description: TODO
 * @date 2023/12/15 11:54
 */
@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadFileController {

    private final String url = "http://222.133.41.27:8019/tpeas/chengnuo";

    @PostMapping("/execTask")
    public Object execTask(@RequestParam("file") MultipartFile file) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (file.isEmpty()) {
            HashMap<String, Object> rMap = new HashMap<>();
            rMap.put("code", "500");
            rMap.put("msg", "Please select a file to upload.");
            list.add(rMap);
            return list;
        }
        InputStream inputStream = null;
        // 读取 Excel 文件
        try {
            inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream);
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 遍历每一行，从第二行开始读取参数
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                String name = "";
                String idCode = "";
                String phone = "";
                if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                    name = NumberToTextConverter.toText(row.getCell(0).getNumericCellValue());
                } else if (row.getCell(0).getCellType() == CellType.STRING) {
                    name = row.getCell(0).getStringCellValue();
                }
                if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                    idCode = NumberToTextConverter.toText(row.getCell(1).getNumericCellValue());
                } else if (row.getCell(1).getCellType() == CellType.STRING) {
                    idCode = row.getCell(1).getStringCellValue();
                }
                if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                    phone = NumberToTextConverter.toText(row.getCell(2).getNumericCellValue());
                } else if (row.getCell(2).getCellType() == CellType.STRING) {
                    phone = row.getCell(2).getStringCellValue();
                }
                TaskRequest taskRequest = new TaskRequest();
                taskRequest.setXingming(name).setIdcode(idCode).setPhone(phone);
                // 构造请求数据
                String requestBody = JSON.toJSONString(taskRequest);
                // 发送 HTTP 请求
                HttpClient httpClient = HttpClients.createDefault();
                HttpPost request = new HttpPost(url);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                // 执行请求
                HttpResponse response = httpClient.execute(request);
                // 处理响应
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String responseBody = EntityUtils.toString(entity);
                    // 响应处理逻辑
                    log.info("请求成功，响应值{}", responseBody);
                    Map<String, Object> map = JSONObject.parseObject(responseBody, new TypeReference<Map<String, Object>>() {
                    });
                    String code = map.getOrDefault("code", "500").toString();
                    HashMap<String, Object> rMap = new HashMap<>();
                    rMap.put("code", code);
                    String msg = "";
                    if ("200".equals(code)) {
                        msg = "姓名：" + name + "，身份证号：" + idCode + "，手机号：" + phone + " 上传成功";
                    } else {
                        msg = "姓名：" + name + "，身份证号：" + idCode + "，手机号：" + phone + " 上传失败";
                    }
                    rMap.put("msg", msg);
                    list.add(rMap);
                } else {
                    log.info("请求失败，状态码{}", statusCode);
                    HashMap<String, Object> rMap = new HashMap<>();
                    rMap.put("code", statusCode);
                    rMap.put("msg", "姓名：" + name + "，身份证号：" + idCode + "，手机号：" + phone + " 上传失败");
                    list.add(rMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return list;
    }

    @PostMapping("/testUpload")
    public Object testUpload() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("test","xiaoming");
        return map;
    }
}

