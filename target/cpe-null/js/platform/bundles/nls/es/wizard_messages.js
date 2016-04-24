define({
	//populate-smartwizard-plugin.js messages
	stepDataErrorMsg: "No se han podido captar los datos de los pasos",
	stepDetailsErrorMsg:"No se han podido captar los detalles de los pasos",
	authFailedTitle: "Ha fallado la autenticación",
	noDataFoundMsg: "No se han encontrado datos",
	usernameApiKeyMissingMsg: "Especifique el nombre de usuario y la clave de API.",
	validClusterNameMsg: "El nombre de clúster está disponible para su uso",
	invalidClusterNameMsg: "El nombre de clúster no es válido. Sólo debe contener caracteres alfanuméricos, espacios, \".\", \"(\", \")\", guiones bajos, puntos, guiones y caracteres chinos, y no debe superar los 64 caracteres.",
	clusterNameExistMsg: "Ya existe el mismo nombre de clúster; pruebe con otro nombre",
	noSpinnerDataMsg: "No se ha podido rellenar el selector cíclico",
	paramMissingMsg: "Falta el parámetro",
	noTransferDataFoundMsg: "No se han podido captar los datos de transferencia",
	stepLabel: "Paso",
	yesBtnText: "Sí",
	noBtnText: "No",
	nextBtnLabel: "Siguiente",
	prevBtnLabel: "Anterior",
	finishBtnLabel: "Finalizar",
	cancelBtnLabel: "Cancelar",
	saveBtnLabel: "Chatee y ahorre",
	addBtnText: "Añadir",
	nodeConfigurationTitle: "Configuración de nodo",
	clusterSizeCalculationText: "Fórmula para calcular el tamaño del clúster de Hadoop",
	smallDataNodeText: "Número de nodos de datos pequeños = (tamaño de datos en bruto en TB * 4)/ 7",
	largeDataNodeText: "Número de nodos de datos grandes = (tamaño de datos en bruto en TB * 4)/ 35",
	transferDataText: "Centros de datos de transferencia",
	transferDataDesc: "Transferir datos",
	protectedNetworkTitle: "Redes protegidas",
	
	//for subnet list popup
	customerSubnetHeaderText: "Subredes de cliente",
	softLayerSubnetHeaderText:"Subredes de Softlayer protegido",
	subnetHeaderText:"Subred",
	actionHeaderText:"Acción",
	removeSubnetText:"Eliminar de VPN IPSec",
	ipAddrFieldsText:"Dirección IP",
	cidrFieldsText:"CIDR",
	
	subnetReqFieldsMsg:"Especifique los campos necesarios para añadir una subred",
	subnetIpAddrInvalidMsg:"Especifique una dirección IP válida. El formato válido es: xx.xx.xx.x",
	subnetCIDRInvalidMsg:"Especifique un valor numérico para CIDR",
	emptySubnetListMsg: "La lista de subred no puede estar vacía. Añada al menos otra subred <br> O bien, cancele la operación pulsando el botón \"Cancelar\".",
	subnetValidationMsg: "La lista de subred no puede estar vacía. Pulse el enlace \"Subred de cliente\" para añadir la dirección IP de subred y el CIDR.",
	
	//wizard.js messages
	requiredFieldsMsg: "Faltan algunos campos obligatorios o no son válidos.<br> Proporcione la entrada necesaria.",
	accountQuotaTitle: "No hay cuota de cuenta",
	accountQuotaMsg: "Ha alcanzado su cuota de cuenta.<br> No puede crear un nuevo clúster.",
	addNewClusterFailedTitle: "La adición de un nuevo clúster ha fallado",
	addNewClusterFailedMsg: "No se pueden obtener los detalles del paso para crear un nuevo clúster.<br> Póngase en contacto con el departamento de soporte o el administrador.",
	wizardHeading: "Añadir nuevo clúster",
	
	//common messages for both wizard.js and populate-smartwizard-plugin.js
	validationFailedTitle: "La validación ha fallado",
	infoTitle: "Información",
	okBtnText: "OK",
	
	//edit vpn params popup
	clusterSubnetText: "Subred de clúster",
	customerSubnetText: "Subred de cliente",
});