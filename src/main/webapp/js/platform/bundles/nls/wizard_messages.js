define({
  root: {        
    //populate-smartwizard-plugin.js messages
    stepDataErrorMsg: "Could not fetch step data",
    stepDetailsErrorMsg:"Could not fetch step details",
    authFailedTitle: "Authentication Failed",
    noDataFoundMsg: "No data found",
    usernameApiKeyMissingMsg: "Please enter Username and API Key.",
    validClusterNameMsg: "Cluster Name is available to use",
    invalidClusterNameMsg: "Cluster name is not valid. It should only contain AlphaNumeric, Space, \".\", \"(\", \")\", Underscore, Colon, Dash, and should not exceed 64 characters.",
    clusterNameExistMsg: "Same Cluster Name already exist, please try some other name",
    noSpinnerDataMsg: "Not able to populate spinner",
    paramMissingMsg: "Parameter is missing",
    noTransferDataFoundMsg: "Could not fetch transfer data",
    stepLabel: "Step",
    yesBtnText: "Yes",
    noBtnText: "No",
    nextBtnLabel: "Next",
    prevBtnLabel: "Previous",
    finishBtnLabel: "Finish",
    cancelBtnLabel: "Cancel",
    saveBtnLabel: "Save",
    addBtnText: "Add",
    nodeConfigurationTitle: "Node Configuration",
    clusterSizeCalculationText: "Formula to compute Hadoop cluster size",
    smallDataNodeText: "# of small data nodes = (raw data size in TB * 4)/ 7",
    largeDataNodeText: "# of large data nodes = (raw data size in TB * 4)/ 35",
    transferDataText: "Transfer Data Centers",
    transferDataDesc: "Transfer Data",
    protectedNetworkTitle: "Protected Networks",
    
    //for subnet list popup
    customerSubnetHeaderText: "Customer Subnets",
    softLayerSubnetHeaderText:"Protected Softlayer Subnets",
    subnetHeaderText:"Subnet",
    actionHeaderText:"Action",
    removeSubnetText:"Remove from IPSec VPN",
    ipAddrFieldsText:"IP Address",
    cidrFieldsText:"CIDR",
    
    subnetReqFieldsMsg:"Please enter required fields to add subnet",
    subnetIpAddrInvalidMsg:"Please enter valid IP Address. Valid format is: xx.xx.xx.x",
    subnetCIDRInvalidMsg:"Please enter numeric value for CIDR",
    emptySubnetListMsg: "Subnet list cannot be empty. Please add at least one subnet <br> OR cancel the operation by clicking on \"Cancel\" button.",
    subnetValidationMsg: "Subnet list cannot be empty. Please click on \"Customer Subnet\" link to add subnet IP Address and CIDR.",
    
    //wizard.js messages
    requiredFieldsMsg: "Some mandatory fields are missing or not valid.<br> Please provide the required input.",
    accountQuotaTitle: "No Account Quota",
    accountQuotaMsg: "You have reached your account quota.<br> Cannot create a New Cluster.",
    addNewClusterFailedTitle: "Add New Cluster failed",
    addNewClusterFailedMsg: "Not able to get Step Details to create New Cluster.<br> Please contact Support or Administrator.",
    wizardHeading: "Add new cluster",
    
    //common messages for both wizard.js and populate-smartwizard-plugin.js
    validationFailedTitle: "Validation Failed",
    infoTitle: "Information",
    okBtnText: "OK",
    
    //edit vpn params popup
    clusterSubnetText: "Cluster Subnet",
    customerSubnetText: "Customer Subnet",
  },
  'en_US': true,
  'es': true,
  'it_IT': true,
  'de_DE': true,
  'fr_FR': true
});