define({
	//populate-smartwizard-plugin.js messages
	stepDataErrorMsg: "Impossibile recuperare i dati del passo",
	stepDetailsErrorMsg:"Impossibile recuperare i dettagli del passo",
	authFailedTitle: "Autenticazione non riuscita",
	noDataFoundMsg: "Nessun dato trovato",
	usernameApiKeyMissingMsg: "Immettere Nome utente e Chiave API.",
	validClusterNameMsg: "Nome cluster disponibile per l'uso",
	invalidClusterNameMsg: "Il nome del cluster non è valido. Può contenere solo caratteri alfanumerici, spazi, \".\", \"(\", \")\", caratteri di sottolineatura, due punti, trattini e caratteri cinesi; la lunghezza massima consentita è di 64 caratteri.",
	clusterNameExistMsg: "Esiste già lo stesso Nome cluster, tentare immettendo un altro nome",
	noSpinnerDataMsg: "Impossibile popolare la casella di selezione",
	paramMissingMsg: "Parametro mancante",
	noTransferDataFoundMsg: "Impossibile recuperare i dati del trasferimento",
	stepLabel: "Passo",
	yesBtnText: "Sì",
	noBtnText: "No",
	nextBtnLabel: "Successivo",
	prevBtnLabel: "Precedente",
	finishBtnLabel: "Fine",
	cancelBtnLabel: "Annulla",
	saveBtnLabel: "Salva",
	addBtnText: "Aggiungi",
	nodeConfigurationTitle: "Configurazione nodo",
	clusterSizeCalculationText: "Formula per calcolare la dimensione del cluster Hadoop",
	smallDataNodeText: "n° di nodi di dati di piccole dimensioni = (dimensione dati non elaborati in TB * 4)/ 7",
	largeDataNodeText: "n° di nodi di dati di grandi dimensioni = (dimensione dati non elaborati in TB * 4)/ 35",
	transferDataText: "Data Center di trasferimento",
	transferDataDesc: "Trasferisci i dati",
	protectedNetworkTitle: "Reti protette",
	
	//for subnet list popup
	customerSubnetHeaderText: "Sottorete cliente",
	softLayerSubnetHeaderText:"Sottoreti Softlayer protette",
	subnetHeaderText:"Sottorete",
	actionHeaderText:"Azione",
	removeSubnetText:"Rimuovi da VPN IPSec",
	ipAddrFieldsText:"Indirizzo IP",
	cidrFieldsText:"CIDR",
	
	subnetReqFieldsMsg:"Compilare i campi obbligatori per aggiungere la sottorete",
	subnetIpAddrInvalidMsg:"Immettere un indirizzo IP valido. Il formato valido è: xx.xx.xx.x",
	subnetCIDRInvalidMsg:"Immettere un valore numerico per il CIDR",
	emptySubnetListMsg: "L'elenco delle sottoreti non può essere vuoto. Aggiungere almeno una sottorete <br> OPPURE annullare l'operazione, facendo clic sul pulsante \"Annulla\".",
	subnetValidationMsg: "L'elenco delle sottoreti non può essere vuoto. Fare clic sul collegamento \"Sottorete cliente\" per aggiungere l'indirizzo IP della sottorete e il CIDR.",
	
	//wizard.js messages
	requiredFieldsMsg: "Mancano oppure non sono validi alcuni campi obbligatori.<br> Fornire l'input obbligatorio.",
	accountQuotaTitle: "Nessuna quota account",
	accountQuotaMsg: "ÿ stata raggiunta la quota account.<br> Impossibile creare un nuovo cluster.",
	addNewClusterFailedTitle: "Aggiunta di un nuovo cluster non riuscita",
	addNewClusterFailedMsg: "Impossibile intraprendere il passo Dettagli per creare un nuovo cluster.<br> Contattare il Supporto o l'Amministratore.",
	wizardHeading: "Aggiungi nuovo cluster",
	
	//common messages for both wizard.js and populate-smartwizard-plugin.js
	validationFailedTitle: "Convalida non riuscita",
	infoTitle: "Informazioni",
	okBtnText: "OK",
	
	//edit vpn params popup
	clusterSubnetText: "Sottorete cluster",
	customerSubnetText: "Sottorete cliente",
});