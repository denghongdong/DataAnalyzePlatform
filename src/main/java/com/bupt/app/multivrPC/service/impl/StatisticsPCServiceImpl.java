package com.bupt.app.multivrPC.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.bupt.app.multivrPC.dao.StatisticsPCMapper;
import com.bupt.app.multivrPC.dto.StatisticsPCDTO;
import com.bupt.app.multivrPC.model.StatisticsPC;
import com.bupt.app.multivrPC.model.StatisticsPCExample;
import com.bupt.app.multivrPC.model.StatisticsPCExample.Criteria;
import com.bupt.app.multivrPC.service.StatisticsPCService;
import com.bupt.app.multivrPC.utils.MultivrPCVRTypeUtils;
import com.bupt.core.base.util.Utils;
/**
 * PC多VR词表查询的业务逻辑实现
 * @author litong
 *
 */
@Service("statisticsPCService")
public class StatisticsPCServiceImpl implements StatisticsPCService {
	
	private final Log log = LogFactory.getLog(getClass());
	private boolean debug = log.isDebugEnabled();
	
	@Resource(name="statisticsPCMapper")
	private StatisticsPCMapper statisticsPCMapper;
	
	private Map<Integer,Integer> totalRecordMap = new TreeMap<Integer,Integer>();

	@Override
	public int getTotalRecords(HttpServletRequest request, Boolean search) {
		getTotalRecordMap(request, search);
		int totalRecord = 0;
		for (Integer record : totalRecordMap.values()) {
			totalRecord+=record;
		}
		return totalRecord;
	}

	/**
	 * @param request
	 * @param search
	 * @param statisticsPCExample
	 * @author 李彤 2013-9-17 上午10:54:44
	 */
	private void getTotalRecordMap(HttpServletRequest request, Boolean search) {
		StatisticsPCExample statisticsPCExample = new StatisticsPCExample();
		String startTime = request.getParameter("startTime");
		String endTime = request.getParameter("endTime");
		String timelevel = request.getParameter("timelevel");
		
		int startDay;
		int endDay;
		try {
			startDay = Integer.parseInt(Utils.getDate(startTime));
			endDay = Integer.parseInt(Utils.getDate(endTime));
		} catch (Exception e) {
			// 设置默认选择哪张表
			startDay = endDay = Integer.parseInt(Utils.lastDate(new Date()));
		}

		Integer startHour = null;
		Integer endHour = null;
		if(startDay==endDay){
			statisticsPCExample.setDate(startDay + "");
			startHour = Utils.getHour(startTime);
			endHour = Utils.getHour(endTime);
			if(startHour==0&&endHour==23){
				startHour=null;
				endHour=null;
			}
			if (search) {
				addCriteria(request, statisticsPCExample, startHour, endHour);
			}
			if(timelevel.equals("hour")){
				totalRecordMap.put(startDay, statisticsPCMapper.countByExample(statisticsPCExample));
			}else{
				totalRecordMap.put(startDay, statisticsPCMapper.countDayByExample(statisticsPCExample));
			}
		}else if (startDay < endDay) {
			if (search) {// 添加查询条件
//				if (currentDay == startDay) {startHour = Utils.getHour(startTime);if(startHour!=0) endHour=23;} 
//				if (currentDay == endDay) {endHour = Utils.getHour(endTime);if(endHour!=23) startHour=0;}
				addCriteria(request, statisticsPCExample, startHour, endHour);
			}
			for (int currentDay = startDay; currentDay <= endDay; ++currentDay) {
				statisticsPCExample.setDate(currentDay + "");
				
				totalRecordMap.put(currentDay, statisticsPCMapper.countByExample(statisticsPCExample));
			}
		}
	}

	@Override
	public List<StatisticsPCDTO> listResults(int start, int limit, String sortName,
			String sortOrder, HttpServletRequest request,Boolean search) {

		//DTO
		List<StatisticsPCDTO> statisticsDTOList = new ArrayList<StatisticsPCDTO>();
		
		//获取起始日期
		String startTime = request.getParameter("startTime");
		String endTime = request.getParameter("endTime");
		String timelevel = request.getParameter("timelevel");
		
		int startDay;
		int endDay;
		try {
			startDay = Integer.parseInt(Utils.getDate(startTime));
			endDay = Integer.parseInt(Utils.getDate(endTime));
		} catch (Exception e) {
			// 设置默认选择哪张表
			startDay = endDay = Integer.parseInt(Utils.lastDate(new Date()));
		}
		
		Integer startHour = null;
		Integer endHour = null;
		if(startDay==endDay){//如果是同一天，判断小时
			StatisticsPCExample statisticsPCExample = new StatisticsPCExample();
			statisticsPCExample.setOrderByClause( sortName +" "+ sortOrder);
			statisticsPCExample.setDate(startDay + "");
			startHour = Utils.getHour(startTime);
			endHour = Utils.getHour(endTime);
			if(startHour==0&&endHour==23){
				startHour=null;
				endHour=null;
			}
			if(search) addCriteria(request, statisticsPCExample, startHour, endHour);
			
			statisticsPCExample.setDate(startDay+"");
			statisticsPCExample.setStart(start);
			statisticsPCExample.setLimit(limit);
			statisticsDTOList.addAll(selectByPCExample(statisticsPCExample,timelevel));
		}else if (startDay < endDay) {//不考虑小时
			StatisticsPCExample statisticsPCExample = new StatisticsPCExample();
			statisticsPCExample.setOrderByClause( sortName +" "+ sortOrder);
			if(search) addCriteria(request, statisticsPCExample, startHour, endHour);
			int currentTotalRecord = 0;
			for (int currentDay = startDay; currentDay <= endDay; currentDay++) {
				if(totalRecordMap.isEmpty()){
					getTotalRecordMap(request, search);
				}
				Integer currentRecordsCount = totalRecordMap.get(currentDay);
				if (start + limit <= currentTotalRecord + currentRecordsCount) {
					statisticsPCExample.setDate(currentDay + "");
					statisticsPCExample.setStart(start - currentTotalRecord);
					statisticsPCExample.setLimit(limit);
					statisticsDTOList.addAll(selectByPCExample(statisticsPCExample,timelevel));
					break;
				} else if (start + limit > currentTotalRecord + currentRecordsCount&&currentTotalRecord + currentRecordsCount - start>0) {
					statisticsPCExample.setDate(currentDay + "");
					statisticsPCExample.setStart(start - currentTotalRecord);
					statisticsPCExample.setLimit(currentTotalRecord + currentRecordsCount - start);
					statisticsDTOList.addAll(selectByPCExample(statisticsPCExample,timelevel));
					limit = limit - (currentTotalRecord + currentRecordsCount - start);
					start=currentTotalRecord+currentRecordsCount;
				}
				currentTotalRecord += currentRecordsCount;
			}
		}
		totalRecordMap.clear();
		return statisticsDTOList;
	}

