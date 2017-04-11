package com.baidu.handler;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 八月,2016
 */
public enum BaiduEnum {
    DECODEAREANAME(new String[]{"北京", "天津", "河北", "山西", "内蒙古", "吉林", "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆", "香港", "澳门", "台湾"}),
    AREANAMEARRAY(new String[]{"%E5%8C%97%E4%BA%AC", "%E5%A4%A9%E6%B4%A5", "%E6%B2%B3%E5%8C%97", "%E5%B1%B1%E8%A5%BF", "%E5%86%85%E8%92%99%E5%8F%A4", "%E5%90%89%E6%9E%97", "%E9%BB%91%E9%BE%99%E6%B1%9F", "%E4%B8%8A%E6%B5%B7", "%E6%B1%9F%E8%8B%8F", "%E6%B5%99%E6%B1%9F", "%E5%AE%89%E5%BE%BD", "%E7%A6%8F%E5%BB%BA", "%E6%B1%9F%E8%A5%BF", "%E5%B1%B1%E4%B8%9C", "%E6%B2%B3%E5%8D%97", "%E6%B9%96%E5%8C%97", "%E6%B9%96%E5%8D%97", "%E5%B9%BF%E4%B8%9C", "%E5%B9%BF%E8%A5%BF", "%E6%B5%B7%E5%8D%97", "%E9%87%8D%E5%BA%86", "%E5%9B%9B%E5%B7%9D", "%E8%B4%B5%E5%B7%9E", "%E4%BA%91%E5%8D%97", "%E8%A5%BF%E8%97%8F", "%E9%99%95%E8%A5%BF", "%E7%94%98%E8%82%83", "%E9%9D%92%E6%B5%B7", "%E5%AE%81%E5%A4%8F", "%E6%96%B0%E7%96%86", "%E9%A6%99%E6%B8%AF", "%E6%BE%B3%E9%97%A8", "%E5%8F%B0%E6%B9%BE"}),
    CARDNUMARRAY(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "x"}),
    URL("https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php");

    private String value;
    private String[] arr;

    BaiduEnum(String value) {
        this.value = value;
    }
    BaiduEnum(String[] arr) {
        this.arr = arr;
    }

    @Override
    public String toString() {
        return value;
    }

    public String[] toArray() {
        return arr;
    }
}
