package cmd;

import manager.CalManage;

/**
 * Copyright (C), 2018-2018, XXX有限公司
 * FileName: MainFunc
 * Author:   xiaoge
 * Date:     2018/8/14 22:35
 * Description: ${DESCRIPTION}
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
public class MainFunc {
    public static void main(String[] args) {
        CalManage calManage = new CalManage();
        calManage.calFourNum(1,3,1,8);
    }
}