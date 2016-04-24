define({
  //populate-smartwizard-plugin.js messages
  stepDataErrorMsg: "Schrittdaten konnten nicht abgerufen werden.",
  stepDetailsErrorMsg:"Schrittdetails konnten nicht abgerufen werden.",
  authFailedTitle: "Authentifizierung fehlgeschlagen",
  noDataFoundMsg: "Keine Daten gefunden",
  usernameApiKeyMissingMsg: "Bitte geben Sie Benutzernamen und API-Schlüssel ein.",
  validClusterNameMsg: "Der Clustername ist verfügbar.",
  invalidClusterNameMsg: "Der Clustername ist nicht gültig. Er darf nur alphanumerische Zeichen, Leerschritt, \".\", \"(\", \")\", Unterstrich, Doppelpunkt, Gedankenstrich und chinesische Zeichen enthalten und 64 Zeichen nicht überschreiten. ",
  clusterNameExistMsg: "Der Clustername existiert bereits. Bitte versuchen Sie es mit einem anderen Namen.",
  noSpinnerDataMsg: "Die Belegung des Drehfeldes ist nicht möglich.",
  paramMissingMsg: "Ein Parameter fehlt.",
  noTransferDataFoundMsg: "ÿbertragungsdaten konnten nicht abgerufen werden.",
  stepLabel: "Schritt",
  yesBtnText: "Ja",
  noBtnText: "Nein",
  nextBtnLabel: "Weiter",
  prevBtnLabel: "Vorherige",
  finishBtnLabel: "Fertigstellen",
  cancelBtnLabel: "Abbrechen",
  saveBtnLabel: "Speichern",
  addBtnText: "Hinzufügen",
  nodeConfigurationTitle: "Knotenkonfiguration",
  clusterSizeCalculationText: "Formel zur Berechnung der Gröÿe des Hadoop-Clusters",
  smallDataNodeText: "# der kleinen Datenknoten = (Gröÿe der Rohdaten in TB * 4)/ 7",
  largeDataNodeText: "# der groÿen Datenknoten = (Gröÿe der Rohdaten in TB * 4)/ 35",
  transferDataText: "Rechenzentren für Datentransfer",
  transferDataDesc: "Daten übertragen",
  protectedNetworkTitle: "Geschützte Netze",

  //for subnet list popup
  customerSubnetHeaderText: "Kundenteilnetze",
  softLayerSubnetHeaderText:"Geschützte SoftLayer-Teilnetze",
  subnetHeaderText:"Teilnetz",
  actionHeaderText:"Aktion",
  removeSubnetText:"Aus IPSec-VPN entfernen",
  ipAddrFieldsText:"IP-Adresse",
  cidrFieldsText:"CIDR",

  subnetReqFieldsMsg:"Bitte füllen Sie zum Hinzufügen eines Teilnetzes die erforderlichen Felder aus.",
  subnetIpAddrInvalidMsg:"Bitte geben Sie eine gültige IP-Adresse ein. Gültiges Format: xx.xx.xx.x",
  subnetCIDRInvalidMsg:"Bitte geben sie für CIDR einen numerischen Wert ein.",
  emptySubnetListMsg: "Die Liste der Teilnetze darf nicht leer sein. Bitte fügen Sie mindestens ein Teilnetz hinzu <br> ODER brechen Sie die Operation ab durch Klicken auf die Schaltfläche \"Abbrechen\".",
  subnetValidationMsg: "Die Liste der Teilnetze darf nicht leer sein. Bitte klicken Sie auf den Link \"Kundenteilnetz\", um die IP-Adresse und CIDR des Teilnetzes hinzuzufügen.",

  //wizard.js messages
  requiredFieldsMsg: "Einige Pflichtfelder fehlen oder sind nicht gültig.<br> Bitte geben Sie die erforderlichen Informationen ein.",
  accountQuotaTitle: "Keine Gröÿenbeschränkung für Account",
  accountQuotaMsg: "Sie haben die Gröÿenbeschränkung für Ihren Account erreicht.<br> Es kann kein neuer Cluster erstellt werden.",
  addNewClusterFailedTitle: "Das Hinzufügen eines neuen Clusters schlug fehl.",
  addNewClusterFailedMsg: "Es konnten keine Schrittdetails für die Erstellung eines neuen Clusters abgerufen werden.<br> Bitte wenden Sie sich an das Support-Team oder den Administrator.",
  wizardHeading: "Neuen Cluster hinzufügen",

  //common messages for both wizard.js and populate-smartwizard-plugin.js
  validationFailedTitle: "Validierung fehlgeschlagen",
  infoTitle: "Informationen",
  okBtnText: "OK",

  //edit vpn params popup
  clusterSubnetText: "Clusterteilnetz",
  customerSubnetText: "Kundenteilnetz"
});