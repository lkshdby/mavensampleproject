define([
	"dojo/_base/declare", 
	"dojo/_base/lang", 
	"dojo/_base/window",
	"dojo/dom", 
	"dojo/on", 
	"dojo/request", 
	"dojox/math/round", 

	"dijit/_WidgetBase", 
	"dijit/_TemplatedMixin", 
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Dialog", 
	"dijit/form/Button", 
	"dijit/form/TextBox", 

	"platform/urls",
	"platform/timer",
	"platform/uiutils",
	"platform/smart_wizard",	
	"offering/details",
	
	"dojo/i18n", 
	"dojo/i18n!resource/nls/wizard_messages",
	"dojo/i18n!resource/nls/dashboard_messages",
	
	"dojo/text!./templates/dashboard.html"
], function(
	declare, 
	lang,
	win,
	dom, 
	on, 
	request, 
	mathRound,
	
	_WidgetBase, 
	_TemplatedMixin, 
	_WidgetsInTemplateMixin,
	Dialog, 
	Button, 
	TextBox, 
	
	urls, 
	Timer,
	UIUtils,
	wizard, 
	offeringDetails,
	
	i18n, 
	wizardMessages,
	dashboardMessages,
	
	template
) {

	var saveVPNTunnelParamDetails = null;
	var addVPNTunnelParamDetails = null;
	
	var baseUrl = urls.cluster;
	
	var tunnelUrl = urls.tunnel;
	var gatewayUrl = urls.gateway;
	
	var NAME_COL = 0;
	var OWNER_COL = 1;
	var SIZE_COL = 2;
	var STATUS_COL = 3;
	var LOCATION_COL = 4;
	var CONSOLE_COL = 5;
	var ACTIONS_COL = 6;
	
	var GW_NAME = 0;
	var GW_STATUS = 1;
	var GW_SETTING = 2;
	var GW_ACTIONS = 3;
	
	var quotaLabel = dashboardMessages.quotaNodeText;
	
	var PAGE_IDLE_TIMEOUT = 10 * 60 / 15;
	
	var formatDateTime = UIUtils.formatDateTime;
	
	 var CONSTANTS = {};
	    
    CONSTANTS.baseWizardUrl = urls.wizard;
    CONSTANTS.offeringName = icasConfig.offeringName;
    CONSTANTS.offeringId = icasConfig.offeringId;
    CONSTANTS.plugin = icasConfig.plugin;
    CONSTANTS.contextPath = icasConfig.contextPath;
    CONSTANTS.subscriberId = icasConfig.subscriberId;
    CONSTANTS.apiKey = icasConfig.apiKey;
    CONSTANTS.accountId = icasConfig.accountId;
    isDGWActionAllowed = icasConfig.isDGWActionAllowed;
    
    jQuery('#dialogBox').passConstants(CONSTANTS);
    
    jQuery('#dialogBox').passResourceMessages(wizardMessages);
    
    /*function sanitizeString(s) {
		return s ? s : "";
	}
    
    function getDataTransferParams(details) {
		return {
			masterIp: sanitizeString(details['Private IP']),
			user: sanitizeString(details['Admin']),
			password: sanitizeString(details['Admin Password']) 
		};
	}
    
    function generateContent(details) {
		var consoleUrl = details['Console Url'];
		console.debug("console url: " + consoleUrl);
		if(consoleUrl!=undefined && consoleUrl!='')
		{
			var consoleAction = "javascript:window.open('" + consoleUrl + "');";
			console.debug("console action: " + consoleAction);
			
			var html = "<a class=\"ibm-external-link\" href=\"#\" onclick=\"" + consoleAction + "\">Launch</a>";
			return html;	
		}
		else
		{
			return "";
		}
	}
    
    function getColumnName() {
		return "Console";
	}*/
    
	/* ------------------------------------------------------------------------------------ 
	 * declaring the widget
	 * ------------------------------------------------------------------------------------
	 */
	return declare([ _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin ], {
		templateString : template,
		
		buildRendering : function() {
			this.inherited(arguments);
//			console.log("build rendering called");
			
			var contextUrl = this.contextUrl;
			var addClusterCallback = lang.hitch(this, this.addCluster);
			
			//set UI labels
			this.setUILabels();
			
			saveVPNTunnelParamDetails = this.saveVPNTunnelParamDetails;
			addVPNTunnelParamDetails = this.addVPNTunnelParamDetails;
			/*
			 *  If the description widget is not defined by the plugin, this following
			 *  code will generate a 404 in the log, which is harmless.
			 *  
			 *  TODO: figure out a way to test the existence of the widget JS before 
			 *  calling require.
			 */
			var descriptionNode = this.offeringDescription;
			require(["offering/description"], function(Description){
				new Description({contextUrl: contextUrl, addClusterCallback: addClusterCallback}, descriptionNode);
			});

			var footerNode = this.offeringFooter;
			require(["offering/footer"], function(Footer){
				new Footer({contextUrl: contextUrl}, footerNode);
			});
		},
		
		setUILabels: function(){
			//add new cluster and refresh labels
			this.addClusterLink.innerHTML = dashboardMessages.addNewClusterLinkText;
			this.refreshLink.innerHTML = dashboardMessages.refreshLinkText;
			
			//set cluster list headers

			var headRow = this.clusterList.tHead.rows.item(0);
			
			var nameHeadCell = headRow.cells.item(NAME_COL);
			nameHeadCell.innerHTML = dashboardMessages.clusterNameText;
			
			var ownerHeadCell = headRow.cells.item(OWNER_COL);
			ownerHeadCell.innerHTML = dashboardMessages.ownerText;
			
			var sizeHeadCell = headRow.cells.item(SIZE_COL);
			sizeHeadCell.innerHTML = dashboardMessages.sizeText;
			
			var statusHeadCell = headRow.cells.item(STATUS_COL);
			statusHeadCell.innerHTML = dashboardMessages.statusText;
			
			var locationHeadCell = headRow.cells.item(LOCATION_COL);
			locationHeadCell.innerHTML = dashboardMessages.locationText;
			
			var actionHeadCell = headRow.cells.item(ACTIONS_COL);
			actionHeadCell.innerHTML = dashboardMessages.actionText;
			
			//set gateway setting
			var gatewayHeadRow = this.gatewayList.tHead.rows.item(0);
			
			var gatewayNameHeadCell = gatewayHeadRow.cells.item(GW_NAME);
			gatewayNameHeadCell.innerHTML = dashboardMessages.gatewayNameText;
			
			var gatewayStatusHeadCell = gatewayHeadRow.cells.item(GW_STATUS);
			gatewayStatusHeadCell.innerHTML = dashboardMessages.gatewayStatusText;
			
			var gatewaySettingHeadCell = gatewayHeadRow.cells.item(GW_SETTING);
			gatewaySettingHeadCell.innerHTML = dashboardMessages.gatewaySettingText;

			if(isDGWActionAllowed == "true")
			{
				var gatewayActionsHeadCell = gatewayHeadRow.cells.item(GW_ACTIONS);
				gatewayActionsHeadCell.innerHTML = dashboardMessages.gatewayActionsText;
			}
			else
			{
				gatewayHeadRow.deleteCell(GW_ACTIONS);
				gatewayHeadRow.cells.item(GW_NAME).style.width = "50%";
			}
			
			this.gatewayDescription.innerHTML = dashboardMessages.gatewayDescriptionText;
			this.refreshGatewayLink.innerHTML = dashboardMessages.refreshLinkText;
		},
		
		hideDialogCloseButton: function(dialog) {
			dialog.closeButtonNode.style.display = "none";
		},
		
		postCreate: function() {
//			console.log("postCreate called");
		
			var quota = icasConfig.accountQuota;
			
			if (quota == 0) {
				this.quotaNode.style.display = "none";
				quotaLabel = quotaLabel.replace("{QUOTALIMIT}",dashboardMessages.unlimitedText);
			} else {
				this.quotaNode.style.display = "block";
				quotaLabel = quotaLabel.replace("{QUOTALIMIT}",quota);
			}
			
			this.quotaNode.innerHTML = quotaLabel;
			
			this.connect(this.addClusterLink, "onclick", "addCluster");
			this.connect(this.refreshLink, "onclick", "refresh");
			this.connect(this.refreshGatewayLink, "onclick", "refreshGateways");

			this.timer = new Timer();
			
			var checkIdleCount = lang.hitch(this, "checkIdleCount");
			var updateClusterList = lang.hitch(this, "updateClusterList");
			var showGatewayList = lang.hitch(this, "showGatewayList");
			
			this.timer.on("tick", function() {
				console.debug("Page refresh timer called");
				
				checkIdleCount();
				updateClusterList();
				showGatewayList();
			});
			
			this.updateClusterList();
			this.startRefresh();
			this.showGatewayList();
			
			var resetIdleCount = lang.hitch(this, "resetIdleCount");
			on(win.body(), "click", resetIdleCount);
			on(win.body(), "keypress", resetIdleCount);
			
			//search cluster
			this.connect(this.clusterFilterButton, "onclick", "searchCluster");
			this.clusterFilterButton.innerHTML = dashboardMessages.clusterFilterBtnText;
			this.clusterFilterLabel.innerHTML = dashboardMessages.clusterFilterLabelText;
			
			this.bindVpnDetailsClickEventHandler();
		},
		
		searchCluster: function(){
			var searchValue = this.clusterFilterInput.value;
			jQuery('#clusterList tr').show();
			if(searchValue!=undefined && searchValue!='')
			{
				jQuery('#clusterList tr').find('td:first').filter(function(){
					var val = jQuery(this).text();
					return val.toLowerCase().search(searchValue.toLowerCase())==-1;
				}).closest('tr').hide();
			}
		},
		/* idle timeout related constructs */
		idleCount: 0,
		
		checkIdleCount: function() {
			this.idleCount ++;
			if (this.idleCount >= PAGE_IDLE_TIMEOUT) {
				window.location = "logout.jsp";
			}
		},
		
		resetIdleCount: function() {
			console.log("reset idle count")
			this.idleCount = 0;
		},
		
		/* page refresh related constructs */
		timer: null,

		pauseRefresh: function() {
			console.log("Pausing periodic refresh.");
			this.timer.stop();
		},
		
		startRefresh: function() {
			console.log("Starting periodic refresh.");
			this.timer.start();
		},
		
		/* wizard related constructs */
		showWizard: function() {
			this.mainDashboard.style.display = "none";
			this.addClusterWizard.domNode.style.display = "block";
			this.pauseRefresh();
		},
		
		hideWizard: function() {
			//remove the existing wizard and recreate it
			jQuery('#wizard').destroySmartWizard();
			
			this.mainDashboard.style.display = "block";
			this.addClusterWizard.domNode.style.display = "none";
			this.startRefresh();
			this.refresh();
			this.refreshGateways();
		},
		
		addCluster: function() {
			console.log("addCluster called");
			
			var _show = lang.hitch(this, function() {
				this.showWizard();
			});
			var _cancel = lang.hitch(this, function(){
				this.hideWizard();
			});
			var _submit = lang.hitch(this, function(cluster){
				this.createCluster(cluster, lang.hitch(this, function(){
					this.hideWizard();
				}));
			});

			this.addClusterWizard.launch(_show, _cancel, _submit);
		},

		createCluster: function(cluster, callback) {
			console.log("Creating cluster. description: " + JSON.stringify(cluster, null, 2));
			var showErrorMessage = lang.hitch(this, "showErrorMessage");
			
			//show wait dialog
			var waitPopuParams = {title:dashboardMessages.pleaseWaitTitle, message:dashboardMessages.pleaseWaitClusterCreationMsg};
			jQuery('#wizard').showPopup(waitPopuParams);
			
			request.post(baseUrl, { 
				data : JSON.stringify(cluster),
				handleAs : "json",
				headers : {
					"Accept" : "application/json",
					"Content-Type" : "application/json",
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey
				},
			}).then(function(result){
				console.debug("POST returned: " + JSON.stringify(result));
				//hide wait popup
				jQuery('#wizard').hidePopup(waitPopuParams);
				callback();
			},function(result){
				console.error("POST failed! " + JSON.stringify(result, null, 2));
				//hide wait popup
				jQuery('#wizard').hidePopup(waitPopuParams);
				var error = result.response.data;
				var message = error == null ? dashboardMessages.unknownErrorMsg : error.detail;
				showErrorMessage(dashboardMessages.creatingClusterTitle, message, callback);
			});
		},
		
		showErrorMessage: function(action, msg, callback) {
			var popupParams = {title:action, message:msg, showTitleImage:true, showYesBtn:true, yesBtnCaller:this, yesBtnCallback:callback, yesBtnLabel:dashboardMessages.okBtnText};
			jQuery('#wizard').showPopup(popupParams);
		},

		refresh: function() {
			console.log("refresh called");
			this.updateClusterList();
		},
		
		refreshGateways: function() {
			console.log("refresh Gateways called");
			this.showGatewayList();
		},
		
		updateInProgress: false,
		
		updateClusterList: function() {
			console.log("about to update cluster list.");
			if (this.updateInProgress) {
				console.log("Another update is in progress. Aborting.");
				return;
			} else {
				this.updateInProgress = true;
			}

			var url = baseUrl;
			var clusterList = this.clusterList;
			var showDetails = lang.hitch(this, "showDetails");
			var updateClusterDetails = lang.hitch(this, "updateClusterDetails");
			var currentUsageNode = this.quotaNode;
			
//			console.log("Calling GET " + url);
			request(url, { 
				handleAs: "json",
				headers: {
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey
				}
			}).then(function(clusters){
//				console.debug(JSON.stringify(clusters, null, 2));
				
				console.debug("Repopulating cluster grid.");
				var headRow = clusterList.tHead.rows.item(0);
				var consoleHeadCell = headRow.cells.item(CONSOLE_COL);
				consoleHeadCell.innerHTML = offeringDetails.getColumnName();
				
				// populate the new list
				var tBody = clusterList.tBodies.item(0);
				while (tBody.rows.length > 0) {
					tBody.deleteRow(0);
				}
				
				var currentUsage = 0;
				clusters.forEach(function(cluster){
					currentUsage += cluster.size;
				});
				
				quotaLabel = quotaLabel.replace("{QUOTACURRENTUSAGE}", currentUsage);
				currentUsageNode.innerHTML = quotaLabel;
				
				// sort of a hack here. save the current usage to the global context object. 
				icasConfig.currentUsage = currentUsage;
				
				clusters.forEach(function(cluster){
					var row = tBody.insertRow(-1);
					row.setAttribute("id", cluster.id);
					
					
					var details = {};
					
					if (cluster.details != null) {
						for (var i = 0, len = cluster.details.length; i < len; i ++) {
							var attr = cluster.details[i];
							var name = attr.name;
							var value = attr.value;
							details[name] = value;
						}
					}
					
					row.setAttribute("cpeLocation", details["cpe.location.name"]);
					
					var nameCell = row.insertCell(-1);
					var nameId = "cluster_name_" + tBody.rows.length;
					nameCell.innerHTML = "<a href=\"#\" id=\"" + nameId + "\" title=\"" + dashboardMessages.clickDetailsText + "\">" + cluster.name + "</a>";
					on(dom.byId(nameId), "click", function(event) {
						showDetails(cluster.id, details["cpe.location.name"]);
					});
					
					var ownerCell = row.insertCell(-1);
					ownerCell.innerHTML = cluster.owner.name + (icasConfig.subscriberId == cluster.owner.id ? dashboardMessages.ownerNameSelfText : "");
					
					var sizeCell = row.insertCell(-1);
					sizeCell.innerHTML = cluster.size + (cluster.size > 1 ? dashboardMessages.nodesText : dashboardMessages.nodeText);

					var statusCell = row.insertCell(-1);
					statusCell.innerHTML = "<span class=\"ibm-question-link\">" + dashboardMessages.updateStatusText + "</span>";
/*
					var launchTimeCell = row.insertCell(-1);
					launchTimeCell.innerHTML = formatDateTime(cluster.launchTime);
					
					var expirationCell = row.insertCell(-1);
					if (cluster.terminateTime <= 0) {
						expirationCell.innerHTML = "No expiry";
					} else {
						expirationCell.innerHTML = formatDateTime(cluster.terminateTime);
					}
*/
					var locationCell = row.insertCell(-1);		
					var consoleCell = row.insertCell(-1);
					var actionsCell = row.insertCell(-1);
				});
				
				window.setTimeout(updateClusterDetails, 100, 0);
				
				console.debug("cluster grid updated.");
			});
		},

		updateClusterDetails: function(index) {
			var clusterList = this.clusterList;
			var updateClusterDetails = lang.hitch(this, "updateClusterDetails");
			var deleteCluster = lang.hitch(this, "deleteCluster");
			var editCluster = lang.hitch(this, "editCluster");
			var transferData = lang.hitch(this, "transferData");
			
			var tBody = clusterList.tBodies.item(0);
			if (index >= tBody.rows.length) {
				console.debug("End of list reached! Update is done");
				this.updateInProgress = false;
				return;
			}
			console.debug("Retrieving cluster details for row " + index + " ...");

			var row = tBody.rows.item(index);
			var clusterId = row.getAttribute("id");
			var cpeLocation = row.getAttribute("cpeLocation");
			console.debug("--> cluster id: " + clusterId);
			console.debug("--> cpeLocation: " + cpeLocation);
			
			var url = baseUrl + "/" + clusterId;
			request(url, { 
				handleAs: "json",
				headers: {
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey,
					"cpe-location": cpeLocation
				}
			}).then(function(cluster){
//				console.debug(JSON.stringify(cluster, null, 2));
				var details = {};
				
				if (cluster.details != null) {
					for (var i = 0, len = cluster.details.length; i < len; i ++) {
						var attr = cluster.details[i];
						var name = attr.name;
						var value = attr.value;
						details[name] = value;
					}
				}

				var ready = false;
				var canceling = false;
				var statusCell = row.cells.item(STATUS_COL); 

				if (cluster.currentStep == "INIT") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.INIT + "</span>";
				} else if (cluster.currentStep == "WAITING_FOR_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.WAITING_FOR_GATEWAY + "</span>";
				}else if (cluster.currentStep == "RESERVE_NETWORK") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.RESERVE_NETWORK + "</span>";
				} else if (cluster.currentStep == "CONFIGURE_MGMT_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.CONFIGURE_MGMT_GATEWAY + "</span>";
				} else if (cluster.currentStep == "TRUNK_CUST_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.TRUNK_CUST_GATEWAY + "</span>";
				}else if (cluster.currentStep == "CONFIGURE_CUST_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.CONFIGURE_CUST_GATEWAY + "</span>";
				} else if (cluster.currentStep == "TRUNK_HYPERVISORS") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.TRUNK_HYPERVISORS + "</span>";
				} else if (cluster.currentStep == "PROVISION_NODES") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.PROVISION_NODES + "</span>";
				} else if (cluster.currentStep == "CANCEL_CLUSTER") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.cancellingText + "</span>";
					canceling = true;
				} else if (cluster.currentStep == "UNTRUNK_HYPERVISORS") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.UNTRUNK_HYPERVISORS + "</span>";
				} else if (cluster.currentStep == "UNCONFIGURE_CUST_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.UNCONFIGURE_CUST_GATEWAY + "</span>";
				} else if (cluster.currentStep == "UNTRUNK_CUST_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.UNTRUNK_CUST_GATEWAY + "</span>";
				} else if (cluster.currentStep == "UNCONFIGURE_MGMT_GATEWAY") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.UNCONFIGURE_MGMT_GATEWAY + "</span>";
				} else if (cluster.currentStep == "UNRESERVE_NETWORK") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.UNRESERVE_NETWORK + "</span>";
				} else if (cluster.currentStep == "REMOVE_RECORDS") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.REMOVE_RECORDS + "</span>";
				} else if (details.State == "ACTIVE") {
					if (details.ApplicationAction == "Provision") {
						statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.provisioningText + "</span>";
					} else if (details.ApplicationAction == "Cancel") {
						statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.cancellingText + "</span>";
						canceling = true;
					} else if (details.ApplicationAction == "Flex Up") {
						statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.flexupText + "</span>";
					} else if (details.ApplicationAction == "") {
						ready = true;
						statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.readyText + "</span>";
					} else {
						var appActionTitle = dashboardMessages.failedAppActionTitleText.replace("<<APPACTION>>",details.ApplicationAction);
						statusCell.innerHTML = "<span class=\"ibm-error-link\" title=\"" + appActionTitle + "\">" + dashboardMessages.failedText + "</span>";
					}
				} else if (details.State == "APPROVED") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.approvingText + "</span>";
				} else if (details.State == "CANCELED") {
					statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.cancelledText + "</span>";
					canceling = true;
				} else if (details.State == "EXPIRED") {
					statusCell.innerHTML = "<span class=\"ibm-error-link\" title=\"" + dashboardMessages.expiredTitleText + "\">" + dashboardMessages.expiredText + "</span>";
				} else {
					var stateTitle = dashboardMessages.failedStateTitleText.replace("{STATE}",details.State);
					statusCell.innerHTML = "<span class=\"ibm-error-link\" title=\"" + stateTitle + "\">" + dashboardMessages.failedText + "</span>";
				}
				
				var cpeLocationName = details["cpe.location.name"];

				if (ready) {
					var consoleCell = row.cells.item(CONSOLE_COL);
					consoleCell.innerHTML = offeringDetails.generateContent(details);
				}
				
				var actionsCell = row.cells.item(ACTIONS_COL);
				var cancelId = "cluster_" + cluster.id + "_delete_button";
				var editId = "cluster_" + cluster.id + "_edit_button";
				var transferId = "cluster_" + cluster.id + "_transfer_button";
				var showDelete = !canceling;
				var showEdit = ready && !offeringDetails.disableEdit;
				var showTransfer = ready && offeringDetails.canTransferData;
				/*var showEdit = ready && !details.disableEdit;
				
				var canTransferData = (details.canTransferData!=undefined)?details.canTransferData : true;
				var showTransfer = ready && canTransferData;*/
				
				var actionsHtml = "";
				actionsHtml += "<span class=\"ibm-cancel-link " + (showDelete ? "enabled" : "disabled") +  "\" id=\"" + cancelId + "\" title=\"" + dashboardMessages.deleteBtnTitle + "\">&nbsp;</span> ";
				actionsHtml += "<span class=\"ibm-signin-link " + (showEdit ? "enabled" : "disabled") +  "\" id=\"" + editId + "\" title=\"" + dashboardMessages.editBtnTitle + "\">&nbsp;</span> ";
				actionsHtml += "<span class=\"ibm-upload-link " + (showTransfer ? "enabled" : "disabled") +  "\" id=\"" + transferId + "\" title=\"" + dashboardMessages.transferDataBtnTitle + "\">&nbsp;</span>";
				
				actionsCell.innerHTML = actionsHtml;
				
				var locationCell = row.cells.item(LOCATION_COL);
				locationCell.innerHTML = cpeLocationName;

				if (showDelete) {
					var deleteButton = dom.byId(cancelId);
					on(deleteButton, "click", function(){
						deleteCluster(cluster.id, cpeLocationName);
					});
					deleteButton.tabIndex = 0;
					on(deleteButton, "keypress", function(event){
						if (event.keyCode == 13) {
							deleteCluster(cluster.id, cpeLocationName);
						}
					});
				}
				if (showEdit) {
					var editButton = dom.byId(editId);
					on(editButton, "click", function(){
						editCluster(cluster.id, cluster.size, cpeLocationName);
					});
					editButton.tabIndex = 0;
					on(editButton, "keypress", function(event){
						if (event.keyCode == 13) {
							editCluster(cluster.id, cluster.size, cpeLocationName);
						}
					});
				}
				if (showTransfer) {
					var transferButton = dom.byId(transferId);
					on(transferButton, "click", function(){
						transferData(cluster.id, cpeLocationName);
					});
					transferButton.tabIndex = 0;
					on(transferButton, "keypress", function(event){
						if (event.keyCode == 13) {
							transferData(cluster.id, cpeLocationName);
						}
					});
				}
				
				console.debug("Updated row " + index + ", cluster id: " + clusterId);
				window.setTimeout(updateClusterDetails, 10, index + 1);
			});
		},
		
		bindVpnDetailsClickEventHandler: function() {
			this.showVPNDetails();
			this.editVPNDetails();
		},
		
		editVPNDetails: function() {
			jQuery(document).off('click', 'a.editVPNDetails');
		
			jQuery(document).on('click', 'a.editVPNDetails', function(){
	    		var $this = jQuery(this);
	    		var id = $this.data("vpndetailid");
	    		
	    		jQuery('#dialogBox').initializeParameters();
		    	var $popupContent = jQuery('#dialogBox .content');
		    	
		    	var $vpnDetailsDiv = jQuery('<div class="vpnDetailsDiv"></div>');
		    	$popupContent.append($vpnDetailsDiv);
		    	
		    	var $table = jQuery('<table class="fieldsTable"></table>');
		    	$vpnDetailsDiv.append($table);
		    	
		    	var saveCallbackMethod = saveVPNTunnelParamDetails;
		    	var titleText = dashboardMessages.editVPNDetailsTitle;
		    	
		    	if(id == "" || id == null || id == undefined)
	    		{
		    		saveCallbackMethod = addVPNTunnelParamDetails;
		    		titleText = dashboardMessages.addVPNDetailsTitle;
		    		
		    		$table.populateEditVPNParamsDetails(null, $table);
	    		}
	    		else
	    		{
					var vpnTunnelUpdateUrl = tunnelUrl + "/" + id;
					var headersValues = {'subscriber-id':CONSTANTS.subscriberId, 'api-key':CONSTANTS.apiKey};
					
					$.ajax({
			        	type: 'GET',
						url: vpnTunnelUpdateUrl,
						dataType: "json",
						contentType : "application/json",
						async: false,
						headers: headersValues,
						success: function(data)
				         {
						    	$table.populateEditVPNParamsDetails(data, $table);
						    	
						    	//set vpntunnel id
						    	$table.append("<tr style='display:none;'><td></td><td><input id='vpnTunnelId' value='" + id + "'><td></tr>");
				         },
				        error: function(error)
				         {
				        	console.log('Error Occured in ajax : '+errorMsg);
				        	$popupContent.append("<div>" + dashboardMessages.failedVPNParamsStatusText + "</div>");
				         }
					});
	    		}
		    	
		    	var $parentTr = $this.closest('tr');
		    	var cpeLocation = $parentTr.attr('cpelocation');
		    	$table.append("<tr style='display:none;'><td></td><td><input id='vpnTunnelCpeLocation' value='" + cpeLocation + "'><td></tr>");
		    	
		    	var gatewayId = $parentTr.attr('gatewayId');
		    	$table.append("<tr style='display:none;'><td></td><td><input id='vpnTunnelGatewayId' value='" + gatewayId + "'><td></tr>");
		    	
				var popupParams = {title:titleText, message:'', showYesBtn:true, yesBtnLabel:dashboardMessages.saveBtnLabel, yesBtnCallback: saveCallbackMethod, yesBtnPreventDefaultClose: true,showNoBtn:true, noBtnLabel:dashboardMessages.cancelBtnLabel};
				jQuery('#dialogBox').showPopup(popupParams);
			});
			
		},
	    	
		showVPNDetails: function() {
			
			jQuery(document).off('click', 'a.vpnDetails');
	    	
	    	jQuery(document).on('click', 'a.vpnDetails', function(){
	    		var $this = jQuery(this);
	    		var id = $this.data("vpndetailid");
	    		
	    		jQuery('#dialogBox').initializeParameters();
	    		var $popupContent = jQuery('#dialogBox .content');
	    		
	    		if(id == "" || id == null || id == undefined)
	    		{
	    			$popupContent.append("<div>" + dashboardMessages.vpnParamsEmptyText + "</div>");
	    		}
	    		else
	    		{
	    			var vpnTunnelUpdateUrl = tunnelUrl + "/" + id;
					var headersValues = {'subscriber-id':CONSTANTS.subscriberId, 'api-key':CONSTANTS.apiKey};
					
					$.ajax({
			        	type: 'GET',
						url: vpnTunnelUpdateUrl,
						dataType: "json",
						contentType : "application/json",
						async: false,
						headers: headersValues,
						success: function(data)
				         {
							var vpnDetails = data;
							var params = vpnDetails.params;
							
							var tableRowStart = "<tr><td class='vpnfieldLabel'><label class='parameter_name'>";
							var tableRowEnd = "</td></tr>";
							var tableRowMiddle = ":</label></td><td class='vpnfieldValue'>";
							
				    		var html = "<div class='vpnDetailsDiv'><table class='fieldsTable'>"
				    			+ tableRowStart + dashboardMessages.cloudIpAddrText + tableRowMiddle + vpnDetails.cloudIpAddr + tableRowEnd 
				    			+ tableRowStart + dashboardMessages.custIpText + tableRowMiddle + vpnDetails.custIpAddr + tableRowEnd
				    			+ tableRowStart + dashboardMessages.keyExchangeText + tableRowMiddle + params[dashboardMessages.ikeEncryptAlgoKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.dataIntegrityText + tableRowMiddle + params[dashboardMessages.ikeHashAlgoKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.diffeHellmanGrpIKESAText + tableRowMiddle + params[dashboardMessages.ikeDHGrpKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.authMethodText + tableRowMiddle + params[dashboardMessages.authModeKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.lifeTimeIKESAText + tableRowMiddle + params[dashboardMessages.ikeKeyLifeTimeKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.encryptAlgoESPText + tableRowMiddle + params[dashboardMessages.espEncryptAlgoKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.hashAlgoESPText + tableRowMiddle + params[dashboardMessages.espHashAlgoKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.espPFSText + tableRowMiddle + params[dashboardMessages.espPFSKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.keyLifeTimeESPText + tableRowMiddle + params[dashboardMessages.espKeyLifeTimeKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.preSharedSecretText + tableRowMiddle + params[dashboardMessages.preSharedSecretKeyText] + tableRowEnd
				    			+ tableRowStart + dashboardMessages.clusterSubnetText + tableRowMiddle;
			                    
							jQuery.each(vpnDetails.cloudSubnets, function(index, value){
								html += value.networkAddr + "/" + value.cidr + "<br/>";
							});
							
							html += tableRowStart + dashboardMessages.customerSubnetText + tableRowMiddle;
			                
							jQuery.each(params[dashboardMessages.custSubnetKeyText].split(';'), function(index, value){
								html += value + "<br/>";
							});
							
							html += tableRowEnd; 
							html +=  tableRowStart + dashboardMessages.connectionStatusText + tableRowMiddle + vpnDetails.status + tableRowEnd + "</table></div>";
							
							$popupContent.html(html);
				         },
				        error: function(error)
				         {
				        	console.log('Error Occured in ajax : '+errorMsg);
				        	$popupContent.append("<div>" + dashboardMessages.failedVPNParamsStatusText + "</div>");
				         }
					});
	    		}
				
				var popupParams = {title:dashboardMessages.vpnDetailsTitle, message:'', showNoBtn:true, noBtnLabel:dashboardMessages.closeBtnText};
				jQuery('#dialogBox').showPopup(popupParams);
	    	});
		},
		
		/* ------------------------------------------------------------------------------------ 
		 * Details section related data structures and functions
		 * ------------------------------------------------------------------------------------
		 */
		showDetails: function(clusterId, cpeLocationName) {

			var url = baseUrl + "/" + clusterId;
			request(url, { 
				handleAs: "json",
				headers: {
					"cpe-location": cpeLocationName,
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey
				}
			}).then(function(cluster){
				var details = {};
				
				if (cluster.details != null) {
					for (var i = 0, len = cluster.details.length; i < len; i ++) {
						var attr = cluster.details[i];
						var name = attr.name;
						var value = attr.value;
						details[name] = value;
					}
				}

				var expirationTime;
				if (cluster.terminateTime <= 0) {
					expirationTime = dashboardMessages.noExpirationText;
				} else {
					expirationTime = formatDateTime(cluster.terminateTime);
				}

				var html = "<p><b>" + dashboardMessages.nameText + ":</b> " + cluster.name + "</p>"
					+ "<p><b>" + dashboardMessages.descriptionText + ":</b> " + cluster.description + "</p>" 
					+ "<p><b>" + dashboardMessages.launchTimeText + ":</b> " + formatDateTime(cluster.launchTime) + "</p>" 
					+ "<p><b>" + dashboardMessages.expirationTimeText + ":</b> " + expirationTime + "</p>" 
					+ "<p><b>" + dashboardMessages.clusterIdText + ":</b> " + cluster.clusterId + "</p>";
				
                    /*if(!jQuery.isEmptyObject(details))
                    {
                    	html += '<hr>';
                    	jQuery.each(details, function(key, value){
                    		value = sanitizeString(value);
                    		//if length is greater than 50 show textarea
                    		if(value.length > 50)
                    			value = "<textarea cols='70' rows='8'>"+value+"</textarea>";
    						html += '<p><b>'+key+':</b> '+value+'</p>';						
    					});                    	
                    }*/
				
				if (offeringDetails.generateDetails) {
					html += "<hr>" + offeringDetails.generateDetails(details);
				}
				
				console.log(html);
				
				jQuery('#dialogBox').initializeParameters();
				jQuery('#dialogBox .content').html(html);
				var popupParams = {title:dashboardMessages.clusterDetailsTitle, message:'', showNoBtn:true, noBtnLabel:dashboardMessages.closeBtnText};
				jQuery('#dialogBox').showPopup(popupParams);
			});
		},
		
		deleteCluster: function(id, cpeLocationName) {
			console.log("Delete cluster called. id: " + id);
			var updateClusterList = lang.hitch(this, "updateClusterList");
			
			var deletCluster = function(){
				console.log("Deleting cluster " + id + " and with location " + cpeLocationName);
				var deleteUrl = baseUrl + "/" + id;
				request.del(deleteUrl, {
					handleAs: "json",
					headers: {
						"cpe-location": cpeLocationName,
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					}
				}).then(function(result){
					console.debug("DELETE returned: " + JSON.stringify(result));
					window.setTimeout(updateClusterList, 500);
				});
			};
			 
			jQuery('#dialogBox').initializeParameters();
			var popupParams = {title:dashboardMessages.deleteClusterTitle, message:dashboardMessages.deleteClusterConfirmationMsg, showYesBtn:true, yesBtnCallback:deletCluster, yesBtnLabel:dashboardMessages.deleteBtnText, showNoBtn:true, noBtnLabel:dashboardMessages.closeBtnText};
			jQuery('#dialogBox').showPopup(popupParams);
		},
		
		editCluster: function(id, currentSize, cpeLocationName) {
			console.log("Edit cluster called. id: " + id);
			var updateClusterList = lang.hitch(this, "updateClusterList");
			var showErrorMessage = lang.hitch(this, "showErrorMessage");

			var editCluster = function() {
				var $additionaNodes = jQuery('#dialogBox #additionalNodes');
				var deltaStr = $additionaNodes.val();
				if (isNaN(deltaStr)) {
					var msg = dashboardMessages.notValidNumberMsg.replace("{NUBMER}",deltaStr);
					$additionaNodes.showError(msg);
					return;
				} 
				
				var delta = +deltaStr;
				if (delta <= 0) {
					$additionaNodes.showError(dashboardMessages.positiveNumberMsg);
					return;
				}

				console.log("Delta size: " + delta);
				var newSize = currentSize + delta;
				console.log("New cluster size: " + newSize);
				
				//hide edit popup
				jQuery('#dialogBox').hidePopup(popupParams);
				
				console.log("Modifying cluster...");
				var flexRequest = {size: newSize};
				var url = baseUrl + "/" + id;
				request.put(url, {
					data : JSON.stringify(flexRequest),
					handleAs : "json",
					headers : {
						"Accept" : "application/json",
						"Content-Type" : "application/json",
						"cpe-location": cpeLocationName,
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					},
				}).then(function(result){
					console.debug("PUT returned: " + JSON.stringify(result));
					window.setTimeout(updateClusterList, 500);
				},function(result){
					console.error("PUT failed! " + JSON.stringify(result, null, 2));
					var error = result.response.data;
					var message = error == null ? dashboardMessages.unknownErrorMsg : error.detail;
					showErrorMessage(dashboardMessages.flexUpClusterTitle, message);
				});
				
			};
						
			jQuery('#dialogBox').initializeParameters();
			
			var $this = jQuery('#dialogBox .content');
	    	var $table = jQuery('<table class="fieldsTable textLeft"></table>');
	    	$this.append($table);
	    	
	    	var trHTML = '<tr><td><label class="parameter_name">' + dashboardMessages.currentClusterSizeText + ':</label></td><td>' + currentSize + '</td></tr>';
	    	trHTML = trHTML + '<tr><td><label class="parameter_name">' + dashboardMessages.additionalNotesText + ':<span class="ibm-required">*</span></label></td><td><input id="additionalNodes" title="' + dashboardMessages.additionalNotesTitle + '"></td></tr>';
	    	
	    	$table.append(trHTML);
	    	
			var popupParams = {title:dashboardMessages.editClusterTitle, message:'', showYesBtn:true, yesBtnCallback:editCluster, yesBtnLabel:dashboardMessages.submitBtnText ,yesBtnPreventDefaultClose: true, showNoBtn:true, noBtnLabel:dashboardMessages.closeBtnText};
			jQuery('#dialogBox').showPopup(popupParams);
		},
		
		transferData: function(id, cpeLocationName) {
			console.log("Transfer data called. id: " + id);
			
			var _transferData = lang.hitch(this, "_transferData");
			
			var url = baseUrl + "/" + id;
			request(url, { 
				handleAs: "json",
				headers: {
					"cpe-location": cpeLocationName,
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey
				}
			}).then(function(cluster){
				var details = {};
				
				if (cluster.details != null) {
					for (var i = 0, len = cluster.details.length; i < len; i ++) {
						var attr = cluster.details[i];
						var name = attr.name;
						var value = attr.value;
						details[name] = value;
					}
				}
				
				if (offeringDetails.getDataTransferParams) {
					var params = offeringDetails.getDataTransferParams(details);
					console.log("Master IP: " + params.masterIp);
					_transferData(params);
				}
			});
		},
		
		_transferData: function(params) {
			var actionButton = this.transferDialogActionButton;
			var baseUrl = urls.transfer;
						
			var transferId;
			
			var btnLabel = dashboardMessages.stopBtnText;
			
			var timer = new Timer({"timeout": 2000});
			timer.on("tick", function() {
				console.log("transfer status check...");

				var url = baseUrl + "/" + transferId;
				request(url, {
					handleAs: "json",
					headers: {
						"x-master-ip": params.masterIp,
						"x-username": params.user,
						"x-password": params.password,
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					}
				}).then(function(result){
					console.debug("GET returned: " + JSON.stringify(result));
					var state = result.state;
					if (state == "COMPLETED") {
						var exitCode = parseInt(result.exitCode);
						if (exitCode == 0) {
							$statusLabel.html(dashboardMessages.statusDoneText);
						} else {
							var ErrorMsg = dashboardMessages.statusErrorText.replace("{ERRORCODE}",result.errorCode);
							ErrorMsg = ErrorMsg.replace("{ERRORDESC}",result.stderr);
							$statusLabel.html(ErrorMsg);
						}
						
						_updateUI(false, true);
					}
					else if (state == "ACTIVE") {
						$statusLabel.html(dashboardMessages.statusInProgressText);
					}

				},function(result){
//					console.error("DELETE failed! " + JSON.stringify(result, null, 2));

					var error = result.response.data;
					var message = error == null ? dashboardMessages.unknownErrorMsg : error.message;
					console.error("GET failed! " + message);
					$statusLabel.html(message);
				});

			});
			
			var _updateUI = function(_started, updateTimer) {
				started = _started;
				
				jQuery('.fieldsTable .userInput').attr('disabled',started);
				
				btnLabel = btnLabel==dashboardMessages.startBtnText? dashboardMessages.stopBtnText : dashboardMessages.startBtnText;
				jQuery('#dialogBox .close .yes').html(btnLabel);
				
				if (updateTimer) {
					if (started) {
						timer.start();
					} else {
						timer.stop();
					}
				}
			};

			var _startTransfer = function() {
				console.log("Starting transfer...");
				var transfer = jQuery('#dialogBox').populateTransferData(); 
				console.log(" - request: " + JSON.stringify(transfer, null, 2));

				request.post(baseUrl, { 
					data : JSON.stringify(transfer),
					handleAs : "json",
					headers : {
						"Accept" : "application/json",
						"Content-Type" : "application/json",
						"x-master-ip": params.masterIp,
						"x-username": params.user,
						"x-password": params.password,
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					},
				}).then(function(result){
					console.debug("POST returned: " + JSON.stringify(result));
					
					transferId = result.id;
					$statusLabel.html(dashboardMessages.statusStartedStatus);
					
					_updateUI(true, true);

				},function(result){
					console.error("POST failed! " + JSON.stringify(result, null, 2));
					$statusLabel.html(dashboardMessages.statusFailedText);
				});
			};
			
			var _stopTransfer = function() {
				console.log("Stopping transfer...");

				var deleteUrl = baseUrl + "/" + transferId;
				request.del(deleteUrl, {
					handleAs: "json",
					headers: {
						"x-master-ip": params.masterIp,
						"x-username": params.user,
						"x-password": params.password,
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					}
				}).then(function(result){
					console.debug("DELETE returned: " + JSON.stringify(result));
					$statusLabel.html(dashboardMessages.statusStoppedStatus);
				},function(result){
//					console.error("DELETE failed! " + JSON.stringify(result, null, 2));

					var error = result.response.data;
					var message = error == null ? dashboardMessages.unknownErrorMsg : error.message;
					console.error("DELETE failed! " + message);
					$statusLabel.html(message);
				});
				
				_updateUI(false, true);
			};
			
			var closeTransferDialog = function(){
				timer.stop();
			};
			
			var startStopTransfer = function() {
				var valid = jQuery('#dialogBox').validateTransferData();
				if(!valid)
					return false;
				if (!started) {
					_startTransfer();
				} else {
					_stopTransfer();
				}
			};
			
			_showTransferDialog = function(apiPath)
			{
				jQuery('#dialogBox').initializeParameters();
				jQuery('#dialogBox').retrieveTransferData(apiPath);
				
				var popupParams = {title:dashboardMessages.transferDataTitle, message:'', showYesBtn:true, yesBtnCallback:startStopTransfer, yesBtnLabel:btnLabel, yesBtnPreventDefaultClose: true, showNoBtn:true, noBtnCallback:closeTransferDialog, noBtnLabel:dashboardMessages.closeBtnText};
				jQuery('#dialogBox').showPopup(popupParams);
				$statusLabel = jQuery('#dialogBox .content .fieldsTable .transferStatus .fieldValue');
			};
			
			_updateUI(false, false);
		    
			_showTransferDialog('/transferDataCenters');
		},
		
		showGatewayList: function()
		{
			console.debug("showGatewayList : called");
			var showGatewayDetails = lang.hitch(this, "showGatewayDetails");
			var reloadOsGateway = lang.hitch(this, "reloadOsGateway");
			var resetFwGateway = lang.hitch(this, "resetFwGateway");
			var gatewayList = this.gatewayList;
			var url = baseUrl;
			request(url, { 
				handleAs: "json",
				headers: {
					"dedicated-gateway": true,
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey
				}
			}).then(function(gateways){
				if(jQuery.isEmptyObject(gateways))
					return false;
				
				$("#gatewayInformation").show();
				var tBody = gatewayList.tBodies.item(0);
				while (tBody.rows.length > 0) {
					tBody.deleteRow(0);
				}
				
				gateways.forEach(function(gateway){
					var row = tBody.insertRow(-1);
					
					var details = {};
					
					if (gateway.details != null) {
						for (var i = 0, len = gateway.details.length; i < len; i ++) {
							var attr = gateway.details[i];
							var name = attr.name;
							var value = attr.value;
							details[name] = value;
						}
					}
					
					var gatewayId = gateway.clusterParams["GatewayId"];
					
					row.setAttribute("cpeLocation", details["cpe.location.name"]);
					
					
					var gatewayNameColumn = row.insertCell(-1);
					var gatewayStatusColumn = row.insertCell(-1);
					var gatewaySettingColumn = row.insertCell(-1);
					
					if(gatewayId){
						row.setAttribute("gatewayId", gatewayId);
						gatewayNameColumn.innerHTML = "<a href=\"#\" id=\"" + gatewayId + "\" title=\"" + dashboardMessages.clickDetailsText + "\" data-description=\""+ gateway.description +"\" data-launchtime=\""+ gateway.launchTime +"\">" + gateway.name + "</a>";
						on(dom.byId(gatewayId), "click", function(event) {
							showGatewayDetails(gatewayId);
						});
					}
					else{
						gatewayNameColumn.innerHTML = "<a href=\"#\" id=\"" + gatewayId + "\" title=\"" + dashboardMessages.clickDetailsText + "\" data-description=\""+ gateway.description +"\" data-launchtime=\""+ gateway.launchTime +"\">" + gateway.name + "</a>";
					}
					
					var gatewayStatusLabel = "<span class=\"ibm-error-link\" title=\"" + dashboardMessages.gatewayStatusFailedTitle + "\">" + dashboardMessages.failedText + "</span>";
					var showActions = false;
					if (gateway.currentStep == "INIT") {
						statusCell.innerHTML = "<span class=\"ibm-check-link\">" + dashboardMessages.INIT + "</span>";
					}else if(gateway.currentStep == "ORDER_PENDING")
					{
						gatewayStatusLabel = "<span class=\"ibm-check-link\">" + dashboardMessages.GW_ORDER_PENDING + "</span>";
					}
					else if(gateway.currentStep == "PROVISION_NODES" || gateway.currentStep == "CONFIG_NODES")
					{
						gatewayStatusLabel = "<span class=\"ibm-check-link\">" + dashboardMessages.GW_PROVISION_NODES + "</span>";
					}
					else if(gateway.currentStep == "NONE")
					{
						showActions = true;
						gatewayStatusLabel = "<span class=\"ibm-check-link\">" + dashboardMessages.GW_READY + "</span>";
						
						var vpnTunnelId = '';
						if(!jQuery.isEmptyObject(gateway.vpnTunnel) && gateway.vpnTunnel.id != null && gateway.vpnTunnel.id != undefined)
						{
							vpnTunnelId = gateway.vpnTunnel.id;
						}
						
						gatewaySettingColumn.innerHTML = "<a href='#' data-vpndetailid='" + vpnTunnelId 
						+ "' class='vpnDetails' title='" + dashboardMessages.viewVPNLinkTitle + "'>" 
						+ dashboardMessages.viewVPNLinkText + "</a> / <a href='#' data-vpndetailid='" 
						+ vpnTunnelId + "' class='editVPNDetails' title='" 
						+ dashboardMessages.editVPNLinkTitle + "'>" + dashboardMessages.editVPNLinkText + "</a>";
					}
					
					gatewayStatusColumn.innerHTML = gatewayStatusLabel;
					
					if(isDGWActionAllowed == "true")
					{
						var gatewayActionsColumn = row.insertCell(-1);
						
						var reloadOSId = "gateway_" + gatewayId + "_reloadOS_button";
						var resetFWId = "gateway_" + gatewayId + "_resetFW_button";
						var actionsHtml = "";
						actionsHtml += "<span class=\"ibm-reset-link " + (showActions ? "enabled" : "disabled") +  "\" id=\"" + reloadOSId + "\" title=\"" + dashboardMessages.reloadOSBtnTitle + "\">&nbsp;</span> ";
						actionsHtml += "<span class=\"ibm-signin-link " + (showActions ? "enabled" : "disabled") +  "\" id=\"" + resetFWId + "\" title=\"" + dashboardMessages.resetFWBtnTitle + "\">&nbsp;</span> ";
						gatewayActionsColumn.innerHTML = actionsHtml;	
						
						if (showActions) {
							var reloadOSButton = dom.byId(reloadOSId);
							on(reloadOSButton, "click", function(){
								reloadOsGateway(gatewayId);
							});
							reloadOSButton.tabIndex = 0;
							on(reloadOSButton, "keypress", function(event){
								if (event.keyCode == 13) {
									reloadOsGateway(gatewayId);
								}
							});
							
							var resetFWButton = dom.byId(resetFWId);
							on(resetFWButton, "click", function(){
								resetFwGateway(gatewayId);
							});
							resetFWButton.tabIndex = 0;
							on(resetFWButton, "keypress", function(event){
								if (event.keyCode == 13) {
									resetFwGateway(gatewayId);
								}
							});
						}
					}
				});
			});
		},
		
		reloadOsGateway: function(id) {
			console.log("Reload OS for Gateway called. id: " + id);
			
			var reloadOs = function(){
				console.log("Reloading Gateway " + id);
				var url = gatewayUrl + "/" + gatewayId + "/reloadOS";
				request.del(deleteUrl, {
					handleAs: "json",
					headers: {
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					}
				}).then(function(result){
					console.debug("Reset OS returned: " + JSON.stringify(result));
				});
			};
			 
			jQuery('#dialogBox').initializeParameters();
			var popupParams = {title:dashboardMessages.reloadOsGatewayTitle, message:dashboardMessages.reloadOsGatewayConfirmationMsg, showYesBtn:true, yesBtnCallback:reloadOs, yesBtnLabel:dashboardMessages.reloadOsGatewayBtnText, showNoBtn:true, noBtnLabel:dashboardMessages.cancelBtnLabel};
			jQuery('#dialogBox').showPopup(popupParams);
		},
		
		resetFwGateway: function(id) {
			console.log("Reset FW for Gateway called. id: " + id);
			
			var resetFw = function(){
				console.log("Resetting FW for Gateway " + id);
				var url = gatewayUrl + "/" + gatewayId + "/applyDefaultFirewall";
				request.del(deleteUrl, {
					handleAs: "json",
					headers: {
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey
					}
				}).then(function(result){
					console.debug("Reset OS returned: " + JSON.stringify(result));
				});
			};
			 
			jQuery('#dialogBox').initializeParameters();
			var popupParams = {title:dashboardMessages.resetFwGatewayTitle, message:dashboardMessages.resetFwGatewayConfirmationMsg, showYesBtn:true, yesBtnCallback:resetFw, yesBtnLabel:dashboardMessages.resetFwGatewayBtnText, showNoBtn:true, noBtnLabel:dashboardMessages.cancelBtnLabel};
			jQuery('#dialogBox').showPopup(popupParams);
		},
		
		showGatewayDetails: function(gatewayId) {
            var description = jQuery("#" + gatewayId).data('description');
            var launchTime = jQuery("#" + gatewayId).data('launchtime');
            
			var url = gatewayUrl + "/" + gatewayId;
			request(url, { 
				handleAs: "json",
				headers: {
					"subscriber-id": CONSTANTS.subscriberId,
					"api-key": CONSTANTS.apiKey
				}
			}).then(function(gateway){

				var html = "<p><b>" + dashboardMessages.nameText + ":</b> " + gateway.name + "</p>"
					+ "<p><b>" + dashboardMessages.descriptionText + ":</b> " + description + "</p>" 
					+ "<p><b>" + dashboardMessages.launchTimeText + ":</b> " + formatDateTime(launchTime) + "</p>" 
					+ "<hr>"
					+ "<p><b>" + dashboardMessages.gatewayPublicIpText + ":</b> " + gateway.publicIpAddress + "</p>" 
					+ "<p><b>" + dashboardMessages.gatewayPrivateIpText + ":</b> " + gateway.privateIpAddress + "</p>"
					+ "<p><b>" + dashboardMessages.gatewayUserText + ":</b> " + gateway.gatewayMembers[0].username + "</p>" 
					+ "<p><b>" + dashboardMessages.gatewayPasswordText + ":</b> " + gateway.gatewayMembers[0].password + "</p>";
				
				
				console.log(html);
				
				jQuery('#dialogBox').initializeParameters();
				jQuery('#dialogBox .content').html(html);
				var popupParams = {title:dashboardMessages.gatewayDetailsTitle, message:'', showNoBtn:true, noBtnLabel:dashboardMessages.closeBtnText};
				jQuery('#dialogBox').showPopup(popupParams);
			});
		},
		
		saveVPNTunnelParamDetails: function()
		{
			if(jQuery(this).isValidVPNTunnelParamsValues())
	    	{
	    		var vpnTunnel = jQuery(this).getVPNTunnelParamsValues();
	    		jQuery(this).hidePopup({});
	    	
				//show wait dialog
				var waitPopuParams = {title:dashboardMessages.pleaseWaitTitle, message:dashboardMessages.pleaseWaitVPNParamsSaveMsg};
				jQuery(this).showPopup(waitPopuParams);
				var cpeLocation = vpnTunnel.cpeLocation;
				delete vpnTunnel.cpeLocation;
				
				delete vpnTunnel.gatewayId;
				
				var vpnTunnelUpdateUrl = tunnelUrl + "/" + vpnTunnel.id;
				request.put(vpnTunnelUpdateUrl, {
					data : JSON.stringify(vpnTunnel),
					handleAs: "json",
					headers: {
						"Accept" : "application/json",
						"Content-Type" : "application/json",
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey,
						"cpe-location": cpeLocation
					}
				}).then(function(result){
					console.debug("PUT returned: " + JSON.stringify(result));
					jQuery(this).hidePopup(waitPopuParams);
					var popupParams = {title:dashboardMessages.saveVPNPramsTitle, message:dashboardMessages.vpnSaveSuccessText, showTitleImage:false, showYesBtn:true, yesBtnLabel:dashboardMessages.okBtnText};
					jQuery('#wizard').showPopup(popupParams);
				},function(result){
					jQuery('#wizard').hidePopup(waitPopuParams);
					var error = result.response.data;
					var message = error == null ? dashboardMessages.unknownErrorMsg : error.detail;
					var popupParams = {title:dashboardMessages.saveVPNPramsErrorTitle, message:message, showTitleImage:true, showYesBtn:true, yesBtnLabel:dashboardMessages.okBtnText};
					jQuery('#wizard').showPopup(popupParams);
				});
		      }
		 },
		 
		addVPNTunnelParamDetails: function()
		{
			if(jQuery(this).isValidVPNTunnelParamsValues())
	    	{
	    		var vpnTunnel = jQuery(this).getVPNTunnelParamsValues();
	    		jQuery(this).hidePopup({});
	    	
				//show wait dialog
				var waitPopuParams = {title:dashboardMessages.pleaseWaitTitle, message:dashboardMessages.pleaseWaitVPNParamsSaveMsg};
				jQuery(this).showPopup(waitPopuParams);
				
				var cpeLocation = vpnTunnel.cpeLocation;
				delete vpnTunnel.cpeLocation;
				
				var gatewayId = vpnTunnel.gatewayId;
				delete vpnTunnel.gatewayId;
				
				var vpnTunnelAddUrl = gatewayUrl + "/" + gatewayId + "/addVPNTunnel";
				request.post(vpnTunnelAddUrl, {
					data : JSON.stringify(vpnTunnel),
					handleAs: "json",
					headers: {
						"Accept" : "application/json",
						"Content-Type" : "application/json",
						"subscriber-id": CONSTANTS.subscriberId,
						"api-key": CONSTANTS.apiKey,
						"cpe-location": cpeLocation
					}
				}).then(function(result){
					console.debug("PUT returned: " + JSON.stringify(result));
					jQuery(this).hidePopup(waitPopuParams);
					var popupParams = {title:dashboardMessages.saveVPNPramsTitle, message:dashboardMessages.vpnSaveSuccessText, showTitleImage:false, showYesBtn:true, yesBtnLabel:dashboardMessages.okBtnText};
					jQuery('#wizard').showPopup(popupParams);
				},function(result){
					jQuery('#wizard').hidePopup(waitPopuParams);
					var error = result.response.data;
					var message = error == null ? dashboardMessages.unknownErrorMsg : error.detail;
					var popupParams = {title:dashboardMessages.saveVPNPramsErrorTitle, message:message, showTitleImage:true, showYesBtn:true, yesBtnLabel:dashboardMessages.okBtnText};
					jQuery('#wizard').showPopup(popupParams);
				});
		      }
		 }
	});
});