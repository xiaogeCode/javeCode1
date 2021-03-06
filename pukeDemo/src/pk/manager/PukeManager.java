package pk.manager;

import java.awt.*;
import java.text.BreakIterator;
import java.text.spi.NumberFormatProvider;
import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import com.sun.xml.internal.ws.api.message.MessageWritable;
import pk.model.PukeListModel;
import pk.model.PukeListTypeEnum;
import pk.model.PukeModel;
import pk.model.PukeTypeEnum;

import javax.swing.*;

public class PukeManager {
	
/*数字1-54代表一副扑克
 * 1-13 +n*54 黑桃
 * 2-26 +n*54红桃
 * 3-39	+n*54草花
 * 4-52	+n*54方片
 * 53 +n*54小王 	
 * 54 +n*54大王
	*/
	//int[] pukeList= new int[54];
	
//	发牌 	几副牌:num
	public int[] dealCard(int num) {
		int[] cardList;
		int[] tmp;
		tmp= new int[num*54+1];
		cardList= new int[num*54];
		
		int count = 0;
		while (count<54*num) {
			Random random = new Random();
			int i =random.nextInt(54*num)+1;//1-54*num
			if (tmp[i] == 0) {				//未生成就将其保存
				tmp[i] = 1;
				cardList[count] = i;		//生成的牌顺序存入
				count ++ ;
			}
		}
		
		return cardList;
	}
	public List<PukeModel> getCardList(int[] list) {
		ArrayList<PukeModel> puke_list=new ArrayList<PukeModel>();
		for (int i = 0; i < list.length; i++) {
			PukeModel model = getCard(list[i]);
			puke_list.add(model);
		}
		return puke_list;
	}
//	根据数字计算牌
	public PukeModel getCard(int data) {
		PukeModel pk=new PukeModel();
		
//		计算牌面
		int card54 = (data%54==0)?54:(data%54);
		pk.tag = card54;
		
		int numValue =  card54%13==0?13:(card54%13);
		if (card54 == 53) {
			numValue = pk.blackJokerNum;
		}
		if (card54 == 54) {
			numValue = pk.redJokerNum;
		}
		pk.number =numValue;
		
		//计算花色
		int typeValue = card54/13;
		if (card54%13 != 0) {
			typeValue +=1;
		}
		if (card54 == 53) {
			typeValue = 5;
		}
		if (card54 == 54) {
			typeValue = 6;
		}
		pk.type = PukeTypeEnum.getByValue(typeValue);
		Image img = new ImageIcon("src/Image/"+pk.tag+".jpg").getImage();
		pk.pukeImage=img;

		return pk;
	}
//	获取若干牌的信息
	public PukeListModel getCardListInfo(List<PukeModel> list) {
		PukeListModel tmp = new PukeListModel();
		tmp.type = PukeListTypeEnum.mix;

		int size = list.size();
        int[] num = new int[size];
        for (int i=0;i<list.size();i++){
            num[i] = list.get(i).number;
        }

		switch (list.size()) {
		case 0:{
			
		}
			break;
		case 1:{
		    //单根
			tmp.maxValue = tmp.minValue = list.get(0).number;
			tmp.type = PukeListTypeEnum.single;
		}
			break;
		case 2:{
		    //对子 王炸
			if (list.get(0).number == list.get(1).number) {
				tmp.maxValue = tmp.minValue = list.get(0).number;
				tmp.type = PukeListTypeEnum.paire;	
			}else if (list.get(0).number * list.get(1).number == list.get(0).redJokerNum * list.get(0).blackJokerNum) {
				tmp.type = PukeListTypeEnum.kingBoom;
			}
		}
			break;
		case 3:{
            int nu1 = list.get(0).number;
            int nu2 = list.get(1).number;
            int nu3 = list.get(2).number;
		    //三个相同
            if (equal3(nu1,nu2,nu3)){
                tmp.type = PukeListTypeEnum.three;
            }
		}
			break;
		case 4:{
		    int nu1 = list.get(0).number;
            int nu2 = list.get(1).number;
            int nu3 = list.get(2).number;
            int nu4 = list.get(3).number;
		    //三带一  炸弹
            if (equal3(nu1,nu2,nu3) && equal3(nu2,nu3,nu4)){
                tmp.type = PukeListTypeEnum.boom;
            }else if (equal3(nu1,nu2,nu3) || equal3(nu2,nu3,nu4) || equal3(nu1,nu3,nu4) || equal3(nu1,nu2,nu4)){
                tmp.type = PukeListTypeEnum.threeOne;
            }
		}
			break;
		case 5:{//顺子,三带一对

            if (isDragon(num)){
                tmp.type = PukeListTypeEnum.dragon;
            }else{
                //tmp.type = PukeListTypeEnum.threePair;
            }
		}
			break;
		case 6:{//顺子 连对
            if (isDragon(num)){
                tmp.type = PukeListTypeEnum.dragon;
            }
			//tmp.type = PukeListTypeEnum.dragon;
		}
			break;

		default:
		{
			if (list.size()>4){
                if (isDragon(num)){
                    tmp.type = PukeListTypeEnum.dragon;
                }
            }
		}
			break;
		}
		return tmp;
	}
	public void sortCard(List<PukeModel> list) {
		
		//自定义Comparator对象，自定义排序  
        Comparator c = new Comparator<PukeModel>() {  
            @Override  
            public int compare(PukeModel o1, PukeModel o2) {  
                // TODO Auto-generated method stub  
            	int num1 = o1.number<3?(o1.number+13):o1.number;
            	int num2 = o2.number<3?(o2.number+13):o2.number;
            	
                if(num1<num2)  {
                    return 1;  
                }
                //注意！！返回值必须是一对相反数，否则无效。jdk1.7以后就是这样。  
                else {
                    return -1;
                }

            }  
        }; 
		list.sort(c);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			PukeModel pukeModel = (PukeModel) iterator.next();
			//System.out.println(pukeModel.number+pukeModel.type.toString());
		}
		
	}
	/**
	 * 功能描述: <br>
	 * 〈判断三个数是否相等〉
	   参数         []
	 * 返回 @return:boolean
	 * 作者 @Author:xiaoge
	 * 时间 @Date: 2018/8/3 20:01
	 */
	private boolean equal3(int no1,int no2,int no3){
	    if ((no1 == no2) && (no1 == no3)){
            System.out.println("no1: "+no1+"no2: "+no2+"no3: "+no3);
	        return true;
        }
	    return false;
    }
    /**
     * 功能描述: <br>
     * 〈是否连续数字〉
       参数         [num]
     * 返回 @return:boolean
     * 作者 @Author:xiaoge
     * 时间 @Date: 2018/8/3 20:47
     */
    private boolean isDragon(int[] num){
        int[] sigalArr= new int[18];
        for (int k=0;k<18;k++){
            sigalArr[k] = 0;
        }
        for (int i=0;i<num.length;i++){
            int index = num[i];
            switch (num[i]){
                //A
                case 1:{
                    index = 14;
                    break;
                }
                //2
                case 2:{
                    index = 15;
                    break;
                }
                //小王
                case 53:{
                    index = 16;
                    break;
                }
                //大王
                case 54:{
                    index = 17;
                    break;
                }
                //3-k
                default:{
                    break;
                }
            }

            sigalArr[index]+=1;

            //有俩个相同的
            if (sigalArr[index]>1){
                return false;
            }
        }
        int cout = 0;
        //统计3-A
        for (int index=3;index<15;index++){
            if (cout>0 && (sigalArr[index]==0)){
                break;
            }
            if (sigalArr[index]>0){
                cout++;
            }
        }
        if (cout<num.length){
            return false;
        }else {
            return true;
        }
	}
}
