package com.example.demo.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.demo.entity.Police;
import com.example.demo.request.TaskRequest;
import com.example.demo.service.IPoliceService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author skb
 * @description: TODO
 * @date 2023/12/15 11:54
 */
@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadFileController {

    @Autowired
    private IPoliceService policeService;

    private final String url = "http://222.133.41.27:8019/tpeas/chengnuo";

    @PostMapping("/execTask")
    public Object execTask(@RequestParam("file") MultipartFile file) {
        Map<String,Object> map = new HashMap<>();
        if (file.isEmpty()) {
            map.put("success",false);
            map.put("msg","请选择一个文件");
            return map;
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
                if(StrUtil.isBlank(name) || StrUtil.isBlank(idCode) || StrUtil.isBlank(phone)){
                    map.put("success",true);
                    map.put("msg","导入成功");
                    return map;
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
                log.info("请求参数为：{}",requestBody);
//                log.info("请求参数为：{}，{}，{}，{}，{}，{}",name,idCode,phone,taskRequest.getCompany(),taskRequest.getZhanxian(),taskRequest.getJjtype());
                // 执行请求
                HttpResponse response = httpClient.execute(request);
                // 处理响应
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    String responseBody = EntityUtils.toString(entity);
                    // 响应处理逻辑
                    log.info("请求成功，响应值{}", responseBody);
                    Map<String, Object> rMap = JSONObject.parseObject(responseBody, new TypeReference<Map<String, Object>>() {
                    });
                    String code = rMap.getOrDefault("code", "500").toString();
                    Police police = new Police();
                    police.setId(IdUtil.simpleUUID()).setIdcode(idCode).setName(name).setPhone(phone).setJjtype(taskRequest.getJjtype()).setZhanxian(taskRequest.getZhanxian()).setCompany(taskRequest.getCompany()).setCreateTime(new Date());
                    if ("200".equals(code)) {
                        police.setStatus("1");
                    } else {
                        police.setStatus("0");
                    }
                    policeService.save(police);
                } else {
                    log.info("请求失败，状态码{}", statusCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        map.put("success",true);
        map.put("msg","导入成功");
        return map;
    }


    @PostMapping("/testUpload")
    public Object testUpload() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("test","xiaoming");
        return map;
    }
}

