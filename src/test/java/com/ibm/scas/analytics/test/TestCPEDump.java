package com.ibm.scas.analytics.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.scas.analytics.content.DataProvider;
import com.ibm.scas.analytics.persistence.beans.FormField;
import com.ibm.scas.analytics.persistence.beans.StepDetail;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;

public class TestCPEDump extends BaseTestCase {
	private DataProvider dataProvider;
	
	private static final Logger logger = Logger.getLogger(TestGatewayService.class);
	
	private static final String STEP_CLUSTER_DESC = "Cluster Description";
	private static final String STEP_CLUSTER_PARAMS = "Cluster Parameters";
	private static final String STEP_DATA_AUTOMATION = "Data Automation";
	private static final String STEP_SECURE_HADOOP = "Secure Hadoop";
	private static final String STEP_GATEWAY_PARAMS = "Gateway Parameters";
	private static final String STEP_REVIEW = "Review Details";
	private static final String STEP_TRANSFER_DATA = "Transfer Data";
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	
		executeSQLScript("populate_fake_data.sql");
		dataProvider = injector.getInstance(DataProvider.class);
	}
	
	@Override
	public void tearDown() throws Exception {
		
		super.tearDown();
	}
	
	public void testDataForStreams() throws Exception {
		logger.info("****** testDataForStreams()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "streams"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 5);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 13);
	}
	
	
	public void testDataForHortonworks() throws Exception {
		logger.info("****** testDataForHortonworks()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "hortonworks"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 7);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_DATA_AUTOMATION))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 4 && stepDetail.getDescription().equals(STEP_GATEWAY_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 5 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 43);
	}
	
	
	public void testDataForbiqse() throws Exception {
		logger.info("****** testDataForbiqse()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "bi-qse"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 8);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_DATA_AUTOMATION))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 4 && stepDetail.getDescription().equals(STEP_SECURE_HADOOP) && stepDetail.getIsEnabled() == 0)
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 5 && stepDetail.getDescription().equals(STEP_GATEWAY_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 6 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 47);
	}
	
	public void testDataForbi30() throws Exception {
		logger.info("****** testDataForbi30()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "bi-30"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 8);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_DATA_AUTOMATION))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 4 && stepDetail.getDescription().equals(STEP_SECURE_HADOOP) && stepDetail.getIsEnabled() == 0)
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 5 && stepDetail.getDescription().equals(STEP_GATEWAY_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 6 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 47);
	}
	
	
	public void testDataForbi30WithSDFS() throws Exception {
		logger.info("****** testDataForbi30WithSDFS()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "bi30-sdfs"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 8);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_DATA_AUTOMATION))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 4 && stepDetail.getDescription().equals(STEP_SECURE_HADOOP) && stepDetail.getIsEnabled() == 1)
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 5 && stepDetail.getDescription().equals(STEP_GATEWAY_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 6 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 47);
	}
	
	public void testDataForbi40IOP() throws Exception {
		logger.info("****** testDataForbi40IOP()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "bi40-IOP"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 8);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_DATA_AUTOMATION))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 4 && stepDetail.getDescription().equals(STEP_SECURE_HADOOP) && stepDetail.getIsEnabled() == 0)
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 5 && stepDetail.getDescription().equals(STEP_GATEWAY_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 6 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 47);
	}
	
	public void testDataForbi40Addons() throws Exception {
		logger.info("****** testDataForbi40IOP()");
		
		List<StepDetail> stepDetails = service.getObjectsBy(StepDetail.class, new WhereClause("pluginId", "bi40-addons"));
		logger.info("StepDetails No. : " + stepDetails.size());
		assertEquals(true, stepDetails.size() == 8);
		
		List<String> stepIds = new ArrayList<String>();
		
		Boolean stepsMatch = false;
		for(StepDetail stepDetail : stepDetails)
		{
			if(stepDetail.getStepNumber() == 1 && stepDetail.getDescription().equals(STEP_CLUSTER_DESC))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 2 && stepDetail.getDescription().equals(STEP_CLUSTER_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 3 && stepDetail.getDescription().equals(STEP_DATA_AUTOMATION))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 4 && stepDetail.getDescription().equals(STEP_SECURE_HADOOP) && stepDetail.getIsEnabled() == 0)
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 5 && stepDetail.getDescription().equals(STEP_GATEWAY_PARAMS))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 6 && stepDetail.getDescription().equals(STEP_REVIEW))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == 0 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else if(stepDetail.getStepNumber() == -1 && stepDetail.getDescription().equals(STEP_TRANSFER_DATA))
			{
				stepsMatch = true;
			}
			else
			{
				stepsMatch = false;
			}
			
			logger.info("StepNumber : " + stepDetail.getStepNumber() + " , Description : " + stepDetail.getDescription());
			logger.info("Steps Match : " + stepsMatch);
			stepIds.add(stepDetail.getId());
		}
		
		logger.info("Steps Match : " + stepsMatch);
		assertEquals(true, stepsMatch.booleanValue());
		
		List<FormField> formFields = service.getObjectsBy(FormField.class, new WhereInClause("stepId", stepIds));
		logger.info("FormFields No. : " + formFields.size());
		assertEquals(true, formFields.size() == 47);
	}
}
