package com.example.demo.request;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author skb
 * @description: TODO
 * @date 2023/12/15 12:38
 */
@Data
@Accessors(chain = true)
public class TaskRequest {

    private String vedio = null;
    private String xingming;
    private String idcode;
    private String phone;
    private String count = "1";
    private String type;
    private String jjtype = "平原交警";
    private String company = "王打卦镇";
    private String jjxingming = "";
    private String zhanxian = "医疗卫健场所";
    private String[] item = {};
    private String[] itema = {};
};