	/**
	 * @param statisticsPCExample
	 * @return
	 * @author 李彤 2013-9-12 下午2:15:21
	 */
	private List<StatisticsPCDTO> selectByPCExample(
			StatisticsPCExample statisticsPCExample,String timelevel) {
		Map<String, String> vrMap = MultivrPCVRTypeUtils.getVRType();
		List<StatisticsPCDTO> wordDTOList = new ArrayList<StatisticsPCDTO>();
		List<StatisticsPC> statisticsPCs;
		if(timelevel.equals("hour")){
			statisticsPCs=statisticsPCMapper.selectByExample(statisticsPCExample);
		}else{
			statisticsPCs=statisticsPCMapper.selectDayByExample(statisticsPCExample);
		}
		
		StatisticsPCDTO statisticsPCDTO = null;
		for (StatisticsPC statisticsPC : statisticsPCs) {
			statisticsPCDTO = new StatisticsPCDTO();
			Utils.copyProperties(statisticsPCDTO, statisticsPC);
			//VR类型转换
			statisticsPCDTO.setVrId(statisticsPC.getType());
			if(vrMap.containsKey(statisticsPC.getType())){
				statisticsPCDTO.setType(vrMap.get(statisticsPC.getType()));
			}
			if(statisticsPCDTO.getPv()!=0){
				statisticsPCDTO.setConsumption(statisticsPCDTO.getEclpv()*100/statisticsPCDTO.getPv()+"%");
				statisticsPCDTO.setCtr(statisticsPCDTO.getClick()*100/statisticsPCDTO.getPv()+"%");
			}else{
				statisticsPCDTO.setConsumption("-");
				statisticsPCDTO.setCtr("-");
			}
			wordDTOList.add(statisticsPCDTO);
		}
		return wordDTOList;
	}

	/**
	 * 添加查询条件
	 * @param request
	 * @param search
	 * @param statisticsPCExample
	 * @author 李彤 2013-8-26 下午8:18:21
	 */
	private void addCriteria(HttpServletRequest request,StatisticsPCExample statisticsPCExample,Integer startHour,Integer endHour) {
			String[] type = request.getParameterValues("type[]");
			if(type==null||type.length==0) type = request.getParameterValues("type");
			String[] jhid = request.getParameterValues("jhid[]");
			if(jhid==null||jhid.length==0) jhid = request.getParameterValues("jhid");
			String[] position = request.getParameterValues("position");
			if(position==null||position.length==0) position = request.getParameterValues("position[]");
			String[] abtest = request.getParameterValues("abtest");
			if(abtest==null||abtest.length==0) abtest = request.getParameterValues("abtest[]");
			String clickid = request.getParameter("clickid");
			if(debug){
				log.debug("jhid"+Arrays.toString(jhid)+"type:"+Arrays.toString(type)+"position:"+position+"abtest:"+abtest+"clickid: "+clickid);
			}
			Criteria criteria = statisticsPCExample.createCriteria();
			if(type!=null&&type.length>0&&!type[0].equalsIgnoreCase("null")) criteria.andTypeIn(Arrays.asList(type));
			if(jhid!=null&&jhid.length>0&&!jhid[0].equalsIgnoreCase("null")) criteria.andJhidIn(Arrays.asList(jhid));
			if(position!=null&&position.length>0&&!position[0].equalsIgnoreCase("null")) {
				Integer[] tposition = new Integer[position.length];
				for (int i = 0; i < position.length; i++) {
					tposition[i]=Integer.parseInt(position[i]);
				}
				criteria.andPositionIn(Arrays.asList(tposition));
			}
			if(abtest!=null&&abtest.length>0&&!abtest[0].equalsIgnoreCase("null")) {
				Integer[] tabtest = new Integer[abtest.length];
				for (int i = 0; i < abtest.length; i++) {
					tabtest[i]=Integer.parseInt(abtest[i]);
				}
				criteria.andPositionIn(Arrays.asList(tabtest));
			}
			if(!StringUtils.isEmpty(clickid)) criteria.andClickidLike(clickid);
			if(startHour!=null&&endHour!=null) criteria.andHourBetween(startHour, endHour);
	}
	
	

}
