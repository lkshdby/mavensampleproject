/*
 * This smartwizard plugin is developed to handle population of dynamic data and creation of HTML elements with
 * dynamic data obtained. It is used to attach event handlers and call REST API attached to elements.
 * 
 *  Also validation and collection of data to submit the form is done by this plugin.
 * 
 * @Author - Bhushan Kawadkar (bhushan_kawadkar@persistent.com)
 */

(function ( $ ) {
 
	var stepDetailsFieldMap;
	var radioButtonNames;
	
	var totalSteps = 0;
	var CONSTANTS = {};
	var wizardMessages = null;
	var clusterNameValid = false;
	var clusterNameField = "clusterName";
	//regEx allows alphanumeric, dot, space, comma, colon, round brackets, dash, underscore
	var VALID_CUSTOMER_NAME_REGEX = /^[a-z\d\-_\s:\.,()]+$/i;
    var isPopupOpen = false;
    var CPE_LOCATION = "cpeLocationName";
    
    //flag to know if steps are created or not
    var areStepsCreated = false;
    
	//contains Object store specific handling of select boxes
    var objStoreUserName = ['DATA_IN_USERNAME', 'SDFS_USERNAME'];
    var objStoreAPIKey = ['DATA_IN_APIKEY', 'SDFS_APIKEY'];
	var dataCenterSelectBox = ['DATA_IN_LOCATION', 'SDFS_LOCATION'];
	var containerSelectBox = ['DATA_IN_CONTAINER', 'SDFS_CONTAINER'];
	
	//Cluster parameters - nodes
	var dataNode = 'dataNode';
	var edgeNode = 'edgeNodes';
	
	//constants for Dedicated VPN Gateway
	var subnetHTML= '';
	var PRE_SHARED_SECRET_KEY_CHK = 'PRE_SHARED_SECRET_CHK';
	var PRE_SHARED_SECRET_KEY = 'PRE_SHARED_SECRET';
	var VPN_TUNNEL_FIELD_NAMES = ['IKE_ENCRYPTION_ALG', 'IKE_HASH_ALG', 'IKE_DH_GROUP', 'IKE_KEY_LIFETIME', 'ESP_ENCRYPTION_ALG', 'ESP_HASH_ALG', 'ESP_KEY_LIFETIME', 'AUTHENTICATION_MODE', PRE_SHARED_SECRET_KEY, 'ESP_PFS'];
	var SUBNET_KEY = 'CUST_SUBNETS';
	var VPN_IP_ADDRESS = 'VPN_IP_ADDRESS';
	var CUST_SUBNET_IP_ADDRESS_REGEX =/^(?!0)(?!.*\.$)((1?\d?\d|25[0-5]|2[0-4]\d)(\.|$)){4}$/;
	
	$.fn.passConstants = function(constants) {
		CONSTANTS = constants;
	};
	
	$.fn.passResourceMessages = function(resourceMessages) {
		wizardMessages = resourceMessages;
	};
	
	$.fn.destroySmartWizard = function() {
		var $parent = jQuery(this).parent();
		
		//store wizard html and remove existing
		jQuery(this).remove();
		var wizardHTML = '<div id="wizard" class="swMain"><ul class="stepDetails"></ul></div>';
		
		$parent.append(wizardHTML);
	};
	
	$.fn.initializeParameters = function() {
		 stepDetailsFieldMap = new Object();
		 radioButtonNames = new Object();
		 clusterNameValid = false;
		 isPopupOpen = false;
		 areStepsCreated = false;
		 subnetHTML = '';
	};
	
	$.fn.createStepsAndFetchStepDetails = function() {
		var $this = jQuery(this);
		
		$this.initializeParameters();
		
		$this.sendData($this, CONSTANTS.baseWizardUrl + '/stepDetails', 'createSteps', wizardMessages.stepDataErrorMsg);
		$this.sendData($this, CONSTANTS.baseWizardUrl + '/formFields', 'createStepDetails', wizardMessages.stepDetailsErrorMsg);
		
		return areStepsCreated;
	};
	
	$.fn.sendData = function($this, url, callbackFn, errorMsg, callerHeaders) {
		
		var headersValues = {'subscriber-id':CONSTANTS.subscriberId, 'api-key':CONSTANTS.apiKey, 'account-id' : CONSTANTS.accountId, "offering-id": CONSTANTS.offeringId};
		
		//merge caller headers
		if(!jQuery.isEmptyObject(callerHeaders))
		{
			headersValues = jQuery.extend(headersValues, callerHeaders);			
		}
		
		$.ajax({
        	type: 'GET',
			url: url,
			dataType: "json",
			contentType : "application/json",
			async: false,
			headers: headersValues,
			success: function(data)
	         {
				$this[callbackFn](data);
	         },
	        error: function(error)
	         {
	        	console.log('Error Occured in ajax : '+errorMsg);
	        	$this.showError(errorMsg);
	         },
	         statusCode: {
	        	 //401 - for Authentication failed
	             401: function(response){
	            	 var error = jQuery.parseJSON( response.responseText );
	            	 var popupParams = {title: wizardMessages.authFailedTitle, message: error.detail, showTitleImage:true, showYesBtn:true, yesBtnLabel: wizardMessages.okBtnText};
	            	 $this.showPopup(popupParams);
	             }
	         }
        });
	};
	
	$.fn.readValue = function(){
		var type = jQuery(this).attr('type');
		var value = '';
		if(type!=undefined && type=='checkbox')
		{
			if(jQuery(this).is(':checked'))
			{
				value = jQuery(this).val();
			}
		}
		else if(type!=undefined && type=='radio')
		{
			var radioName = jQuery(this).attr('name');
			
			value = jQuery('input:radio[name="' + radioName + '"]:checked').val();
			
			if(value!=undefined && value!="")
				value = value.replace("(recommended)", '');
		}
		else if(jQuery(this).isSpinner())
		{
			value = jQuery(this).spinner('value');
		}
		else if(jQuery(this).isIPAddressInput())
		{
			var validIPAddress = true; 
			jQuery(this).find('input.ipAddress').each(function(){
				var ipVal = jQuery(this).val().trim();
				if(ipVal=="")
				{
					validIPAddress = false;
					//break loop
					return false;
				}	
					
				value = value + parseInt(ipVal).toString() +".";
			});
			
			if(validIPAddress)
			{
				value = value.substring(0, value.length-1);
			}
			else
			{
				value = "";
			}
			
		}
		else
		{
			value = jQuery(this).val();	
		}
		
		value = (value==undefined)?"":value.toString().trim();
		
		return value;
	};
	
	$.fn.isSpinner = function()
	{
		return jQuery(this).hasClass('ui-spinner-input');
	};
	
	$.fn.isIPAddressInput = function()
	{
		return jQuery(this).hasClass('ipAddressDiv');
	};
	
    /*Populate data in the context provided*/
    $.fn.populateDataForSubmission = function(context){
    	var submission = {};
    	/******** Important **************/
    	//cluster name and description html element must have same name as specified below
    	submission.name = jQuery('#' + clusterNameField).val();
		submission.description = jQuery('#clusterDescription').val();
		
		//calculate cluster size
		var masterNodeValue = parseInt(jQuery('#masterNode').val()) || 0;
		var dataNodeValue = parseInt(jQuery('#'+dataNode).val()) || 0;
		var edgeNodeValue = parseInt(jQuery('#'+edgeNode).val()) || 0;
		submission.size = masterNodeValue + dataNodeValue + edgeNodeValue;
		
		submission.parameters = {};
		submission.vpnTunnel = {};
		submission.vpnTunnel.params = {};
		
		jQuery(this).populateDataParams(stepDetailsFieldMap, submission.parameters);
		
		submission.vpnTunnel.custIpAddr = jQuery('#' + VPN_IP_ADDRESS).readValue();
		
		jQuery(this).populateVPNTunnelParams(submission.parameters, submission.vpnTunnel.params);
		
		jQuery(this).populateCustomerSubnets(SUBNET_KEY, submission.vpnTunnel.params);
		
		//remove params if it is empty
		if(jQuery.isEmptyObject(submission.vpnTunnel.params))
		{
			delete submission.vpnTunnel;
		}
		else
		{
			submission.parameters["DEDICATED_GATEWAY"]="TRUE";	
		}
		
		context.submission = submission;
    };
    
    $.fn.populateDataParams = function(map, dataParams){
    	jQuery.each(map, function(key, value){
			var enteredValue = jQuery('#'+key).readValue();
			if(enteredValue!="")
			{
				//code to get radio button parameter value
				if(key.indexOf('radio-')!=-1)
				{
					key = jQuery('#'+key).attr('name');					
				}
				dataParams[key] = enteredValue;				
			}
		});
    };
    
    $.fn.populateVPNTunnelParams = function(map, dataParams){
    	jQuery.each(VPN_TUNNEL_FIELD_NAMES, function(index, value){
    		if(map[value])
    		{
    			dataParams[value] = map[value];
    			delete map[value];
    		}
		});
    };
    
    $.fn.populateCustomerSubnets = function(key, dataParams){
    	var subnetList = jQuery(this).getCustomerSubnetsList();
    	
    	if(subnetList != "")
    	{
    		dataParams[key] = subnetList;
    	}
    };
    
    $.fn.getCustomerSubnetsList = function(){
    	var subnetList = '';
    	var $subnetTables = jQuery(subnetHTML).find('.subnetListTable');
    	if($subnetTables.length > 0)
    	{
    		$subnetTables.each(function(){
    			jQuery(this).find('tr.subnet').each(function(){
    				subnetList = subnetList + jQuery(this).find('td:first').text() + ";";
    			});
    		});
    	}
    	
    	subnetList = subnetList.substring(0, subnetList.length-1);
    	
    	return subnetList;
    };
      
    /*Method to create steps for the supplied data*/
    $.fn.createSteps = function(stepData){
    	//check if stepData is Empty
    	if(jQuery.isEmptyObject(stepData))
    		return false;
    	
    	var stepHTML = '';
    	var stepDetailsHTML = '';
    	//set total steps
    	totalSteps = stepData.length;
    	var stepNumber = 1;
    	jQuery.each(stepData,function(key, value){
    		stepHTML = stepHTML + '<li>'+
									'<a href="#step-'+value.id+'">'+
										'<label class="stepNumber">'+stepNumber+'</label>'+
										'<span class="stepDesc">' + wizardMessages.stepLabel + ' ' +
										stepNumber+'<br />'+
											'<small>'+value.description+'</small>'+
										'</span>'+
								    '</a>'+
								  '</li>';
    		
    		stepNumber++;
    		
    		//add step details description
    		var headerFooter = value.formDescription.trim();
    		var headerText = '';
    		var footerText = '';
    		if(headerFooter!="")
			{
    			//replace offering name
    			headerFooter = headerFooter.replace("{name}", CONSTANTS.offeringName);
    			headerFooter = headerFooter.split("~");
    			headerText = '<p class="StepHeader">' + headerFooter[0] + '</p>';
    			if(headerFooter.length > 1)
    				footerText = '<p class="StepFooter">' + headerFooter[1] + '</p>';
			}
    		stepDetailsHTML = stepDetailsHTML + '<div id="step-' + value.id + '"><h2 class="StepTitle">' + value.formTitle + '</h2>' 
    		                                  + headerText + '<div class="fieldsDiv"><table class="fieldsTable"></table></div>' + footerText + '</div>';
		});
    	
    	var stepDetailsParentUL = jQuery(this).find('.stepDetails');
    	stepDetailsParentUL.empty().append(stepHTML);
    	
    	jQuery(this).append(stepDetailsHTML);
    	
    	//set steps created flag to true
    	areStepsCreated = true;
    };
    
    /*Method to create steps for the supplied data*/
    $.fn.createStepDetails = function(stepDetailsData){
    	
    	//check if stepDetailsData is Empty
    	if(jQuery.isEmptyObject(stepDetailsData))
    		return false;
    	
    	console.log('createStepDetails Called');
    	jQuery.each(stepDetailsData,function(key, value){
    		jQuery(this).createStepDetailForParent(value, jQuery('#step-' + value.stepId + ' .fieldsTable'));
		});
    	
    	//method to finalise step details by calling event handler, sorting, populating fields etc
    	jQuery(this).finaliseStepDetails();
    };
    
    $.fn.finaliseStepDetails = function(){
    	
    	//sort step details page content
    	jQuery(this).sortAllPageDetails();
    	
    	//remove extra radio button labels
    	jQuery(this).removeExtraRadioBtnLabels();
    	
    	//populate all select data
    	jQuery(this).populateFields('original');
    	
    	//bind event handler
    	jQuery(this).bindEventHandlers();
    	
    	//fire default events
    	jQuery(this).fireEventHandlers();
    };
    
    $.fn.fireEventHandlers = function(){
    	jQuery(this).fireCheckboxChangeEventHandler();
    };
    
    $.fn.bindEventHandlers = function(){
    	//bind change event handler
    	jQuery(this).bindSelectChangeEventHandler();
    	jQuery(this).bindCheckboxChangeEventHandler();
    	jQuery(this).bindRadioChangeEventHandler();
    	jQuery(this).bindIPAddressChangeEventHandler();
    	
    	//bind click event handler
    	jQuery(this).bindInfoIconClickEventHandler();
    	jQuery(this).bindLinkClickEventHandler();
    };
    
    $.fn.populateFields = function(className){
    	//populate all select data
    	jQuery(this).populateSelectData(className);
    	
    	//populate spinner data
    	jQuery(this).populateSpinnerData(className);
    };
    
    $.fn.createStepDetailForParent = function(detailsData, parent){
    	//set index
    	console.log('createStepDetailForParent Called');
		
		var stepFieldsHTML = jQuery(this).createStepDetailRow(detailsData, detailsData.orderIndex, "original");
		
		console.log('stepFieldsHTML : ' + stepFieldsHTML);
		var $field = jQuery(stepFieldsHTML);
		console.log('$field : ' + $field);
		var aa = $field.appendTo(parent);
		console.log('appended to parent : ' + aa);
    };
    
    $.fn.createStepDetailRow = function(detailsData, index, className){
    	var stepFieldsHTML = '<tr data-index="' + index + '" class="'+className+'">';
    	
    	stepFieldsHTML = stepFieldsHTML + jQuery(this).createFieldLabel(detailsData);
		
		stepFieldsHTML = stepFieldsHTML + jQuery(this).createFieldValue(detailsData);
		
		stepFieldsHTML = stepFieldsHTML + '</tr>';
		
		return stepFieldsHTML;
    };
    
    $.fn.createFieldLabel = function(detailsData){
    	if(detailsData.type=="link")
    		return '';
    	
    	var stepFieldLabelHTML = '<td class="fieldLabel">';
    	
		stepFieldLabelHTML = stepFieldLabelHTML + '<label for="' + detailsData.name + '" class="parameter_name">' +detailsData.label + ':';
		if(detailsData.isMandetory)
		{
			stepFieldLabelHTML = stepFieldLabelHTML + '<span class="ibm-required">*</span>';
		}
		
		stepFieldLabelHTML = stepFieldLabelHTML + '</label>';    		
		
		stepFieldLabelHTML = stepFieldLabelHTML + '</td>';
		
		return stepFieldLabelHTML;
    };
    
    $.fn.createFieldValue = function(detailsData){
    	var colSpan = "";
    	
    	if(detailsData.type=="link")
    		colSpan = 'colspan="2"';
    	
    	return '<td class="fieldValue" ' + colSpan + '>' + jQuery(this).getInputField(detailsData) + '</td>';
    };
    
    /*This method will display the error message specified*/
    $.fn.showError = function(msg){
        var $this = jQuery(this);
    	
    	//handle spinner
    	if($this.isSpinner())
    	{
    		var $this = jQuery(this).closest('.ui-spinner.ui-widget.ui-widget-content');
    	}
    	
		//clear error message
    	$this.nextAll('span.smartWizardError, br').remove();
    	
    	$this.parent().append('<br><span class="smartWizardError">'+msg+'</span>'); 
    };
    
    $.fn.getInputField = function(data){
    	var type = data.type;
    	if(type=="textarea")
		{
		  return jQuery(this).getInputTextArea(data);
		}
    	else if(type=="select")
    	{
    		return jQuery(this).getInputSelect(data);
    	}
    	else if(type=="checkbox")
		{
    		return jQuery(this).getInputCheckbox(data);
		}
    	else if(type=="radio")
		{
    		return jQuery(this).getInputRadio(data);
		}
    	else if(type=="text")
    	{
    		return jQuery(this).getInputText(data);
    	}
    	else if(type=="spinner")
    	{
    		return jQuery(this).getInputSpinner(data);
    	}
    	else if(type=="link")
    	{
    		return jQuery(this).getInputLink(data);
    	}
    	else if(type=="ipaddress")
    	{
    		return jQuery(this).getInputIpAddress(data);
    	}
    	else if(type=="label")
    	{
    		return data.value;
    	}
    	else
    	{
    		return '';
    	}
    };
    
    /********* Textarea : start ***********/
    $.fn.getInputTextArea = function(data){
    	stepDetailsFieldMap[data.name] = data.isMandetory;
    	var defaultValue = data.defaultValue==null?"":data.defaultValue;
    	var maxValue = data.maximumValue;
    	maxValue = (maxValue!=0)?maxValue:"250";
    	
    	return '<textarea rows="5" name="' + data.name + '" id="' + data.name + '" title="' + data.description 
	       + '" class="width-85 userInput" maxlength="' + maxValue + '">' + defaultValue + '</textarea>' + jQuery(this).getInfoIcon(data)
	       + "<br><p>Maximum characters allowed : " + maxValue + "</p>";
    };
    
    /********* Textarea : end ***********/
    
    /********* select box : start ***********/
    $.fn.getInputSelect = function(data){
    	stepDetailsFieldMap[data.name] = data.isMandetory;
    	var defaultValue = data.defaultValue==null?"":data.defaultValue;
    	var defaultOption = '';
    	
    	//for datacenter select boxes
    	var className = 'width-85 userInput';
    	var isDataCenterSelectBox = jQuery(this).isDataCenterSelectBox(data.name);
    	var isContainerSelectBox = jQuery(this).isContainerSelectBox(data.name);
    	
    	if(isDataCenterSelectBox)
    	{
    		className = className + ' dataCenterSelect';
    	}
    	else if(isContainerSelectBox)
    	{
    		className = className + ' containerSelect';
    	}
    	
    	if(data.value!=null && data.value!='')
    	{
    		var optionVal = (isContainerSelectBox || isDataCenterSelectBox)?"":data.value;
    		defaultOption = '<option value="' + optionVal + '">'+ data.value + '</option>';    		
    	}
    	
    	var disabled = '';
    	if(data.name == CPE_LOCATION)
    		disabled = 'disabled';
    	
    	return '<select name="' + data.name + '" id="' + data.name + '" value="' + defaultValue
    			+ '" data-url="' + data.attachedRESTEvent + '"'
    			+ '" title="' + data.description + '" class="' + className + '" '+ disabled +'>' 
    			+ defaultOption + '</select>' + jQuery(this).getInfoIcon(data);
    };
    
    $.fn.isDataCenterSelectBox = function(name){
    	return jQuery.inArray(name, dataCenterSelectBox)!=-1;
    };
    
    $.fn.isContainerSelectBox = function(name){
    	return jQuery.inArray(name, containerSelectBox)!=-1;
    };
    
    $.fn.bindSelectChangeEventHandler = function(){
    	jQuery(this).nonDCSelectChangeEventHandler();
    	jQuery(this).dcSelectChangeEventHandler();
    };
    
    $.fn.nonDCSelectChangeEventHandler = function(){
    	
    	jQuery(document).off('change', 'select.userInput:not(.dataCenterSelect)');
    	
    	//exclude data center select box
    	jQuery(document).on('change', 'select.userInput:not(.dataCenterSelect)', function(){
    		var $this = jQuery(this);
    		
    		$this.removeOnDemandStepDetails();
    		
    		var url = $this.find('option[value="'+$this.val()+'"]').data('url');
    		
    		if(url!=undefined && url!='')
    		{
    			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'createOnDemandStepDetails', wizardMessages.noDataFoundMsg);
    		}
    	});
    };
    
    $.fn.dcSelectChangeEventHandler = function(){
    	
    	jQuery(document).off('change', 'select.dataCenterSelect');
    	
    	//exclude data center select box
    	jQuery(document).on('change', 'select.dataCenterSelect', function(){
    		var $this = jQuery(this);
    		var $parent = $this.closest('table');
    		var $username = $parent.find('.objUserName');
    		var $apiKey = $parent.find('.objAPIKey');
    		var $container = $parent.find('.containerSelect');
    		
    		$container.find('option').not(':first').remove();
    		
    		//remove already existing error messages
    		$this.nextAll('.smartWizardError, br').remove();
    		$container.nextAll('.smartWizardError, br').remove();
    		
    		if($username.val()=='' || $apiKey.val()=='')
    		{
    			var msg = wizardMessages.usernameApiKeyMissingMsg;
    			if(isPopupOpen)
    			{
    				$this.showError(msg);
    			}
    			else
    			{
    				var popupParams = {title:wizardMessages.validationFailedTitle, message: msg, showTitleImage:true, showYesBtn:true, yesBtnLabel:wizardMessages.okBtnText};
        			$this.showPopup(popupParams);	
    			}
    			
    			//reset select box
    			$this.val('');
    			return false;
    		}
    		else if($this.val()!='')
    		{  			
    			var headers = $this.getAuthUserHeaders();
    			
    			var url = $container.data('url');
    			
    			$this.sendData($container, CONSTANTS.baseWizardUrl + '/' + url, 'createSelectOptions', wizardMessages.noDataFoundMsg, headers);
    		}
    		
    	});
    };
    
    $.fn.getAuthUserHeaders = function()
    {
    	var $this = jQuery(this);
    	var $parent = $this.closest('table');
    	var headers = {};
		
		var isPrivateURL = false;
		var $username = $parent.find('.objUserName');
		if($username.attr('name').indexOf("DATA_IN") >= 0)
		{
			isPrivateURL = true;
		}
		
		headers['authUser'] = $username.val();
		headers['authKey'] = $parent.find('.objAPIKey').val();
		headers['location'] = $parent.find('.dataCenterSelect').val();
		headers["isPrivateURL"] = isPrivateURL;
		
		return headers;
    };
    
    $.fn.populateSelectData = function(className){
    	jQuery('tr.' + className + ' select.userInput').each(function(){
    		var $this = jQuery(this);
    		if(!$this.isContainerSelectBox($this.attr('name')))
    		{
	    		var url = $this.data('url');
	        	//clear previous error message
	    		$this.siblings('span.smartWizardError').remove();
	    		if(url!='')
	    		{
	    			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'createSelectOptions', wizardMessages.noDataFoundMsg);    			
	    		}
    		}
    	});
    };
    
    $.fn.createSelectOptions = function(data){
    	var $this = jQuery(this);
    	
    	jQuery.each(data, function(index, value){
    		if(value!='')
    		{
    			value = value.toString().split("~");
        		var optionValue = value[0];
        		var optionUrl = '';
        		if(value.length > 1)
    			{
        			optionUrl = value[1];
    			}
        		var option = '<option data-url="' + optionUrl + '" value="' + optionValue + '">' + optionValue +'</option>';
        		$this.append(option);    			
    		}
    	});
    };
    /********* select box : end ***********/
    
    /********* checkbox : start ***********/
    $.fn.getInputCheckbox = function(data){
    	stepDetailsFieldMap[data.name] = data.isMandetory;
    	var checked = (data.value == data.defaultValue)? "checked":"";
    	return '<input name="' + data.name + '" id="' + data.name + '" value="' + data.value + '" title="' + data.description 
    	       + '" data-url="' + data.attachedRESTEvent + '" type="checkbox" class="userInput"  ' + checked + '>' + jQuery(this).getInfoIcon(data);
    };
    
    $.fn.fireCheckboxChangeEventHandler = function(){
    	jQuery('input.userInput[type="checkbox"]:checked').trigger("change");
    };
    
    $.fn.bindCheckboxChangeEventHandler = function(){
    	jQuery(document).off('change', 'input.userInput[type="checkbox"]');
    	
    	jQuery(document).on('change', 'input.userInput[type="checkbox"]', function(){
    		var $this = jQuery(this);
    		var name = $this.attr('name');
    		
    		if(name == PRE_SHARED_SECRET_KEY_CHK)
    		{
    			$this.handlePreSharedKey($this.is(':checked'));
    		}
    		else
    		{
    			$this.removeOnDemandStepDetails();
        		
        		if(jQuery(this).is(":checked"))
        		{
        			var url = $this.data('url');
            		
            		if(url!=undefined && url!='')
            		{
            			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'createOnDemandStepDetails', wizardMessages.noDataFoundMsg);
            		}    			
        		}
    		}
    	});
    };
    
    $.fn.handlePreSharedKey = function(checkedStatus){
    	var preSharedSecret = checkedStatus? jQuery(this).generatePreSharedSecret(45):"";
    	jQuery('#'+PRE_SHARED_SECRET_KEY).prop('disabled',checkedStatus).val(preSharedSecret);
    };
    
    $.fn.generatePreSharedSecret = function(secretKeyLength){
    	var secret = "";
        while(secret.length < secretKeyLength && secretKeyLength > 0){
            var randomCharacter = Math.random();
            secret+= (randomCharacter<0.1?Math.floor(randomCharacter*100):String.fromCharCode(Math.floor(randomCharacter*26) + (randomCharacter>0.5?97:65)));
        }
        return secret;
    };
    
    /********* checkbox : end ***********/
    
    /********* radio button : start ***********/
    $.fn.getInputRadio = function(data){
    	var minRadioIndex = parseInt(radioButtonNames[data.name]);
    	minRadioIndex = (minRadioIndex >= 0)? minRadioIndex : -1;
    	
    	radioButtonNames[data.name] = (minRadioIndex ==-1 || minRadioIndex >= data.orderIndex)?data.orderIndex : minRadioIndex;
    	
    	stepDetailsFieldMap["radio-"+data.name + data.orderIndex] = data.isMandetory;
    	
    	var radioHTML = '<input name="' + data.name + '" id="radio-' + data.name + data.orderIndex + '" value="' + data.value + '" title="' + data.description + '" data-url="' + data.attachedRESTEvent +'"';
    	if(data.value == data.defaultValue)
    		radioHTML = radioHTML + ' checked';
    	
    	radioHTML = radioHTML + ' type="radio" class="userInput">' + data.value;
    	
    	radioHTML = radioHTML + jQuery(this).getInfoIcon(data);
    	
    	return radioHTML;
    };
    
    $.fn.bindRadioChangeEventHandler = function(){
    	jQuery(document).off('change', 'input.userInput[type="radio"]');
    	
    	jQuery(document).on('change', 'input.userInput[type="radio"]', function(){
    		var $this = jQuery(this);
    		
    		$this.removeOnDemandStepDetails();
    		
    		var url = $this.data('url');
    		
    		if(url!=undefined && url!='')
    		{
    			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'createOnDemandStepDetails', wizardMessages.noDataFoundMsg);
    		}
    	});
    };
    /********* radio button : end ***********/
    
    /********* input text : start ***********/
    $.fn.getInputText = function(data){
    	var $this = jQuery(this);
    	stepDetailsFieldMap[data.name] = data.isMandetory;
    	var defaultValue = data.defaultValue==null?"":data.defaultValue;
    	var isObjectUserName = $this.isObjectStoreUserName(data.name);
    	var isObjectAPIKey = $this.isObjectStoreAPIKey(data.name);
    	var className = "userInput";
    	var disabled = "";
    	
    	//set default value from cookies if available for Object store username and API key
    	if(isObjectUserName || isObjectAPIKey)
    	{
    		var cookieValue = $this.getCookieValue(data.name);
    		if(cookieValue!='')
    		{
    			defaultValue = cookieValue;
    		}
    	}
    	
    	if(isObjectUserName)
		{
    		className = className + " width-85 objUserName";
		}
    	else if(isObjectAPIKey)
		{
    		className = className + " width-85 objAPIKey";
		}
    	
    	//handle master node box
    	else if(data.name=='masterNode')
    	{
    		className = className + " masterNode";
    		disabled = "disabled"; 
    	}
    	else
    	{
    		className = className + " width-85";
    	}
    	
    	if(data.attachedRESTEvent!=null && data.attachedRESTEvent!="")
    	{
    		jQuery(this).onFocustOutEventHandler(data);
    	}
    	
    	var maxValue = data.maximumValue;
    	maxValue = (maxValue!=0)?maxValue:"";
    	
    	return '<input name="' + data.name + '" id="' + data.name + '" value="' + defaultValue + '" title="' + data.description 
        + '"  type="text" class="' + className + '" ' + disabled + ' data-url="' + data.attachedRESTEvent + '" maxlength="' + maxValue + '">'  + jQuery(this).getInfoIcon(data);
    };
    
    $.fn.getCookieValue = function(name){
    	var value = jQuery.cookie(name);
    	return (value==undefined || value.trim() == '')?'':value;
    };
    
    $.fn.setCookieValue = function(name, valueNew){
    	jQuery.removeCookie(name);
		jQuery.cookie(name, valueNew, {expires: 10000, secure: true});
    };
    
    $.fn.onFocustOutEventHandler = function(data){
    	var name = data.name;
    	jQuery(document).off("focusout", '#' + name);
    	
    	jQuery(document).on("focusout", '#' + name, function(){
    		console.log('focusout event : ' + name);
    		var $this = jQuery(this);
    		//clear previous error message and status images
    		$this.siblings('span.smartWizardError').remove();
    		
    		if($this.isObjectStoreUserName(name) || $this.isObjectStoreAPIKey(name))
    		{
    			//set to cookies
    			$this.setCookieValue(name, $this.val());
    			$this.authenticateUserNameAndAPIKey();
    		}
    		else if(name == clusterNameField)
    		{
    			$this.siblings('.statusImage').remove();
        		
        		var clusterName = $this.val();
        		
        		if(clusterName!="")
        		{
        			if(!VALID_CUSTOMER_NAME_REGEX.test(clusterName))
        			{
        				jQuery(this).showError(wizardMessages.invalidClusterNameMsg);
        			}
        			else
        			{
        				var url = $this.data('url');
            			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url + '/' + clusterName, 'validateFieldAvailability', '');
        			}
        		}
    		}
    	});
    	
    };
    
    $.fn.validateFieldAvailability = function(notValid){
    	if(!notValid)
    	{
    		jQuery(this).addImage(CONSTANTS.contextPath + '/images/accepted.png', wizardMessages.validClusterNameMsg);
    		clusterNameValid = true;
    	}
    	else
		{
    		jQuery(this).addImage(CONSTANTS.contextPath + '/images/rejected.png', wizardMessages.clusterNameExistMsg);
    		jQuery(this).showError(wizardMessages.clusterNameExistMsg);
    		clusterNameValid = false;
		}
    };
    
    $.fn.addImage = function(imagePath, toolTip){
    	jQuery(this).after('<img class="statusImage" src="'+imagePath+'" title="' + toolTip + '"></img>');
    };
    
    $.fn.isObjectStoreUserName = function(name){
    	return jQuery.inArray(name, objStoreUserName)!=-1;
    };
    
    $.fn.isObjectStoreAPIKey = function(name){
    	return jQuery.inArray(name, objStoreAPIKey)!=-1;
    };
    
    $.fn.authenticateUserNameAndAPIKey = function(){
    	var $this = jQuery(this);
    	var $parent = $this.closest('table');
    	
    	var username = $parent.find('.objUserName').val();
    	var apikey = $parent.find('.objAPIKey').val();
    	var dataCenter = $parent.find('.dataCenterSelect').val();
    	
    	//clear error messages
    	$parent.find('span.smartWizardError').remove();
    	//empty container boxe
		$parent.find('select.containerSelect').find('option').not(':first').remove();
		
    	if(username!="" && apikey!="" && dataCenter!="")
        {
    		var headers = $this.getAuthUserHeaders();
    		var url = $this.data('url');
    		$this.sendData($parent.find('.containerSelect'), CONSTANTS.baseWizardUrl + '/' + url, 'populateDataCenterList', 'No Data found', headers);    	
        }
    };
    
    $.fn.populateDataCenterList = function(){
    	var $this = jQuery(this);
    	var $parent = $this.closest('table');
    	var $dataCenterSelectBox = $parent.find('select.dataCenterSelect');
    	$dataCenterSelectBox.change();
    };
    /********* input text : end ***********/
    
    /********* input Spinner : start ***********/
    $.fn.getInputSpinner = function(data){
    	stepDetailsFieldMap[data.name] = data.isMandetory;
    	var defaultValue = data.defaultValue==null?"":data.defaultValue;
    	
    	return '<input readonly name="' + data.name + '" id="' + data.name + '" value="' + defaultValue + '" title="' 
    	        + data.description + '" data-url="' + data.attachedRESTEvent + '" data-maxvalue="' + data.maximumValue + '" data-minvalue="' + data.minimumValue  + '"  type="text" class="userInput spinner">' + jQuery(this).getInfoIcon(data);
    };
    
    $.fn.populateSpinnerData = function(className){
    	jQuery('tr.' + className + ' input.spinner.userInput').each(function(){
    		var $this = jQuery(this);
    		var url = $this.data('url');
    		var maxValue = parseInt($this.data('maxvalue'));
    		var minValue = parseInt($this.data('minvalue'));
    		
        	//clear previous error message
    		$this.siblings('span.smartWizardError').remove();
    		if(url!=null && url!='' && url!=undefined)
    		{
    			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'createSpinner', wizardMessages.noSpinnerDataMsg);    			
    		}
    		else
    		{
    			$this.createSpinner([minValue, maxValue]);
    		}
    	});    	
    };
    
    $.fn.createSpinner = function(data){
    	//check if data empty then show 
    	if(data==undefined || jQuery.isEmptyObject(data))
    	{
    		data = [0];
    	}
    	
    	var maxVal = data[data.length-1];
    	jQuery(this).spinner({
    	    min : data[0],
    	    max : maxVal,
    	    showOn : 'both'
    	});
    	
    	if(jQuery(this).attr('name') == dataNode)
    	{
    		jQuery(this).bindDataNodeChangeEventHandler();
    	}
    };
    
    $.fn.bindDataNodeChangeEventHandler = function(){
    	jQuery(document).off('spinstop', '#'+dataNode);
    	
    	jQuery(document).on('spinstop', '#'+dataNode, function(){
    		if(jQuery('#'+edgeNode).length > 0)
    		{
    			var $edgeNode = jQuery('#'+edgeNode);
    			var value = jQuery(this).spinner( "value" );
        		if(value >= 1)
        		{
        			$edgeNode.spinner('destroy');
        			$edgeNode.spinner({
        	    	    min : 0,
        	    	    max : value,
        	    	    showOn : 'both'
        	    	});
        			$edgeNode.spinner('value',0);
        		}
        		else
        		{
        			$edgeNode.spinner('disable');
        		}
    		}
    	}); 
    };
    /********* input Spinner : end ***********/
    
    /********* input Link : start ***********/
    $.fn.getInputLink = function(data){
    	//create subnet table if link is SUBNET
    	if(data.name == SUBNET_KEY)
    	{
    		var $table = jQuery('<table class"subnetTable"></table>');
    		$table.sendData($table, CONSTANTS.baseWizardUrl + '/' + data.attachedRESTEvent, 'createSubnetInfoHTML', wizardMessages.noDataFoundMsg);
    	}
    	
    	return '<a name="' + data.name + '" id="' + data.name + '" title="' 
    	        + data.description + '" data-url="' + data.attachedRESTEvent + '" class="userInput link">' + data.label + '</a>' + jQuery(this).getInfoIcon(data);
    };
    
    $.fn.bindLinkClickEventHandler = function()
    {
        jQuery(document).off('click', 'a.link');
    	
    	jQuery(document).on('click', 'a.link', function(){
    		var $this = jQuery(this);
    		var url = $this.data('url');
        	//clear previous error message
    		$this.siblings('span.smartWizardError').remove();
    		if(url!='' && url!=null)
    		{
    			$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'showLinkDetails', wizardMessages.noDataFoundMsg);    			
    		}
    		else
    		{
    			$this.showLinkDetails(null);
    		}
    	}); 
    };
    
    var subnetPopupOkClickHandler = function(){
    	//clear previous errors
    	jQuery('.subnetTable').find('.smartWizardError').remove();
    	
    	var subnetCount = jQuery('.subnetTable').find('tr.subnet').length;
    	
    	if(subnetCount > 0)
    	{
    		subnetHTML = jQuery('.subnetTable');
    		jQuery(this).hidePopup({});
    	}
    	else
    	{
    		//show error
    		var errorMessage = '<tr class="smartWizardError"><td><span>' + wizardMessages.emptySubnetListMsg + '</span></td></tr>';
    		jQuery('.subnetTable').prepend(errorMessage);
    	}
    };
    
    $.fn.showLinkDetails = function(data)
    {
    	var name = jQuery(this).attr('name');
    	var popupParams = '';
    	var $this = jQuery('#dialogBox .content');
    	var $table = jQuery('<table></table>');
    	
    	$this.append($table);
    	
        if(name == "nodeConfiguration")
        {
        	popupParams = $this.showNodeConfigurationInfo(data, $table);
        }
        else if(name == SUBNET_KEY)
        {
        	popupParams = $this.showSubnetInfo(data, $table);
        }
        
    	$this.showPopup(popupParams);
    };
    
    $.fn.showNodeConfigurationInfo = function(data, $table){
    	$table.addClass("nodeCofigTable");
    	var count = 0;
    	jQuery.each(data,function(key, value){
    		if(count==0)
    		{
    			var header = "<tr class='nodeCofigTableHeader'><th>" + value.nodeSize + "</th><th>" + value.specification + "</th><th>" + value.dataBandwidth + "</th><th>" + value.usedFor + "</th></tr>";
    	    	$table.append(header);
    		}
    		else
    		{
    			var row = "<tr><td class='boldText'>" + value.nodeSize + "</td><td>" + jQuery(this).insertNewLine(value.specification) + "</td><td>" + jQuery(this).insertNewLine(value.dataBandwidth) + "</td><td>" + jQuery(this).insertNewLine(value.usedFor) + "</td></tr>";
    	    	$table.append(row);
    		}
    	   	count++;
    	});
    	 	
    	var nodeCalculationText = "<div>&nbsp;</div><div class='textLeft'>" + wizardMessages.clusterSizeCalculationText + "</div><div class='textLeft'>" + wizardMessages.smallDataNodeText + "</div><div class='textLeft'>" + wizardMessages.largeDataNodeText + "</div>";
    	$table.after(nodeCalculationText);
    	
    	var popupParams = {title:wizardMessages.nodeConfigurationTitle, message:'', showYesBtn:true, yesBtnLabel:wizardMessages.okBtnText};
    	return popupParams;
    };
    
    $.fn.showSubnetInfo = function(data, $table){
    	$table.addClass("subnetTable");
    	
    	$table.createSubnetInfoHTML(data);
    	
    	//bind add / remove subnet button click handler
    	$table.bindAddSubnetClickHandler();
    	$table.bindRemoveSubnetClickHandler();
    	
    	var popupParams = {title:wizardMessages.customerSubnetHeaderText, 
    			           message:'', 
    			           showYesBtn:true, 
    			           yesBtnLabel:wizardMessages.saveBtnLabel, 
    			           yesBtnCallback:subnetPopupOkClickHandler, 
    			           yesBtnPreventDefaultClose: true,
    			           showNoBtn:true, 
    			           noBtnLabel:wizardMessages.cancelBtnLabel};
    	return popupParams;
    };
    
    $.fn.createSubnetInfoHTML = function(data){
    	var $table = jQuery(this);
    	if(subnetHTML == '')
    	{
    		$table.append("<tr><th>" + wizardMessages.customerSubnetHeaderText + "</th></tr><tr>");
        	$table.append("<tr class='customerSubnet subnetList'><td><table class='customerSubnetTable subnetListTable'><tr><th>" + 
        			       wizardMessages.subnetHeaderText + "</th><th>" + 
        			       wizardMessages.actionHeaderText + "</th></tr></table></td></tr>");
        	
        	if(data != null)
        	{
        		jQuery.each(data,function(key, value){
            		var subnetInfo = $table.getSubnetRow(value.networkAddr, value.cidr);
            	    $table.find('.customerSubnetTable').append(subnetInfo);
            	});
        	}
        	 	
        	//add subnet fields
        	var addSubnetFields = "<tr><td><table>"+
        								"<tr>"
        									+"<td>" + wizardMessages.ipAddrFieldsText + "</td>" 
        									+"<td>&nbsp;" + wizardMessages.cidrFieldsText + "</td>"
        									+"<td></td>"
        								+"</tr>"
        								+"<tr>"
        									+"<td><span class='userInput ipAddressDiv subnetIPAddress'><input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'>.<input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'>.<input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'>.<input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'></span>&nbsp; / &nbsp; </td>"
        									+"<td>&nbsp;<input type='text' class='subnetCIDR' size='2'> </td>"
        									+"<td>&nbsp;<input type='button' value='<ADD_SUBNET_LABEL>' class='addSubnetBtn'></td>"
        								+"</tr>"
        							+"</table></td></tr><tr><td>&nbsp;</td></tr>";
        	
        	$table.find('.customerSubnet').after(addSubnetFields.replace("<ADD_SUBNET_LABEL>", wizardMessages.addBtnText));
        	
        	subnetHTML = $table; 
    	}
    	else
    	{
    		$table.html(subnetHTML.html());
    	}
    };
    
    $.fn.getSubnetRow = function(ipAddress , cidr){
    	return '<tr class="subnet"><td>' + ipAddress + '/'+ cidr + '</td><td><a class="removeSubnet">' + wizardMessages.removeSubnetText + '</a></td></tr>';
    };
    
    $.fn.bindAddSubnetClickHandler = function(){
        jQuery(document).off('click', 'input.addSubnetBtn');
    	
    	jQuery(document).on('click', 'input.addSubnetBtn', function(){
    		var $parentTr = jQuery(this).closest('tr');
    		var $subnetIPAddress = $parentTr.find('.subnetIPAddress');
    		var $subnetCIDR = $parentTr.find('.subnetCIDR');
    		var ipAddr = "";
    		 
    		validIPAddress = true;
    		$subnetIPAddress.find('input.ipAddress').each(function(){
				var ipVal = jQuery(this).val().trim();
				if(ipVal=="")
				{
					validIPAddress = false;
					//break loop
					return false;
				}	
					
				ipAddr = ipAddr + parseInt(ipVal).toString() +".";
			});
			
			if(validIPAddress)
			{
				ipAddr = ipAddr.substring(0, ipAddr.length-1);
			}
			else
			{
				ipAddr = "";
			}
			
    		var cidr = $subnetCIDR.val();
    		
    		//remove previous errors if any
    		$parentTr.closest('table.subnetTable').find('tr.smartWizardError').remove();
    		
    		//Validate IP address and CIDR
    		if(ipAddr=="" || cidr=="")
    		{
    			$parentTr.after("<tr class='smartWizardError'><td colspan='3'><span>" + wizardMessages.subnetReqFieldsMsg + "</span></td></tr>");
    			return false;
    		}
    		else if(!CUST_SUBNET_IP_ADDRESS_REGEX.test(ipAddr))
    		{
    			$parentTr.after("<tr class='smartWizardError'><td colspan='3'><span>" + wizardMessages.subnetIpAddrInvalidMsg + "</span></td></tr>");
    			return false;
    		}
    		else if(isNaN(cidr))
    		{
    			$parentTr.after("<tr class='smartWizardError'><td colspan='3'><span>" + wizardMessages.subnetCIDRInvalidMsg + "</span></td></tr>");
    			return false;
    		}
    		
    		var $parentTable = $parentTr.closest('table');
    		var $mainTr = $parentTable.closest('tr');
    		var $subnetTable = $mainTr.prevAll('tr.subnetList:first').find('.subnetListTable');
    		
    		$subnetTable.append($parentTr.getSubnetRow(ipAddr,cidr));
    		
    		//empty entered fields
    		$subnetIPAddress.find('input.ipAddress').val('');
    		$subnetCIDR.val('');
    	}); 
    };
    
    $.fn.bindRemoveSubnetClickHandler = function(){
        jQuery(document).off('click', 'a.removeSubnet');
    	
    	jQuery(document).on('click', 'a.removeSubnet', function(){
    		jQuery(this).closest('tr').remove();
    	}); 
    };
    
    $.fn.insertNewLine = function(text){
    	text = text.replace(/~/g, '<br/>');
    	return text;
    };
    
    /********* input Link : end ***********/
    
    /********* help icon : start ***********/
    $.fn.getInfoIcon = function(data){
    	
    	if(data.isHelpEnabled)
    	{
    		return '<img src="' + CONSTANTS.contextPath + '/images/infoIcon.png" class="infoImage" data-helptext="' + data.helpDescription + '" title="' + data.description + '"></img>';
    	}
    	else
    	{
    		return "";
    	}
    };
    
    $.fn.bindInfoIconClickEventHandler = function(){
    	jQuery(document).off('click', 'img.infoImage');
    	
    	jQuery(document).on('click', 'img.infoImage', function(){
    		var $this = jQuery(this);
    		var description = $this.data('helptext');
    		var popupParams = {title:wizardMessages.infoTitle, message: description, showTitleImage:true, showYesBtn:true, yesBtnLabel:wizardMessages.okBtnText};
			$this.showPopup(popupParams);
    	});    	
    };
    
    /********* help icon : start ***********/
    
    /********* input address : start ***********/
    $.fn.getInputIpAddress = function(data){
    	stepDetailsFieldMap[data.name] = data.isMandetory;
    	var defaultValue = data.defaultValue==null?"":data.defaultValue;

    	defaultValue = defaultValue.split(".");
    	var firstPart = defaultValue[0];
    	var secondPart = '';
    	var thirdPart = '';
    	var forthPart = '';
    	
    	if(defaultValue.length > 1)
    	{
    		secondPart = defaultValue[1];
    	}
    	
    	if(defaultValue.length > 2)
    	{
    		thirdPart = defaultValue[2];
    	}
    	
    	if(defaultValue.length > 3)
    	{
    		forthPart = defaultValue[3];
    	}
    	
    	return '<div id="' + data.name + '" name="' + data.name + '" class="userInput ipAddressDiv">'
    		   + '<input type="number" min="1" max="999" placeholder="123" class="userInput ipAddress" value="' + firstPart + '"> . '
    		   + '<input type="number" min="1" max="999" placeholder="123" class="userInput ipAddress" value="' + secondPart + '"> . '
    		   + '<input type="number" min="1" max="999" placeholder="123" class="userInput ipAddress" value="' + thirdPart + '"> . '
    		   + '<input type="number" min="1" max="999" placeholder="123" class="userInput ipAddress" value="' + forthPart + '">'
    	       + '</div>'
    	       + jQuery(this).getInfoIcon(data);
    };
    
    
    $.fn.bindIPAddressChangeEventHandler = function(){
        jQuery(document).off('keyup', 'input.ipAddress');
    	
    	jQuery(document).on('keyup', 'input.ipAddress', function(){
    		jQuery(this).val(this.value.match("[0-9]{1,3}"));
    		if(jQuery(this).val().length === 3)
	        {
    			jQuery(this).next('input.ipAddress').focus().select();
	        }
    	});
    };
    /********* input address : end ***********/
    
    /******** On Demand fields : start **********/
    $.fn.removeOnDemandStepDetails = function(){
    	var $this = jQuery(this);
    	var classNames = $this.attr('name');
    	//clear previous elements
    	$this.closest('tr').siblings('tr.'+classNames).each(function()
    	{
    		var $element = jQuery(this);
    		var elementId = $element.find('.userInput').attr('id');
    		
    		//delete map entry for the user input
    		delete stepDetailsFieldMap[elementId];
    		$element.remove();
    		
    		//remove subnet popup html
    		if(elementId == SUBNET_KEY)
    			subnetHTML = '';
    	});
    };
    
    $.fn.createOnDemandStepDetails = function(data){
    	var $this = jQuery(this);
    	var $parent = $this.closest('table.fieldsTable');
    	
    	var classNames = $this.closest('tr').attr('class') + " " + $this.attr('name');
    	
    	jQuery.each(data, function(key, value){
    		
    		//add class to identify the dynamically added elements
    		var stepFieldsHTML = jQuery(this).createStepDetailRow(value, value.orderIndex,classNames);
    		var $stepFieldElement = jQuery(stepFieldsHTML);
    		
    		$parent.append($stepFieldElement);
    	});
    	
		$parent.sortPageDetails();
		//remove extra radio button labels
		$parent.removeExtraRadioBtnLabels();
    	
    	//populate all select data
		$parent.populateFields($this.attr('name'));
    	
    	//bind event handler
		$parent.bindEventHandlers();
    };
    
    $.fn.populateOnDemandSelectData = function(){
    	var $this = jQuery(this);
    	if(!$this.isContainerSelectBox($this.attr('name')))
		{
			var url = $this.data('url');
	    	//clear previous error message
			$this.siblings('span.smartWizardError').remove();
			if(url!=undefined && url!='')
			{
				$this.sendData($this, CONSTANTS.baseWizardUrl + '/' + url, 'createSelectOptions', wizardMessages.noDataFoundMsg);    			
			}
		}
    };
    /******** On Demand fields : end **********/
    
    $.fn.sortAllPageDetails = function() {
    	jQuery("table.fieldsTable").each(function()
    	{
    		jQuery(this).sortPageDetails();
    	});
    };
    
    $.fn.sortPageDetails = function() {
		jQuery(this).find('tr').sortRows();
    };
    
    $.fn.sortRows = function() {
		jQuery(this).sort(function(a,b){
			var index1 = parseInt(jQuery(a).data('index'));
			var index2 = parseInt(jQuery(b).data('index'));
			return index1 - index2;
		}).each(function (_, paragraph) {
			  jQuery(paragraph).parent().append(paragraph);
		});
    };
    
    $.fn.removeExtraRadioBtnLabels = function(){
    	jQuery.each(radioButtonNames, function(key, value)
    	{
    		jQuery('input[name="'+key+'"]').closest('tr').each(function(){
    			var index = parseInt(jQuery(this).data('index'));
    			if(index > value)
    				jQuery(this).find('.fieldLabel').empty();
    		});
    	});
    };
    
    /**Method to update last **/
    $.fn.updateLastPage = function(){
    	//get the last step table and make it empty to append updated data
    	var $table = jQuery('div[id^=step-]:last').find('.fieldsTable').empty();
    	
    	//iterate through all steps but the last
		jQuery('div[id^=step-]:not(:last)').find('.fieldsTable tr').each(function(){
			var $cloneTR = jQuery(this).clone(true);
			var fieldLabelChildrenCount = $cloneTR.find('.fieldLabel').children().length;
			if(fieldLabelChildrenCount > 0)
			{
				var $input = $cloneTR.find('.userInput');
                
                //text area value not reading, workaround to read all input values
                var inputId = $input.attr('id');
                var text = jQuery('#'+inputId).readValue();
                	
                var isMandetory = stepDetailsFieldMap[inputId];
                
                //show field if mandetory otherwise show it if value provided
                if(isMandetory || (!isMandetory && text!=""))
                {
                	//remove error message and images if any
        			$cloneTR.find('.smartWizardError').remove();
        			$cloneTR.find('.statusImage').remove();
        			
        			//remove required asteric marks
        			$cloneTR.find('.ibm-required').remove();
        			
                	var $label = jQuery('<label>' + text + '</label>');
                	$cloneTR.find('.fieldValue').html($label);
                    
                    $table.append($cloneTR);                	
                }    				
			}
			else if($cloneTR.find('#' + SUBNET_KEY).length > 0)
			{
				$table.append('<tr><td class="fieldLabel"><label class="parameter_name">' + $cloneTR.find('#' + SUBNET_KEY).text() + '</label></td><td class="fieldValue">' + $cloneTR.getCustomerSubnetsList() + '</td></tr>');  
			}
		});
    };
    
    $.fn.checkIfLastPageAndUpdate = function(toStep){
    	if(totalSteps==toStep)
		{
		  jQuery(this).updateLastPage();
		}
    };
    
    $.fn.validateAllStep = function(){
    	jQuery(this).clearPreviousError();
    	var count = 0;
    	for(var i=1; i<=totalSteps; i++)
    	{
    		if(!jQuery(this).validateCurrentStep(i))
    			count++;
    	}
    	
    	return count==0;
    };
    
    $.fn.clearPreviousError = function()
    {
    	//remove already existing error messages
    	jQuery('.smartWizardError').remove();
    };
    
    $.fn.validateCurrentStep = function(currentStep){
    	
    	var stepIndexToValidate = parseInt(currentStep) - 1;
    	var $stepDiv = jQuery('div[id^="step-"]:eq(' + stepIndexToValidate + ')');
    	var valid =  $stepDiv.validateData();
   	
    	//check cluster name validation
    	if(jQuery('#' + clusterNameField).val()!='' && !clusterNameValid)
    	{
    		valid = false;
    		jQuery('#' + clusterNameField).showError(wizardMessages.clusterNameExistMsg);
    	}
    	
    	//validate customer subnet list
    	if($stepDiv.find('#'+SUBNET_KEY).length!=0)
    	{
    		if(subnetHTML=='' || jQuery(subnetHTML).find('.subnetListTable tr.subnet').length ==0)
    		{
    			valid = false;
    			jQuery('#' + SUBNET_KEY).showError(wizardMessages.subnetValidationMsg);
    		}
    	}
    	
    	return valid;
    };
    
    $.fn.validateData = function(){
    	var valid = true;
    	jQuery(this).find('.userInput').each(function(){
    		 if(!jQuery(this).validateField())
    			 valid = false;
    	});
    	
    	return valid;
    };
    
    $.fn.validateField = function(){
    	var valid = true;
		var key = jQuery(this).attr('id');
		var isMandetory = stepDetailsFieldMap[key];
		if(isMandetory)
		{
			var enteredValue = jQuery(this).readValue();
						  			  
			if(enteredValue.trim()=="")
			{
				var msg = wizardMessages.paramMissingMsg + '<br>';
				jQuery(this).showError(msg);
				valid = false;
			}
			else
			{
				//clear error message
				jQuery(this).nextAll('span.smartWizardError, br').remove();
			}
		}
    	
    	return valid;
    };
    
    $.fn.recalculateStepContainerHeight = function(){
    	
    	   var stepsCount = jQuery('.swMain ul.stepDetails li').length - 4;
    	   //if step count is more that 4 steps then increase height accordigly
    	   if(stepsCount > 0)
    	   {
    		   var heigthToAdd = jQuery('.swMain ul.stepDetails li:first').height() * stepsCount;
    		   
    		   var containerHeight = jQuery('.swMain .stepContainer').height();
    		   jQuery('.swMain .stepContainer').height(heigthToAdd + containerHeight);
    		   
    		   var fieldsDivHeight = jQuery('.swMain .fieldsDiv:first').height();
    		   jQuery('.swMain .fieldsDiv').height(heigthToAdd + fieldsDivHeight);
    		   
    		   var contentHeight = jQuery('.swMain .content:first').height();
    		   jQuery('.swMain .content').height(heigthToAdd + contentHeight);
    		   
    	   }
    };
    
    $.fn.showPopup = function(popupParams){
    	jQuery('#dialogBox .title .titleMsg').html(popupParams.title);
    	
    	if(popupParams.message!='')
    	{
    		jQuery('#dialogBox .content').html(popupParams.message);    		
    	}
    	
		//show error image
    	if(popupParams.showTitleImage)
    	{
    		//show image path with default image if titleImagePath not passed
    		jQuery('#dialogBox .title .titleImage').show();
    		if(popupParams.titleImagePath!=undefined && popupParams.titleImagePath!="")
    		{
    			jQuery('#dialogBox .title .titleImage').attr('src',popupParams.titleImagePath);
    		}
    	}
		
   		jQuery(this).setYesButton(popupParams);
    	
   		jQuery(this).setNoButton(popupParams);
    	
    	
    	jQuery('#dialogBox').addClass(popupParams.addDialogClass);
    	
    	jQuery('#dialogPane').show();
		jQuery('#dialogBox').show();
		
		isPopupOpen = true;
    };
    
    $.fn.setYesButton = function(popupParams){
    	var $yesBtn = jQuery('#dialogBox .close .yes');
    	
    	if(popupParams.showYesBtn)
    		$yesBtn.show();
    	else
    		$yesBtn.hide();
    	
    	$yesBtn.off('click');
    	
    	$yesBtn.on('click',function(){
			if(popupParams.yesBtnCallback!=undefined && popupParams.yesBtnCallback!='')
	    	{
				popupParams.yesBtnCallback();
	    	}
			
			if(popupParams.yesBtnPreventDefaultClose==undefined || !popupParams.yesBtnPreventDefaultClose)
				jQuery(this).hidePopup(popupParams);
		});
    	
    	if(popupParams.yesBtnLabel!=undefined && popupParams.yesBtnLabel!='')
    	{
    		$yesBtn.html(popupParams.yesBtnLabel);
    	}
    };
    
    $.fn.setNoButton = function(popupParams){
    	
    	var $noBtn = jQuery('#dialogBox .close .no');
    	
    	if(popupParams.showNoBtn)
    		$noBtn.show();
    	else
    		$noBtn.hide();
    	
    	$noBtn.off('click');
    	
    	$noBtn.on('click',function(){
			if(popupParams.noBtnCallback!=undefined && popupParams.noBtnCallback!='')
	    	{
				popupParams.noBtnCallback();
	    	}
			
			if(popupParams.noBtnPreventDefaultClose==undefined || !popupParams.noBtnPreventDefaultClose)
				jQuery(this).hidePopup(popupParams);
		});
    	
    	if(popupParams.noBtnLabel!=undefined && popupParams.noBtnLabel!='')
    	{
    		$noBtn.html(popupParams.noBtnLabel);
    	}
    };
    
    
    $.fn.hidePopup = function(popupParams){
    	isPopupOpen = false;
    	
    	jQuery('#dialogBox .title .titleMsg').html('');
    	jQuery('#dialogBox .content').empty();
    	
    	jQuery('#dialogBox .title .titleImage').hide();
    	jQuery('#dialogBox .title .titleImage').attr('src',CONSTANTS.contextPath + '/images/error.png');
    	
    	jQuery('.yes').html(wizardMessages.yesBtnText).hide();
    	jQuery('.no').html(wizardMessages.noBtnText).hide();
    	
    	jQuery('#dialogBox').removeClass(popupParams.addDialogClass);
    	
    	jQuery('#dialogPane').hide();
		jQuery('#dialogBox').hide();
    };
    
   $.fn.retrieveTransferData = function(apiPath){
    	
    	var $this = jQuery('#dialogBox .content');
    	$this.empty();
    	var $table = jQuery('<table class="fieldsTable"></table>');
    	
    	$this.append($table);
    	
    	$this.sendData($table, CONSTANTS.baseWizardUrl + apiPath, 'createStepDetailsForTransferData', 'Could not fetch transfer data');
    	
    	//add transfer status row status
    	var statusData = {name:'status', label:'Status', orderIndex: 100};
    	$table.append($table.createStepDetailRow(statusData, 100, 'transferStatus'));
    };
    
    /*Method to create steps for transfer data popup*/
    $.fn.createStepDetailsForTransferData = function(stepDetailsData){
    	
    	var $table = jQuery(this);
    	
    	//check if stepDetailsData is Empty
    	if(jQuery.isEmptyObject(stepDetailsData))
    		return false;
    	
    	console.log('createStepDetailsForTransferData Called');
    	jQuery.each(stepDetailsData,function(key, value){
    		$table.createStepDetailForParent(value, $table);
    		//assigne id to TR and hide "TO Softlayer" TRs
    		var fieldName = value.name;
    		if(fieldName=="DATA_IN_PATH" || fieldName=="DATA_IN_DESTINATION")
    		{
    			var transferDirection = (value.stepDetail.stepNumber==0)? "from":"to";
    			var $tr = $table.find("tr:last");
    			$tr.attr("id", fieldName + transferDirection);
    			if(value.stepDetail.stepNumber!=0)
    				$tr.hide();
    		}
		});
    	
    	//method to finalise step details by calling event handler, sorting, populating fields etc
    	$table.finaliseStepDetails();
    	
    	$table.bindTrasnferRadioChangeEventHandler();
    };
    
    $.fn.bindTrasnferRadioChangeEventHandler = function(){
    	jQuery(document).off('change', 'input.userInput[type="radio"]');
    	
    	jQuery(document).on('change', 'input.userInput[type="radio"]', function(){
    		var $this = jQuery(this);
    		var $table = $this.closest('table.fieldsTable');
    		
    		//hide all data in path and data in destination fields
    		$table.find('tr[id^="DATA_IN_PATH"]').hide();
        	$table.find('tr[id^="DATA_IN_DESTINATION"]').hide();
    		
    		var radioVal = $this.val();
    		
    		var transferDirection = "to";
    		if(radioVal.toLowerCase().indexOf("from")!=-1)
        	{
        		transferDirection = "from";
        	}	
        	
    		$table.find('tr[id="DATA_IN_PATH' + transferDirection + '"]').show();
        	$table.find('tr[id="DATA_IN_DESTINATION' + transferDirection + '"]').show();
    	});
    };
    
    $.fn.populateTransferData = function(){
    	var transferData = {};
    	var $table = jQuery('#dialogBox .content .fieldsTable');
    	
    	var transferDirection = $table.find('.userInput[name="transferDirection"]:checked').val();
    	
    	if(transferDirection.toLowerCase().indexOf("from")!=-1)
    	{
    		transferDirection = "from";
    	}	
    	else
    	{
    		transferDirection = "to";
    	}
    	
    	transferData['transferDirection']= transferDirection;
    	transferData['username']= $table.find('.userInput[name="DATA_IN_USERNAME"]').val();
    	transferData['apiKey']= $table.find('.userInput[name="DATA_IN_APIKEY"]').val();
    	transferData['location']= $table.find('.userInput[name="DATA_IN_LOCATION"]').val();
    	transferData['container']= $table.find('.userInput[name="DATA_IN_CONTAINER"]').val();
    	transferData['path']= $table.find('.userInput[name="DATA_IN_PATH"]:visible').val();
    	transferData['dest']= $table.find('.userInput[name="DATA_IN_DESTINATION"]:visible').val();
    	
    	console.log('transferData : '+transferData);
    	return transferData;
    };
    
    $.fn.validateTransferData = function(){
    	var valid = true;
    	jQuery(this).find('.userInput:visible').each(function(){
    		 if(!jQuery(this).validateField())
    			 valid = false;
    	});
    	
    	return valid;
    };
    
    $.fn.populateEditVPNParamsDetails = function(vpnDetails, $table){
    	var params = (jQuery.isEmptyObject(vpnDetails))? null : vpnDetails.params;
    	
    	$table.sendData($table, CONSTANTS.baseWizardUrl + '/vpnTunnelParamFields', 'createOnDemandStepDetails', wizardMessages.noDataFoundMsg);
    	
    	//set default values
    	if(!jQuery.isEmptyObject(vpnDetails))
    	  $table.setSelectedVPNParamsDetails(vpnDetails, $table);
    	
    	//remove existing subnet link
    	$table.find('[name="' + SUBNET_KEY + '"]').closest('tr').remove();
    	
    	//set subnet list
    	var $lastRow = $table.find('tr:last');
    	
    	var $parentCustomerSubnetTR = jQuery('<tr><td class="vpnfieldLabel"><label class="parameter_name">' + wizardMessages.customerSubnetText + '</td><td class="vpnfieldValue">' + $table.getCustomerSubnetHTML(params) + '</td></tr>');
    	$lastRow.after($parentCustomerSubnetTR);
    		
    	$parentCustomerSubnetTR.bindAddSubnetIPAddressClickHandler();
    	$parentCustomerSubnetTR.bindRemoveSubnetIPAddressClickHandler();
    };
    
    $.fn.setSelectedVPNParamsDetails = function(vpnDetails, $table){
    	var params = vpnDetails.params;
		//set default values
		$table.find('tr').each(function(){
			var $input = jQuery(this).find('.userInput');
			var type = $input.attr('type');
			var inputName = $input.attr('name');
			var value = params[inputName];
			
			if($input.hasClass('ipAddressDiv'))
			{
				value = vpnDetails.custIpAddr.split('.');
				$input.find('input.ipAddress').each(function(index){
					jQuery(this).val(value[index]);
				});
			}
			else if(type == 'radio' || type == 'checkbox')
			{
				if(value  == $input.val())
					$input.prop('checked', true);
			}
			else if($input.is("select"))
			{
				$input.find('option[value="' + value + '"]').prop('selected', true);
			}
			else
			{
				$input.val(value);
			}
		});
    };
	
    $.fn.getCustomerSubnetHTML = function(params){
    	var subnetIPAddressHTML = '<table class="subnetIpAddrCIDRTable">';
    	
    	if(!jQuery.isEmptyObject(params))
    	{
    		var subnetList = params[SUBNET_KEY].split(';');
        	jQuery.each(subnetList, function(index, value){
        		subnetIPAddressHTML += '<tr class="subnetIpAddrCIDRDiv" ><td><label class="subnetIpAddrCIDR">' + value + "</label></td><td><a href='#' class='removeSubnet'>remove</a></td></tr>";
    		});
    	}
    	
    	subnetIPAddressHTML += '</table></br>';
    	subnetIPAddressHTML += "<span class='subnetIPAddress ipAddressDiv'><input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'>.<input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'>.<input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'>.<input type='number' min='1' max='999' placeholder='123' class='userInput ipAddress'></span>";
    	subnetIPAddressHTML += ' / <input class="subnetCIDR" placeholder="CIDR" size="2"> &nbsp;<input type="button" class="addSubnetIPAddressBtn" value="' + wizardMessages.addBtnText +'"><br/>';
    	return subnetIPAddressHTML;
    };
    
    $.fn.isValidVPNTunnelParamsValues = function(){
    	var valid = true;
    	
    	valid = jQuery("table.fieldsTable").validateData();
    	
    	var $subnetTd = jQuery('input.addSubnetIPAddressBtn').closest('td');
    	$subnetTd.find('.smartWizardError').remove();
    	
    	if(jQuery('.fieldsTable .subnetIpAddrCIDR').length <=0)
    	{
    		valid = false;
    		$subnetTd.prepend("<span class='smartWizardError'>" + wizardMessages.emptySubnetListMsg + "</span>");
    	}
    	
    	return valid;
    };
    
    $.fn.getVPNTunnelParamsValues = function(){
    	vpnTunnel = {};
		vpnTunnel.params = {};
		
		jQuery(this).populateDataParams(stepDetailsFieldMap, vpnTunnel.params);
		
		vpnTunnel.custIpAddr = vpnTunnel.params[VPN_IP_ADDRESS];
		//delete unwanted params
		delete vpnTunnel.params[VPN_IP_ADDRESS];
		delete vpnTunnel.params[PRE_SHARED_SECRET_CHK];
		
		vpnTunnel.id = jQuery('#vpnTunnelId').readValue();
		vpnTunnel.cpeLocation = jQuery('#vpnTunnelCpeLocation').readValue();
		vpnTunnel.gatewayId = jQuery('#vpnTunnelGatewayId').readValue();
		
		//read subnet details
		var subnetList='';
		jQuery('.fieldsTable .subnetIpAddrCIDR').each(function(){
			subnetList += jQuery(this).text() + ";";
		});
		
		vpnTunnel.params[SUBNET_KEY] = subnetList;
		
		console.log("saveVPNTunnelParams : " + JSON.stringify(vpnTunnel));
        return vpnTunnel;
    };
    
    $.fn.bindAddSubnetIPAddressClickHandler = function(){
        jQuery(document).off('click', 'input.addSubnetIPAddressBtn');
    	
    	jQuery(document).on('click', 'input.addSubnetIPAddressBtn', function(){
    		var $subnetCIDR = jQuery(this).prev('.subnetCIDR');
    		var $subnetIPAddress = $subnetCIDR.prev('.subnetIPAddress');
    		
    		var ipAddr = "";
   		 
    		validIPAddress = true;
    		$subnetIPAddress.find('input.ipAddress').each(function(){
				var ipVal = jQuery(this).val().trim();
				if(ipVal=="")
				{
					validIPAddress = false;
					//break loop
					return false;
				}	
					
				ipAddr = ipAddr + parseInt(ipVal).toString() +".";
			});
			
			if(validIPAddress)
			{
				ipAddr = ipAddr.substring(0, ipAddr.length-1);
			}
			else
			{
				ipAddr = "";
			}
			
    		var cidr = $subnetCIDR.val();
    		
    		//remove previous errors if any
    		var $parentTd = $subnetIPAddress.closest('td');
    		$parentTd.find('.smartWizardError').remove();
    		
    		//Validate IP address and CIDR
    		if(ipAddr=="" || cidr=="")
    		{
    			jQuery(this).next('br').after("<span class='smartWizardError'>" + wizardMessages.subnetReqFieldsMsg + "</span>");
    			return false;
    		}
    		else if(!CUST_SUBNET_IP_ADDRESS_REGEX.test(ipAddr))
    		{
    			jQuery(this).next('br').after("<span class='smartWizardError'>" + wizardMessages.subnetIpAddrInvalidMsg + "</span>");
    			return false;
    		}
    		else if(isNaN(cidr))
    		{
    			jQuery(this).next('br').after("<span class='smartWizardError'>" + wizardMessages.subnetCIDRInvalidMsg + "</span>");
    			return false;
    		}
    		
    		
    		$parentTd.find('table.subnetIpAddrCIDRTable').append('<tr class="subnetIpAddrCIDRDiv" ><td><label class="subnetIpAddrCIDR">' + ipAddr + "/" + cidr + "</label></td><td><a href='#' class='removeSubnet'>remove</a></td></tr>");
    		
    		//empty entered fields
    		$subnetIPAddress.find('input.ipAddress').val('');
    		$subnetCIDR.val('');
    	}); 
    };
    
    $.fn.bindRemoveSubnetIPAddressClickHandler = function(){
        jQuery(document).off('click', 'a.removeSubnet');
    	
    	jQuery(document).on('click', 'a.removeSubnet', function(){
    		jQuery(this).closest('.subnetIpAddrCIDRDiv').remove();
    	}); 
    };
    
}( jQuery ));