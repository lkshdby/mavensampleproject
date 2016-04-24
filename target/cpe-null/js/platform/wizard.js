define.amd.jQuery = true;
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/on",
	"dijit/_WidgetBase", 
	"dijit/_TemplatedMixin",
	"dojo/i18n", 
	"dojo/i18n!resource/nls/wizard_messages",
	"dojo/text!./templates/wizard.html"
], function(declare, lang, on, _WidgetBase, _TemplatedMixin, i18n, wizardMessages, template) {
    var _cancel;
    var _submit;
    var context = {};
    var stepsCreated = false;
    
    /** Callback function to validate current step**/
    function validateCurrentStep(objs, onShowStepContext){
    	
    	if(!isNaN(onShowStepContext.fromStep) || !isNaN(onShowStepContext.toStep))
    	{
    		//check if this is not the first step rendering of wizard screen
        	//hence check if from and to step are not same.
        	if(onShowStepContext.fromStep != onShowStepContext.toStep)
        	{
        		jQuery(this).clearPreviousError();
        		
        		if(!jQuery(this).validateCurrentStep(onShowStepContext.fromStep))
        		{
        			showMissingFieldsPopup();
        			return false;
        		}    		
        	}    		
    	}

    	return true;
	}
    
    function showMissingFieldsPopup()
    {
		var popupParams = {title:wizardMessages.validationFailedTitle, message:wizardMessages.requiredFieldsMsg, showTitleImage:true, showYesBtn:true, yesBtnLabel:wizardMessages.okBtnText};
		jQuery('#wizard').showPopup(popupParams);
    }
    
	/** Callback function for Last step of the smart wizard ***/
	function onShowStepCallback(objs, onShowStepContext){
		jQuery(this).checkIfLastPageAndUpdate(onShowStepContext.toStep);
	}
	
	/** Callback function for Finish button of the smart wizard ***/
	function onFinishCallback(objs, onfinishContext){
		console.log('Finish has been called');
        //check if valid data
		if(jQuery(this).validateAllStep()){
        	//populate all required data in context
			jQuery(this).populateDataForSubmission(context);
			_submit(context.submission);
        }
		else
		{
			showMissingFieldsPopup();
		}
    }
	
	/** Callback function for Cancel button of the smart wizard ***/
	function onCancelCallback(objs, onCancelContext){
		_cancel();	
	}
	
	return declare([ _WidgetBase, _TemplatedMixin ], {
		templateString : template,
		
		postCreate: function() {
//			console.log("wizard created");
			this.wizardHeader.innerHTML = wizardMessages.wizardHeading;
		},
	
		pages: [],

		addPage: function(page) {
			this.pages.push(page);
			this.hidePage(page);
			page.placeAt(this.pageContainer);
		},
		
		updateUI: function(currentPage) {
			console.log("Showing wizard page \"" + this.pages[currentPage].name + "\"");
			this.applySmartWizard();
		},
		
		showPage: function(page) {
			page.domNode.style.display = "block";
		},
		
		hidePage: function(page) {
			page.domNode.style.display = "none";
		},
		
		isDummy: function() {
			return false;
		},
		submitWizard: function(wizardSubmissionContext){
			_submit(wizardSubmissionContext);
		},
		
		applySmartWizard: function() {
			console.log(" applySmartWizard : Applying dynamic smart wizard");
			
			//if quota is full, show error dialog
			if(icasConfig.accountQuota == icasConfig.currentUsage)
			{
    			var popupParams = {title:wizardMessages.accountQuotaTitle, message:wizardMessages.accountQuotaMsg, showTitleImage:true, showYesBtn:true, yesBtnLabel:wizardMessages.okBtnText};
    			jQuery('#wizard').showPopup(popupParams);
    			
    			stepsCreated = false;
    			return false;
			}
			
			stepsCreated = jQuery('#wizard').createStepsAndFetchStepDetails();
			//if steps created then show wizard screen otherwise show error pop-up
			if(stepsCreated)
			{
				jQuery('#wizard').smartWizard({transitionEffect: 'none', 
					                           onFinish: onFinishCallback, 
					                           onShowStep: onShowStepCallback, 
					                           onCancel: onCancelCallback, 
					                           beforeShowStep: validateCurrentStep, 
					                           keyNavigation: false, 
					                           labelNext: wizardMessages.nextBtnLabel,
										       labelPrevious: wizardMessages.prevBtnLabel,
										       labelFinish: wizardMessages.finishBtnLabel,
										       labelCancel: wizardMessages.cancelBtnLabel});				
			}
			else
			{				
    			var popupParams = {title:wizardMessages.addNewClusterFailedTitle, message:wizardMessages.addNewClusterFailedMsg, showTitleImage:true, showYesBtn:true, yesBtnLabel:wizardMessages.okBtnText};
    			jQuery('#wizard').showPopup(popupParams);
			}
		},
		
		launch: function(onShow, onCancel, onSubmit) {
			console.log("Launching the wizard.");
			if (this.isDummy()) {
				console.log("Dummy!");
				return;
			}

			
			var currentPage = 0;
			
			_cancel = lang.hitch(this, function() {
				console.log("Wizard cancel button clicked");
				onCancel();
			});
			
			_submit = lang.hitch(this, function(detail) {
				console.log(JSON.stringify(detail, null, 2));
				onSubmit(detail);
			});
			
			this.pages.forEach(lang.hitch(this, function(page){
				this.hidePage(page);
			}));
			this.updateUI(currentPage);
            
			//show wizard screen only if steps created
			if(stepsCreated)
			{
				var page = this.pages[currentPage];
				this.showPage(page);
				page.show(context);
				onShow();
				//recalculate stepcontainer height
				jQuery('#wizard').recalculateStepContainerHeight();	
			}
		}
	});
});
